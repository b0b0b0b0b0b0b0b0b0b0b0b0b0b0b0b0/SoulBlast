package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.ExplosionEffectsSettings;
import bm.b0b0b0.SoulBlast.config.GeneralSettings;
import bm.b0b0b0.SoulBlast.integration.coreprotect.CoreProtectBridge;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class TsarGradualDrainService {

    private final CoreProtectBridge coreProtect;

    public TsarGradualDrainService(CoreProtectBridge coreProtect) {
        this.coreProtect = coreProtect;
    }

    public boolean tick(ExplosionJob job, int budget, GeneralSettings general) {
        ExplosionEffectsSettings effects = job.getDynamite().explosion.effects;
        if (!effects.removeWater && !effects.removeLava) {
            return true;
        }
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null) {
            return true;
        }
        float radius = effects.liquidRadius > 0 ? effects.liquidRadius : job.getDynamite().explosion.radius;
        int maxRing = (int) Math.ceil(radius);
        while (budget > 0) {
            int ring = job.getTsarDrainRing();
            if (ring > maxRing) {
                return true;
            }
            RingDrainResult result = drainRing(job, world, center, ring, radius, effects, general, budget);
            budget = result.budgetLeft();
            if (result.cleared() > 0 && ring % 4 == 0) {
                playDrainPulse(world, center, ring);
            }
            if (result.ringComplete()) {
                job.setTsarDrainRing(ring + 1);
                continue;
            }
            break;
        }
        return job.getTsarDrainRing() > maxRing;
    }

    private RingDrainResult drainRing(
            ExplosionJob job,
            World world,
            Location center,
            int ring,
            float radius,
            ExplosionEffectsSettings effects,
            GeneralSettings general,
            int budget
    ) {
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        double sphereSq = radius * radius;
        double innerSq = ring == 0 ? -1.0 : (double) (ring - 1) * (ring - 1);
        double outerSq = Math.min((double) ring * ring, sphereSq);
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 1;
        int verticalReach = (int) Math.ceil(radius);
        int cleared = 0;
        boolean ringComplete = true;
        outer:
        for (int dx = -ring; dx <= ring; dx++) {
            for (int dz = -ring; dz <= ring; dz++) {
                double horizSq = (double) dx * dx + dz * dz;
                if (horizSq <= innerSq || horizSq > outerSq) {
                    continue;
                }
                int x = cx + dx;
                int z = cz + dz;
                if (general.sampleOnlyLoadedChunks && !world.isChunkLoaded(x >> 4, z >> 4)) {
                    continue;
                }
                for (int y = Math.max(minY, cy - verticalReach); y <= Math.min(maxY, cy + verticalReach); y++) {
                    double distSq = (double) dx * dx + (y - cy) * (y - cy) + dz * dz;
                    if (distSq > sphereSq) {
                        continue;
                    }
                    Block block = world.getBlockAt(x, y, z);
                    if (!shouldClear(block.getType(), effects)) {
                        continue;
                    }
                    if (budget <= 0) {
                        ringComplete = false;
                        break outer;
                    }
                    if (coreProtect != null) {
                        coreProtect.logLiquidClear(job, block);
                    }
                    block.setType(Material.AIR, false);
                    cleared++;
                    budget--;
                }
            }
        }
        return new RingDrainResult(cleared, budget, ringComplete);
    }

    private static void playDrainPulse(World world, Location center, int ring) {
        double angle = ring * 0.62;
        double x = center.getX() + Math.cos(angle) * ring;
        double z = center.getZ() + Math.sin(angle) * ring;
        world.spawnParticle(Particle.BUBBLE_POP, x, center.getY() + 1.2, z, 8, 0.35, 0.2, 0.35, 0.02);
        world.spawnParticle(Particle.SMOKE, x, center.getY() + 0.6, z, 3, 0.2, 0.15, 0.2, 0.01);
    }

    private static boolean shouldClear(Material material, ExplosionEffectsSettings effects) {
        if (effects.removeWater && isWater(material)) {
            return true;
        }
        return effects.removeLava && material == Material.LAVA;
    }

    private static boolean isWater(Material material) {
        return material == Material.WATER
                || material == Material.KELP
                || material == Material.KELP_PLANT
                || material == Material.SEAGRASS
                || material == Material.TALL_SEAGRASS
                || material == Material.BUBBLE_COLUMN;
    }

    private record RingDrainResult(int cleared, int budgetLeft, boolean ringComplete) {
    }

}
