package bm.b0b0b0.SoulBlast.decay.listener;

import bm.b0b0b0.SoulBlast.decay.DecayModule;
import bm.b0b0b0.SoulBlast.decay.model.DecayingBlockKey;
import bm.b0b0b0.SoulBlast.decay.service.DecayManualRepairService;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class DecayManualRepairListener implements Listener {

    private final DecayModule module;

    public DecayManualRepairListener(DecayModule module) {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!module.isEnabled()) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        var repair = module.manualRepairSettings();
        if (repair == null || !repair.enabled) {
            return;
        }
        if (action == Action.RIGHT_CLICK_BLOCK && !repair.rightClick) {
            return;
        }
        if (action == Action.LEFT_CLICK_BLOCK && !repair.leftClick) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null || !isDamagedDecayBlock(block)) {
            return;
        }
        DecayManualRepairService service = module.manualRepairService();
        if (service == null) {
            return;
        }
        DecayManualRepairService.Result result = service.tryRepair(player, block);
        if (result == DecayManualRepairService.Result.SUCCESS) {
            event.setCancelled(true);
        }
    }

    private boolean isDamagedDecayBlock(Block block) {
        return module.store()
                .find(DecayingBlockKey.of(block.getWorld(), block.getX(), block.getY(), block.getZ()))
                .filter(state -> state.damage() > 0.0f && block.getType() == state.material())
                .isPresent();
    }

}
