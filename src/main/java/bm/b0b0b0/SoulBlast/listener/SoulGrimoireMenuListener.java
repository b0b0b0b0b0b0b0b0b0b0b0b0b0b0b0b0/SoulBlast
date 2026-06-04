package bm.b0b0b0.SoulBlast.listener;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.gui.MenuItemGuard;
import bm.b0b0b0.SoulBlast.gui.holder.SoulGrimoireHolder;
import bm.b0b0b0.SoulBlast.gui.menu.MenuIconType;
import bm.b0b0b0.SoulBlast.gui.menu.MenuSortingType;
import bm.b0b0b0.SoulBlast.gui.menu.SoulGrimoireMenuService;
import bm.b0b0b0.SoulBlast.message.MessageService;
import bm.b0b0b0.SoulBlast.model.PlayerProfile;
import bm.b0b0b0.SoulBlast.repository.DynamiteRegistry;
import bm.b0b0b0.SoulBlast.service.DynamiteCooldownMessenger;
import bm.b0b0b0.SoulBlast.service.DynamiteCooldownService;
import bm.b0b0b0.SoulBlast.service.DynamiteItemFactory;
import bm.b0b0b0.SoulBlast.service.DynamitePurchaseService;
import bm.b0b0b0.SoulBlast.service.DynamitePurchaseService.PurchasePreview;
import bm.b0b0b0.SoulBlast.service.PlayerProfileService;
import bm.b0b0b0.SoulBlast.service.VanillaTntInventoryService;
import bm.b0b0b0.SoulBlast.util.PluginKeys;
import bm.b0b0b0.SoulBlast.util.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.Optional;

public final class SoulGrimoireMenuListener implements Listener {

    private final PluginKeys keys;
    private final SoulGrimoireMenuService menuService;
    private final DynamiteRegistry registry;
    private final DynamiteItemFactory itemFactory;
    private final PlayerProfileService profileService;
    private final DynamitePurchaseService purchaseService;
    private final VanillaTntInventoryService vanillaTntService;
    private final MessageService messageService;
    private final MenuItemGuard menuItemGuard;
    private final DynamiteCooldownService cooldownService;
    private final DynamiteCooldownMessenger cooldownMessages;

