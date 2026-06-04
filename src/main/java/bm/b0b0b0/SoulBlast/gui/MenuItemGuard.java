package bm.b0b0b0.SoulBlast.gui;

import bm.b0b0b0.SoulBlast.util.PluginKeys;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

public final class MenuItemGuard {

    private final PluginKeys keys;

    public MenuItemGuard(PluginKeys keys) {
        this.keys = keys;
    }

    public boolean isMenuItem(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR || !stack.hasItemMeta()) {
            return false;
        }
        return stack.getItemMeta().getPersistentDataContainer().has(keys.menuIconType, PersistentDataType.STRING);
    }

    public void stripFromPlayer(Player player) {
        ItemStack cursor = player.getItemOnCursor();
        if (isMenuItem(cursor)) {
            player.setItemOnCursor(null);
        }
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getStorageContents();
        for (int slot = 0; slot < contents.length; slot++) {
            if (isMenuItem(contents[slot])) {
                inventory.setItem(slot, null);
            }
        }
        ItemStack offhand = inventory.getItemInOffHand();
        if (isMenuItem(offhand)) {
            inventory.setItemInOffHand(null);
        }
    }

}
