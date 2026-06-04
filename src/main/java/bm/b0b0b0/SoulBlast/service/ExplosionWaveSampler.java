package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.ExplosionAlgorithmSettings;
import bm.b0b0b0.SoulBlast.config.ExplosionLimits;
import bm.b0b0b0.SoulBlast.config.ExplosionSettings;
import bm.b0b0b0.SoulBlast.config.GeneralSettings;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import bm.b0b0b0.SoulBlast.model.ExplosionRaySamplingProgress;
import bm.b0b0b0.SoulBlast.model.ExplosionWaveCell;
import bm.b0b0b0.SoulBlast.util.BlockCoordPacker;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ExplosionWaveSampler {

    private final ExplosionBlockInclusion inclusion;

    public ExplosionWaveSampler(ExplosionBlockInclusion inclusion) {
        this.inclusion = inclusion;
    }

    public static boolean usesWave(ExplosionSettings settings) {
        return "WAVE".equalsIgnoreCase(settings.quality);
    }

    public void beginSampling(ExplosionJob job, ExplosionLimits limits, GeneralSettings general) {
        ExplosionRaySamplingProgress progress = job.getRaySampling();
        progress.reset();
        ExplosionSettings settings = job.getDynamite().explosion;
        if (!usesWave(settings) || !settings.breakBlocks) {
            return;
        }
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        float radius = settings.radius;
        double radiusSq = radius * radius;
        double cx = center.getX();
        double cy = center.getY();
        double cz = center.getZ();
        int minX = (int) Math.floor(cx - radius);
        int maxX = (int) Math.floor(cx + radius);
        int minY = (int) Math.floor(cy - radius);
        int maxY = (int) Math.floor(cy + radius);
        int minZ = (int) Math.floor(cz - radius);
        int maxZ = (int) Math.floor(cz + radius);
        int worldMinY = world.getMinHeight();
        int worldMaxY = world.getMaxHeight() - 1;
        List<ExplosionWaveCell> cells = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (y < worldMinY || y > worldMaxY) {
                    continue;
                }
                for (int z = minZ; z <= maxZ; z++) {
                    double dx = x + 0.5 - cx;
                    double dy = y + 0.5 - cy;
                    double dz = z + 0.5 - cz;
                    double distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq > radiusSq) {
                        continue;
                    }
                    cells.add(new ExplosionWaveCell(x, y, z, distSq));
                }
            }
        }
        cells.sort(Comparator.comparingDouble(ExplosionWaveCell::distSq));
        progress.setWaveCells(cells);
        progress.active = true;
        progress.totalRays = cells.size();
    }

    public int continueSampling(
            ExplosionJob job,
            ExplosionLimits limits,
            GeneralSettings general,
            int stepBudget
    ) {
        ExplosionRaySamplingProgress progress = job.getRaySampling();
        if (!progress.active || stepBudget <= 0 || !usesWave(job.getDynamite().explosion)) {
            return stepBudget;
        }
        ExplosionSettings settings = job.getDynamite().explosion;
        ExplosionAlgorithmSettings algorithm = settings.algorithm;
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null) {
            progress.active = false;
            return stepBudget;
        }
        int maxBlocks = Math.max(500, (int) (limits.maxBlocksPerExplosion() * settings.blockBudgetMultiplier));
        boolean onlyLoaded = general.sampleOnlyLoadedChunks;
        boolean requireLineOfSight = algorithm.waveLineOfSight;
        int spent = 0;
        List<ExplosionWaveCell> cells = progress.waveCells;
        while (spent < stepBudget && progress.waveIndex < cells.size()) {
            if (progress.seen.size() >= maxBlocks) {
                progress.active = false;
                break;
            }
            ExplosionWaveCell cell = cells.get(progress.waveIndex++);
            int x = cell.x();
            int y = cell.y();
            int z = cell.z();
            if (onlyLoaded && !world.isChunkLoaded(x >> 4, z >> 4)) {
                spent++;
                continue;
            }
            Block block = world.getBlockAt(x, y, z);
            if (!inclusion.includes(job, block)) {
                spent++;
                continue;
            }
            if (requireLineOfSight && !ExplosionWaveLineOfSight.canReach(world, center, block)) {
                spent++;
                continue;
            }
            long key = BlockCoordPacker.pack(x, y, z);
            if (progress.seen.add(key)) {
                job.getPendingBlocks().addLast(new ExplosionJob.BlockTarget(x, y, z));
            }
            spent++;
        }
        if (progress.waveIndex >= cells.size()) {
            progress.active = false;
        }
        return stepBudget - spent;
    }

}
