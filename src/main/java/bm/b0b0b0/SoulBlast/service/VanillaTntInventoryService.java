package bm.b0b0b0.SoulBlast.service;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class VanillaTntInventoryService {

    private final DynamiteItemFactory itemFactory;

    public VanillaTntInventoryService(DynamiteItemFactory itemFactory) {
        this.itemFactory = itemFactory;
    }

    public int countVanillaTnt(Player player) {
        int total = 0;
        PlayerInventory inventory = player.getInventory();
        for (ItemStack stack : inventory.getStorageContents()) {
            total += stackAmount(stack);
        }
        total += stackAmount(inventory.getItemInOffHand());
        total += stackAmount(player.getItemOnCursor());
        return total;
    }

    public int removeVanillaTnt(Player player, int amount) {
        if (amount <= 0) {
            return 0;
        }
        int remaining = amount;
        PlayerInventory inventory = player.getInventory();
        ItemStack[] storage = inventory.getStorageContents();
        for (int slot = 0; slot < storage.length && remaining > 0; slot++) {
            remaining -= takeFromSlot(inventory, slot, remaining);
        }
        if (remaining > 0) {
            remaining -= takeFromHeld(inventory.getItemInOffHand(), inventory::setItemInOffHand, remaining);
        }
        if (remaining > 0) {
            remaining -= takeFromHeld(player.getItemOnCursor(), player::setItemOnCursor, remaining);
        }
        return amount - remaining;
    }

    private int takeFromSlot(PlayerInventory inventory, int slot, int maxTake) {
        ItemStack stack = inventory.getItem(slot);
        TakeResult result = takeFromStack(stack, maxTake);
        if (result.taken() <= 0) {
            return 0;
        }
        if (result.stack() == null || result.stack().getAmount() <= 0) {
            inventory.setItem(slot, null);
        } else {
            inventory.setItem(slot, result.stack());
        }
        return result.taken();
    }

    private int takeFromHeld(ItemStack stack, java.util.function.Consumer<ItemStack> setter, int maxTake) {
        TakeResult result = takeFromStack(stack, maxTake);
        if (result.taken() <= 0) {
            return 0;
        }
        if (result.stack() == null || result.stack().getAmount() <= 0) {
            setter.accept(null);
        } else {
            setter.accept(result.stack());
        }
        return result.taken();
    }

    private TakeResult takeFromStack(ItemStack stack, int maxTake) {
        if (!isVanillaTnt(stack) || maxTake <= 0) {
            return new TakeResult(stack, 0);
        }
        int take = Math.min(maxTake, stack.getAmount());
        stack.setAmount(stack.getAmount() - take);
        if (stack.getAmount() <= 0) {
            return new TakeResult(null, take);
        }
        return new TakeResult(stack, take);
    }

    private int stackAmount(ItemStack stack) {
        return isVanillaTnt(stack) ? stack.getAmount() : 0;
    }

    private boolean isVanillaTnt(ItemStack stack) {
        if (stack == null || stack.getType() != Material.TNT) {
            return false;
        }
        return itemFactory.readDynamiteId(stack) == null;
    }

    private record TakeResult(ItemStack stack, int taken) {
    }

}
