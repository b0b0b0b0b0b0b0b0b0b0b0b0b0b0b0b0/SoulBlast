package bm.b0b0b0.SoulBlast.util;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Locale;

public final class BukkitKeys {

    private BukkitKeys() {
    }

    public static Material material(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return Material.matchMaterial(raw.trim());
    }

    public static String materialConfigKey(Material material) {
        return material.getKey().value().toUpperCase(Locale.ROOT);
    }

    public static String entityConfigKey(EntityType type) {
        return type.getKey().value().toUpperCase(Locale.ROOT);
    }

}
