package bm.b0b0b0.SoulBlast.decay.service;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

public final class DecayInventoryMaterials {

    private DecayInventoryMaterials() {
    }

    public static DecayRepairMaterialCost findAffordable(Player player, List<DecayRepairMaterialCost> costs) {
        if (player == null || costs == null) {
            return null;
        }
        PlayerInventory inventory = player.getInventory();
        for (DecayRepairMaterialCost cost : costs) {
            if (count(inventory, cost.material()) >= cost.amount()) {
                return cost;
            }
        }
        return null;
    }

    public static boolean consume(Player player, DecayRepairMaterialCost cost) {
        if (player == null || cost == null) {
            return false;
        }
        int remaining = cost.amount();
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getStorageContents();
        for (int slot = 0; slot < contents.length && remaining > 0; slot++) {
            ItemStack stack = contents[slot];
            if (stack == null || stack.getType() != cost.material()) {
                continue;
            }
            int remove = Math.min(remaining, stack.getAmount());
            stack.setAmount(stack.getAmount() - remove);
            remaining -= remove;
            if (stack.getAmount() <= 0) {
                contents[slot] = null;
            }
        }
        inventory.setStorageContents(contents);
        return remaining <= 0;
    }

    private static int count(PlayerInventory inventory, Material material) {
        int total = 0;
        for (ItemStack stack : inventory.getStorageContents()) {
            if (stack != null && stack.getType() == material) {
                total += stack.getAmount();
            }
        }
        return total;
    }

}
