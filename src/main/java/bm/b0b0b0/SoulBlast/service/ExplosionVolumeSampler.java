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
        int maxBlocks = Math.max(500, TsarBombRules.blockCap(limits, job.getDynamite(), general));
        LongOpenHashSet seen = job.getRaySampling().seen;
        boolean onlyLoaded = general.sampleOnlyLoadedChunks;
        int added = 0;
        for (int shell = 0; shell <= intRadius; shell++) {
            List<ExplosionJob.BlockTarget> shellTargets = new ArrayList<>();
            for (ExplosionSphereShells.ShellCell cell : ExplosionSphereShells.collectShell(
                    world,
                    cx,
                    cy,
                    cz,
                    shell,
                    radiusSq,
                    onlyLoaded,
                    y -> true
            )) {
                long key = BlockCoordPacker.pack(cell.x(), cell.y(), cell.z());
                if (!seen.add(key)) {
                    continue;
                }
                Block block = world.getBlockAt(cell.x(), cell.y(), cell.z());
                if (!shouldAnnihilate(block.getType())) {
                    continue;
                }
                shellTargets.add(new ExplosionJob.BlockTarget(cell.x(), cell.y(), cell.z()));
            }
            if (!shellTargets.isEmpty() && added + shellTargets.size() > maxBlocks) {
                return;
            }
            for (ExplosionJob.BlockTarget target : shellTargets) {
                job.getPendingBlocks().addLast(target);
                added++;
            }
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

}
