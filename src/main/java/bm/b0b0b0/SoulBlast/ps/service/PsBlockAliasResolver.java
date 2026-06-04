package bm.b0b0b0.SoulBlast.ps.service;

import bm.b0b0b0.SoulBlast.ps.integration.ProtectionStonesBridge;
import org.bukkit.block.Block;

import java.util.Optional;

public final class PsBlockAliasResolver {

    private PsBlockAliasResolver() {
    }

    public static String canonicalForStorage(
            String storedAlias,
            Block block,
            ProtectionStonesBridge bridge,
            PsTypeRegistry types
    ) {
        if (storedAlias != null && !storedAlias.isBlank() && types != null) {
            String canonicalStored = bridge == null ? storedAlias : bridge.canonicalAlias(storedAlias);
            if (types.findAlias(canonicalStored).isPresent()) {
                return canonicalStored;
            }
            if (types.findAlias(storedAlias).isPresent()) {
                return storedAlias;
            }
        }
        if (block != null && bridge != null && bridge.available()) {
            Optional<String> fromBlock = bridge.aliasForProtectionBlock(block);
            if (fromBlock.isPresent()) {
                String canonicalBlock = bridge.canonicalAlias(fromBlock.get());
                if (types != null && types.findAlias(canonicalBlock).isPresent()) {
                    return canonicalBlock;
                }
            }
        }
        if (storedAlias != null && !storedAlias.isBlank() && types != null) {
            if (types.findAlias(storedAlias).isPresent()) {
                return bridge == null ? storedAlias : bridge.canonicalAlias(storedAlias);
            }
            if (bridge != null && bridge.available()) {
                String canonical = bridge.canonicalAlias(storedAlias);
                if (types.findAlias(canonical).isPresent()) {
                    return canonical;
                }
            }
        }
        if (block != null && bridge != null && bridge.available()) {
            Optional<String> fromMaterial = bridge.aliasForMaterial(block.getType());
            if (fromMaterial.isPresent()) {
                return bridge.canonicalAlias(fromMaterial.get());
            }
        }
        return storedAlias == null ? "" : storedAlias;
    }

}
