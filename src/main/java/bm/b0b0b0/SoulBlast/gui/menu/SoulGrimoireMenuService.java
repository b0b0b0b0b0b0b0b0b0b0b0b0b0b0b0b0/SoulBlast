package bm.b0b0b0.SoulBlast.gui.menu;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.config.menu.MenuFileConfig;
import bm.b0b0b0.SoulBlast.config.menu.MenuIconDefinition;
import bm.b0b0b0.SoulBlast.gui.holder.SoulGrimoireHolder;
import bm.b0b0b0.SoulBlast.model.PlayerProfile;
import bm.b0b0b0.SoulBlast.repository.DynamiteRegistry;
import bm.b0b0b0.SoulBlast.service.PlayerProfileService;
import bm.b0b0b0.SoulBlast.util.TextParser;
import bm.b0b0b0.SoulBlast.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class SoulGrimoireMenuService {

    private final JavaPlugin plugin;
    private final DynamiteRegistry registry;
    private final MenuIconFactory iconFactory;
    private final PlayerProfileService profileService;
    private final MenuDynamiteSorter sorter;
    private MenuFileConfig menuConfig = new MenuFileConfig();
    private MenuLayout layout = new MenuLayout(menuConfig);

    public SoulGrimoireMenuService(
            JavaPlugin plugin,
            DynamiteRegistry registry,
            MenuIconFactory iconFactory,
            PlayerProfileService profileService
    ) {
        this.plugin = plugin;
        this.registry = registry;
        this.iconFactory = iconFactory;
        this.profileService = profileService;
        this.sorter = new MenuDynamiteSorter();
    }

    public void reload(MenuFileConfig config) {
        menuConfig = config;
        layout = new MenuLayout(menuConfig);
    }

    public void open(Player player) {
        open(player, 0, MenuSortingType.fromConfig(menuConfig.options.defaultSortingType));
    }

    public void open(Player player, int page, MenuSortingType sorting) {
        profileService.preload(player).thenAccept(profile ->
                Bukkit.getScheduler().runTask(plugin, () -> openNow(player, page, sorting, profile))
        );
    }

    private void openNow(Player player, int page, MenuSortingType sorting, PlayerProfile profile) {
        SoulGrimoireHolder holder = createHolder(player, page, sorting);
        fill(holder, profile);
        player.openInventory(holder.getInventory());
    }

    public void refresh(Player player, SoulGrimoireHolder holder) {
        fill(holder, profileService.profile(player));
    }

    public void navigate(SoulGrimoireHolder holder, Player player, int page, MenuSortingType sorting) {
        holder.setPage(page);
        holder.setSorting(sorting);
        refresh(player, holder);
    }

    private SoulGrimoireHolder createHolder(Player player, int page, MenuSortingType sorting) {
        var title = TextParser.parse(TextUtil.apply(menuConfig.options.title, Map.of()));
        return new SoulGrimoireHolder(layout.size(), title, player.getUniqueId(), page, sorting);
    }

    private void fill(SoulGrimoireHolder holder, PlayerProfile profile) {
        Inventory inventory = holder.getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, null);
        }
        List<DynamiteDefinition> visible = visibleDynamites(holder.sorting());
        int perPage = Math.max(1, layout.dynamiteSlotsPerPage());
        int maxPages = Math.max(1, (int) Math.ceil(visible.size() / (double) perPage));
        int page = Math.min(holder.page(), maxPages - 1);
        holder.setPage(page);
        Map<String, String> navigation = Map.of(
                "current_page", String.valueOf(page + 1),
                "max_pages", String.valueOf(maxPages),
                "sort_label", holder.sorting().label()
        );
        DynamiteDefinition goalDefinition = resolveGoalDefinition(profile);
        int dynamiteStart = page * perPage;
        int dynamiteIndex = 0;
        for (Map.Entry<Integer, String> entry : layout.symbolBySlot().entrySet()) {
            int slot = entry.getKey();
            String symbol = entry.getValue();
            MenuIconDefinition icon = menuConfig.icons.get(symbol);
            if (icon == null) {
                continue;
            }
            MenuIconType type = MenuIconType.fromConfig(icon.type);
            if (type == MenuIconType.DYNAMITE_ENTRY) {
                int listIndex = dynamiteStart + dynamiteIndex;
                if (listIndex >= visible.size()) {
                    continue;
                }
                inventory.setItem(
                        slot,
                        iconFactory.buildDynamiteEntry(icon, visible.get(listIndex), profile, navigation)
                );
                dynamiteIndex++;
                continue;
            }
            ItemStack item = resolveSlotItem(type, icon, navigation, page, maxPages, profile, goalDefinition);
            if (item != null) {
                inventory.setItem(slot, item);
            }
        }
    }

    private DynamiteDefinition resolveGoalDefinition(PlayerProfile profile) {
        String goalId = profile.goalDynamiteId();
        if (goalId == null || goalId.isBlank()) {
            return null;
        }
        return registry.find(goalId).orElse(null);
    }

    private ItemStack resolveSlotItem(
            MenuIconType type,
            MenuIconDefinition icon,
            Map<String, String> navigation,
            int page,
            int maxPages,
            PlayerProfile profile,
            DynamiteDefinition goalDefinition
    ) {
        return switch (type) {
            case GOAL_SLOT -> iconFactory.buildGoalSlot(icon, goalDefinition, profile, navigation);
            case PLAYER_SETTINGS -> iconFactory.buildPlayerSettings(icon, profile, navigation);
            case PREVIOUS_PAGE -> {
                if (page <= 0) {
                    yield emptyIfRemoved();
                }
                yield iconFactory.build(icon, navigation);
            }
            case NEXT_PAGE -> {
                if (page >= maxPages - 1) {
                    yield emptyIfRemoved();
                }
                yield iconFactory.build(icon, navigation);
            }
            case SORT_CYCLE, INFO_PANEL, FILLER -> iconFactory.build(icon, navigation);
            default -> null;
        };
    }

    private ItemStack emptyIfRemoved() {
        if (!menuConfig.options.removeDirectionIconIfNoneExists) {
            MenuIconDefinition filler = menuConfig.icons.get(" ");
            if (filler != null) {
                return iconFactory.build(filler, Map.of());
            }
        }
        return null;
    }

    private List<DynamiteDefinition> visibleDynamites(MenuSortingType sorting) {
        Set<String> excluded = new HashSet<>();
        for (String id : menuConfig.options.exclude) {
            excluded.add(id.toLowerCase(Locale.ROOT));
        }
        List<DynamiteDefinition> list = new ArrayList<>();
        for (DynamiteDefinition definition : registry.all()) {
            if (!excluded.contains(definition.id.toLowerCase(Locale.ROOT))) {
                list.add(definition);
            }
        }
        return sorter.sort(list, sorting);
    }

    public MenuFileConfig menuConfig() {
        return menuConfig;
    }

}
