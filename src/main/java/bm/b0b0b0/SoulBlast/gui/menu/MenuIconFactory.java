package bm.b0b0b0.SoulBlast.gui.menu;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.config.menu.MenuIconDefinition;
import bm.b0b0b0.SoulBlast.config.menu.MenuIconDisplaySettings;
import bm.b0b0b0.SoulBlast.model.PlayerProfile;
import bm.b0b0b0.SoulBlast.service.DynamitePurchaseService;
import bm.b0b0b0.SoulBlast.util.BukkitKeys;
import bm.b0b0b0.SoulBlast.util.ItemMetaUtil;
import bm.b0b0b0.SoulBlast.util.PluginKeys;
import bm.b0b0b0.SoulBlast.util.TextUtil;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

public final class MenuIconFactory {

    private final PluginKeys keys;
    private DynamitePurchaseService purchaseService;

    public MenuIconFactory(PluginKeys keys) {
        this.keys = keys;
    }

    public void setPurchaseService(DynamitePurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    public ItemStack build(
            MenuIconDefinition iconDefinition,
            Map<String, String> placeholders
    ) {
        MenuIconDisplaySettings display = iconDefinition.display;
        Material material = BukkitKeys.material(display.material);
        if (material == null) {
            material = Material.STONE;
        }
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }
        TextUtil.setDisplayName(meta, TextUtil.apply(display.name, placeholders));
        List<String> lore = display.lore.stream()
                .map(line -> TextUtil.apply(line, placeholders))
                .collect(Collectors.toList());
        TextUtil.setLore(meta, lore);
        if (display.customModelData > 0) {
            ItemMetaUtil.applyCustomModelData(meta, display.customModelData);
        }
        MenuIconType type = MenuIconType.fromConfig(iconDefinition.type);
        meta.getPersistentDataContainer().set(keys.menuIconType, PersistentDataType.STRING, type.name());
        applySkullTexture(meta, display.texture);
        stack.setItemMeta(meta);
        return stack;
    }

    public ItemStack buildDynamiteEntry(
            MenuIconDefinition template,
            DynamiteDefinition definition,
            PlayerProfile profile,
            Map<String, String> navigationPlaceholders
    ) {
        Map<String, String> placeholders = new HashMap<>(navigationPlaceholders);
        placeholders.put("soul_name", TextUtil.apply(definition.item.displayName, Map.of()));
        List<String> lore = definition.item.lore;
        placeholders.put("soul_desc_1", lore.isEmpty() ? "" : TextUtil.apply(lore.get(0), Map.of()));
        placeholders.put("soul_desc_2", lore.size() > 1 ? TextUtil.apply(lore.get(1), Map.of()) : "");
        placeholders.put("soul_desc_gap", "");
        placeholders.put("blast_power", String.format("%.1f", definition.explosion.power));
        placeholders.put("fuse_seconds", String.format("%.1f", definition.fuseTicks / 20.0));
        List<String> mergedLore = template.display.lore.stream()
                .map(line -> TextUtil.apply(line, placeholders))
                .collect(Collectors.toCollection(ArrayList::new));
        if (purchaseService != null) {
            mergedLore.addAll(purchaseService.purchaseLoreLines(definition, profile));
        }
        ItemStack stack = build(template, placeholders);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }
        TextUtil.setLore(meta, mergedLore);
        meta.getPersistentDataContainer().set(keys.menuDynamiteId, PersistentDataType.STRING, definition.id);
        stack.setItemMeta(meta);
        return stack;
    }

    public ItemStack buildGoalSlot(
            MenuIconDefinition template,
            DynamiteDefinition definition,
            PlayerProfile profile,
            Map<String, String> navigationPlaceholders
    ) {
        if (definition == null) {
            return build(template, navigationPlaceholders);
        }
        Map<String, String> placeholders = new HashMap<>(navigationPlaceholders);
        placeholders.put("soul_name", TextUtil.apply(definition.item.displayName, Map.of()));
        Material material = BukkitKeys.material(definition.item.material);
        if (material == null) {
            material = Material.TNT;
        }
        MenuIconDefinition visualTemplate = new MenuIconDefinition();
        visualTemplate.type = MenuIconType.GOAL_SLOT.name();
        visualTemplate.display.material = BukkitKeys.materialConfigKey(material);
        visualTemplate.display.name = "%soul_name%";
        visualTemplate.display.lore = List.of();
        ItemStack stack = build(visualTemplate, placeholders);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }
        List<String> lore = new ArrayList<>();
        if (purchaseService != null) {
            lore.addAll(purchaseService.goalLoreLines(definition, profile));
        }
        TextUtil.setLore(meta, lore);
        meta.getPersistentDataContainer().set(keys.menuIconType, PersistentDataType.STRING, MenuIconType.GOAL_SLOT.name());
        meta.getPersistentDataContainer().set(keys.menuDynamiteId, PersistentDataType.STRING, definition.id);
        stack.setItemMeta(meta);
        return stack;
    }

    public ItemStack buildPlayerSettings(MenuIconDefinition template, PlayerProfile profile, Map<String, String> navigationPlaceholders) {
        Map<String, String> placeholders = new HashMap<>(navigationPlaceholders);
        placeholders.put("auto_ignite_label", profile.autoIgnite() ? "&#86EFACвзрыв сразу" : "&#AAAAAAтолько от огнива");
        return build(template, placeholders);
    }

    private void applySkullTexture(ItemMeta meta, String texture) {
        if (!(meta instanceof SkullMeta skullMeta) || texture == null || texture.isBlank()) {
            return;
        }
        com.destroystokyo.paper.profile.PlayerProfile skullProfile = Bukkit.createProfile(UUID.randomUUID());
        skullProfile.setProperty(new ProfileProperty("textures", texture));
        skullMeta.setPlayerProfile(skullProfile);
    }

}
