package bm.b0b0b0.SoulBlast.ps.service;

import bm.b0b0b0.SoulBlast.ps.config.PsProtectionTypeDefinition;
import bm.b0b0b0.SoulBlast.ps.config.PsTypeCatalog;
import bm.b0b0b0.SoulBlast.ps.integration.PsConfiguredBlockInfo;
import bm.b0b0b0.SoulBlast.ps.integration.ProtectionStonesBridge;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PsTypesMerger {

    private PsTypesMerger() {
    }

    public static Map<String, PsProtectionTypeDefinition> forRegistry(Map<String, PsProtectionTypeDefinition> fromYaml) {
        if (fromYaml != null && !fromYaml.isEmpty()) {
            return Map.copyOf(fromYaml);
        }
        return Map.of();
    }

    public static Map<String, PsProtectionTypeDefinition> normalizeKeys(Map<String, PsProtectionTypeDefinition> types) {
        if (types == null || types.isEmpty()) {
            return Map.of();
        }
        Map<String, PsProtectionTypeDefinition> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, PsProtectionTypeDefinition> entry : types.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null) {
                continue;
            }
            normalized.put(entry.getKey().trim(), entry.getValue());
        }
        return Map.copyOf(normalized);
    }

    public static Map<String, PsProtectionTypeDefinition> runtimeDefaults(ProtectionStonesBridge bridge) {
        if (bridge == null || !bridge.available()) {
            return Map.of();
        }
        List<PsConfiguredBlockInfo> blocks = bridge.listConfiguredBlocks();
        if (blocks.isEmpty()) {
            return Map.of();
        }
        Map<String, PsProtectionTypeDefinition> merged = new LinkedHashMap<>();
        for (PsConfiguredBlockInfo block : blocks) {
            if (block.alias() == null || block.alias().isBlank()) {
                continue;
            }
            merged.put(block.alias(), PsTypeCatalog.template(block.alias(), block.material()));
        }
        return Map.copyOf(merged);
    }

}
