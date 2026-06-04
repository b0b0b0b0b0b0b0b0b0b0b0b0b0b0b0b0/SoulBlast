package bm.b0b0b0.SoulBlast.decay.service;

import bm.b0b0b0.SoulBlast.decay.config.DecayBlockTypeDefinition;
import bm.b0b0b0.SoulBlast.decay.config.DecayManualRepairSettings;
import bm.b0b0b0.SoulBlast.util.BukkitKeys;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DecayRepairMaterialParser {

    private DecayRepairMaterialParser() {
    }

    public static List<DecayRepairMaterialCost> costsFor(
            DecayBlockTypeDefinition type,
            DecayManualRepairSettings settings
    ) {
        if (settings != null && settings.sandOnly) {
            return List.of(new DecayRepairMaterialCost(Material.SAND, 1));
        }
        List<DecayRepairMaterialCost> costs = new ArrayList<>();
        if (type != null && type.regeneration != null) {
            for (Map.Entry<String, String> entry : type.regeneration.materials.entrySet()) {
                Material material = BukkitKeys.material(entry.getKey());
                if (material == null || material.isAir()) {
                    continue;
                }
                costs.add(new DecayRepairMaterialCost(material, parseAmount(entry.getValue())));
            }
        }
        if (!costs.isEmpty()) {
            return List.copyOf(costs);
        }
        Material fallback = BukkitKeys.material(settings == null ? "SAND" : settings.fallbackMaterial);
        if (fallback == null || fallback.isAir()) {
            fallback = Material.SAND;
        }
        return List.of(new DecayRepairMaterialCost(fallback, 1));
    }

    private static int parseAmount(String raw) {
        if (raw == null || raw.isBlank()) {
            return 1;
        }
        String digits = raw.split(":")[0].trim();
        try {
            return Math.max(1, Integer.parseInt(digits));
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

}
