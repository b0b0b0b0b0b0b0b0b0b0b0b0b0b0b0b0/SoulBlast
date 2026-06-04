package bm.b0b0b0.SoulBlast.ps.service;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class PsItemGlowApplicator {

    private PsItemGlowApplicator() {
    }

    public static void apply(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }
        if (meta.hasEnchant(Enchantment.UNBREAKING)) {
            return;
        }
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        stack.setItemMeta(meta);
    }

}
