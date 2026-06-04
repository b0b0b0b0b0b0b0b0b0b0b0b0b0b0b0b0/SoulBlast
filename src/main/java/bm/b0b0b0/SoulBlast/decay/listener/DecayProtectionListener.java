package bm.b0b0b0.SoulBlast.decay.listener;

import bm.b0b0b0.SoulBlast.decay.DecayModule;
import bm.b0b0b0.SoulBlast.decay.model.DecayingBlockKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

public final class DecayProtectionListener implements Listener {

    private final DecayModule module;

    public DecayProtectionListener(DecayModule module) {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (!module.isEnabled()) {
            return;
        }
        for (Block block : event.getBlocks()) {
            if (isActivelyDecaying(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!module.isEnabled()) {
            return;
        }
        for (Block block : event.getBlocks()) {
            if (isActivelyDecaying(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private boolean isActivelyDecaying(Block block) {
        DecayingBlockKey key = DecayingBlockKey.of(block.getWorld(), block.getX(), block.getY(), block.getZ());
        return module.store().find(key).isPresent();
    }

}
