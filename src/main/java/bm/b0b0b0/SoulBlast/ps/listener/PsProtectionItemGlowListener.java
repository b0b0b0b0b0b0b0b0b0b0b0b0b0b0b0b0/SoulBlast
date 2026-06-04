package bm.b0b0b0.SoulBlast.ps.listener;

import bm.b0b0b0.SoulBlast.ps.config.PsProtectionTypeDefinition;
import bm.b0b0b0.SoulBlast.ps.integration.ProtectionStonesBridge;
import bm.b0b0b0.SoulBlast.ps.service.PsItemGlowApplicator;
import bm.b0b0b0.SoulBlast.ps.service.PsTypeRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.Optional;

public final class PsProtectionItemGlowListener implements Listener {

    private final JavaPlugin plugin;
    private final ProtectionStonesBridge bridge;
    private final PsTypeRegistry types;

    public PsProtectionItemGlowListener(
            JavaPlugin plugin,
            ProtectionStonesBridge bridge,
            PsTypeRegistry types
    ) {
        this.plugin = plugin;
        this.bridge = bridge;
        this.types = types;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        scheduleInventoryGlow(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        applyToStack(event.getItem().getItemStack());
        scheduleInventoryGlow(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        applyToStack(event.getCurrentItem());
        applyToStack(event.getCursor());
        scheduleInventoryGlow(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase(Locale.ROOT).trim();
        if (!message.startsWith("/ps get") && !message.startsWith("/ps give")) {
            return;
        }
        scheduleInventoryGlow(event.getPlayer());
    }

    private void scheduleInventoryGlow(Player player) {
        if (player == null) {
            return;
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> glowInventory(player), 2L);
    }

    private void glowInventory(Player player) {
        if (!player.isOnline()) {
            return;
        }
        ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack stack : contents) {
            applyToStack(stack);
        }
        applyToStack(player.getInventory().getItemInOffHand());
    }

    private void applyToStack(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return;
        }
        Optional<String> alias = resolveAlias(stack.getType());
        if (alias.isEmpty()) {
            return;
        }
        Optional<PsProtectionTypeDefinition> type = types.findAlias(alias.get());
        if (type.isEmpty() || !type.get().itemGlow.enabled) {
            return;
        }
        PsItemGlowApplicator.apply(stack);
    }

    private Optional<String> resolveAlias(Material material) {
        return bridge.aliasForMaterial(material);
    }

}
