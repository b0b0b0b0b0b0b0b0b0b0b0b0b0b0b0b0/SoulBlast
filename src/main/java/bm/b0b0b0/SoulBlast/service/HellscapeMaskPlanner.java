package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.CraterFillSettings;
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

import java.util.Random;

public final class HellscapeMaskPlanner {

    public void planIntoJob(ExplosionJob job, ExplosionLimits limits, GeneralSettings general) {
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        int reach = resolveReach(job);
        int maxOps = TsarBombRules.craterCap(limits, job.getDynamite());
        LongOpenHashSet applied = new LongOpenHashSet();
        for (int ring = 0; ring <= reach; ring++) {
            if (applied.size() >= maxOps) {
                break;
            }
            planHorizontalRing(job, ring, applied, limits, general);
        }
        job.getDiagnostics().setPlannedMask(applied.size());
        job.getDiagnostics().setMaskMeta(reach, estimateColumns(reach));
    }

    public int resolveReach(ExplosionJob job) {
        ExplosionSettings settings = job.getDynamite().explosion;
        CraterFillSettings fill = settings.effects.craterFill;
        return (int) Math.ceil(Math.max(
                fill.radius + fill.magmaShellWidth,
                settings.radius * 1.02f
        ));
    }

    public int estimateColumns(int reach) {
        return (int) Math.ceil(Math.PI * reach * reach);
    }

    public void planHorizontalRing(
            ExplosionJob job,
            int ring,
            ExplosionLimits limits,
            GeneralSettings general
    ) {
        planHorizontalRing(job, ring, job.getTsarMaskKeys(), limits, general);
    }

    public void planHorizontalRing(
            ExplosionJob job,
            int ring,
            LongOpenHashSet applied,
            ExplosionLimits limits,
            GeneralSettings general
    ) {
        ExplosionSettings settings = job.getDynamite().explosion;
        CraterFillSettings fill = settings.effects.craterFill;
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        float blastRadius = settings.radius;
        int reach = resolveReach(job);
        double innerSq = fill.radius * fill.radius;
        double outerSq = (double) reach * reach;
        int maxOps = TsarBombRules.craterCap(limits, job.getDynamite());
        boolean onlyLoaded = general.sampleOnlyLoadedChunks;
        Random random = new Random(maskSeed(cx, cy, cz) ^ ring);
        double bandInnerSq = ring == 0 ? -1.0 : (double) (ring - 1) * (ring - 1);
        double bandOuterSq = Math.min((double) ring * ring, outerSq);
        for (int dx = -ring; dx <= ring; dx++) {
            for (int dz = -ring; dz <= ring; dz++) {
                if (applied.size() >= maxOps) {
                    return;
                }
                double distSq = (double) dx * dx + dz * dz;
                if (distSq <= bandInnerSq || distSq > bandOuterSq) {
                    continue;
                }
                int x = cx + dx;
                int z = cz + dz;
                if (onlyLoaded && !world.isChunkLoaded(x >> 4, z >> 4)) {
                    continue;
                }
                int surfaceY = resolveSurfaceY(world, x, z, cy, blastRadius, fill);
                if (surfaceY == Integer.MIN_VALUE) {
                    continue;
                }
                Material surface = pickSurfaceMaterial(distSq, innerSq, outerSq, x, z, fill, random);
                enqueueReplace(job, applied, x, surfaceY, z, surface);
                if (surface == Material.SOUL_SAND) {
                    enqueueSoulFire(job, applied, world, x, surfaceY + 1, z);
                }
                if (distSq <= innerSq * 0.5) {
                    planInnerDepth(job, applied, maxOps, world, x, z, surfaceY, fill, random, innerSq, distSq);
                }
            }
        }
    }