    public SoulGrimoireMenuListener(
            PluginKeys keys,
            SoulGrimoireMenuService menuService,
            DynamiteRegistry registry,
            DynamiteItemFactory itemFactory,
            PlayerProfileService profileService,
            DynamitePurchaseService purchaseService,
            VanillaTntInventoryService vanillaTntService,
            MessageService messageService,
            MenuItemGuard menuItemGuard,
            DynamiteCooldownService cooldownService,
            DynamiteCooldownMessenger cooldownMessages
    ) {
        this.keys = keys;
        this.menuService = menuService;
        this.registry = registry;
        this.itemFactory = itemFactory;
        this.profileService = profileService;
        this.purchaseService = purchaseService;
        this.vanillaTntService = vanillaTntService;
        this.messageService = messageService;
        this.menuItemGuard = menuItemGuard;
        this.cooldownService = cooldownService;
        this.cooldownMessages = cooldownMessages;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder(false) instanceof SoulGrimoireHolder holder)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }
        if (!player.getUniqueId().equals(holder.viewerId())) {
            event.setCancelled(true);
            return;
        }
        if (blocksMenuInteraction(event, top)) {
            event.setCancelled(true);
            return;
        }
        Inventory clicked = event.getClickedInventory();
        if (clicked == null || clicked != top) {
            return;
        }
        event.setCancelled(true);
        handleTopClick(event, player, holder);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder(false) instanceof SoulGrimoireHolder holder)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }
        if (!player.getUniqueId().equals(holder.viewerId())) {
            event.setCancelled(true);
            return;
        }
        int topSize = top.getSize();
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < topSize) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder(false) instanceof SoulGrimoireHolder)) {
            return;
        }
        if (event.getPlayer() instanceof Player player) {
            menuItemGuard.stripFromPlayer(player);
        }
    }

    private boolean blocksMenuInteraction(InventoryClickEvent event, Inventory top) {
        ClickType click = event.getClick();
        InventoryAction action = event.getAction();
        Inventory clicked = event.getClickedInventory();
        if (click == ClickType.DOUBLE_CLICK) {
            return true;
        }
        if (click.isShiftClick()) {
            return true;
        }
        if (click == ClickType.NUMBER_KEY || click == ClickType.SWAP_OFFHAND) {
            return clicked == null || clicked == top;
        }
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            return true;
        }
        if (action == InventoryAction.COLLECT_TO_CURSOR) {
            return true;
        }
        if (clicked == null) {
            return true;
        }
        if (event.getRawSlot() < top.getSize() && clicked != top) {
            return true;
        }
        return false;
    }

    private void handleTopClick(InventoryClickEvent event, Player player, SoulGrimoireHolder holder) {
        ItemStack current = event.getCurrentItem();
        if (current == null || !current.hasItemMeta()) {
            return;
        }
        String typeRaw = current.getItemMeta().getPersistentDataContainer().get(keys.menuIconType, PersistentDataType.STRING);
        MenuIconType type = MenuIconType.fromConfig(typeRaw);
        boolean rightClick = event.getClick() == ClickType.RIGHT;
        boolean leftClick = event.getClick() == ClickType.LEFT;
        switch (type) {
            case DYNAMITE_ENTRY -> handleDynamiteEntry(player, holder, current, rightClick, leftClick);
            case GOAL_SLOT -> handleGoalSlot(player, holder, current, rightClick, leftClick);
            case PLAYER_SETTINGS -> toggleAutoIgnite(player, holder);
            case PREVIOUS_PAGE -> menuService.navigate(holder, player, Math.max(0, holder.page() - 1), holder.sorting());
            case NEXT_PAGE -> menuService.navigate(holder, player, holder.page() + 1, holder.sorting());
            case SORT_CYCLE -> menuService.navigate(holder, player, 0, holder.sorting().next());
            default -> {
            }
        }
    }

    private void handleDynamiteEntry(Player player, SoulGrimoireHolder holder, ItemStack menuItem, boolean rightClick, boolean leftClick) {
        String id = readDynamiteId(menuItem);
        if (id == null) {
            return;
        }
        Optional<DynamiteDefinition> definition = registry.find(id);
        if (definition.isEmpty()) {
            return;
        }
        if (rightClick) {
            selectGoal(player, holder, definition.get());
            return;
        }
        if (leftClick) {
            attemptCatalogPurchase(player, holder, definition.get());
        }
    }

    private void attemptCatalogPurchase(Player player, SoulGrimoireHolder holder, DynamiteDefinition definition) {
        PlayerProfile profile = profileService.profile(player);
        DynamitePurchaseService.CatalogEvaluation evaluation = purchaseService.evaluateCatalog(player, definition, profile);
        PurchasePreview preview = evaluation.preview();
        if (!preview.catalogPaymentRequired()) {
            messageService.send(player, "catalog-pay-not-required");
            return;
        }
        if (!evaluation.canPurchase()) {
            if (evaluation.firstFailure() == DynamitePurchaseService.PurchaseFailure.INVENTORY_FULL) {
                messageService.send(player, "purchase-failed-inventory");
                return;
            }
            if (evaluation.firstFailure() == DynamitePurchaseService.PurchaseFailure.INSUFFICIENT_EXPERIENCE) {
                messageService.send(player, "purchase-failed-experience");
                return;
            }
            if (evaluation.firstFailure() == DynamitePurchaseService.PurchaseFailure.INSUFFICIENT_MONEY) {
                messageService.send(player, "purchase-failed-money");
                return;
            }
            String missing = purchaseService.formatCatalogMissing(evaluation);
            if (!missing.isBlank()) {
                messageService.send(player, "purchase-requirements", Map.of("missing", missing));
            }
            return;
        }
        grantDynamite(player, holder, definition, () -> purchaseService.tryCatalogPurchase(player, definition, profile));
    }

    private void handleCopilkaLeftClick(Player player, SoulGrimoireHolder holder, DynamiteDefinition definition) {
        PlayerProfile profile = profileService.profile(player);
        DynamitePurchaseService.CopilkaEvaluation evaluation = purchaseService.evaluateCopilka(definition, profile, player);
        PurchasePreview preview = evaluation.preview();
        if (!preview.copilkaPaymentRequired()) {
            messageService.send(player, "tnt-deposit-not-required");
            return;
        }
        if (evaluation.canClaim() && preview.tntDepositComplete()) {
            grantDynamite(player, holder, definition, () -> purchaseService.tryCopilkaClaim(player, definition, profile));
            return;
        }
        if (evaluation.tntBlocking() && vanillaTntService.countVanillaTnt(player) > 0) {
            depositTnt(player, holder, definition);
            return;
        }
        if (evaluation.firstFailure() == DynamitePurchaseService.PurchaseFailure.INVENTORY_FULL) {
            messageService.send(player, "purchase-failed-inventory");
            return;
        }
        messageService.send(player, "purchase-failed-tnt", Map.of(
                "current", String.valueOf(preview.vanillaTntDeposited()),
                "required", String.valueOf(preview.vanillaTntRequired())
        ));
    }

    private void grantDynamite(
            Player player,
            SoulGrimoireHolder holder,
            DynamiteDefinition definition,
            java.util.function.Supplier<DynamitePurchaseService.PurchaseFailure> charge
    ) {
        if (cooldownMessages.blockPurchase(player, definition)) {
            return;
        }
        DynamitePurchaseService.PurchaseFailure failure = charge.get();
        if (failure != DynamitePurchaseService.PurchaseFailure.NONE) {
            sendFailure(player, failure);
            return;
        }
        profileService.persist(profileService.profile(player));
        player.getInventory().addItem(itemFactory.create(definition, 1));
        cooldownService.record(player, definition, DynamiteCooldownService.CooldownKind.PURCHASE);
        messageService.send(player, "purchase-success", Map.of(
                "display", TextUtil.stripColor(TextUtil.apply(definition.item.displayName, Map.of()))
        ));
        menuService.refresh(player, holder);
    }

    private void handleGoalSlot(Player player, SoulGrimoireHolder holder, ItemStack menuItem, boolean rightClick, boolean leftClick) {
        if (rightClick) {
            if (readDynamiteId(menuItem) != null) {
                clearGoal(player, holder);
            }
            return;
        }
        if (!leftClick) {
            return;
        }
        String id = readDynamiteId(menuItem);
        if (id == null) {
            messageService.send(player, "goal-not-selected");
            return;
        }
        registry.find(id).ifPresent(definition -> handleCopilkaLeftClick(player, holder, definition));
    }

    private void clearGoal(Player player, SoulGrimoireHolder holder) {
        PlayerProfile profile = profileService.profile(player);
        if (profile.goalDynamiteId() == null || profile.goalDynamiteId().isBlank()) {
            return;
        }
        profile.setGoalDynamiteId(null);
        profileService.persist(profile);
        messageService.send(player, "goal-cleared");
        menuService.refresh(player, holder);
    }

    private void selectGoal(Player player, SoulGrimoireHolder holder, DynamiteDefinition definition) {
        PlayerProfile profile = profileService.profile(player);
        profile.setGoalDynamiteId(definition.id);
        profileService.persist(profile);
        messageService.send(player, "goal-selected", Map.of(
                "display", TextUtil.stripColor(TextUtil.apply(definition.item.displayName, Map.of()))
        ));
        menuService.refresh(player, holder);
    }

    private void depositTnt(Player player, SoulGrimoireHolder holder, DynamiteDefinition definition) {
        PlayerProfile profile = profileService.profile(player);
        DynamitePurchaseService.PurchasePreview preview = purchaseService.preview(definition, profile);
        if (!preview.vanillaTntRequiredFlag()) {
            messageService.send(player, "tnt-deposit-not-required");
            return;
        }
        int remaining = preview.vanillaTntRequired() - preview.vanillaTntDeposited();
        if (remaining <= 0) {
            messageService.send(player, "tnt-deposit-complete");
            return;
        }
        int available = vanillaTntService.countVanillaTnt(player);
        if (available <= 0) {
            messageService.send(player, "tnt-deposit-none");
            return;
        }
        int toTake = Math.min(remaining, available);
        int taken = vanillaTntService.removeVanillaTnt(player, toTake);
        if (taken <= 0) {
            messageService.send(player, "tnt-deposit-none");
            return;
        }
        profile.addVanillaTntDeposit(definition.id, taken);
        profileService.persist(profile);
        int current = profile.depositedVanillaTnt(definition.id);
        int required = preview.vanillaTntRequired();
        messageService.send(player, "tnt-deposited", Map.of(
                "amount", String.valueOf(taken),
                "current", String.valueOf(current),
                "required", String.valueOf(required)
        ));
        if (current >= required) {
            messageService.send(player, "copilka-tnt-full", Map.of(
                    "current", String.valueOf(current),
                    "required", String.valueOf(required)
            ));
        }
        menuService.refresh(player, holder);
    }

    private void toggleAutoIgnite(Player player, SoulGrimoireHolder holder) {
        PlayerProfile profile = profileService.profile(player);
        profile.setAutoIgnite(!profile.autoIgnite());
        profileService.persist(profile);
        messageService.send(player, profile.autoIgnite() ? "auto-ignite-on" : "auto-ignite-off");
        menuService.refresh(player, holder);
    }

    private void sendFailure(Player player, DynamitePurchaseService.PurchaseFailure failure) {
        String key = switch (failure) {
            case INSUFFICIENT_MONEY -> "purchase-failed-money";
            case INSUFFICIENT_EXPERIENCE -> "purchase-failed-experience";
            case INSUFFICIENT_TNT_DEPOSIT -> "purchase-failed-tnt";
            case INVENTORY_FULL -> "purchase-failed-inventory";
            default -> "purchase-failed";
        };
        messageService.send(player, key);
    }

    private String readDynamiteId(ItemStack menuItem) {
        return menuItem.getItemMeta().getPersistentDataContainer().get(keys.menuDynamiteId, PersistentDataType.STRING);
    }

}
