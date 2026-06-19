package bm.b0b0b0.SoulBlast.ps.listener;

import bm.b0b0b0.SoulBlast.ps.config.PsSettings;
import bm.b0b0b0.SoulBlast.ps.integration.ProtectionStonesBridge;
import bm.b0b0b0.SoulBlast.ps.integration.PsRegionSnapshot;
import bm.b0b0b0.SoulBlast.ps.service.PsBlockAliasResolver;
import bm.b0b0b0.SoulBlast.ps.service.PsDebugLog;
import bm.b0b0b0.SoulBlast.ps.service.PsLifecycleService;
import bm.b0b0b0.SoulBlast.ps.service.PsTypeRegistry;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

public final class PsLifecycleListener {

    private final PsSettings settings;
    private final ProtectionStonesBridge bridge;
    private final PsLifecycleService lifecycle;
    private final PsTypeRegistry types;
    private final PsDebugLog debug;

    public PsLifecycleListener(
            PsSettings settings,
            ProtectionStonesBridge bridge,
            PsTypeRegistry types,
            PsLifecycleService lifecycle,
            PsDebugLog debug
    ) {
        this.settings = settings;
        this.bridge = bridge;
        this.types = types;
        this.lifecycle = lifecycle;
        this.debug = debug;
    }

    public void handleCreate(Event event) {
        if (!bridge.available()) {
            debug.line("PSCreate: bridge недоступен");
            return;
        }
        Object region = bridge.regionFromEvent(event);
        Optional<PsRegionSnapshot> snapshot = bridge.snapshotFromRegion(region);
        if (snapshot.isEmpty()) {
            debug.line("PSCreate: snapshot пустой (region=" + (region == null ? "null" : region.getClass().getSimpleName()) + ")");
            return;
        }
        Block block = resolveBlock(region);
        if (block == null) {
            debug.line("PSCreate: блок привата не получен из region.getProtectBlock()");
            return;
        }
        Player player = resolvePlayer(event);
        if (settings.blockMergeWithOtherRegions && shouldBlockMerge(block, player)) {
            debug.line("PSCreate: отмена — пересечение с чужим регионом");
            if (event instanceof Cancellable cancellable) {
                cancellable.setCancelled(true);
            }
            return;
        }
        PsRegionSnapshot data = snapshot.get();
        String rawAlias = data.alias();
        Optional<String> fromBlock = bridge.aliasForProtectionBlock(block);
        String alias = PsBlockAliasResolver.canonicalForStorage(rawAlias, block, bridge, types);
        debug.line("PSCreate: rawAlias=\"" + rawAlias + "\" fromBlock=" + fromBlock.orElse("—")
                + " canonical=\"" + alias + "\" player=" + (player == null ? "—" : player.getName()));
        if (player != null) {
            data = new PsRegionSnapshot(
                    alias,
                    player.getUniqueId(),
                    player.getName(),
                    data.radiusX(),
                    data.radiusY(),
                    data.radiusZ()
            );
        } else if (!alias.equals(data.alias())) {
            data = new PsRegionSnapshot(
                    alias,
                    data.ownerId(),
                    data.ownerName(),
                    data.radiusX(),
                    data.radiusY(),
                    data.radiusZ()
            );
        }
        lifecycle.onCreate(block, player, data);
    }

    public void handleRemove(Event event) {
        if (!bridge.available()) {
            return;
        }
        Block block = resolveBlock(bridge.regionFromEvent(event));
        if (block != null) {
            lifecycle.onRemove(block);
        }
    }

    private boolean shouldBlockMerge(Block block, Player player) {
        UUID placerId = player == null ? null : player.getUniqueId();
        return bridge.overlapsForeignRegion(block.getLocation(), placerId);
    }

    private Block resolveBlock(Object region) {
        if (region == null) {
            return null;
        }
        try {
            Method getProtectBlock = region.getClass().getMethod("getProtectBlock");
            Object block = getProtectBlock.invoke(region);
            if (block instanceof Block resolved) {
                return resolved;
            }
        } catch (ReflectiveOperationException exception) {
            return null;
        }
        return null;
    }

    private Player resolvePlayer(Event event) {
        if (event instanceof org.bukkit.event.player.PlayerEvent playerEvent) {
            return playerEvent.getPlayer();
        }
        try {
            Method getPlayer = event.getClass().getMethod("getPlayer");
            Object player = getPlayer.invoke(event);
            if (player instanceof Player resolved) {
                return resolved;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
    }

}
