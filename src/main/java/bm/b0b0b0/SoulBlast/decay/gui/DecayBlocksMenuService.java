package bm.b0b0b0.SoulBlast.decay.gui;

import bm.b0b0b0.SoulBlast.decay.config.DecayBlockTypeDefinition;
import bm.b0b0b0.SoulBlast.decay.config.DecayMessagesFileConfig;
import bm.b0b0b0.SoulBlast.decay.gui.holder.DecayBlocksHolder;
import bm.b0b0b0.SoulBlast.decay.service.DecayBlockRegistry;
import bm.b0b0b0.SoulBlast.util.TextParser;
import bm.b0b0b0.SoulBlast.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class DecayBlocksMenuService {

    private static final int PAGE_SIZE = 28;

    private final JavaPlugin plugin;
    private final DecayMessagesFileConfig messages;
    private final DecayBlockRegistry registry;

    public DecayBlocksMenuService(JavaPlugin plugin, DecayMessagesFileConfig messages, DecayBlockRegistry registry) {
        this.plugin = plugin;
        this.messages = messages;
        this.registry = registry;
    }

    public void open(Player player, int page, DecayBlocksSortMode sortMode) {
        List<DecayBlockRegistry.DecayBlockEntry> entries = sortedEntries(sortMode);
        int maxPage = Math.max(0, (entries.size() - 1) / PAGE_SIZE);
        int safePage = Math.clamp(page, 0, maxPage);
        DecayBlocksHolder holder = new DecayBlocksHolder(safePage, sortMode);
        Inventory inventory = Bukkit.createInventory(
                holder,
                45,
                TextParser.parse(TextUtil.colorize(messages.menuTitle))
        );
        holder.bind(inventory);
        fillContent(inventory, entries, safePage);
        fillNavigation(inventory, safePage, maxPage, sortMode);
        player.openInventory(inventory);
    }

    private List<DecayBlockRegistry.DecayBlockEntry> sortedEntries(DecayBlocksSortMode sortMode) {
        List<DecayBlockRegistry.DecayBlockEntry> entries = new ArrayList<>(registry.menuEntries());
        Comparator<DecayBlockRegistry.DecayBlockEntry> comparator;
        if (sortMode == DecayBlocksSortMode.LOWEST_RESISTANCE) {
            comparator = Comparator.comparing(entry -> entry.type().resistance);
        } else if (sortMode == DecayBlocksSortMode.NAME) {
            comparator = Comparator.comparing(entry -> entry.material().name());
        } else {
            comparator = Comparator.comparing((DecayBlockRegistry.DecayBlockEntry entry) -> entry.type().resistance)
                    .reversed();
        }
        entries.sort(comparator);
        return entries;
    }

    private void fillContent(Inventory inventory, List<DecayBlockRegistry.DecayBlockEntry> entries, int page) {
        int start = page * PAGE_SIZE;
        int slot = 10;
        for (int index = start; index < Math.min(start + PAGE_SIZE, entries.size()); index++) {
            if (slot == 17 || slot == 26) {
                slot += 2;
            }
            if (slot >= 35) {
                break;
            }
            inventory.setItem(slot++, createEntryIcon(entries.get(index)));
        }
    }

    private void fillNavigation(Inventory inventory, int page, int maxPage, DecayBlocksSortMode sortMode) {
        if (page > 0) {
            inventory.setItem(39, named(Material.ARROW, "&cПредыдущая", List.of(
                    "",
                    " &7Страница &f" + (page + 1) + "&8/&2" + (maxPage + 1),
                    ""
            )));
        }
        if (page < maxPage) {
            inventory.setItem(41, named(Material.ARROW, "&aСледующая", List.of(
                    "",
                    " &7Страница &f" + (page + 2) + "&8/&2" + (maxPage + 1),
                    ""
            )));
        }
        inventory.setItem(40, named(Material.PLAYER_HEAD, messages.menuInfoName, List.of(
                "",
                " &fТипы из &edecay/blocks.yml ",
                " &fПохожие блоки (кирпич, бетон…) ",
                " &fтоже ломаются постепенно. ",
                ""
        )));
        inventory.setItem(42, named(Material.HOPPER, messages.menuSortName, List.of(
                "",
                " &fСортировка&8: &b" + sortMode.displayName(),
                "",
                " &7Нажмите для смены. ",
                ""
        )));
    }

    private ItemStack createEntryIcon(DecayBlockRegistry.DecayBlockEntry entry) {
        DecayBlockTypeDefinition type = entry.type();
        Map<String, String> vars = type.variables;
        String display = vars.getOrDefault("display_name", entry.material().name());
        String resistance = vars.getOrDefault("resistance_name", String.valueOf(type.resistance));
        String regeneration = vars.getOrDefault("regeneration_name", type.regeneration.every);
        Material iconMaterial = menuIconMaterial(entry.material());
        return named(iconMaterial, display, List.of(
                "",
                " &7ID: &f" + entry.configKey(),
                " &fПрочность&8: " + resistance + " ",
                " &fРегенерация&8: " + regeneration + " ",
                ""
        ));
    }

    private Material menuIconMaterial(Material material) {
        if (material != null && material.isItem()) {
            return material;
        }
        return Material.BRICKS;
    }

    private ItemStack named(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material, 1);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }
        meta.displayName(TextParser.parse(TextUtil.colorize(name)));
        meta.lore(lore.stream().map(line -> TextParser.parse(TextUtil.colorize(line))).toList());
        stack.setItemMeta(meta);
        return stack;
    }

}
