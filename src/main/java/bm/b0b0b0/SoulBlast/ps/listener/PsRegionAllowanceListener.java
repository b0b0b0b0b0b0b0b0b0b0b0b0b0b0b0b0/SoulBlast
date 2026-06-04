package bm.b0b0b0.SoulBlast.ps.listener;

import bm.b0b0b0.SoulBlast.ps.config.PsAllowInRegionSettings;
import bm.b0b0b0.SoulBlast.ps.config.PsSettings;
import bm.b0b0b0.SoulBlast.ps.integration.ProtectionStonesBridge;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class PsRegionAllowanceListener implements Listener {

    private final PsSettings settings;
    private final ProtectionStonesBridge bridge;

    public PsRegionAllowanceListener(PsSettings settings, ProtectionStonesBridge bridge) {
        this.settings = settings;
        this.bridge = bridge;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFallingBlock(EntityChangeBlockEvent event) {
        if (!allow(settings.allowInRegion.fallingBlock)) {
            return;
        }
        if (event.getEntityType() != EntityType.FALLING_BLOCK) {
            return;
        }
        uncancelIfProtectedRegion(event, event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onWitherBreak(EntityChangeBlockEvent event) {
        if (!allow(settings.allowInRegion.breakBlockWithWither)) {
            return;
        }
        if (event.getEntityType() != EntityType.WITHER) {
            return;
        }
        uncancelIfProtectedRegion(event, event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (!allow(settings.allowInRegion.usePiston)) {
            return;
        }
        for (Block block : event.getBlocks()) {
            uncancelIfProtectedRegion(event, block.getLocation());
            if (!(event instanceof Cancellable cancellable) || !cancellable.isCancelled()) {
                continue;
            }
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!allow(settings.allowInRegion.usePiston)) {
            return;
        }
        for (Block block : event.getBlocks()) {
            uncancelIfProtectedRegion(event, block.getLocation());
            if (!(event instanceof Cancellable cancellable) || !cancellable.isCancelled()) {
                continue;
            }
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMinecartInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Minecart minecart)) {
            return;
        }
        PsAllowInRegionSettings allow = settings.allowInRegion;
        boolean fishingRod = isFishingRod(event.getPlayer().getInventory().getItemInMainHand())
                || isFishingRod(event.getPlayer().getInventory().getItemInOffHand());
        if (allow.mineCart.hookUp && fishingRod) {
            uncancelIfProtectedRegion(event, minecart.getLocation());
            return;
        }
        if (allow.mineCart.open && isStorageMinecart(minecart.getType())) {
            uncancelIfProtectedRegion(event, minecart.getLocation());
        }
    }

    private static boolean isStorageMinecart(EntityType type) {
        return type == EntityType.HOPPER_MINECART || type == EntityType.CHEST_MINECART;
    }

    private static boolean isFishingRod(ItemStack item) {
        return item != null && item.getType() == Material.FISHING_ROD;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawnEgg(PlayerInteractEvent event) {
        if (!allow(settings.allowInRegion.useSpawnEggs)) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || !item.getType().name().endsWith("_SPAWN_EGG")) {
            return;
        }
        uncancelIfProtectedRegion(event, event.getClickedBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFireArrow(EntityDamageByEntityEvent event) {
        if (!allow(settings.allowInRegion.useFireArrowToIgniteTnt)) {
            return;
        }
        if (event.getEntityType() != EntityType.TNT) {
            return;
        }
        Entity damager = event.getDamager();
        if (!(damager instanceof Projectile projectile)) {
            return;
        }
        if (projectile.getFireTicks() <= 0) {
            return;
        }
        uncancelIfProtectedRegion(event, event.getEntity().getLocation());
    }

    private boolean allow(boolean flag) {
        return bridge.available() && flag;
    }

    private void uncancelIfProtectedRegion(Cancellable event, org.bukkit.Location location) {
        if (!bridge.snapshotAt(location).isPresent()) {
            return;
        }
        event.setCancelled(false);
    }

}
