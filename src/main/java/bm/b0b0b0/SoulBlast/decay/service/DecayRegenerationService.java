package bm.b0b0b0.SoulBlast.decay.service;

import bm.b0b0b0.SoulBlast.decay.config.DecayBlockTypeDefinition;
import bm.b0b0b0.SoulBlast.decay.model.DecayingBlockState;
import bm.b0b0b0.SoulBlast.decay.repository.DecayingBlockStore;
import bm.b0b0b0.SoulBlast.decay.util.DecayDurationParser;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class DecayRegenerationService {

    private final DecayingBlockStore store;
    private final DecayCrackBroadcaster crackBroadcaster;

    public DecayRegenerationService(DecayingBlockStore store, DecayCrackBroadcaster crackBroadcaster) {
        this.store = store;
        this.crackBroadcaster = crackBroadcaster;
    }

    public void tick() {
        long now = System.currentTimeMillis();
        for (DecayingBlockState state : store.snapshot()) {
            if (state.damage() <= 0.0f) {
                continue;
            }
            DecayBlockTypeDefinition type = state.type();
            long interval = DecayDurationParser.parseMillis(type.regeneration.every);
            if (now - state.lastRegenerationGameTime() < interval) {
                continue;
            }
            World world = Bukkit.getWorld(state.key().worldId());
            if (world == null) {
                store.remove(state.key());
                continue;
            }
            Block block = world.getBlockAt(state.key().x(), state.key().y(), state.key().z());
            if (block.getType() != state.material()) {
                store.remove(state.key());
                continue;
            }
            state.reduceDamage(0.08f);
            state.touchRegeneration();
            if (state.damage() <= 0.0f) {
                crackBroadcaster.clearBlock(state);
                store.remove(state.key());
            } else {
                crackBroadcaster.broadcastState(state, true);
            }
        }
    }

}
