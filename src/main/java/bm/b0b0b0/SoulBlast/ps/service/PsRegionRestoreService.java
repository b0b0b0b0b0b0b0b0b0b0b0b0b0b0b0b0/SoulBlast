package bm.b0b0b0.SoulBlast.ps.service;

import bm.b0b0b0.SoulBlast.integration.worldguard.WorldGuardRegionScan;
import bm.b0b0b0.SoulBlast.ps.config.PsProtectionTypeDefinition;
import bm.b0b0b0.SoulBlast.ps.integration.LuckPermsBridge;
import bm.b0b0b0.SoulBlast.ps.integration.ProtectionStonesBridge;
import bm.b0b0b0.SoulBlast.ps.integration.PsRegionSnapshot;
import bm.b0b0b0.SoulBlast.ps.model.PsBlockKey;
import bm.b0b0b0.SoulBlast.ps.model.PsBlockState;
import bm.b0b0b0.SoulBlast.ps.repository.PsBlockPersistence;
import bm.b0b0b0.SoulBlast.ps.repository.PsBlockStore;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PsRegionRestoreService {

    private final JavaPlugin plugin;
    private final ProtectionStonesBridge bridge;
    private final LuckPermsBridge luckPerms;
    private final PsTypeRegistry types;
    private final PsBlockStore store;
    private final PsHologramService holograms;
    private final PsBlockPersistence persistence;

    public PsRegionRestoreService(
            JavaPlugin plugin,
            ProtectionStonesBridge bridge,
            LuckPermsBridge luckPerms,
            PsTypeRegistry types,
            PsBlockStore store,
            PsHologramService holograms,
            PsBlockPersistence persistence
    ) {
        this.plugin = plugin;
        this.bridge = bridge;
        this.luckPerms = luckPerms;
        this.types = types;
        this.store = store;
        this.holograms = holograms;
        this.persistence = persistence;
    }

    public int restoreFromDisk() {
        return restoreFromStore();
    }

    public int restoreInWorld(World world) {
        if (world == null) {
            return 0;
        }
        int count = 0;
        Server server = plugin.getServer();
        UUID worldId = world.getUID();
        for (PsBlockState state : List.copyOf(store.all())) {
            if (!state.key().worldId().equals(worldId)) {
                continue;
            }
            if (restoreOne(server, state)) {
                count++;
            }
        }
        return count;
    }

    public int restoreInChunk(Chunk chunk) {
        if (chunk == null || chunk.getWorld() == null) {
            return 0;
        }
        int count = 0;
        Server server = plugin.getServer();
        World world = chunk.getWorld();
        int minX = chunk.getX() << 4;
        int maxX = minX + 15;
        int minZ = chunk.getZ() << 4;
        int maxZ = minZ + 15;
        for (PsBlockState state : List.copyOf(store.all())) {
            PsBlockKey key = state.key();
            if (!key.worldId().equals(world.getUID())) {
                continue;
            }
            if (key.x() < minX || key.x() > maxX || key.z() < minZ || key.z() > maxZ) {
                continue;
            }
            if (restoreOne(server, state)) {
                count++;
            }
        }
        return count;
    }

    public int discoverMissing() {
        return discoverFromWorldGuard();
    }

    private int restoreFromStore() {
        int count = 0;
        Server server = plugin.getServer();
        for (PsBlockState state : List.copyOf(store.all())) {
            if (restoreOne(server, state)) {
                count++;
            }
        }
        return count;
    }

    private boolean restoreOne(Server server, PsBlockState state) {
        Optional<Block> block = PsWorldLookup.resolveBlock(server, state.key());
        if (block.isEmpty()) {
            return false;
        }
        Block resolved = block.get();
        if (resolved.getType().isAir()) {
            purgeMissing(state);
            return false;
        }
        ensureChunkLoaded(resolved.getWorld(), state.key());
        String canonicalAlias = PsBlockAliasResolver.canonicalForStorage(state.typeAlias(), resolved, bridge, types);
        if (canonicalAlias.isBlank()) {
            canonicalAlias = state.typeAlias();
        }
        Optional<PsProtectionTypeDefinition> type = types.resolve(canonicalAlias, resolved);
        if (type.isEmpty()) {
            return false;
        }
        if (!bridge.isProtectBlock(resolved) && !bridge.available()) {
            return false;
        }
        if (!bridge.isProtectBlock(resolved) && bridge.available()) {
            Optional<Block> protectBlock = bridge.protectBlockAt(resolved.getLocation());
            if (protectBlock.isEmpty()) {
                return false;
            }
            resolved = protectBlock.get();
            canonicalAlias = PsBlockAliasResolver.canonicalForStorage(state.typeAlias(), resolved, bridge, types);
            if (canonicalAlias.isBlank()) {
                canonicalAlias = state.typeAlias();
            }
            type = types.resolve(canonicalAlias, resolved);
            if (type.isEmpty()) {
                return false;
            }
        }
        if (!canonicalAlias.equals(state.typeAlias())) {
            state.updateTypeAlias(canonicalAlias);
            persistence.saveState(state);
        }
        alignWorldId(state, resolved.getWorld());
        refreshSnapshotMeta(state, resolved);
        if (!state.hologramHidden()) {
            holograms.attach(resolved.getWorld(), state, type.get());
        }
        return true;
    }

    private void alignWorldId(PsBlockState state, World world) {
        if (world == null || state.key().worldId().equals(world.getUID())) {
            return;
        }
        PsBlockState realigned = state.withKey(
                PsBlockKey.of(world, state.key().x(), state.key().y(), state.key().z())
        );
        store.remove(state.key());
        persistence.removeState(state.key());
        store.put(realigned);
        persistence.saveState(realigned);
    }

    private void purgeMissing(PsBlockState state) {
        store.remove(state.key());
        holograms.remove(state.key());
        persistence.removeState(state.key());
    }

    private int discoverFromWorldGuard() {
        int count = 0;
        for (World world : plugin.getServer().getWorlds()) {
            for (Block block : WorldGuardRegionScan.listProtectionStoneBlocks(plugin, bridge, world)) {
                PsBlockKey key = PsBlockKey.of(world, block.getX(), block.getY(), block.getZ());
                if (store.find(key).isPresent()) {
                    continue;
                }
                Optional<PsRegionSnapshot> snapshot = bridge.snapshotAt(block.getLocation());
                if (snapshot.isEmpty()) {
                    continue;
                }
                String alias = PsBlockAliasResolver.canonicalForStorage(
                        snapshot.get().alias(),
                        block,
                        bridge,
                        types
                );
                Optional<PsProtectionTypeDefinition> type = types.resolve(alias, block);
                if (type.isEmpty()) {
                    continue;
                }
                PsProtectionTypeDefinition definition = type.get();
                int maximum = definition.durability.enabled ? Math.max(1, definition.durability.maximum) : 0;
                String ownerName = snapshot.get().ownerName();
                LuckPermsBridge.ChatMeta meta = snapshot.get().ownerId() == null
                        ? new LuckPermsBridge.ChatMeta("", "")
                        : luckPerms.resolve(snapshot.get().ownerId()).orElse(new LuckPermsBridge.ChatMeta("", ""));
                PsBlockState state = new PsBlockState(
                        key,
                        alias,
                        maximum,
                        maximum,
                        ownerName,
                        meta.prefix(),
                        meta.suffix(),
                        snapshot.get().ownerId(),
                        snapshot.get().radiusX(),
                        snapshot.get().radiusY(),
                        snapshot.get().radiusZ(),
                        false
                );
                store.put(state);
                persistence.saveState(state);
                ensureChunkLoaded(world, key);
                if (!state.hologramHidden()) {
                    holograms.attach(world, state, definition);
                }
                count++;
            }
        }
        return count;
    }

    private void ensureChunkLoaded(World world, PsBlockKey key) {
        if (world == null) {
            return;
        }
        int chunkX = key.x() >> 4;
        int chunkZ = key.z() >> 4;
        if (!world.isChunkLoaded(chunkX, chunkZ)) {
            Chunk chunk = world.getChunkAt(chunkX, chunkZ);
            if (!chunk.isLoaded()) {
                chunk.load();
            }
        }
    }

    private void refreshSnapshotMeta(PsBlockState state, Block block) {
        bridge.snapshotAt(block.getLocation()).ifPresent(snapshot -> {
            String ownerName = snapshot.ownerName();
            if (ownerName != null && !"?".equals(ownerName)) {
                state.updateOwnerMeta(ownerName, state.ownerPrefix(), state.ownerSuffix());
            }
            UUID ownerId = snapshot.ownerId();
            if (ownerId != null) {
                state.updateOwnerId(ownerId);
                luckPerms.resolve(ownerId).ifPresent(meta ->
                        state.updateOwnerMeta(
                                ownerName == null || "?".equals(ownerName) ? state.ownerName() : ownerName,
                                meta.prefix(),
                                meta.suffix()
                        )
                );
            }
            state.updateRadii(snapshot.radiusX(), snapshot.radiusY(), snapshot.radiusZ());
            String alias = PsBlockAliasResolver.canonicalForStorage(snapshot.alias(), block, bridge, types);
            if (!alias.isBlank()) {
                state.updateTypeAlias(alias);
            }
        });
    }

}
