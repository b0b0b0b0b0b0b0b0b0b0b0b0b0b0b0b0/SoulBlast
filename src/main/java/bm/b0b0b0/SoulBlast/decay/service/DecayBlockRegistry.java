package bm.b0b0b0.SoulBlast.decay.service;

import bm.b0b0b0.SoulBlast.config.MaterialGroupCatalog;
import bm.b0b0b0.SoulBlast.decay.config.DecayBlockCatalog;
import bm.b0b0b0.SoulBlast.decay.config.DecayBlockTypeCopier;
import bm.b0b0b0.SoulBlast.decay.config.DecayBlockTypeDefinition;
import bm.b0b0b0.SoulBlast.decay.config.DecayBlocksFileConfig;
import bm.b0b0b0.SoulBlast.util.BukkitKeys;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;

public final class DecayBlockRegistry {

    private final Map<Material, DecayBlockTypeDefinition> byMaterial = new EnumMap<>(Material.class);
    private List<DecayBlockEntry> menuEntries = List.of();

    public void reload(DecayBlocksFileConfig config) {
        byMaterial.clear();
        List<DecayBlockEntry> menu = new ArrayList<>();
        for (Map.Entry<String, DecayBlockTypeDefinition> entry : config.types.entrySet()) {
            Material material = BukkitKeys.material(entry.getKey());
            if (material == null) {
                continue;
            }
            byMaterial.put(material, entry.getValue());
            menu.add(new DecayBlockEntry(entry.getKey(), material, entry.getValue()));
        }
        expandMasonryFamily(config);
        menu.sort(Comparator.comparing(entry -> entry.type().resistance));
        menuEntries = List.copyOf(menu);
    }

    public Optional<DecayBlockTypeDefinition> find(Block block) {
        if (block == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byMaterial.get(block.getType()));
    }

    public boolean supports(Material material) {
        return byMaterial.containsKey(material);
    }

    public List<DecayBlockEntry> menuEntries() {
        return menuEntries;
    }

    public int explosionMaterialCount() {
        return byMaterial.size();
    }

    private void expandMasonryFamily(DecayBlocksFileConfig config) {
        DecayBlockTypeDefinition template = config.types.get("STONE_BRICKS");
        if (template == null) {
            template = DecayBlockCatalog.defaults().get("STONE_BRICKS");
        }
        if (template == null) {
            return;
        }
        for (String materialName : MaterialGroupCatalog.raidMasonryMaterialNames()) {
            Material material = BukkitKeys.material(materialName);
            if (material == null || byMaterial.containsKey(material)) {
                continue;
            }
            byMaterial.put(material, DecayBlockTypeCopier.copy(template));
        }
    }

    public record DecayBlockEntry(String configKey, Material material, DecayBlockTypeDefinition type) {
    }

}
