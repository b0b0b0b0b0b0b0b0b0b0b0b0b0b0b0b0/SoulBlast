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

import java.util.List;

public final class ExplosionLiquidSampler {

    public int clearInnerLiquidsImmediately(
            Location center,
            DynamiteDefinition dynamite,
            GeneralSettings general,
            int innerRingRadius
    ) {
        ExplosionEffectsSettings effects = dynamite.explosion.effects;
        if (!effects.removeWater && !effects.removeLava) {
            return 0;
        }
        World world = center.getWorld();
        if (world == null || innerRingRadius <= 0) {
            return 0;
        }
        float radius = effects.liquidRadius > 0 ? effects.liquidRadius : dynamite.explosion.radius;
        return clearHorizontalRings(world, center, radius, effects, general.sampleOnlyLoadedChunks, 0, innerRingRadius);
    }

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

    private int clearHorizontalRings(
            World world,
            Location center,
            float radius,
            ExplosionEffectsSettings effects,
            boolean onlyLoaded,
            int fromRing,
            int toRing
    ) {
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        double sphereSq = radius * radius;
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 1;
        int verticalReach = (int) Math.ceil(radius);
        int cleared = 0;
        for (int ring = fromRing; ring <= toRing; ring++) {
            double innerSq = ring == 0 ? -1.0 : (double) (ring - 1) * (ring - 1);
            double outerSq = Math.min((double) ring * ring, sphereSq);
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    double horizSq = (double) dx * dx + dz * dz;
                    if (horizSq <= innerSq || horizSq > outerSq) {
                        continue;
                    }
                    int x = cx + dx;
                    int z = cz + dz;
                    if (onlyLoaded && !world.isChunkLoaded(x >> 4, z >> 4)) {
                        continue;
                    }
                    for (int y = Math.max(minY, cy - verticalReach); y <= Math.min(maxY, cy + verticalReach); y++) {
                        double distSq = (double) dx * dx + (y - cy) * (y - cy) + dz * dz;
                        if (distSq > sphereSq) {
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
        }
        return cleared;
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
        for (int shell = 0; shell <= intRadius; shell++) {
            List<ExplosionSphereShells.ShellCell> cells = ExplosionSphereShells.collectShell(
                    world,
                    cx,
                    cy,
                    cz,
                    shell,
                    radiusSq,
                    onlyLoaded,
                    y -> true
            );
            int shellLiquids = 0;
            for (ExplosionSphereShells.ShellCell cell : cells) {
                Block block = world.getBlockAt(cell.x(), cell.y(), cell.z());
                if (shouldClear(block.getType(), effects)) {
                    shellLiquids++;
                }
            }
            if (shellLiquids > 0 && cleared + shellLiquids > maxBlocks) {
                return cleared;
            }
            for (ExplosionSphereShells.ShellCell cell : cells) {
                Block block = world.getBlockAt(cell.x(), cell.y(), cell.z());
                if (shouldClear(block.getType(), effects)) {
                    block.setType(Material.AIR, false);
                    cleared++;
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
        for (int shell = 0; shell <= intRadius; shell++) {
            List<ExplosionSphereShells.ShellCell> cells = ExplosionSphereShells.collectShell(
                    world,
                    cx,
                    cy,
                    cz,
                    shell,
                    radiusSq,
                    onlyLoaded,
                    y -> true
            );
            int shellAdds = 0;
            for (ExplosionSphereShells.ShellCell cell : cells) {
                long key = BlockCoordPacker.pack(cell.x(), cell.y(), cell.z());
                if (seen.contains(key)) {
                    continue;
                }
                Block block = world.getBlockAt(cell.x(), cell.y(), cell.z());
                if (shouldClear(block.getType(), effects)) {
                    shellAdds++;
                }
            }
            if (shellAdds > 0 && seen.size() + shellAdds > maxBlocks) {
                return;
            }
            for (ExplosionSphereShells.ShellCell cell : cells) {
                if (seen.size() >= maxBlocks) {
                    return;
                }
                long key = BlockCoordPacker.pack(cell.x(), cell.y(), cell.z());
                if (seen.contains(key)) {
                    continue;
                }
                Block block = world.getBlockAt(cell.x(), cell.y(), cell.z());
                if (!shouldClear(block.getType(), effects)) {
                    continue;
                }
                seen.add(key);
                job.getPendingBlocks().addLast(new ExplosionJob.BlockTarget(
                        cell.x(), cell.y(), cell.z(), ExplosionBlockAction.CLEAR_LIQUID, Material.AIR
                ));
            }
        }
    }

    public static int resolveLiquidClearBudget(float radius, ExplosionEffectsSettings effects) {
        if (effects.liquidDrainMaxBlocks > 0) {
            return effects.liquidDrainMaxBlocks;
        }
        int estimated = (int) Math.ceil((4.0 / 3.0) * Math.PI * radius * radius * radius);
        int floor = Math.max(1500, (int) (radius * radius * 14));
        int ceiling = Math.min(250_000, Math.max(24_000, (int) (radius * radius * 22)));
        return Math.min(ceiling, Math.max(floor, estimated));
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
