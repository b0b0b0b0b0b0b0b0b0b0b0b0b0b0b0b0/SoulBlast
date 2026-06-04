package bm.b0b0b0.SoulBlast.util;

import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;

public final class ItemMetaUtil {

    private ItemMetaUtil() {
    }

    public static void applyCustomModelData(ItemMeta meta, int value) {
        if (value <= 0) {
            return;
        }
        CustomModelDataComponent component = meta.getCustomModelDataComponent();
        component.setFloats(List.of((float) value));
        meta.setCustomModelDataComponent(component);
    }

}
