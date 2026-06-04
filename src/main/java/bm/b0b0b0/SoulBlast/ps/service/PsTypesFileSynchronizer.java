package bm.b0b0b0.SoulBlast.ps.service;

import bm.b0b0b0.SoulBlast.ps.config.PsProtectionTypeDefinition;
import bm.b0b0b0.SoulBlast.ps.config.PsTypeCatalog;
import bm.b0b0b0.SoulBlast.ps.integration.PsConfiguredBlockInfo;
import bm.b0b0b0.SoulBlast.ps.integration.ProtectionStonesBridge;
import bm.b0b0b0.SoulBlast.ps.repository.PsTypesDirectory;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PsTypesFileSynchronizer {

    private PsTypesFileSynchronizer() {
    }

    public static int syncAndSave(
            JavaPlugin plugin,
            PsTypesDirectory typesDirectory,
            Map<String, PsProtectionTypeDefinition> loadedTypes,
            ProtectionStonesBridge bridge
    ) {
        if (!bridge.available()) {
            return 0;
        }
        Map<String, PsProtectionTypeDefinition> cleaned = cleanTypes(loadedTypes, bridge);
        int added = 0;
        List<PsConfiguredBlockInfo> blocks = bridge.listConfiguredBlocks();
        for (PsConfiguredBlockInfo block : blocks) {
            if (block.alias() == null || block.alias().isBlank()) {
                continue;
            }
            if (cleaned.containsKey(block.alias())) {
                continue;
            }
            PsProtectionTypeDefinition template = PsTypeCatalog.template(block.alias(), block.material());
            cleaned.put(block.alias(), template);
            typesDirectory.saveType(block.alias(), template);
            added++;
        }
        if (added > 0) {
            plugin.getLogger().info(
                    "[ProtectionStones+] В ps/types/ записано новых типов: " + added
                            + " — отредактируй голограмму, прочность и динамиты в файлах"
            );
        }
        return added;
    }

    private static Map<String, PsProtectionTypeDefinition> cleanTypes(
            Map<String, PsProtectionTypeDefinition> fromDisk,
            ProtectionStonesBridge bridge
    ) {
        Map<String, PsProtectionTypeDefinition> cleaned = new LinkedHashMap<>();
        if (fromDisk == null) {
            return cleaned;
        }
        for (Map.Entry<String, PsProtectionTypeDefinition> entry : fromDisk.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.isBlank()) {
                continue;
            }
            if (isRedundantMaterialKey(key, bridge)) {
                continue;
            }
            cleaned.put(key.trim(), entry.getValue());
        }
        return cleaned;
    }

    private static boolean isRedundantMaterialKey(String key, ProtectionStonesBridge bridge) {
        Material material = materialFromName(key);
        if (material == null) {
            return false;
        }
        for (PsConfiguredBlockInfo block : bridge.listConfiguredBlocks()) {
            if (material.equals(block.material()) && !key.equals(block.alias())) {
                return true;
            }
        }
        return false;
    }

    private static Material materialFromName(String name) {
        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

}
