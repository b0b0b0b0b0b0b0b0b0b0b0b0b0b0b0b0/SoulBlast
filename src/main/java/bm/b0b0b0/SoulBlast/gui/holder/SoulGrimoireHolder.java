package bm.b0b0b0.SoulBlast.gui.holder;

import bm.b0b0b0.SoulBlast.gui.menu.MenuSortingType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public final class SoulGrimoireHolder implements InventoryHolder {

    private final Inventory inventory;
    private final UUID viewerId;
    private int page;
    private MenuSortingType sorting;

    public SoulGrimoireHolder(int size, Component title, UUID viewerId, int page, MenuSortingType sorting) {
        this.viewerId = viewerId;
        this.page = page;
        this.sorting = sorting;
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public UUID viewerId() {
        return viewerId;
    }

    public int page() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public MenuSortingType sorting() {
        return sorting;
    }

    public void setSorting(MenuSortingType sorting) {
        this.sorting = sorting;
    }

}
