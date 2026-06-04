package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.BlockResistanceEntry;
import bm.b0b0b0.SoulBlast.config.MaterialGroupDefinition;
import bm.b0b0b0.SoulBlast.config.MaterialGroupsFileConfig;
import bm.b0b0b0.SoulBlast.config.PluginConfig;
import bm.b0b0b0.SoulBlast.util.BukkitKeys;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;

public final class BlastResistanceService {

    private final Map<Material, Float> materialResistance = new HashMap<>();
    private final Map<String, MaterialGroupDefinition> groups = new HashMap<>();

    public void reload(PluginConfig config, MaterialGroupsFileConfig materialGroupsConfig) {
        materialResistance.clear();
        groups.clear();
        groups.putAll(materialGroupsConfig.materialGroups);
        for (BlockResistanceEntry entry : config.blockBlastResistance.values()) {
            applyEntry(entry.target, entry.blastResistance);
        }
        for (Map.Entry<String, MaterialGroupDefinition> groupEntry : materialGroupsConfig.materialGroups.entrySet()) {
            MaterialGroupDefinition group = groupEntry.getValue();
            for (String materialName : group.materials) {
                Material material = BukkitKeys.material(materialName);
                if (material != null) {
                    materialResistance.put(material, group.blastResistance);
                }
            }
        }
    }

    public float resolve(Block block) {
        Material material = block.getType();
        if (materialResistance.containsKey(material)) {
            return materialResistance.get(material);
        }
        return material.getBlastResistance();
    }

    public boolean matchesTarget(String target, Block block) {
        if (target == null) {
            return false;
        }
        if (groups.containsKey(target)) {
            MaterialGroupDefinition group = groups.get(target);
            return group.materials.stream()
                    .map(BukkitKeys::material)
                    .anyMatch(m -> m != null && m == block.getType());
        }
        Material material = BukkitKeys.material(target);
        return material != null && block.getType() == material;
    }

    private void applyEntry(String target, float resistance) {
        if (groups.containsKey(target)) {
            MaterialGroupDefinition group = groups.get(target);
            for (String materialName : group.materials) {
                Material material = BukkitKeys.material(materialName);
                if (material != null) {
                    materialResistance.put(material, resistance);
                }
            }
            return;
        }
        Material material = BukkitKeys.material(target);
        if (material != null) {
            materialResistance.put(material, resistance);
        }
    }

}
