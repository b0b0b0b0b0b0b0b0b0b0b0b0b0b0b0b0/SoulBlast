package bm.b0b0b0.SoulBlast.listener;

import bm.b0b0b0.SoulBlast.SoulBlast;
import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.ps.PsModule;
import bm.b0b0b0.SoulBlast.ps.service.PsDurabilityTrace;
import bm.b0b0b0.SoulBlast.config.IgnitionSettings;
import bm.b0b0b0.SoulBlast.message.MessageService;
import bm.b0b0b0.SoulBlast.repository.DynamiteRegistry;
import bm.b0b0b0.SoulBlast.service.DynamiteCooldownMessenger;
import bm.b0b0b0.SoulBlast.service.DynamiteItemFactory;
import bm.b0b0b0.SoulBlast.service.PlacedDynamiteTracker;
import bm.b0b0b0.SoulBlast.service.PrimedDynamiteService;
import bm.b0b0b0.SoulBlast.service.region.RegionProtectionMessenger;
import bm.b0b0b0.SoulBlast.service.region.RegionProtectionService;
import bm.b0b0b0.SoulBlast.util.PluginKeys;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.Optional;

public final class DynamiteIgniteListener implements Listener {

    private final SoulBlast plugin;
    private final PluginKeys keys;
    private final DynamiteRegistry registry;
    private final DynamiteItemFactory itemFactory;
    private final PlacedDynamiteTracker placedTracker;
    private final PrimedDynamiteService primedService;
    private final MessageService messages;
    private final RegionProtectionService regionProtection;
    private final RegionProtectionMessenger regionMessages;
    private final DynamiteCooldownMessenger cooldownMessages;

    public DynamiteIgniteListener(
            SoulBlast plugin,
            PluginKeys keys,
            DynamiteRegistry registry,
            DynamiteItemFactory itemFactory,
            PlacedDynamiteTracker placedTracker,
            PrimedDynamiteService primedService,
            MessageService messages,
            RegionProtectionService regionProtection,
            RegionProtectionMessenger regionMessages,
            DynamiteCooldownMessenger cooldownMessages
    ) {
        this.plugin = plugin;
        this.keys = keys;
        this.registry = registry;
        this.itemFactory = itemFactory;
        this.placedTracker = placedTracker;
        this.primedService = primedService;
        this.messages = messages;
        this.regionProtection = regionProtection;
        this.regionMessages = regionMessages;
        this.cooldownMessages = cooldownMessages;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        ItemStack hand = event.getItem();
        if (hand == null) {
            return;
        }
        String dynamiteId = itemFactory.readDynamiteId(hand);
        if (dynamiteId == null) {
            return;
        }
        Material handType = hand.getType();
        if (handType != Material.FLINT_AND_STEEL && handType != Material.FIRE_CHARGE) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block.getType() != Material.TNT) {
            return;
        }
        Optional<DynamiteDefinition> definition = registry.find(dynamiteId);
        if (definition.isEmpty()) {
            return;
        }
        if (!definition.get().ignition.allowFlintAndSteel) {
            event.setCancelled(true);
            messages.send(event.getPlayer(), "ignite-denied");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.TNT) {
            return;
        }
        Optional<DynamiteDefinition> definition = resolvePlaced(block);
        if (definition.isEmpty()) {
            return;
        }
        IgnitionSettings settings = definition.get().ignition;
        BlockIgniteEvent.IgniteCause cause = event.getCause();
        if (!isAllowed(cause, settings)) {
            cancel(event);
            return;
        }
        Entity source = event.getIgnitingEntity();
        Player player = source instanceof Player igniter ? igniter : null;
        if (event.isCancelled()) {
            return;
        }
        RegionProtectionService.RegionCheckResult regionCheck;
        try {
            regionCheck = regionProtection.evaluate(
                    block.getLocation(),
                    definition.get(),
                    player
            );
        } catch (RuntimeException exception) {
            event.setCancelled(true);
            return;
        }
        if (!regionCheck.permitted()) {
            event.setCancelled(true);
            if (player != null) {
                regionMessages.sendBlocked(player, regionCheck, definition.get());
            }
            return;
        }
        if (player != null && cooldownMessages.blockUse(player, definition.get())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        placedTracker.untrack(block);
        block.setType(Material.AIR);
        Location igniteAt = block.getLocation().add(0.5, 0.0, 0.5);
        tracePlace(player, igniteAt, definition.get().id);
        primedService.spawnPrimed(igniteAt, definition.get(), source);
    }

    private void tracePlace(Player player, Location at, String dynamiteId) {
        PsModule psModule = plugin.getPsModule();
        if (psModule == null || !psModule.active()) {
            return;
        }
        PsDurabilityTrace trace = psModule.durabilityTrace();
        if (trace != null) {
            trace.dynamitePlaced(player, at, dynamiteId, psModule.blockStore());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlacedDynamiteBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Optional<String> dynamiteId = placedTracker.dynamiteId(block);
        if (dynamiteId.isEmpty()) {
            return;
        }
        Optional<DynamiteDefinition> definition = registry.find(dynamiteId.get());
        placedTracker.untrack(block);
        event.setDropItems(false);
        if (definition.isEmpty()) {
            return;
        }
        ItemStack custom = itemFactory.create(definition.get(), 1);
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        Map<Integer, ItemStack> overflow = inventory.addItem(custom);
        if (!overflow.isEmpty()) {
            for (ItemStack leftover : overflow.values()) {
                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), leftover);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreakMonitor(BlockBreakEvent event) {
        placedTracker.untrack(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPrimedSpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed primed)) {
            return;
        }
        if (primed.getPersistentDataContainer().has(keys.primedDynamiteId, PersistentDataType.STRING)) {
            return;
        }
        Entity source = primed.getSource();
        if (source instanceof Player player) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            String id = itemFactory.readDynamiteId(hand);
            if (id != null) {
                registry.find(id).ifPresent(def -> {
                    primed.getPersistentDataContainer().set(keys.primedDynamiteId, PersistentDataType.STRING, def.id);
                    primed.setFuseTicks(def.fuseTicks);
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed primed)) {
            return;
        }
        Optional<DynamiteDefinition> definition = primedService.resolvePrimed(primed);
        if (definition.isEmpty()) {
            return;
        }
        Entity damager = event.getDamager();
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player) {
            if (!definition.get().ignition.allowFlameBow) {
                event.setCancelled(true);
            }
        }
    }

    private Optional<DynamiteDefinition> resolvePlaced(Block block) {
        return placedTracker.dynamiteId(block).flatMap(registry::find);
    }

    private boolean isAllowed(BlockIgniteEvent.IgniteCause cause, IgnitionSettings settings) {
        if (cause == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) {
            return settings.allowFlintAndSteel;
        }
        if (cause == BlockIgniteEvent.IgniteCause.EXPLOSION) {
            return settings.allowExplosion;
        }
        if (isRedstoneCause(cause)) {
            return settings.allowRedstone;
        }
        if (isFireCause(cause)) {
            return settings.allowFire;
        }
        return false;
    }

    private boolean isFireCause(BlockIgniteEvent.IgniteCause cause) {
        return cause.name().contains("FIRE") || cause.name().contains("LAVA");
    }

    private boolean isRedstoneCause(BlockIgniteEvent.IgniteCause cause) {
        return cause.name().contains("REDSTONE");
    }

    private void cancel(BlockIgniteEvent event) {
        event.setCancelled(true);
        if (event.getIgnitingEntity() instanceof Player player) {
            messages.send(player, "ignite-denied");
        }
    }

}
