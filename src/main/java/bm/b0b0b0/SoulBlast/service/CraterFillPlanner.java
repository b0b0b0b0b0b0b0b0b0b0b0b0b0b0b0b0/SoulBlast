package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.CraterFillSettings;
import bm.b0b0b0.SoulBlast.config.ExplosionSettings;
import bm.b0b0b0.SoulBlast.config.ExplosionLimits;
import bm.b0b0b0.SoulBlast.config.GeneralSettings;
import bm.b0b0b0.SoulBlast.model.ExplosionBlockAction;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import bm.b0b0b0.SoulBlast.util.BlockCoordPacker;
import bm.b0b0b0.SoulBlast.util.BukkitKeys;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public final class CraterFillPlanner {

    private final HellscapeMaskPlanner hellscapeMaskPlanner = new HellscapeMaskPlanner();

    public void planIntoJob(ExplosionJob job, ExplosionLimits limits, GeneralSettings general) {
        ExplosionSettings settings = job.getDynamite().explosion;
        CraterFillSettings fill = settings.effects.craterFill;
        boolean tsar = TsarBombRules.isTsar(job.getDynamite());
        if (!tsar && (!fill.enabled || !TsarBombRules.allowsHellFill(job.getDynamite()))) {
            return;
        }
        if (tsar && !fill.enabled) {
            fill.enabled = true;
        }
        if (tsar) {
            hellscapeMaskPlanner.planIntoJob(job, limits, general);
            return;
        }
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        Material floor = BukkitKeys.material(fill.floorMaterial);
        if (floor == null) {
            floor = Material.MAGMA_BLOCK;
        }
        Material coat = resolveCoatMaterial(fill);
        Material shell = fill.magmaShell
                ? BukkitKeys.material(fill.shellMaterial)
                : null;
        if (shell == null && fill.magmaShell) {
            shell = Material.MAGMA_BLOCK;
        }
        int shellReach = fill.magmaShell
                ? (int) Math.ceil(fill.radius + fill.magmaShellWidth)
                : (int) Math.ceil(fill.radius);
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        double innerSq = fill.radius * fill.radius;
        double outerSq = (fill.radius + fill.magmaShellWidth) * (fill.radius + fill.magmaShellWidth);
        int maxOps = TsarBombRules.craterCap(limits, job.getDynamite());
        LongOpenHashSet breakKeys = new LongOpenHashSet();
        LongOpenHashSet placeKeys = new LongOpenHashSet();
        boolean onlyLoaded = general.sampleOnlyLoadedChunks;
        boolean extreme = "EXTREME".equalsIgnoreCase(settings.quality);
        boolean hellScatter = extreme && fill.hellFloorScatter && !TsarBombRules.isTsar(job.getDynamite());
        Random scatterRandom = hellScatter
                ? new Random(scatterSeed(cx, cy, cz, job.getDynamite().id))
                : null;
        List<FillColumn> columns = buildColumns(shellReach, innerSq, outerSq);
        if (extreme && !TsarBombRules.isTsar(job.getDynamite())) {
            carveBowl(job, breakKeys, world, cx, cy, cz, fill, innerSq, maxOps / 2, onlyLoaded);
        }
        for (FillColumn column : columns) {
            if (breakKeys.size() + placeKeys.size() >= maxOps) {
                break;
            }
            int x = cx + column.dx();
            int z = cz + column.dz();
            if (onlyLoaded && !world.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }
            int surfaceY = findCraterFloorY(world, x, z, cy, settings.radius, fill.radius);
            if (surfaceY == Integer.MIN_VALUE && extreme) {
                surfaceY = cy - (int) Math.ceil(fill.radius * 0.35);
            }
            if (surfaceY == Integer.MIN_VALUE) {
                continue;
            }
            surfaceY = tsar
                    ? resolveTsarSurfaceY(world, surfaceY, cy, fill)
                    : clampSurfaceY(world, surfaceY, cy);
            if (surfaceY == Integer.MIN_VALUE) {
                continue;
            }
            boolean inCrater = column.distSq() <= innerSq;
            boolean inShell = fill.magmaShell && column.distSq() > innerSq && column.distSq() <= outerSq;
            if (hellScatter && (inCrater || inShell)) {
                clearWalkSpace(job, breakKeys, world, x, z, surfaceY);
                planHellFloorScatter(
                        job,
                        breakKeys,
                        placeKeys,
                        world,
                        x,
                        z,
                        surfaceY,
                        fill,
                        floor,
                        scatterRandom,
                        inShell,
                        innerSq,
                        outerSq,
                        column.distSq()
                );
                continue;
            }
            if (inCrater) {
                carveColumn(job, breakKeys, world, x, z, surfaceY, fill, extreme, tsar);
                planCraterFloor(job, breakKeys, placeKeys, world, x, z, surfaceY, cy, fill, floor, coat, extreme, tsar);
            }
            if (inShell && shell != null) {
                planMagmaShell(job, breakKeys, placeKeys, world, x, z, surfaceY, fill, shell);
            }
        }
    }

    private static long scatterSeed(int cx, int cy, int cz, String dynamiteId) {
        long seed = BlockCoordPacker.pack(cx, cy, cz);
        seed ^= (long) dynamiteId.hashCode() * 0x9E3779B97L;
        return seed;
    }

    private void clearWalkSpace(
            ExplosionJob job,
            LongOpenHashSet breakKeys,
            World world,
            int x,
            int z,
            int surfaceY
    ) {
        int maxY = world.getMaxHeight() - 1;
        for (int y = surfaceY + 1; y <= Math.min(maxY, surfaceY + 3); y++) {
            enqueueBreak(job, breakKeys, world, x, y, z);
        }
    }

    private void planHellFloorScatter(
            ExplosionJob job,
            LongOpenHashSet breakKeys,
            LongOpenHashSet placeKeys,
            World world,
            int x,
            int z,
            int surfaceY,
            CraterFillSettings fill,
            Material magma,
            Random scatterRandom,
            boolean rimZone,
            double innerSq,
            double outerSq,
            double distSq
    ) {
        if (!shouldScatterBlock(x, z, fill, scatterRandom, rimZone, innerSq, outerSq, distSq)) {
            return;
        }
        enqueueBreak(job, breakKeys, world, x, surfaceY, z);
        enqueuePlace(job, placeKeys, x, surfaceY, z, pickHellFloorBlock(scatterRandom, magma, fill.hellFloorLavaRatio));
    }

    private Material pickHellFloorBlock(Random scatterRandom, Material magma, double accentRatio) {
        double roll = scatterRandom.nextDouble();
        if (roll < accentRatio * 0.45) {
            return Material.NETHERRACK;
        }
        if (roll < accentRatio) {
            return Material.SOUL_SAND;
        }
        return magma;
    }

    private boolean shouldScatterBlock(
            int x,
            int z,
            CraterFillSettings fill,
            Random scatterRandom,
            boolean rimZone,
            double innerSq,
            double outerSq,
            double distSq
    ) {
        double chance = fill.hellFloorDensity;
        if (((x + z) & 1) == 0) {
            chance += 0.11;
        } else {
            chance -= 0.06;
        }
        if (rimZone) {
            double innerRadius = Math.sqrt(innerSq);
            double outerRadius = Math.sqrt(outerSq);
            double span = Math.max(0.5, outerRadius - innerRadius);
            double rimFactor = (Math.sqrt(distSq) - innerRadius) / span;
            chance += 0.08 * Math.min(1.0, Math.max(0.0, rimFactor));
        }
        chance += (scatterRandom.nextDouble() - 0.5) * 0.22;
        chance = Math.min(0.72, Math.max(0.14, chance));
        return scatterRandom.nextDouble() < chance;
    }

    private void carveBowl(
            ExplosionJob job,
            LongOpenHashSet breakKeys,
            World world,
            int cx,
            int cy,
            int cz,
            CraterFillSettings fill,
            double innerSq,
            int budget,
            boolean onlyLoaded
    ) {
        int reach = (int) Math.ceil(fill.radius);
        int top = clampY(world, cy + reach / 2 + 4);
        int bottom = clampY(world, cy - reach - 6);
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                if (dx * dx + dz * dz > innerSq) {
                    continue;
                }
                int x = cx + dx;
                int z = cz + dz;
                if (onlyLoaded && !world.isChunkLoaded(x >> 4, z >> 4)) {
                    continue;
                }
                for (int y = bottom; y <= top; y++) {
                    if (breakKeys.size() >= budget) {
                        return;
                    }
                    enqueueBreak(job, breakKeys, world, x, y, z);
                }
            }
        }
    }

    private void carveColumn(
            ExplosionJob job,
            LongOpenHashSet breakKeys,
            World world,
            int x,
            int z,
            int surfaceY,
            CraterFillSettings fill,
            boolean extreme,
            boolean tsar
    ) {
        int depth = tsar ? fill.floorDepth + 2 : (extreme ? Math.min(4, fill.floorDepth) : fill.floorDepth + 2);
        int top = clampY(world, surfaceY + (tsar ? 6 : (extreme ? 3 : 4)));
        int bottom = clampY(world, surfaceY - depth);
        for (int y = bottom; y <= top; y++) {
            enqueueBreak(job, breakKeys, world, x, y, z);
        }
    }

    private List<FillColumn> buildColumns(int shellReach, double innerSq, double outerSq) {
        List<FillColumn> columns = new ArrayList<>();
        for (int dx = -shellReach; dx <= shellReach; dx++) {
            for (int dz = -shellReach; dz <= shellReach; dz++) {
                double distSq = dx * dx + dz * dz;
                if (distSq > outerSq) {
                    continue;
                }
                columns.add(new FillColumn(dx, dz, distSq));
            }
        }
        columns.sort(Comparator.comparingDouble(FillColumn::distSq).reversed());
        return columns;
    }

    private void planCraterFloor(
            ExplosionJob job,
            LongOpenHashSet breakKeys,
            LongOpenHashSet placeKeys,
            World world,
            int x,
            int z,
            int surfaceY,
            int centerY,
            CraterFillSettings fill,
            Material floor,
            Material coat,
            boolean extreme,
            boolean tsar
    ) {
        int floorLayers = tsar ? fill.floorDepth : (extreme ? Math.min(2, fill.floorDepth) : fill.floorDepth);
        for (int depth = 0; depth < floorLayers; depth++) {
            int y = surfaceY - depth;
            enqueueBreak(job, breakKeys, world, x, y, z);
            enqueuePlace(job, placeKeys, x, y, z, floor);
        }
        if (coat == null || !canPlaceCoat(world, x, z, surfaceY, centerY, tsar)) {
            return;
        }
        int coatLayers = tsar
                ? Math.max(3, fill.floorDepth / 3)
                : (extreme ? 1 : (fill.lavaChance >= 0.999 ? Math.max(2, fill.floorDepth) : 1));
        for (int layer = 0; layer < coatLayers; layer++) {
            int coatY = surfaceY + 1 + layer;
            if (!isOpenPitCell(world, x, coatY, z)) {
                break;
            }
            enqueueBreak(job, breakKeys, world, x, coatY, z);
            enqueuePlace(job, placeKeys, x, coatY, z, coat);
        }
    }

    private static Material resolveCoatMaterial(CraterFillSettings fill) {
        if (!fill.allowLavaCoat || fill.coatMaterial == null || fill.coatMaterial.isBlank()) {
            return null;
        }
        if (fill.lavaChance <= 0.0 && !"LAVA".equalsIgnoreCase(fill.coatMaterial)) {
            return BukkitKeys.material(fill.coatMaterial);
        }
        if (fill.lavaChance <= 0.0) {
            return null;
        }
        return BukkitKeys.material(fill.coatMaterial);
    }

    private static boolean canPlaceCoat(World world, int x, int z, int surfaceY, int centerY, boolean tsar) {
        if (tsar) {
            return isOpenPitCell(world, x, surfaceY + 1, z);
        }
        return surfaceY <= centerY + 2 && isOpenPitCell(world, x, surfaceY + 1, z);
    }

    private static int resolveTsarSurfaceY(World world, int surfaceY, int centerY, CraterFillSettings fill) {
        int rimCap = centerY + (int) Math.ceil(fill.radius * 0.55);
        if (surfaceY > rimCap) {
            surfaceY = centerY - (int) Math.ceil(fill.radius * 0.38);
        }
        return clampY(world, surfaceY);
    }

    private static boolean isOpenPitCell(World world, int x, int y, int z) {
        Material type = world.getBlockAt(x, y, z).getType();
        return type.isAir() || type == Material.CAVE_AIR || type == Material.WATER || type == Material.LAVA;
    }

    private static int clampSurfaceY(World world, int surfaceY, int centerY) {
        int maxSurface = centerY + 2;
        if (surfaceY > maxSurface) {
            return Integer.MIN_VALUE;
        }
        return clampY(world, surfaceY);
    }

    private void planMagmaShell(
            ExplosionJob job,
            LongOpenHashSet breakKeys,
            LongOpenHashSet placeKeys,
            World world,
            int x,
            int z,
            int surfaceY,
            CraterFillSettings fill,
            Material shell
    ) {
        int top = clampY(world, surfaceY + fill.magmaShellLayers + 2);
        int bottom = clampY(world, surfaceY - fill.floorDepth - fill.magmaShellLayers - 2);
        for (int y = top; y >= bottom; y--) {
            enqueueBreak(job, breakKeys, world, x, y, z);
        }
        for (int y = top; y >= bottom; y--) {
            enqueuePlace(job, placeKeys, x, y, z, shell);
        }
    }

    private void enqueueBreak(ExplosionJob job, LongOpenHashSet breakKeys, World world, int x, int y, int z) {
        y = clampY(world, y);
        Block block = world.getBlockAt(x, y, z);
        Material type = block.getType();
        if (type.isAir() || type == Material.CAVE_AIR || type == Material.LAVA || type == Material.WATER) {
            return;
        }
        if (type == Material.BEDROCK || type == Material.BARRIER) {
            return;
        }
        long key = BlockCoordPacker.pack(x, y, z);
        if (!breakKeys.add(key)) {
            return;
        }
        job.getPendingBlocks().addLast(new ExplosionJob.BlockTarget(x, y, z));
    }

    private void enqueuePlace(ExplosionJob job, LongOpenHashSet placeKeys, int x, int y, int z, Material material) {
        World world = job.getCenter().getWorld();
        if (world == null) {
            return;
        }
        y = clampY(world, y);
        long key = BlockCoordPacker.pack(x, y, z);
        if (!placeKeys.add(key)) {
            return;
        }
        job.getPendingBlocks().addLast(new ExplosionJob.BlockTarget(
                x, y, z, ExplosionBlockAction.PLACE, material
        ));
    }

    private int findCraterFloorY(World world, int x, int z, int centerY, float blastRadius, float fillRadius) {
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 1;
        int verticalReach = (int) Math.ceil(Math.max(blastRadius, fillRadius) * 1.35f) + 14;
        int bottom = Math.max(minY, centerY - verticalReach);
        int top = Math.min(maxY, centerY + (int) Math.ceil(fillRadius) + 8);
        int best = Integer.MIN_VALUE;
        for (int y = bottom; y <= top; y++) {
            Block block = world.getBlockAt(x, y, z);
            if (block.getType().isAir() || block.getType() == Material.CAVE_AIR || block.isLiquid()) {
                continue;
            }
            if (y + 1 <= maxY) {
                Block above = world.getBlockAt(x, y + 1, z);
                if (above.getType().isAir() || above.getType() == Material.CAVE_AIR || above.isLiquid()) {
                    if (y > best) {
                        best = y;
                    }
                }
            }
        }
        return best;
    }

    private static int clampY(World world, int y) {
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 1;
        return Math.max(minY, Math.min(maxY, y));
    }

    private record FillColumn(int dx, int dz, double distSq) {
    }

}
