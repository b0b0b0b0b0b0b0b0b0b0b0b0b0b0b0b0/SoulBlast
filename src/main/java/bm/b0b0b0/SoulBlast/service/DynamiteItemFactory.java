package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.util.BukkitKeys;
import bm.b0b0b0.SoulBlast.util.ItemMetaUtil;
import bm.b0b0b0.SoulBlast.util.PluginKeys;
import bm.b0b0b0.SoulBlast.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public final class DynamiteItemFactory {

    private final PluginKeys keys;

    public DynamiteItemFactory(PluginKeys keys) {
        this.keys = keys;
    }

    public ItemStack create(DynamiteDefinition definition, int amount) {
        Material material = BukkitKeys.material(definition.item.material);
        if (material == null) {
            material = Material.TNT;
        }
        ItemStack stack = new ItemStack(material, Math.max(1, amount));
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }
        Map<String, String> placeholders = Map.of("dyn_id", definition.id);
        TextUtil.setDisplayName(meta, TextUtil.apply(definition.item.displayName, placeholders));
        TextUtil.setLore(meta, TextUtil.colorizeLore(definition.item.lore, placeholders));
        if (definition.item.customModelData > 0) {
            ItemMetaUtil.applyCustomModelData(meta, definition.item.customModelData);
        }
        if (definition.item.glow) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.getPersistentDataContainer().set(keys.dynamiteId, PersistentDataType.STRING, definition.id);
        stack.setItemMeta(meta);
        return stack;
    }

    public String readDynamiteId(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return null;
        }
        return stack.getItemMeta().getPersistentDataContainer().get(keys.dynamiteId, PersistentDataType.STRING);
    }

}
