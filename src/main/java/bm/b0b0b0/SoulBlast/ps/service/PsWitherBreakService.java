package bm.b0b0b0.SoulBlast.ps.service;

import bm.b0b0b0.SoulBlast.ps.config.PsProtectionTypeDefinition;
import bm.b0b0b0.SoulBlast.ps.integration.ProtectionStonesBridge;
import bm.b0b0b0.SoulBlast.ps.model.PsBlockKey;
import bm.b0b0b0.SoulBlast.ps.model.PsBlockState;
import bm.b0b0b0.SoulBlast.ps.repository.PsBlockStore;
import org.bukkit.block.Block;

import java.util.Optional;

public final class PsWitherBreakService {

    private final PsTypeRegistry types;
    private final PsBlockStore store;
    private final ProtectionStonesBridge bridge;
    private final PsLifecycleService lifecycle;

    public PsWitherBreakService(
            PsTypeRegistry types,
            PsBlockStore store,
            ProtectionStonesBridge bridge,
            PsLifecycleService lifecycle
    ) {
        this.types = types;
        this.store = store;
        this.bridge = bridge;
        this.lifecycle = lifecycle;
    }

    public boolean tryBreak(Block block) {
        Optional<PsProtectionTypeDefinition> type = types.findBlock(block);
        if (type.isEmpty() || !type.get().breakProtectionBlock.withWither.enabled) {
            return false;
        }
        PsBlockKey key = PsBlockKey.of(block.getWorld(), block.getX(), block.getY(), block.getZ());
        PsBlockState state = store.find(key).orElse(null);
        if (state != null && type.get().durability.enabled) {
            state.applyDamage(state.maximum());
        }
        bridge.deleteRegion(block);
        lifecycle.onRemove(block);
        block.setType(org.bukkit.Material.AIR, false);
        return true;
    }

}
