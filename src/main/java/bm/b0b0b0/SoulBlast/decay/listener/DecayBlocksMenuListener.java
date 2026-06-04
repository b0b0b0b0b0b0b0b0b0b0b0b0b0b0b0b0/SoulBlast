package bm.b0b0b0.SoulBlast.decay.listener;

import bm.b0b0b0.SoulBlast.decay.DecayModule;
import bm.b0b0b0.SoulBlast.decay.gui.DecayBlocksSortMode;
import bm.b0b0b0.SoulBlast.decay.gui.holder.DecayBlocksHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class DecayBlocksMenuListener implements Listener {

    private final DecayModule module;

    public DecayBlocksMenuListener(DecayModule module) {
        this.module = module;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof DecayBlocksHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        int slot = event.getRawSlot();
        int page = holder.page();
        DecayBlocksSortMode sortMode = holder.sortMode();
        if (slot == 39) {
            module.menuService().open(player, page - 1, sortMode);
            return;
        }
        if (slot == 41) {
            module.menuService().open(player, page + 1, sortMode);
            return;
        }
        if (slot == 42) {
            module.menuService().open(player, page, sortMode.next());
        }
    }

}
