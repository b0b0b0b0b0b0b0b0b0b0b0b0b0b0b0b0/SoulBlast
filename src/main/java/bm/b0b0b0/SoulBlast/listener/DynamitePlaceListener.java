package bm.b0b0b0.SoulBlast.listener;

import bm.b0b0b0.SoulBlast.SoulBlast;
import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.ps.PsModule;
import bm.b0b0b0.SoulBlast.ps.service.PsDurabilityTrace;
import bm.b0b0b0.SoulBlast.repository.DynamiteRegistry;
import bm.b0b0b0.SoulBlast.service.DynamiteCooldownMessenger;
import bm.b0b0b0.SoulBlast.service.DynamiteItemFactory;
import bm.b0b0b0.SoulBlast.service.PlacedDynamiteTracker;
import bm.b0b0b0.SoulBlast.service.PlayerProfileService;
import bm.b0b0b0.SoulBlast.service.PrimedDynamiteService;
import bm.b0b0b0.SoulBlast.service.region.RegionProtectionMessenger;
import bm.b0b0b0.SoulBlast.service.region.RegionProtectionService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class DynamitePlaceListener implements Listener {

    private final SoulBlast plugin;
    private final DynamiteRegistry registry;
    private final DynamiteItemFactory itemFactory;
    private final PrimedDynamiteService primedService;
    private final PlayerProfileService profileService;
    private final PlacedDynamiteTracker placedTracker;
    private final RegionProtectionService regionProtection;
    private final RegionProtectionMessenger regionMessages;
    private final DynamiteCooldownMessenger cooldownMessages;

    public DynamitePlaceListener(
            SoulBlast plugin,
            DynamiteRegistry registry,
            DynamiteItemFactory itemFactory,
            PrimedDynamiteService primedService,
            PlayerProfileService profileService,
            PlacedDynamiteTracker placedTracker,
            RegionProtectionService regionProtection,
            RegionProtectionMessenger regionMessages,
            DynamiteCooldownMessenger cooldownMessages
    ) {
        this.plugin = plugin;
        this.registry = registry;
        this.itemFactory = itemFactory;
        this.primedService = primedService;
        this.profileService = profileService;
        this.placedTracker = placedTracker;
        this.regionProtection = regionProtection;
        this.regionMessages = regionMessages;
        this.cooldownMessages = cooldownMessages;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlace(BlockPlaceEvent event) {
        String dynamiteId = itemFactory.readDynamiteId(event.getItemInHand());
        if (dynamiteId == null) {
            return;
        }
        Optional<DynamiteDefinition> definition = registry.find(dynamiteId);
        if (definition.isEmpty()) {
            return;
        }
        DynamiteDefinition dynamite = definition.get();
        Player player = event.getPlayer();
        RegionProtectionService.RegionCheckResult regionCheck;
        try {
            regionCheck = regionProtection.evaluate(
                    event.getBlockPlaced().getLocation(),
                    dynamite,
                    player
            );
        } catch (RuntimeException exception) {
            event.setCancelled(true);
            return;
        }
        if (!regionCheck.permitted()) {
            event.setCancelled(true);
            regionMessages.sendBlocked(player, regionCheck, dynamite);
            return;
        }
        if (cooldownMessages.blockUse(player, dynamite)) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        boolean autoIgnite = profileService.resolvesAutoIgnite(player, dynamite.autoIgniteOnPlace);
        Block block = event.getBlockPlaced();
        Location igniteAt = block.getLocation().add(0.5, 0.0, 0.5);
        tracePlace(player, igniteAt, dynamite.id);
        if (autoIgnite) {
            primedService.spawnPrimed(igniteAt, dynamite, player);
            consumePlacedItem(event);
            return;
        }
        block.setType(Material.TNT, false);
        placedTracker.track(block, dynamite.id);
        consumePlacedItem(event);
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

    private void consumePlacedItem(BlockPlaceEvent event) {
        EquipmentSlot hand = event.getHand();
        ItemStack item = event.getItemInHand();
        if (item.getAmount() <= 1) {
            if (hand == EquipmentSlot.OFF_HAND) {
                event.getPlayer().getInventory().setItemInOffHand(null);
            } else {
                event.getPlayer().getInventory().setItemInMainHand(null);
            }
            return;
        }
        item.setAmount(item.getAmount() - 1);
    }

}
