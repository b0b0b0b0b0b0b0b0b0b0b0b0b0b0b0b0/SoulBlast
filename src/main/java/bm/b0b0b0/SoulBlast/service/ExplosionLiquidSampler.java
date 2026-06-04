package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.config.ExplosionEffectsSettings;
import bm.b0b0b0.SoulBlast.config.ExplosionLimits;
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

public final class ExplosionLiquidSampler {

    public int clearLiquidsImmediately(
            Location center,
            DynamiteDefinition dynamite,
            ExplosionLimits limits,
            GeneralSettings general
    ) {
        ExplosionEffectsSettings effects = dynamite.explosion.effects;
        if (!effects.removeWater && !effects.removeLava) {
            return 0;
        }
        World world = center.getWorld();
        if (world == null) {
            return 0;
        }
        float radius = effects.liquidRadius > 0 ? effects.liquidRadius : dynamite.explosion.radius;
        int maxBlocks = resolveLiquidClearBudget(radius, effects);
        return clearSphere(world, center, radius, effects, general.sampleOnlyLoadedChunks, maxBlocks);
    }

    public void sampleIntoJob(ExplosionJob job, ExplosionLimits limits, GeneralSettings general) {
        ExplosionSettings settings = job.getDynamite().explosion;
        ExplosionEffectsSettings effects = settings.effects;
        if (!effects.removeWater && !effects.removeLava) {
            return;
        }
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        float radius = effects.liquidRadius > 0 ? effects.liquidRadius : settings.radius;
        int maxExtra = Math.max(200, resolveLiquidClearBudget(radius, effects) / 2);
        LongOpenHashSet seen = new LongOpenHashSet();
        collectIntoJob(job, world, center, radius, effects, general.sampleOnlyLoadedChunks, seen, maxExtra);
    }

    private int clearSphere(
            World world,
            Location center,
            float radius,
            ExplosionEffectsSettings effects,
            boolean onlyLoaded,
            int maxBlocks
    ) {
        int intRadius = (int) Math.ceil(radius);
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        double radiusSq = radius * radius;
        int cleared = 0;
        outer:
        for (int dx = -intRadius; dx <= intRadius; dx++) {
            for (int dy = -intRadius; dy <= intRadius; dy++) {
                for (int dz = -intRadius; dz <= intRadius; dz++) {
                    if (cleared >= maxBlocks) {
                        break outer;
                    }
                    if (dx * dx + dy * dy + dz * dz > radiusSq) {
                        continue;
                    }
                    int x = cx + dx;
                    int y = cy + dy;
                    int z = cz + dz;
                    if (onlyLoaded && !world.isChunkLoaded(x >> 4, z >> 4)) {
                        continue;
                    }
                    Block block = world.getBlockAt(x, y, z);
                    if (shouldClear(block.getType(), effects)) {
                        block.setType(Material.AIR, false);
                        cleared++;
                    }
                }
            }
        }
        return cleared;
    }

    private void collectIntoJob(
            ExplosionJob job,
            World world,
            Location center,
            float radius,
            ExplosionEffectsSettings effects,
            boolean onlyLoaded,
            LongOpenHashSet seen,
            int maxBlocks
    ) {
        int intRadius = (int) Math.ceil(radius);
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        double radiusSq = radius * radius;
        outer:
        for (int dx = -intRadius; dx <= intRadius; dx++) {
            for (int dy = -intRadius; dy <= intRadius; dy++) {
                for (int dz = -intRadius; dz <= intRadius; dz++) {
                    if (seen.size() >= maxBlocks) {
                        break outer;
                    }
                    if (dx * dx + dy * dy + dz * dz > radiusSq) {
                        continue;
                    }
                    int x = cx + dx;
                    int y = cy + dy;
                    int z = cz + dz;
                    if (onlyLoaded && !world.isChunkLoaded(x >> 4, z >> 4)) {
                        continue;
                    }
                    long key = BlockCoordPacker.pack(x, y, z);
                    if (!seen.add(key)) {
                        continue;
                    }
                    Block block = world.getBlockAt(x, y, z);
                    if (shouldClear(block.getType(), effects)) {
                        job.getPendingBlocks().add(new ExplosionJob.BlockTarget(
                                x, y, z, ExplosionBlockAction.CLEAR_LIQUID, Material.AIR
                        ));
                    }
                }
            }
        }
    }

    public static int resolveLiquidClearBudget(float radius, ExplosionEffectsSettings effects) {
        if (effects.liquidDrainMaxBlocks > 0) {
            return effects.liquidDrainMaxBlocks;
        }
        int estimated = (int) Math.ceil((4.0 / 3.0) * Math.PI * radius * radius * radius);
        int floor = Math.max(1500, (int) (radius * radius * 14));
        return Math.min(18000, Math.max(floor, estimated));
    }

    public static boolean drainsLiquids(DynamiteDefinition dynamite) {
        if (dynamite == null) {
            return false;
        }
        ExplosionEffectsSettings effects = dynamite.explosion.effects;
        return effects.removeWater || effects.removeLava;
    }

    private boolean shouldClear(Material material, ExplosionEffectsSettings effects) {
        if (effects.removeWater && isWater(material)) {
            return true;
        }
        return effects.removeLava && isLava(material);
    }

    private boolean isWater(Material material) {
        return material == Material.WATER
                || material == Material.KELP
                || material == Material.KELP_PLANT
                || material == Material.SEAGRASS
                || material == Material.TALL_SEAGRASS
                || material == Material.BUBBLE_COLUMN;
    }

    private boolean isLava(Material material) {
        return material == Material.LAVA;
    }

}
