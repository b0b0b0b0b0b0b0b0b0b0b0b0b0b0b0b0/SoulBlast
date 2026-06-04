package bm.b0b0b0.SoulBlast.ps.service;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.integration.worldguard.WorldGuardRegionScan;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import bm.b0b0b0.SoulBlast.ps.config.PsDurabilitySettings;
import bm.b0b0b0.SoulBlast.ps.config.PsExplosionBreakSettings;
import bm.b0b0b0.SoulBlast.ps.config.PsProtectionTypeDefinition;
import bm.b0b0b0.SoulBlast.ps.config.PsSettings;
import bm.b0b0b0.SoulBlast.ps.integration.ProtectionStonesBridge;
import bm.b0b0b0.SoulBlast.ps.integration.PsRegionSnapshot;
import bm.b0b0b0.SoulBlast.ps.model.PsBlockKey;
import bm.b0b0b0.SoulBlast.ps.model.PsBlockState;
import bm.b0b0b0.SoulBlast.ps.repository.PsBlockPersistence;
import bm.b0b0b0.SoulBlast.ps.repository.PsBlockStore;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class PsExplosionBridge {

    private final JavaPlugin plugin;
    private final PsSettings settings;
    private final PsTypeRegistry types;
    private final PsBlockStore store;
    private final PsHologramService holograms;
    private final ProtectionStonesBridge bridge;
    private final PsLifecycleService lifecycle;
    private final PsBlockPersistence persistence;
    private final PsDebugLog debug;
    private final PsDurabilityTrace trace;

    public PsExplosionBridge(
            JavaPlugin plugin,
            PsSettings settings,
            PsTypeRegistry types,
            PsBlockStore store,
            PsHologramService holograms,
            ProtectionStonesBridge bridge,
            PsLifecycleService lifecycle,
            PsBlockPersistence persistence,
            PsDebugLog debug,
            PsDurabilityTrace trace
    ) {
        this.plugin = plugin;
        this.settings = settings;
        this.types = types;
        this.store = store;
        this.holograms = holograms;
        this.bridge = bridge;
        this.lifecycle = lifecycle;
        this.persistence = persistence;
        this.debug = debug;
        this.trace = trace;
    }

    public boolean tryAbsorbExplosion(Block block, DynamiteDefinition dynamite, ExplosionJob job) {
        if (!settings.supportSoulblast || block == null || dynamite == null) {
            return false;
        }
        if (!bridge.isProtectBlock(block)) {
            return false;
        }
        return applyProtectBlockDamage(block, dynamite, job);
    }

    public void applyProximityDamage(ExplosionJob job) {
        if (job == null) {
            return;
        }
        if (!settings.supportSoulblast) {
            trace.moduleInactive("ps/settings.yml support-soulblast=false");
            return;
        }
        Location center = job.getCenter();
        World world = center.getWorld();
        if (world == null) {
            trace.moduleInactive("мир взрыва=null");
            return;
        }
        DynamiteDefinition dynamite = job.getDynamite();
        trace.explosionPassStart(center, dynamite.id, store.size());
        Set<PsBlockKey> processed = new HashSet<>();
        bridge.protectBlockAt(center).ifPresent(block ->
                tryApplyProximityForBlock(job, center, world, dynamite, block, processed)
        );
        for (PsBlockState stored : store.all()) {
            Block block = world.getBlockAt(stored.key().x(), stored.key().y(), stored.key().z());
            if (!isProtectionCandidate(block)) {
                trace.skipped("store " + stored.key().x() + "," + stored.key().y() + "," + stored.key().z()
                        + " — блок не распознан как приват (PS API)");
                continue;
            }
            tryApplyProximityForBlock(job, center, world, dynamite, block, processed);
        }
        scanProtectBlocksNearExplosion(job, center, world, dynamite, processed);
        for (Block block : WorldGuardRegionScan.listProtectionStoneBlocks(plugin, bridge, world)) {
            tryApplyProximityForBlock(job, center, world, dynamite, block, processed);
        }
    }

    private void scanProtectBlocksNearExplosion(
            ExplosionJob job,
            Location center,
            World world,
            DynamiteDefinition dynamite,
            Set<PsBlockKey> processed
    ) {
        int horizontal = (int) Math.ceil(Math.max(8.0, dynamite.explosion.radius + 3.0));
        int vertical = (int) Math.ceil(Math.max(6.0, dynamite.explosion.radius + 2.0));
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int horizontalSquared = horizontal * horizontal;
        for (int dx = -horizontal; dx <= horizontal; dx++) {
            for (int dy = -vertical; dy <= vertical; dy++) {
                for (int dz = -horizontal; dz <= horizontal; dz++) {
                    if (dx * dx + dz * dz > horizontalSquared) {
                        continue;
                    }
                    Block block = world.getBlockAt(cx + dx, cy + dy, cz + dz);
                    if (!isProtectionCandidate(block)) {
                        continue;
                    }
                    tryApplyProximityForBlock(job, center, world, dynamite, block, processed);
                }
            }
        }
    }

    private void tryApplyProximityForBlock(
            ExplosionJob job,
            Location center,
            World world,
            DynamiteDefinition dynamite,
            Block block,
            Set<PsBlockKey> processed
    ) {
        PsBlockKey key = PsBlockKey.of(world, block.getX(), block.getY(), block.getZ());
        if (!processed.add(key)) {
            return;
        }
        if (job.alreadyPsDurabilityHit(key.x(), key.y(), key.z())) {
            trace.skipped(key.x() + "," + key.y() + "," + key.z() + " — урон уже учтён в этом взрыве");
            return;
        }
        PsBlockState existing = resolveStoredState(world, block);
        Optional<PsProtectionTypeDefinition> type = resolveTypeForBlock(block, existing);
        if (type.isEmpty()) {
            debugSkip(block, "тип не найден (alias="
                    + (existing == null ? types.resolveAlias(block).orElse("—") : existing.typeAlias()) + ")");
            return;
        }
        PsBlockState state = ensureTrackedState(block, type.get(), processed);
        if (state == null) {
            debugSkip(block, "не удалось создать состояние (нет PS-региона на блоке)");
            return;
        }
        if (!type.get().durability.enabled) {
            debugSkip(block, "durability.enabled=false");
            return;
        }
        PsExplosionBreakSettings explosion = type.get().breakProtectionBlock.withExplosion;
        if (!explosion.enabled) {
            debugSkip(block, "break-protection-block.with-explosion.enabled=false");
            return;
        }
        int damage = resolveDamage(explosion, dynamite.id);
        if (damage <= 0) {
            debugSkip(block, "урон=0 для динамита \"" + dynamite.id + "\" (only-dynamite-types)");
            return;
        }
        if (!withinProximity(center, block, state, type.get().durability, dynamite)) {
            debugSkip(block, "вне радиуса proximity");
            return;
        }
        if (!type.get().durability.proximityDamage
                && !bridge.explosionCenterAffectsProtectBlock(center, block)
                && !centerInsideRegionBox(center, state)) {
            debugSkip(block, "proximity-damage=false и взрыв не в зоне привата");
            return;
        }
        job.markPsDurabilityHit(key.x(), key.y(), key.z());
        applyDurabilityDamage(block, type.get(), damage, dynamite.id);
    }

    private PsBlockState resolveStoredState(World world, Block block) {
        PsBlockKey key = PsBlockKey.of(world, block.getX(), block.getY(), block.getZ());
        Optional<PsBlockState> direct = store.find(key);
        if (direct.isPresent()) {
            return alignStoredWorld(direct.get(), world);
        }
        Optional<PsBlockState> byCoords = store.findAtCoordinates(block.getX(), block.getY(), block.getZ());
        if (byCoords.isEmpty()) {
            return null;
        }
        PsBlockState state = byCoords.get();
        if (!state.key().worldId().equals(world.getUID())) {
            trace.skipped("мигрирую world-id региона " + state.key().x() + "," + state.key().y() + "," + state.key().z()
                    + " " + state.key().worldId() + " -> " + world.getUID());
        }
        return alignStoredWorld(state, world);
    }

    private PsBlockState alignStoredWorld(PsBlockState state, World world) {
        if (state.key().worldId().equals(world.getUID())) {
            return state;
        }
        PsBlockKey oldKey = state.key();
        PsBlockState aligned = store.alignWorld(state, world);
        if (persistence != null) {
            persistence.removeState(oldKey);
            persistence.saveState(aligned);
        }
        return aligned;
    }

    private PsBlockState ensureTrackedState(
            Block block,
            PsProtectionTypeDefinition type,
            Set<PsBlockKey> processed
    ) {
        PsBlockState existing = resolveStoredState(block.getWorld(), block);
        if (existing != null) {
            String canonical = PsBlockAliasResolver.canonicalForStorage(
                    existing.typeAlias(),
                    block,
                    bridge,
                    types
            );
            if (!canonical.isBlank() && !canonical.equals(existing.typeAlias())) {
                existing.updateTypeAlias(canonical);
                if (persistence != null) {
                    persistence.saveState(existing);
                }
            }
            return existing;
        }
        Optional<String> alias = types.resolveAlias(block);
        if (alias.isEmpty()) {
            return null;
        }
        return createTrackedState(block, type, alias.get());
    }

    private boolean applyProtectBlockDamage(Block block, DynamiteDefinition dynamite, ExplosionJob job) {
        Optional<PsProtectionTypeDefinition> type = resolveTypeForBlock(block, null);
        if (type.isEmpty()) {
            return false;
        }
        PsExplosionBreakSettings explosion = type.get().breakProtectionBlock.withExplosion;
        if (!explosion.enabled) {
            return true;
        }
        int damage = resolveDamage(explosion, dynamite.id);
        if (damage <= 0) {
            return true;
        }
        if (job != null) {
            job.markPsDurabilityHit(block.getX(), block.getY(), block.getZ());
        }
        applyDurabilityDamage(block, type.get(), damage, dynamite.id);
        return true;
    }

    private Optional<PsProtectionTypeDefinition> resolveTypeForBlock(Block block, PsBlockState state) {
        Optional<PsProtectionTypeDefinition> fromBlock = types.findBlock(block);
        if (fromBlock.isPresent()) {
            return fromBlock;
        }
        if (state != null) {
            return types.resolve(state.typeAlias(), block);
        }
        return types.resolveAlias(block).flatMap(alias -> types.findAlias(alias));
    }

    private boolean isProtectionCandidate(Block block) {
        if (block == null) {
            return false;
        }
        if (bridge.isProtectBlock(block)) {
            return true;
        }
        return types.resolveAlias(block).isPresent();
    }

    private void debugSkip(Block block, String reason) {
        if (block == null) {
            return;
        }
        debug.line("взрыв: пропуск " + block.getX() + "," + block.getY() + "," + block.getZ() + " — " + reason);
    }

    private boolean withinProximity(
            Location center,
            Block protectBlock,
            PsBlockState state,
            PsDurabilitySettings durability,
            DynamiteDefinition dynamite
    ) {
        if (bridge.explosionCenterAffectsProtectBlock(center, protectBlock)) {
            return true;
        }
        if (centerInsideRegionBox(center, state)) {
            return true;
        }
        double radius = resolveProximityRadius(durability, dynamite);
        double dx = center.getX() - (protectBlock.getX() + 0.5);
        double dy = center.getY() - (protectBlock.getY() + 0.5);
        double dz = center.getZ() - (protectBlock.getZ() + 0.5);
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    private boolean centerInsideRegionBox(Location center, PsBlockState state) {
        int halfX = Math.max(1, state.radiusX());
        int halfZ = Math.max(1, state.radiusZ());
        int halfY = state.radiusY() > 0 ? state.radiusY() : Math.max(halfX, halfZ);
        double bx = state.key().x() + 0.5;
        double by = state.key().y() + 0.5;
        double bz = state.key().z() + 0.5;
        return Math.abs(center.getX() - bx) <= halfX + 0.5
                && Math.abs(center.getY() - by) <= halfY + 0.5
                && Math.abs(center.getZ() - bz) <= halfZ + 0.5;
    }

    private PsBlockState createTrackedState(Block block, PsProtectionTypeDefinition type, String alias) {
        Optional<PsRegionSnapshot> snapshot = bridge.snapshotAt(block.getLocation());
        if (snapshot.isEmpty()) {
            return null;
        }
        String canonical = PsBlockAliasResolver.canonicalForStorage(alias, block, bridge, types);
        int maximum = type.durability.enabled ? Math.max(1, type.durability.maximum) : 0;
        PsBlockKey key = PsBlockKey.of(block.getWorld(), block.getX(), block.getY(), block.getZ());
        PsBlockState state = new PsBlockState(
                key,
                canonical,
                maximum,
                maximum,
                snapshot.get().ownerName(),
                "",
                "",
                snapshot.get().radiusX(),
                snapshot.get().radiusY(),
                snapshot.get().radiusZ()
        );
        store.put(state);
        if (block.getWorld() != null) {
            holograms.attach(block.getWorld(), state, type);
        }
        if (persistence != null) {
            persistence.saveState(state);
        }
        return state;
    }

    private void applyDurabilityDamage(Block block, PsProtectionTypeDefinition type, int damage, String dynamiteId) {
        PsBlockState state = resolveStoredState(block.getWorld(), block);
        if (state == null) {
            Optional<String> alias = types.resolveAlias(block);
            if (alias.isEmpty()) {
                trace.skipped("applyDurabilityDamage: нет state и alias для "
                        + block.getX() + "," + block.getY() + "," + block.getZ());
                return;
            }
            state = createTrackedState(block, type, alias.get());
        }
        if (state == null) {
            trace.skipped("applyDurabilityDamage: не создан state для "
                    + block.getX() + "," + block.getY() + "," + block.getZ());
            return;
        }
        if (!type.durability.enabled) {
            bridge.deleteRegion(block);
            lifecycle.onRemove(block);
            block.setType(org.bukkit.Material.AIR, false);
            return;
        }
        trace.blockBefore(state, dynamiteId);
        state.applyDamage(damage);
        trace.blockAfter(state, damage, dynamiteId);
        PsBlockKey key = state.key();
        holograms.refresh(state, type);
        if (persistence != null) {
            persistence.saveState(state);
        }
        if (state.isBroken()) {
            bridge.deleteRegion(block);
            lifecycle.onRemove(block);
            block.setType(org.bukkit.Material.AIR, false);
            persistence.removeState(key);
        }
    }

    private double resolveProximityRadius(PsDurabilitySettings durability, DynamiteDefinition dynamite) {
        if (durability.proximityRadius >= 0.0) {
            return durability.proximityRadius;
        }
        return Math.max(8.0, dynamite.explosion.radius + 2.0);
    }

    private int resolveDamage(PsExplosionBreakSettings explosion, String dynamiteId) {
        Map<String, Object> map = explosion.onlyDynamiteTypes;
        if (map == null || map.isEmpty()) {
            return 1;
        }
        if (dynamiteId == null || dynamiteId.isBlank()) {
            return 0;
        }
        Integer damage = parseDamageValue(map.get(dynamiteId));
        if (damage == null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(dynamiteId)) {
                    damage = parseDamageValue(entry.getValue());
                    break;
                }
            }
        }
        if (damage == null) {
            return 0;
        }
        return Math.max(0, damage);
    }

    private static Integer parseDamageValue(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number number) {
            return number.intValue();
        }
        if (raw instanceof String text) {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException exception) {
                return null;
            }
        }
        return null;
    }

}
