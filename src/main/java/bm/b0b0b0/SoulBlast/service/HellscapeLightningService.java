package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.GeneralSettings;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
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
        strikeDrainRing(job, bolts);
    }

    public void tickDuringMask(ExplosionJob job, GeneralSettings general) {
        int bolts = general.griefLastPyreMaskLightningPerTick;
        if (bolts <= 0) {
            return;
        }
        float spread = general.griefLastPyreMaskLightningRadius > 0
                ? general.griefLastPyreMaskLightningRadius
                : job.getDynamite().explosion.radius;
        strikeSpread(job, bolts, spread, job.getCenter().getY() + 1.0);
    }

    private void strikeDrainRing(ExplosionJob job, int bolts) {
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        float maxRadius = resolveLiquidRadius(job);
        int maxRing = Math.max(1, (int) Math.ceil(maxRadius));
        int ring = Math.clamp(job.getTsarDrainRing(), 0, maxRing);
        if (ring > maxRing) {
            return;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double baseY = center.getY() + 0.8 + random.nextDouble() * 1.6;
        int cx = center.getBlockX();
        int cz = center.getBlockZ();
        for (int index = 0; index < bolts; index++) {
            double angle = (Math.PI * 2.0 * index) / Math.max(1, bolts)
                    + random.nextDouble() * 0.9
                    + ring * 0.17;
            double distance = ring == 0
                    ? random.nextDouble() * 1.4
                    : ring + (random.nextDouble() - 0.5) * 0.85;
            distance = Math.clamp(distance, 0.0, maxRadius);
            double x = cx + Math.cos(angle) * distance;
            double z = cz + Math.sin(angle) * distance;
            Location strike = new Location(world, x, baseY + random.nextDouble() * 0.5, z);
            world.strikeLightningEffect(strike);
        }
        if (random.nextInt(5) == 0) {
            double pulseAngle = ring * 0.62 + random.nextDouble() * 0.4;
            double pulseDist = Math.max(0.5, ring);
            Location pulse = new Location(
                    world,
                    cx + Math.cos(pulseAngle) * pulseDist,
                    baseY,
                    cz + Math.sin(pulseAngle) * pulseDist
            );
            world.strikeLightningEffect(pulse);
            world.playSound(pulse, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 0.82f + random.nextFloat() * 0.22f);
        }
    }

    private void strikeSpread(ExplosionJob job, int bolts, float spread, double strikeY) {
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null || spread <= 0f) {
            return;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int cx = center.getBlockX();
        int cz = center.getBlockZ();
        for (int index = 0; index < bolts; index++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = Math.sqrt(random.nextDouble()) * spread;
            double x = cx + Math.cos(angle) * distance;
            double z = cz + Math.sin(angle) * distance;
            Location strike = new Location(world, x, strikeY + random.nextDouble() * 0.4, z);
            world.strikeLightningEffect(strike);
        }
        if (random.nextInt(6) == 0) {
            world.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.55f, 0.85f + random.nextFloat() * 0.2f);
        }
    }

    private static float resolveLiquidRadius(ExplosionJob job) {
        float liquid = job.getDynamite().explosion.effects.liquidRadius;
        if (liquid > 0.01f) {
            return liquid;
        }
        return job.getDynamite().explosion.radius;
    }

}
