package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.CraterFillSettings;
import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.config.ExplosionAlgorithmSettings;
import bm.b0b0b0.SoulBlast.config.ExplosionSettings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;

public final class ExplosionFireSupport {

    private final Random random = new Random();

    public void igniteOnSolidBeforeBreak(Block block, ExplosionSettings settings, ExplosionAlgorithmSettings algorithm) {
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
    }

    public void igniteHellscape(Location center, DynamiteDefinition dynamite) {
        if (!dynamite.explosion.createFire) {
            return;
        }
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
                for (int y = Math.max(minY, cy - horizontal); y <= Math.min(maxY, cy + 12); y++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (!isHellFuel(block.getType())) {
                        continue;
                    }
                    Block above = block.getRelative(0, 1, 0);
                    if (!above.getType().isAir() && above.getType() != Material.FIRE) {
                        continue;
                    }
                    if (random.nextDouble() > 0.38) {
                        continue;
                    }
                    Material fire = block.getType() == Material.SOUL_SAND
                            ? Material.SOUL_FIRE
                            : Material.FIRE;
                    above.setType(fire, false);
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

    public void removeUnsupportedFire(Location center, float radius) {
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
                        block.setType(Material.AIR, false);
                    }
                }
            }
        }
    }

}
