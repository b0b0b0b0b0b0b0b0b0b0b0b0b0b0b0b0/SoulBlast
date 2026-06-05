package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.GeneralSettings;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;

import java.util.concurrent.ThreadLocalRandom;

public final class HellscapeLightningService {

    public void tickDuringDrain(ExplosionJob job, GeneralSettings general) {
        int bolts = general.griefLastPyreDrainLightningPerTick;
        if (bolts <= 0) {
            return;
        }
        strikeSpread(job, general, bolts, drainSpread(job, general));
    }

    public void tickDuringMask(ExplosionJob job, GeneralSettings general) {
        int bolts = general.griefLastPyreMaskLightningPerTick;
        if (bolts <= 0) {
            return;
        }
        float spread = general.griefLastPyreMaskLightningRadius > 0
                ? general.griefLastPyreMaskLightningRadius
                : job.getDynamite().explosion.radius;
        strikeSpread(job, general, bolts, spread);
    }

    private void strikeSpread(ExplosionJob job, GeneralSettings general, int bolts, float spread) {
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null || spread <= 0f) {
            return;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int index = 0; index < bolts; index++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = Math.sqrt(random.nextDouble()) * spread;
            int blockX = center.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
            int blockZ = center.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
            int surfaceY = world.getHighestBlockYAt(blockX, blockZ, HeightMap.MOTION_BLOCKING);
            Location strike = new Location(world, blockX + 0.5, surfaceY + 1.0, blockZ + 0.5);
            world.strikeLightningEffect(strike);
        }
        if (random.nextInt(6) == 0) {
            world.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.55f, 0.85f + random.nextFloat() * 0.2f);
        }
    }

    private float drainSpread(ExplosionJob job, GeneralSettings general) {
        float radius = general.griefLastPyreMaskLightningRadius > 0
                ? general.griefLastPyreMaskLightningRadius
                : resolveLiquidRadius(job);
        int maxRing = Math.max(1, (int) Math.ceil(radius));
        int ring = Math.min(maxRing, Math.max(1, job.getTsarDrainRing()));
        return radius * (0.22f + (ring / (float) maxRing) * 0.78f);
    }

    private static float resolveLiquidRadius(ExplosionJob job) {
        float liquid = job.getDynamite().explosion.effects.liquidRadius;
        if (liquid > 0.01f) {
            return liquid;
        }
        return job.getDynamite().explosion.radius;
    }

}
