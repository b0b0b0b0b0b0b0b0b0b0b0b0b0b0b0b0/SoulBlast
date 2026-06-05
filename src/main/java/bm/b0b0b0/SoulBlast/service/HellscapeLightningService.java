package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.GeneralSettings;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;

import java.util.concurrent.ThreadLocalRandom;

public final class HellscapeLightningService {

    public void tickDuringMask(ExplosionJob job, GeneralSettings general) {
        int bolts = general.griefLastPyreMaskLightningPerTick;
        if (bolts <= 0) {
            return;
        }
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        float spread = general.griefLastPyreMaskLightningRadius > 0
                ? general.griefLastPyreMaskLightningRadius
                : job.getDynamite().explosion.radius;
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

}
