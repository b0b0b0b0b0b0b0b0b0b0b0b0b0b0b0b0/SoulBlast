package bm.b0b0b0.SoulBlast.decay.gui.holder;

import bm.b0b0b0.SoulBlast.decay.gui.DecayBlocksSortMode;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class DecayBlocksHolder implements InventoryHolder {

    private final int page;
    private final DecayBlocksSortMode sortMode;
    private Inventory inventory;

    public DecayBlocksHolder(int page, DecayBlocksSortMode sortMode) {
        this.page = page;
        this.sortMode = sortMode;
    }

    public int page() {
        return page;
    }

    public DecayBlocksSortMode sortMode() {
        return sortMode;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void bind(Inventory inventory) {
        this.inventory = inventory;
    }

}
