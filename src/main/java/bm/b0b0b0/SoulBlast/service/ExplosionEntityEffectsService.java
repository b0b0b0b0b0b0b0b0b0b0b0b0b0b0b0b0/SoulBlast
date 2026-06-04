package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.ExplosionEffectsSettings;
import bm.b0b0b0.SoulBlast.config.ExplosionSettings;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;

public final class ExplosionEntityEffectsService {

    public void apply(ExplosionJob job) {
        ExplosionSettings settings = job.getDynamite().explosion;
        ExplosionEffectsSettings effects = settings.effects;
        if (!effects.detonateOtherPrimed) {
            return;
        }
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        double radius = settings.radius * 1.5;
        Entity source = job.getSource();
        for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
            if (!(entity instanceof TNTPrimed primed)) {
                continue;
            }
            if (source != null && entity.getUniqueId().equals(source.getUniqueId())) {
                continue;
            }
            primed.setFuseTicks(Math.max(1, primed.getFuseTicks() / 4));
        }
    }

    public void breakTntBlocks(ExplosionJob job, ExplosionJob.BlockTarget target) {
        ExplosionEffectsSettings effects = job.getDynamite().explosion.effects;
        if (!effects.destroyTntBlocks) {
            return;
        }
        World world = job.getCenter().getWorld();
        if (world == null) {
            return;
        }
        if (world.getBlockAt(target.x(), target.y(), target.z()).getType() == Material.TNT) {
            world.getBlockAt(target.x(), target.y(), target.z()).setType(Material.AIR, false);
        }
    }

}
