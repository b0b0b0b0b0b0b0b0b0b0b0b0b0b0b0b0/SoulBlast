package bm.b0b0b0.SoulBlast.ps.listener;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.repository.DynamiteRegistry;
import bm.b0b0b0.SoulBlast.service.DynamiteItemFactory;
import bm.b0b0b0.SoulBlast.service.PlacedDynamiteTracker;
import bm.b0b0b0.SoulBlast.service.PrimedDynamiteService;
import bm.b0b0b0.SoulBlast.util.PluginKeys;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

public final class PsSoulblastDynamiteWorldGuardListener implements Listener {

    private final DynamiteRegistry registry;
    private final DynamiteItemFactory itemFactory;
    private final PlacedDynamiteTracker placedTracker;
    private final PluginKeys keys;

    public PsSoulblastDynamiteWorldGuardListener(
            DynamiteRegistry registry,
            DynamiteItemFactory itemFactory,
            PlacedDynamiteTracker placedTracker,
            PluginKeys keys
    ) {
        this.registry = registry;
        this.itemFactory = itemFactory;
        this.placedTracker = placedTracker;
        this.keys = keys;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void allowInteractHigh(PlayerInteractEvent event) {
        allowInteract(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void allowInteractHighest(PlayerInteractEvent event) {
        allowInteract(event);
    }

    private void allowInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }
        if (isSoulblastInteract(event)) {
            event.setUseInteractedBlock(Event.Result.ALLOW);
            event.setUseItemInHand(Event.Result.ALLOW);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void allowBlockIgniteHigh(BlockIgniteEvent event) {
        allowBlockIgnite(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void allowBlockIgniteHighest(BlockIgniteEvent event) {
        allowBlockIgnite(event);
    }

    private void allowBlockIgnite(BlockIgniteEvent event) {
        if (resolvePlacedTnt(event.getBlock()).isPresent()) {
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void allowPrimedSpawnHigh(EntitySpawnEvent event) {
        allowPrimedSpawn(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void allowPrimedSpawnHighest(EntitySpawnEvent event) {
        allowPrimedSpawn(event);
    }

    private void allowPrimedSpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed primed)) {
            return;
        }
        if (resolvePrimedDynamite(primed).isPresent()) {
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void forceAllowInteract(PlayerInteractEvent event) {
        if (!interactDenied(event) || event.getClickedBlock() == null) {
            return;
        }
        if (isSoulblastInteract(event)) {
            event.setUseInteractedBlock(Event.Result.ALLOW);
            event.setUseItemInHand(Event.Result.ALLOW);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void forceAllowBlockIgnite(BlockIgniteEvent event) {
        if (!event.isCancelled()) {
            return;
        }
        if (resolvePlacedTnt(event.getBlock()).isPresent()) {
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void forceAllowPrimedSpawn(EntitySpawnEvent event) {
        if (!event.isCancelled() || !(event.getEntity() instanceof TNTPrimed primed)) {
            return;
        }
        if (resolvePrimedDynamite(primed).isPresent()) {
            event.setCancelled(false);
        }
    }

    private static boolean interactDenied(PlayerInteractEvent event) {
        return event.useInteractedBlock() == Event.Result.DENY || event.useItemInHand() == Event.Result.DENY;
    }

    private boolean isSoulblastInteract(PlayerInteractEvent event) {
        Block clicked = event.getClickedBlock();
        if (resolveInteractDynamite(event).isPresent()) {
            return true;
        }
        ItemStack item = event.getItem();
        if (item == null) {
            return false;
        }
        Material type = item.getType();
        if (type != Material.FLINT_AND_STEEL && type != Material.FIRE_CHARGE) {
            return false;
        }
        if (clicked.getType() != Material.TNT) {
            return false;
        }
        return placedTracker.dynamiteId(clicked).flatMap(registry::find).isPresent();
    }

    private Optional<DynamiteDefinition> resolvePlacedTnt(Block block) {
        if (block == null || block.getType() != Material.TNT) {
            return Optional.empty();
        }
        return placedTracker.dynamiteId(block).flatMap(registry::find);
    }

    private Optional<DynamiteDefinition> resolvePrimedDynamite(TNTPrimed primed) {
        Optional<DynamiteDefinition> pending = PrimedDynamiteService.pendingSpawnDefinition();
        if (pending.isPresent()) {
            return pending;
        }
        String id = primed.getPersistentDataContainer().get(keys.primedDynamiteId, PersistentDataType.STRING);
        if (id == null) {
            return Optional.empty();
        }
        return registry.find(id);
    }

    private Optional<DynamiteDefinition> resolveInteractDynamite(PlayerInteractEvent event) {
        Optional<DynamiteDefinition> main = resolveHandDynamite(event.getItem());
        if (main.isPresent()) {
            return main;
        }
        if (event.getPlayer() == null) {
            return Optional.empty();
        }
        return resolveHandDynamite(event.getPlayer().getInventory().getItemInOffHand());
    }

    private Optional<DynamiteDefinition> resolveHandDynamite(ItemStack item) {
        String dynamiteId = itemFactory.readDynamiteId(item);
        if (dynamiteId == null) {
            return Optional.empty();
        }
        return registry.find(dynamiteId);
    }

}
