package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.SoulBlast;
import bm.b0b0b0.SoulBlast.config.CraftingSettings;
import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.repository.DynamiteRegistry;
import bm.b0b0b0.SoulBlast.util.BukkitKeys;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;

import java.util.HashMap;
import java.util.Map;

public final class CraftingRegistrar {

    private final SoulBlast plugin;
    private final DynamiteRegistry registry;
    private final DynamiteItemFactory itemFactory;

    public CraftingRegistrar(SoulBlast plugin, DynamiteRegistry registry, DynamiteItemFactory itemFactory) {
        this.plugin = plugin;
        this.registry = registry;
        this.itemFactory = itemFactory;
    }

    public void registerAll() {
        for (DynamiteDefinition definition : registry.all()) {
            register(definition);
        }
    }

    public void unregisterAll() {
        for (DynamiteDefinition definition : registry.all()) {
            if (definition.crafting.enabled) {
                NamespacedKey key = new NamespacedKey(plugin, definition.crafting.recipeKey);
                plugin.getServer().removeRecipe(key);
            }
        }
    }

    private void register(DynamiteDefinition definition) {
        CraftingSettings crafting = definition.crafting;
        if (!crafting.enabled || crafting.shape.isEmpty()) {
            return;
        }
        NamespacedKey key = new NamespacedKey(plugin, crafting.recipeKey);
        ShapedRecipe recipe = new ShapedRecipe(key, itemFactory.create(definition, crafting.resultAmount));
        String[] shape = crafting.shape.stream().limit(3).map(String::toUpperCase).toArray(String[]::new);
        recipe.shape(shape);
        Map<Character, Material> ingredients = parseIngredients(crafting.ingredients);
        for (Map.Entry<Character, Material> entry : ingredients.entrySet()) {
            recipe.setIngredient(entry.getKey(), entry.getValue());
        }
        plugin.getServer().addRecipe(recipe);
    }

    private Map<Character, Material> parseIngredients(java.util.List<String> lines) {
        Map<Character, Material> map = new HashMap<>();
        for (String line : lines) {
            int index = line.indexOf('=');
            if (index <= 0) {
                continue;
            }
            char symbol = line.charAt(0);
            Material material = BukkitKeys.material(line.substring(index + 1).trim());
            if (material != null) {
                map.put(symbol, material);
            }
        }
        return map;
    }

}
