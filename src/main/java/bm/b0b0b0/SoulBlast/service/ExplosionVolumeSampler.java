package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.ExplosionLimits;
import bm.b0b0b0.SoulBlast.config.ExplosionSettings;
import bm.b0b0b0.SoulBlast.config.GeneralSettings;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import bm.b0b0b0.SoulBlast.util.BlockCoordPacker;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ExplosionVolumeSampler {

    public void fillIntoJob(ExplosionJob job, ExplosionLimits limits, GeneralSettings general) {
        ExplosionSettings settings = job.getDynamite().explosion;
        if (!"EXTREME".equalsIgnoreCase(settings.quality)) {
            return;
        }
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null || !settings.breakBlocks) {
            return;
        }
        float radius = settings.radius;
        if (TsarBombRules.isTsar(job.getDynamite())) {
            radius *= 1.05f;
        }
        int intRadius = (int) Math.ceil(radius);
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        double radiusSq = radius * radius;
        int maxBlocks = Math.max(500, TsarBombRules.blockCap(limits, job.getDynamite()));
        LongOpenHashSet seen = job.getRaySampling().seen;
        boolean onlyLoaded = general.sampleOnlyLoadedChunks;
        List<VolumeCell> cells = new ArrayList<>();
        for (int dx = -intRadius; dx <= intRadius; dx++) {
            for (int dy = -intRadius; dy <= intRadius; dy++) {
                for (int dz = -intRadius; dz <= intRadius; dz++) {
                    double distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq > radiusSq) {
                        continue;
                    }
                    cells.add(new VolumeCell(dx, dy, dz, distSq));
                }
            }
        }
        cells.sort(Comparator.comparingDouble(VolumeCell::distSq));
        for (VolumeCell cell : cells) {
            if (seen.size() >= maxBlocks) {
                break;
            }
            int x = cx + cell.dx();
            int y = cy + cell.dy();
            int z = cz + cell.dz();
            int minY = world.getMinHeight();
            int maxY = world.getMaxHeight() - 1;
            if (y < minY || y > maxY) {
                continue;
            }
            if (onlyLoaded && !world.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }
            long key = BlockCoordPacker.pack(x, y, z);
            if (!seen.add(key)) {
                continue;
            }
            Block block = world.getBlockAt(x, y, z);
            if (!shouldAnnihilate(block.getType())) {
                continue;
            }
            job.getPendingBlocks().addLast(new ExplosionJob.BlockTarget(x, y, z));
        }
    }

    private boolean shouldAnnihilate(Material type) {
        if (type.isAir() || type == Material.CAVE_AIR) {
            return false;
        }
        return type != Material.BEDROCK
                && type != Material.BARRIER
                && type != Material.END_PORTAL
                && type != Material.END_PORTAL_FRAME
                && type != Material.NETHER_PORTAL
                && type != Material.REINFORCED_DEEPSLATE
                && type != Material.STRUCTURE_BLOCK
                && type != Material.JIGSAW
                && type != Material.COMMAND_BLOCK
                && type != Material.CHAIN_COMMAND_BLOCK
                && type != Material.REPEATING_COMMAND_BLOCK;
    }

    private record VolumeCell(int dx, int dy, int dz, double distSq) {
    }

}
