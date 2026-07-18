package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.GeneralSettings;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import bm.b0b0b0.SoulBlast.util.PluginKeys;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public final class TsarDecoyBurstService {

    private static final int MAX_ACTIVE = 8;

    private final PluginKeys keys;

    public TsarDecoyBurstService(PluginKeys keys) {
        this.keys = keys;
    }

    public void tick(ExplosionJob job, GeneralSettings general) {
        int spawns = general.griefLastPyreDecoyBurstsPerTick;
        if (spawns <= 0) {
            return;
        }
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        float radius = resolveLiquidRadius(job);
        int maxRing = Math.max(1, (int) Math.ceil(radius));
        int ring = Math.clamp(job.getTsarDrainRing(), 1, maxRing);
        double wave = ring / (double) maxRing;
        if (wave > 0.88) {
            return;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (wave > 0.72 && random.nextDouble() > 0.42) {
            return;
        }
        if (countActiveNear(center, radius + 6.0) >= MAX_ACTIVE) {
            return;
        }
        for (int index = 0; index < spawns; index++) {
            if (countActiveNear(center, radius + 6.0) >= MAX_ACTIVE) {
                return;
            }
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = radius * (0.12 + wave * 0.55 + random.nextDouble() * 0.18);
            double x = center.getX() + Math.cos(angle) * distance;
            double z = center.getZ() + Math.sin(angle) * distance;
            double y = center.getY() + 1.1 + random.nextDouble() * 2.8;
            launchDecoy(world, new Location(world, x, y, z), center, angle, random);
        }
    }

    public void purgeNear(ExplosionJob job) {
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        float radius = resolveLiquidRadius(job) + 10.0f;
        double vertical = Math.max(12.0, radius * 0.35);
        for (Entity entity : world.getNearbyEntities(center, radius, vertical, radius)) {
            if (!(entity instanceof TNTPrimed primed)) {
                continue;
            }
            if (!isDecoy(primed)) {
                continue;
            }
            fizzle(primed);
        }
    }

    public static void fizzle(TNTPrimed primed) {
        World world = primed.getWorld();
        if (world != null) {
            Location at = primed.getLocation();
            world.spawnParticle(Particle.SMOKE, at, 8, 0.12, 0.1, 0.12, 0.025);
            world.spawnParticle(Particle.SMALL_FLAME, at, 3, 0.08, 0.06, 0.08, 0.012);
            world.spawnParticle(Particle.ASH, at, 4, 0.1, 0.08, 0.1, 0.008);
        }
        primed.remove();
    }

    private void launchDecoy(
            World world,
            Location spawnAt,
            Location center,
            double outwardAngle,
            ThreadLocalRandom random
    ) {
        TNTPrimed primed = (TNTPrimed) world.spawnEntity(spawnAt, EntityType.TNT);
        primed.setFuseTicks(28 + random.nextInt(24));
        primed.getPersistentDataContainer().set(keys.tsarDecoy, PersistentDataType.BYTE, (byte) 1);
        double speed = 0.42 + random.nextDouble() * 0.62;
        double lift = 0.28 + random.nextDouble() * 0.52;
        double spread = outwardAngle + (random.nextDouble() - 0.5) * 0.55;
        Vector velocity = new Vector(Math.cos(spread) * speed, lift, Math.sin(spread) * speed);
        double towardCore = 0.08 + random.nextDouble() * 0.14;
        velocity.add(new Vector(
                (center.getX() - spawnAt.getX()) * towardCore * 0.02,
                0.0,
                (center.getZ() - spawnAt.getZ()) * towardCore * 0.02
        ));
        primed.setVelocity(velocity);
    }

    private int countActiveNear(Location center, double radius) {
        World world = center.getWorld();
        if (world == null) {
            return 0;
        }
        int count = 0;
        double vertical = Math.max(10.0, radius * 0.3);
        for (Entity entity : world.getNearbyEntities(center, radius, vertical, radius)) {
            if (entity instanceof TNTPrimed primed && isDecoy(primed)) {
                count++;
            }
        }
        return count;
    }

    private boolean isDecoy(TNTPrimed primed) {
        return primed.getPersistentDataContainer().has(keys.tsarDecoy, PersistentDataType.BYTE);
    }

    private static float resolveLiquidRadius(ExplosionJob job) {
        float liquid = job.getDynamite().explosion.effects.liquidRadius;
        if (liquid > 0.01f) {
            return liquid;
        }
        return job.getDynamite().explosion.radius;
    }

}
