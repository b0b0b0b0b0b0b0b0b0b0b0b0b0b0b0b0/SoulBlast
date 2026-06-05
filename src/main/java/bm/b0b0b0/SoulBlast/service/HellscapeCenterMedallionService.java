package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.CraterFillSettings;
import bm.b0b0b0.SoulBlast.config.ExplosionSettings;
import bm.b0b0b0.SoulBlast.config.GeneralSettings;
import bm.b0b0b0.SoulBlast.model.ExplosionBlockAction;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import bm.b0b0b0.SoulBlast.util.BlockCoordPacker;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class HellscapeCenterMedallionService {

    private final BlockExplosionApplier blockApplier;
    private final ExplosionChunkContext chunkContext = new ExplosionChunkContext();

    public HellscapeCenterMedallionService(BlockExplosionApplier blockApplier) {
        this.blockApplier = blockApplier;
    }

    public void planIntoJob(ExplosionJob job, GeneralSettings general) {
        if (!general.griefLastPyreCenterMedallionEnabled) {
            return;
        }
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        float radius = Math.max(1.5f, general.griefLastPyreCenterMedallionRadius);
        double radiusSq = radius * radius;
        double cryingRatio = clampRatio(general.griefLastPyreCenterMedallionCryingRatio);
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        ExplosionSettings settings = job.getDynamite().explosion;
        CraterFillSettings fill = settings.effects.craterFill;
        int reach = (int) Math.ceil(radius);
        LongOpenHashSet applied = job.getTsarMedallionKeys();
        boolean onlyLoaded = general.sampleOnlyLoadedChunks;
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                double distSq = (double) dx * dx + dz * dz;
                if (distSq > radiusSq) {
                    continue;
                }
                int x = cx + dx;
                int z = cz + dz;
                if (onlyLoaded && !world.isChunkLoaded(x >> 4, z >> 4)) {
                    continue;
                }
                int surfaceY = resolveSurfaceY(world, x, z, cy, settings.radius, fill);
                if (surfaceY == Integer.MIN_VALUE) {
                    continue;
                }
                Material material = pickMedallionMaterial(x, z, cx, cz, distSq, radiusSq, cryingRatio);
                enqueueReplace(job, applied, x, surfaceY, z, material);
            }
        }
        ExplosionBlockOrder.prioritizeHellscape(job);
    }

    public boolean tick(ExplosionJob job, int budget, GeneralSettings general) {
        if (!general.griefLastPyreCenterMedallionEnabled || !job.isTsarMedallionQueued()) {
            return true;
        }
        if (medallionQueueEmpty(job)) {
            return true;
        }
        chunkContext.reset();
        while (budget > 0 && !job.getPendingBlocks().isEmpty()) {
            ExplosionJob.BlockTarget peek = job.getPendingBlocks().peekFirst();
            if (peek == null || !isMedallionAction(job, peek)) {
                break;
            }
            ExplosionJob.BlockTarget target = job.getPendingBlocks().pollFirst();
            blockApplier.applyBlock(job, target, chunkContext);
            budget--;
        }
        return medallionQueueEmpty(job);
    }

    public boolean isComplete(ExplosionJob job, GeneralSettings general) {
        if (!general.griefLastPyreCenterMedallionEnabled) {
            return true;
        }
        return job.isTsarMedallionQueued() && medallionQueueEmpty(job);
    }

    private static boolean medallionQueueEmpty(ExplosionJob job) {
        for (ExplosionJob.BlockTarget target : job.getPendingBlocks()) {
            if (isMedallionAction(job, target)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isMedallionAction(ExplosionJob job, ExplosionJob.BlockTarget target) {
        if (target.action() != ExplosionBlockAction.REPLACE) {
            return false;
        }
        long key = BlockCoordPacker.pack(target.x(), target.y(), target.z());
        return job.getTsarMedallionKeys().contains(key);
    }

    private Material pickMedallionMaterial(
            int x,
            int z,
            int cx,
            int cz,
            double distSq,
            double radiusSq,
            double cryingRatio
    ) {
        double distNorm = distSq / Math.max(1.0, radiusSq);
        double field = Math.sin(x * 0.41 + z * 0.29) * Math.cos(x * 0.17 - z * 0.33);
        double ripple = Math.sin((x - z) * 0.21 + cx * 0.03 - cz * 0.02) * 0.55;
        double vein = Math.sin(x * 0.09 - z * 0.11 + cx * 0.07) * 0.35;
        double density = (field + ripple + vein + 1.62) / 3.24;
        double centerPull = (1.0 - distNorm) * cryingRatio * 0.55;
        double threshold = 1.0 - cryingRatio;
        if (density + centerPull >= threshold) {
            return Material.CRYING_OBSIDIAN;
        }
        return Material.OBSIDIAN;
    }

    private void enqueueReplace(
            ExplosionJob job,
            LongOpenHashSet applied,
            int x,
            int y,
            int z,
            Material material
    ) {
        World world = job.getCenter().getWorld();
        if (world == null) {
            return;
        }
        y = clampY(world, y);
        long key = BlockCoordPacker.pack(x, y, z);
        if (!applied.add(key)) {
            return;
        }
        job.getPendingBlocks().addLast(new ExplosionJob.BlockTarget(
                x, y, z, ExplosionBlockAction.REPLACE, material
        ));
    }

    private static int resolveSurfaceY(
            World world,
            int x,
            int z,
            int centerY,
            float blastRadius,
            CraterFillSettings fill
    ) {
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 1;
        int top = Math.min(maxY, centerY + (int) Math.ceil(blastRadius) + 24);
        int bottom = Math.max(minY, centerY - (int) Math.ceil(blastRadius) - 24);
        for (int y = top; y >= bottom; y--) {
            Material type = world.getBlockAt(x, y, z).getType();
            if (isReplaceableSurface(type)) {
                return y;
            }
        }
        for (int y = top; y >= bottom; y--) {
            Material type = world.getBlockAt(x, y, z).getType();
            if (type.isAir() || type == Material.CAVE_AIR || isLiquid(type)) {
                continue;
            }
            return y;
        }
        int fallback = centerY - (int) Math.ceil(fill.radius * 0.3);
        return clampY(world, fallback);
    }

    private static boolean isReplaceableSurface(Material type) {
        if (type.isAir() || type == Material.CAVE_AIR) {
            return false;
        }
        if (isLiquid(type)) {
            return false;
        }
        return type != Material.BEDROCK
                && type != Material.BARRIER
                && type != Material.FIRE
                && type != Material.SOUL_FIRE;
    }

    private static boolean isLiquid(Material type) {
        return type == Material.WATER || type == Material.LAVA;
    }

    private static double clampRatio(double ratio) {
        return Math.min(0.45, Math.max(0.05, ratio));
    }

    private static int clampY(World world, int y) {
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 1;
        return Math.max(minY, Math.min(maxY, y));
    }

}
