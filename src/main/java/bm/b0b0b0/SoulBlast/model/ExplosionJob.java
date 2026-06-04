package bm.b0b0b0.SoulBlast.model;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.service.ExplosionCenter;
import bm.b0b0b0.SoulBlast.util.BlockCoordPacker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.ArrayDeque;

public final class ExplosionJob {

    private final Location center;
    private final DynamiteDefinition dynamite;
    private final Entity source;
    private final ArrayDeque<BlockTarget> pendingBlocks = new ArrayDeque<>();
    private final ExplosionRaySamplingProgress raySampling = new ExplosionRaySamplingProgress();
    private final LongOpenHashSet physicsEdgeKeys = new LongOpenHashSet();
    private final LongOpenHashSet psDurabilityHitKeys = new LongOpenHashSet();
    private ExplosionJobPhase phase = ExplosionJobPhase.BREAK;

    public ExplosionJob(Location center, DynamiteDefinition dynamite, Entity source) {
        this.center = ExplosionCenter.snap(center);
        this.dynamite = dynamite;
        this.source = source;
    }

    public Location getCenter() {
        return center;
    }

    public DynamiteDefinition getDynamite() {
        return dynamite;
    }

    public Entity getSource() {
        return source;
    }

    public ArrayDeque<BlockTarget> getPendingBlocks() {
        return pendingBlocks;
    }

    public ExplosionRaySamplingProgress getRaySampling() {
        return raySampling;
    }

    public boolean isRaySampling() {
        return raySampling.active || raySampling.rayOverlayActive;
    }

    public boolean isRayBoosted(int x, int y, int z) {
        return raySampling.rayBoosted.contains(BlockCoordPacker.pack(x, y, z));
    }

    public ExplosionJobPhase getPhase() {
        return phase;
    }

    public void setPhase(ExplosionJobPhase phase) {
        this.phase = phase;
    }

    public void clearPhysicsEdgeKeys() {
        physicsEdgeKeys.clear();
    }

    public void addPhysicsEdgeKey(long key) {
        physicsEdgeKeys.add(key);
    }

    public boolean requiresPhysics(BlockTarget target) {
        if (physicsEdgeKeys.isEmpty()) {
            return false;
        }
        return physicsEdgeKeys.contains(BlockCoordPacker.pack(target.x(), target.y(), target.z()));
    }

    public void markPsDurabilityHit(int x, int y, int z) {
        psDurabilityHitKeys.add(BlockCoordPacker.pack(x, y, z));
    }

    public boolean alreadyPsDurabilityHit(int x, int y, int z) {
        return psDurabilityHitKeys.contains(BlockCoordPacker.pack(x, y, z));
    }

    public record BlockTarget(
            int x,
            int y,
            int z,
            ExplosionBlockAction action,
            Material placeMaterial
    ) {
        public BlockTarget(int x, int y, int z) {
            this(x, y, z, ExplosionBlockAction.BREAK, Material.AIR);
        }
    }

}
