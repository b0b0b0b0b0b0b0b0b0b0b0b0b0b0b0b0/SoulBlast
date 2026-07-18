package bm.b0b0b0.SoulBlast.integration.worldguard;

import bm.b0b0b0.SoulBlast.repository.DynamiteRegistry;
import bm.b0b0b0.SoulBlast.service.DynamiteItemFactory;
import bm.b0b0b0.SoulBlast.service.PlacedDynamiteTracker;
import bm.b0b0b0.SoulBlast.service.PrimedDynamiteService;
import bm.b0b0b0.SoulBlast.util.PluginKeys;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;

public final class WorldGuardSoulblastEventBridge {

    private static volatile boolean registered;

    private static final Listener REGISTRATION_STUB = new Listener() {
    };

    private static final String PLACE_BLOCK_EVENT = "com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent";
    private static final String USE_ITEM_EVENT = "com.sk89q.worldguard.bukkit.event.inventory.UseItemEvent";
    private static final String USE_BLOCK_EVENT = "com.sk89q.worldguard.bukkit.event.block.UseBlockEvent";
    private static final String SPAWN_ENTITY_EVENT = "com.sk89q.worldguard.bukkit.event.entity.SpawnEntityEvent";

    private final JavaPlugin plugin;
    private final DynamiteItemFactory itemFactory;
    private final DynamiteRegistry registry;
    private final PlacedDynamiteTracker placedTracker;
    private final PluginKeys keys;
    private final Method getOriginalEvent;
    private final Method setAllowed;
    private final Method setSilent;

    private WorldGuardSoulblastEventBridge(
            JavaPlugin plugin,
            DynamiteItemFactory itemFactory,
            DynamiteRegistry registry,
            PlacedDynamiteTracker placedTracker,
            PluginKeys keys,
            Method getOriginalEvent,
            Method setAllowed,
            Method setSilent
    ) {
        this.plugin = plugin;
        this.itemFactory = itemFactory;
        this.registry = registry;
        this.placedTracker = placedTracker;
        this.keys = keys;
        this.getOriginalEvent = getOriginalEvent;
        this.setAllowed = setAllowed;
        this.setSilent = setSilent;
    }

    public static void register(
            JavaPlugin plugin,
            DynamiteItemFactory itemFactory,
            DynamiteRegistry registry,
            PlacedDynamiteTracker placedTracker,
            PluginKeys keys
    ) {
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            return;
        }
        if (registered) {
            return;
        }
        try {
            Class<?> delegateClass = Class.forName("com.sk89q.worldguard.bukkit.event.DelegateEvent");
            Method getOriginalEvent = delegateClass.getMethod("getOriginalEvent");
            Method setAllowed = delegateClass.getMethod("setAllowed", boolean.class);
            Method setSilent = delegateClass.getMethod("setSilent", boolean.class);
            WorldGuardSoulblastEventBridge bridge = new WorldGuardSoulblastEventBridge(
                    plugin,
                    itemFactory,
                    registry,
                    placedTracker,
                    keys,
                    getOriginalEvent,
                    setAllowed,
                    setSilent
            );
            bridge.bind(PLACE_BLOCK_EVENT, bridge::handleDelegate);
            bridge.bind(USE_ITEM_EVENT, bridge::handleDelegate);
            bridge.bind(USE_BLOCK_EVENT, bridge::handleDelegate);
            bridge.bind(SPAWN_ENTITY_EVENT, bridge::handleDelegate);
            registered = true;
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().warning(
                    "WorldGuard SoulBlast override не подключён: " + exception.getMessage()
            );
        }
    }

    @SuppressWarnings("unchecked")
    private void bind(String eventClassName, EventExecutor executor) throws ReflectiveOperationException {
        Class<? extends Event> eventClass = (Class<? extends Event>) Class.forName(eventClassName);
        plugin.getServer().getPluginManager().registerEvent(
                eventClass,
                REGISTRATION_STUB,
                EventPriority.LOWEST,
                executor,
                plugin,
                false
        );
    }

    private void handleDelegate(org.bukkit.event.Listener ignored, Event wgEvent) {
        try {
            if (!shouldAllow(wgEvent)) {
                return;
            }
            setAllowed.invoke(wgEvent, true);
            setSilent.invoke(wgEvent, true);
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().warning("WorldGuard allow failed: " + exception.getMessage());
        }
    }

    private boolean shouldAllow(Event wgEvent) throws ReflectiveOperationException {
        Event original = (Event) getOriginalEvent.invoke(wgEvent);
        if (original == null) {
            return false;
        }
        if (original instanceof BlockPlaceEvent place) {
            return isSoulblastItem(place.getItemInHand());
        }
        if (wgEvent.getClass().getName().endsWith("UseItemEvent")) {
            try {
                Method getItemStack = wgEvent.getClass().getMethod("getItemStack");
                ItemStack stack = (ItemStack) getItemStack.invoke(wgEvent);
                return isSoulblastItem(stack);
            } catch (ReflectiveOperationException exception) {
                return false;
            }
        }
        return switch (original) {
            case PlayerInteractEvent interact -> resolveInteractSoulblast(interact);
            case BlockIgniteEvent ignite -> resolvePlacedSoulblast(ignite.getBlock());
            case EntitySpawnEvent spawn when spawn.getEntity() instanceof TNTPrimed primed ->
                    resolvePrimedSoulblast(primed);
            default -> false;
        };
    }

    private boolean resolveInteractSoulblast(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return false;
        }
        if (resolveHand(event.getItem())) {
            return true;
        }
        if (event.getPlayer() != null && resolveHand(event.getPlayer().getInventory().getItemInOffHand())) {
            return true;
        }
        ItemStack tool = event.getItem();
        if (tool == null) {
            return false;
        }
        Material toolType = tool.getType();
        if (toolType != Material.FLINT_AND_STEEL && toolType != Material.FIRE_CHARGE) {
            return false;
        }
        Block clicked = event.getClickedBlock();
        if (clicked.getType() != Material.TNT) {
            return false;
        }
        return placedTracker.dynamiteId(clicked).flatMap(registry::find).isPresent();
    }

    private boolean resolvePlacedSoulblast(Block block) {
        if (block == null || block.getType() != Material.TNT) {
            return false;
        }
        return placedTracker.dynamiteId(block).flatMap(registry::find).isPresent();
    }

    private boolean resolvePrimedSoulblast(TNTPrimed primed) {
        if (PrimedDynamiteService.pendingSpawnDefinition().isPresent()) {
            return true;
        }
        String id = primed.getPersistentDataContainer().get(keys.primedDynamiteId, PersistentDataType.STRING);
        if (id != null) {
            return registry.find(id).isPresent();
        }
        Entity source = primed.getSource();
        if (source instanceof Player player) {
            return resolveHand(player.getInventory().getItemInMainHand())
                    || resolveHand(player.getInventory().getItemInOffHand());
        }
        return false;
    }

    private boolean resolveHand(ItemStack item) {
        String dynamiteId = itemFactory.readDynamiteId(item);
        return dynamiteId != null && registry.find(dynamiteId).isPresent();
    }

    private boolean isSoulblastItem(ItemStack item) {
        return resolveHand(item);
    }

}