    private void planInnerDepth(
            ExplosionJob job,
            LongOpenHashSet applied,
            int maxOps,
            World world,
            int x,
            int z,
            int surfaceY,
            CraterFillSettings fill,
            Random random,
            double innerSq,
            double distSq
    ) {
        int layers = Math.min(4, Math.max(2, fill.floorDepth / 4));
        for (int depth = 1; depth <= layers; depth++) {
            if (applied.size() >= maxOps) {
                return;
            }
            int y = surfaceY - depth;
            if (!isReplaceable(world.getBlockAt(x, y, z).getType())) {
                continue;
            }
            enqueueReplace(job, applied, x, y, z, Material.MAGMA_BLOCK);
        }
        if (distSq > innerSq * 0.22 || random.nextDouble() > 0.055) {
            return;
        }
        int lavaY = surfaceY + 1;
        Block above = world.getBlockAt(x, lavaY, z);
        if (!isOpenCell(above.getType())) {
            return;
        }
        Block above2 = world.getBlockAt(x, lavaY + 1, z);
        if (!isOpenCell(above2.getType())) {
            return;
        }
        enqueueReplace(job, applied, x, lavaY, z, Material.LAVA);
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
        int topSolid = Integer.MIN_VALUE;
        for (int y = top; y >= bottom; y--) {
            Material type = world.getBlockAt(x, y, z).getType();
            if (isReplaceable(type)) {
                topSolid = y;
                break;
            }
        }
        if (topSolid != Integer.MIN_VALUE) {
            return topSolid;
        }
        int pitFloor = Integer.MIN_VALUE;
        for (int y = top; y >= bottom; y--) {
            Material type = world.getBlockAt(x, y, z).getType();
            if (type.isAir() || type == Material.CAVE_AIR || isLiquidMaterial(type)) {
                continue;
            }
            pitFloor = y;
            break;
        }
        if (pitFloor != Integer.MIN_VALUE) {
            return pitFloor;
        }
        int fallback = centerY - (int) Math.ceil(fill.radius * 0.3);
        return clampY(world, fallback);
    }

    private Material pickSurfaceMaterial(
            double distSq,
            double innerSq,
            double outerSq,
            int x,
            int z,
            CraterFillSettings fill,
            Random random
    ) {
        double distNorm = distSq / Math.max(1.0, outerSq);
        double accent = Math.min(0.42, Math.max(0.12, fill.hellFloorLavaRatio + 0.08));
        if (distSq <= innerSq * 0.35) {
            accent *= 0.55;
        } else if (distSq > innerSq) {
            accent = Math.min(0.48, accent + 0.14);
        }
        int hash = (x * 31_412 ^ z * 17_171 ^ (int) (distNorm * 1000)) & 0xFFFF;
        double roll = (hash / 65535.0 + random.nextDouble() * 0.08) % 1.0;
        if (roll < accent * 0.42) {
            return Material.SOUL_SAND;
        }
        if (roll < accent * 0.72) {
            return Material.NETHERRACK;
        }
        return Material.MAGMA_BLOCK;
    }

    private void enqueueSoulFire(
            ExplosionJob job,
            LongOpenHashSet applied,
            World world,
            int x,
            int y,
            int z
    ) {
        y = clampY(world, y);
        Material type = world.getBlockAt(x, y, z).getType();
        if (!type.isAir() && type != Material.FIRE && type != Material.SOUL_FIRE) {
            return;
        }
        long key = BlockCoordPacker.pack(x, y, z);
        if (!applied.add(key)) {
            return;
        }
        job.getPendingBlocks().addLast(new ExplosionJob.BlockTarget(
                x, y, z, ExplosionBlockAction.REPLACE, Material.SOUL_FIRE
        ));
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
        Material type = world.getBlockAt(x, y, z).getType();
        if (!isReplaceable(type) && !isOpenCell(type)) {
            if (type == Material.BEDROCK || type == Material.BARRIER) {
                return;
            }
        }
        long key = BlockCoordPacker.pack(x, y, z);
        if (!applied.add(key)) {
            return;
        }
        job.getPendingBlocks().addLast(new ExplosionJob.BlockTarget(
                x, y, z, ExplosionBlockAction.REPLACE, material
        ));
    }

    private static boolean isReplaceable(Material type) {
        if (type.isAir() || type == Material.CAVE_AIR) {
            return false;
        }
        if (isLiquidMaterial(type)) {
            return false;
        }
        return type != Material.BEDROCK
                && type != Material.BARRIER
                && type != Material.FIRE
                && type != Material.SOUL_FIRE;
    }

    private static boolean isOpenCell(Material type) {
        return type.isAir() || type == Material.CAVE_AIR || isLiquidMaterial(type);
    }

    private static boolean isLiquidMaterial(Material type) {
        return type == Material.WATER || type == Material.LAVA;
    }

    private static long maskSeed(int cx, int cy, int cz) {
        return BlockCoordPacker.pack(cx, cy, cz) ^ 0x5DEECE66DL;
    }

    private static int clampY(World world, int y) {
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 1;
        return Math.max(minY, Math.min(maxY, y));
    }

}
