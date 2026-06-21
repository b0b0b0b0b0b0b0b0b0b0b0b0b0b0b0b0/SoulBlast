package bm.b0b0b0.SoulBlast.ps.service;

import bm.b0b0b0.SoulBlast.ps.config.PsProtectionTypeDefinition;
import bm.b0b0b0.SoulBlast.ps.integration.ProtectionStonesBridge;
import bm.b0b0b0.SoulBlast.ps.integration.PsRegionSnapshot;
import bm.b0b0b0.SoulBlast.ps.model.PsBlockKey;
import bm.b0b0b0.SoulBlast.ps.model.PsBlockState;
import bm.b0b0b0.SoulBlast.ps.repository.PsBlockPersistence;
import bm.b0b0b0.SoulBlast.ps.repository.PsBlockStore;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public final class PsHologramVisibilityService {

    public static final String PERMISSION = "soulblast.ps.hologram";
    public static final String ADMIN_PERMISSION = "soulblast.ps.hologram.admin";

    private final ProtectionStonesBridge bridge;
    private final PsTypeRegistry types;
    private final PsBlockStore store;
    private final PsHologramService holograms;
    private final PsBlockPersistence persistence;

    public PsHologramVisibilityService(
            ProtectionStonesBridge bridge,
            PsTypeRegistry types,
            PsBlockStore store,
            PsHologramService holograms,
            PsBlockPersistence persistence
    ) {
        this.bridge = bridge;
        this.types = types;
        this.store = store;
        this.holograms = holograms;
        this.persistence = persistence;
    }

    public Result applyHit(Player player, Block block, PsHologramHideSession.Mode mode) {
        if (player == null || block == null || block.getWorld() == null || mode == null) {
            return Result.NOT_PROTECT_BLOCK;
        }
        if (!bridge.available() || !bridge.isProtectBlock(block)) {
            return Result.NOT_PROTECT_BLOCK;
        }
        Optional<PsBlockState> stateOptional = resolveState(block);
        if (stateOptional.isEmpty()) {
            return Result.NOT_TRACKED;
        }
        PsBlockState state = stateOptional.get();
        if (!canManage(player, state, block)) {
            return Result.NOT_OWNER;
        }
        boolean hidden = resolveHidden(state, mode);
        if (hidden == state.hologramHidden()) {
            return hidden ? Result.ALREADY_HIDDEN : Result.ALREADY_VISIBLE;
        }
        state.setHologramHidden(hidden);
        if (persistence != null) {
            persistence.saveState(state);
        }
        Optional<PsProtectionTypeDefinition> type = types.findAlias(state.typeAlias());
        if (hidden) {
            holograms.remove(state.key());
            return Result.HIDDEN;
        }
        if (type.isPresent() && block.getWorld() != null) {
            holograms.attach(block.getWorld(), state, type.get());
        }
        return Result.SHOWN;
    }

    private boolean resolveHidden(PsBlockState state, PsHologramHideSession.Mode mode) {
        return switch (mode) {
            case HIDE -> true;
            case SHOW -> false;
            case TOGGLE -> !state.hologramHidden();
        };
    }

    private Optional<PsBlockState> resolveState(Block block) {
        PsBlockKey key = PsBlockKey.of(block.getWorld(), block.getX(), block.getY(), block.getZ());
        Optional<PsBlockState> direct = store.find(key);
        if (direct.isPresent()) {
            return direct;
        }
        return store.findAtCoordinates(block.getX(), block.getY(), block.getZ());
    }

    public boolean canManage(Player player, PsBlockState state, Block block) {
        if (player == null || state == null) {
            return false;
        }
        if (player.hasPermission(ADMIN_PERMISSION)) {
            return true;
        }
        UUID playerId = player.getUniqueId();
        if (state.ownerId() != null && state.ownerId().equals(playerId)) {
            return true;
        }
        if (state.ownerName() != null
                && !"?".equals(state.ownerName())
                && player.getName().equalsIgnoreCase(state.ownerName())) {
            return true;
        }
        return bridge.snapshotAt(block.getLocation())
                .map(snapshot -> isOwner(snapshot, playerId, player.getName()))
                .orElse(false);
    }

    private static boolean isOwner(PsRegionSnapshot snapshot, UUID playerId, String playerName) {
        if (snapshot.ownerId() != null && snapshot.ownerId().equals(playerId)) {
            return true;
        }
        String ownerName = snapshot.ownerName();
        return !"?".equals(ownerName)
                && playerName != null
                && playerName.equalsIgnoreCase(ownerName);
    }

    public enum Result {
        HIDDEN,
        SHOWN,
        ALREADY_HIDDEN,
        ALREADY_VISIBLE,
        NOT_OWNER,
        NOT_TRACKED,
        NOT_PROTECT_BLOCK
    }

}
