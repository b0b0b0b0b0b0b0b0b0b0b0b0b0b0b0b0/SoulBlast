package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.CraterFillSettings;
import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.config.ExplosionAlgorithmSettings;
import bm.b0b0b0.SoulBlast.config.ExplosionSettings;
import bm.b0b0b0.SoulBlast.integration.coreprotect.CoreProtectBridge;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;

public final class ExplosionFireSupport {

    private final CoreProtectBridge coreProtect;
    private final Random random = new Random();

    public ExplosionFireSupport(CoreProtectBridge coreProtect) {
        this.coreProtect = coreProtect;
    }

    public void igniteOnSolidBeforeBreak(
            ExplosionJob job,
            Block block,
            ExplosionSettings settings,
            ExplosionAlgorithmSettings algorithm
    ) {
        if (!settings.createFire || algorithm.fireChance <= 0) {
            return;
        }
        if (random.nextDouble() >= algorithm.fireChance) {
            return;
        }
        Material type = block.getType();
        if (!type.isSolid() || type.isAir() || block.isLiquid()) {
            return;
        }
        Block above = block.getRelative(0, 1, 0);
        Material aboveType = above.getType();
        if (!aboveType.isAir() && aboveType != Material.FIRE) {
            return;
        }
        above.setType(Material.FIRE, false);
        if (coreProtect != null) {
            coreProtect.logPlace(job, above);
        }
    }

    public void igniteHellscape(ExplosionJob job) {
        DynamiteDefinition dynamite = job.getDynamite();
        if (!dynamite.explosion.createFire) {
            return;
        }
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        CraterFillSettings fill = dynamite.explosion.effects.craterFill;
        float radius = fill.radius + fill.magmaShellWidth + 4.0f;
        int horizontal = (int) Math.ceil(radius);
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 1;
        double radiusSq = radius * radius;
        for (int dx = -horizontal; dx <= horizontal; dx++) {
            for (int dz = -horizontal; dz <= horizontal; dz++) {
                if (dx * dx + dz * dz > radiusSq) {
                    continue;
                }
                int x = cx + dx;
                int z = cz + dz;
                world.getChunkAt(x >> 4, z >> 4);
                if (HellscapePassageLayout.isPassage(cx, cz, x, z)) {
                    continue;
                }
                for (int y = Math.max(minY, cy - horizontal); y <= Math.min(maxY, cy + 12); y++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material fuel = block.getType();
                    if (!isHellFuel(fuel) || fuel == Material.SOUL_SAND) {
                        continue;
                    }
                    Block above = block.getRelative(0, 1, 0);
                    Material aboveType = above.getType();
                    if (!aboveType.isAir() && aboveType != Material.FIRE && aboveType != Material.SOUL_FIRE) {
                        continue;
                    }
                    if (!shouldScatterHellFire(x, z, y)) {
                        continue;
                    }
                    above.setType(Material.FIRE, false);
                    if (coreProtect != null) {
                        coreProtect.logPlace(job, above);
                    }
                }
            }
        }
    }

    private static boolean isHellFuel(Material type) {
        return type == Material.MAGMA_BLOCK
                || type == Material.LAVA
                || type == Material.NETHERRACK
                || type == Material.SOUL_SAND;
    }

    private boolean shouldScatterHellFire(int x, int z, int y) {
        double field = Math.sin(x * 0.27 + z * 0.19 + y * 0.07)
                * Math.cos(z * 0.23 - x * 0.11 - y * 0.05);
        double ripple = Math.sin((x - z) * 0.13 + y * 0.04) * 0.55;
        double density = (field + ripple + 1.42) / 2.84;
        if (density < 0.64) {
            return false;
        }
        return random.nextDouble() < 0.28;
    }

    public void removeUnsupportedFire(ExplosionJob job) {
        Location center = job.getCenter();
        float radius = job.getDynamite().explosion.radius;
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        int horizontal = (int) Math.ceil(radius) + 4;
        int vertical = (int) Math.ceil(radius * 0.6f) + 8;
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 1;
        for (int dx = -horizontal; dx <= horizontal; dx++) {
            for (int dz = -horizontal; dz <= horizontal; dz++) {
                if (dx * dx + dz * dz > horizontal * horizontal) {
                    continue;
                }
                int x = cx + dx;
                int z = cz + dz;
                if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                    continue;
                }
                for (int y = Math.max(minY, cy - vertical); y <= Math.min(maxY, cy + vertical); y++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() != Material.FIRE) {
                        continue;
                    }
                    Block below = block.getRelative(0, -1, 0);
                    Material belowType = below.getType();
                    if (!belowType.isSolid() || belowType == Material.FIRE) {
                        if (coreProtect != null) {
                            coreProtect.logBreak(job, block);
                        }
                        block.setType(Material.AIR, false);
                    }
                }
            }
        }
    }

}
