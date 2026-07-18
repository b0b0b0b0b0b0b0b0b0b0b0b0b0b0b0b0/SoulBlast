package bm.b0b0b0.SoulBlast.ps.service;

import bm.b0b0b0.SoulBlast.ps.config.PsProtectionTypeDefinition;
import bm.b0b0b0.SoulBlast.ps.integration.ProtectionStonesBridge;
import bm.b0b0b0.SoulBlast.ps.integration.PsConfiguredBlockInfo;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public final class PsTypeRegistry {

    private final ProtectionStonesBridge bridge;
    private Map<String, PsProtectionTypeDefinition> byAlias = Map.of();
    private final Map<Material, String> aliasByMaterial = new EnumMap<>(Material.class);

    public PsTypeRegistry(ProtectionStonesBridge bridge) {
        this.bridge = bridge;
    }

    public void reload(Map<String, PsProtectionTypeDefinition> types) {
        byAlias = types == null ? Map.of() : Map.copyOf(types);
        aliasByMaterial.clear();
        if (bridge.available()) {
            for (PsConfiguredBlockInfo block : bridge.listConfiguredBlocks()) {
                if (block.material() != null && block.alias() != null) {
                    aliasByMaterial.put(block.material(), block.alias());
                }
            }
        }
        for (String alias : byAlias.keySet()) {
            Material material = materialFromAlias(alias);
            if (material != null) {
                aliasByMaterial.put(material, alias);
            }
        }
    }

    public java.util.Collection<PsProtectionTypeDefinition> allTypes() {
        return byAlias.values();
    }

    public Optional<PsProtectionTypeDefinition> findAlias(String alias) {
        if (alias == null) {
            return Optional.empty();
        }
        PsProtectionTypeDefinition direct = byAlias.get(alias);
        if (direct != null) {
            return Optional.of(direct);
        }
        if (!bridge.available()) {
            return Optional.empty();
        }
        String canonical = bridge.canonicalAlias(alias);
        if (canonical != null && !canonical.equals(alias)) {
            direct = byAlias.get(canonical);
            if (direct != null) {
                return Optional.of(direct);
            }
        }
        for (Map.Entry<String, PsProtectionTypeDefinition> entry : byAlias.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(alias)) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    public Optional<PsProtectionTypeDefinition> resolve(String alias, Block block) {
        Optional<PsProtectionTypeDefinition> fromAlias = findAlias(alias);
        if (fromAlias.isPresent()) {
            return fromAlias;
        }
        return findBlock(block);
    }

    public Optional<PsProtectionTypeDefinition> findBlock(Block block) {
        if (block == null) {
            return Optional.empty();
        }
        String alias = aliasByMaterial.get(block.getType());
        if (alias == null) {
            alias = bridge.aliasForMaterial(block.getType()).orElse(null);
        }
        if (alias == null) {
            return Optional.empty();
        }
        return findAlias(alias);
    }

    public Optional<String> resolveAlias(Block block) {
        if (block == null) {
            return Optional.empty();
        }
        Optional<String> fromBlock = bridge.aliasForProtectionBlock(block);
        if (fromBlock.isPresent()) {
            return fromBlock;
        }
        String cached = aliasByMaterial.get(block.getType());
        if (cached != null) {
            return Optional.of(cached);
        }
        return bridge.aliasForMaterial(block.getType());
    }

    private Material materialFromAlias(String alias) {
        try {
            return Material.valueOf(alias);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

}
