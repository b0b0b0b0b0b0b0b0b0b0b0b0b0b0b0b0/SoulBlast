package bm.b0b0b0.SoulBlast.gui.menu;

import bm.b0b0b0.SoulBlast.config.menu.MenuFileConfig;
import bm.b0b0b0.SoulBlast.config.menu.MenuIconDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MenuLayout {

    private final int size;
    private final Map<Integer, String> symbolBySlot;
    private final List<Integer> dynamiteSlots;

    public MenuLayout(MenuFileConfig config) {
        List<String> rows = config.options.layout;
        int rowCount = Math.clamp(rows.size(), 1, 6);
        this.size = rowCount * 9;
        this.symbolBySlot = new LinkedHashMap<>();
        this.dynamiteSlots = new ArrayList<>();
        for (int row = 0; row < rowCount; row++) {
            String line = row < rows.size() ? rows.get(row) : "         ";
            if (line.length() < 9) {
                line = (line + "         ").substring(0, 9);
            } else if (line.length() > 9) {
                line = line.substring(0, 9);
            }
            for (int column = 0; column < 9; column++) {
                char symbol = line.charAt(column);
                int slot = row * 9 + column;
                String key = String.valueOf(symbol);
                symbolBySlot.put(slot, key);
                MenuIconDefinition icon = config.icons.get(key);
                if (icon != null && MenuIconType.DYNAMITE_ENTRY == MenuIconType.fromConfig(icon.type)) {
                    dynamiteSlots.add(slot);
                }
            }
        }
    }

    public int size() {
        return size;
    }

    public Map<Integer, String> symbolBySlot() {
        return symbolBySlot;
    }

    public List<Integer> dynamiteSlots() {
        return dynamiteSlots;
    }

    public int dynamiteSlotsPerPage() {
        return dynamiteSlots.size();
    }

}
