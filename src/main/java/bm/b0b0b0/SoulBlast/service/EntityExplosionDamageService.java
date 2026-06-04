package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.EntityExplosionEntry;
import bm.b0b0b0.SoulBlast.config.ExplosionSettings;
import bm.b0b0b0.SoulBlast.util.BukkitKeys;
import bm.b0b0b0.SoulBlast.config.PluginConfig;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public final class EntityExplosionDamageService {

    private final Map<String, EntityExplosionEntry> byType = new HashMap<>();

    public void reload(PluginConfig config) {
        byType.clear();
        byType.putAll(config.entityExplosions);
    }

    public void applyDamage(ExplosionSettings settings, Entity source, Iterable<Entity> entities, org.bukkit.Location center) {
        double radius = settings.radius;
        for (Entity entity : entities) {
            if (entity.equals(source)) {
                continue;
            }
            if (entity instanceof org.bukkit.entity.Item) {
                continue;
            }
            double distance = entity.getLocation().distance(center);
            if (distance > radius * 2) {
                continue;
            }
            EntityExplosionEntry entry = byType.get(BukkitKeys.entityConfigKey(entity.getType()));
            double damageMultiplier = entry == null ? 1.0 : entry.damageMultiplier;
            double knockbackMultiplier = entry == null ? 1.0 : entry.knockbackMultiplier;
            if (entry != null && entry.destroyOnExplosion) {
                entity.remove();
                continue;
            }
            if (entity instanceof LivingEntity living && settings.entityDamage > 0) {
                boolean mayDamagePlayer = !(living instanceof Player) || settings.damagePlayers;
                if (mayDamagePlayer) {
                    double impact = (1.0 - distance / (radius * 2)) * settings.entityDamage * damageMultiplier;
                    if (impact > 0) {
                        living.damage(impact, source);
                    }
                }
            }
            Vector direction = entity.getLocation().toVector().subtract(center.toVector()).normalize();
            double knockback = settings.power * knockbackMultiplier / Math.max(0.5, distance);
            entity.setVelocity(direction.multiply(knockback));
        }
    }

}
