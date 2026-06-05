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
    private boolean presentationPlayed;
    private boolean hellMaskQueued;
    private boolean tsarOrchestrated;
    private boolean tsarBreakFinished;
    private boolean tsarDrainFinished;
    private int tsarDrainRing;
    private int tsarMaskRing;
    private final LongOpenHashSet tsarMaskKeys = new LongOpenHashSet();
    private final LongOpenHashSet tsarMedallionKeys = new LongOpenHashSet();
    private boolean tsarMedallionQueued;
    private final ExplosionJobDiagnostics diagnostics = new ExplosionJobDiagnostics();

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

    public boolean isPresentationPlayed() {
        return presentationPlayed;
    }

    public void markPresentationPlayed() {
        presentationPlayed = true;
    }

    public boolean isHellMaskQueued() {
        return hellMaskQueued;
    }

    public void markHellMaskQueued() {
        hellMaskQueued = true;
    }

    public boolean isTsarOrchestrated() {
        return tsarOrchestrated;
    }

    public void markTsarOrchestrated() {
        tsarOrchestrated = true;
    }

    public boolean isTsarBreakFinished() {
        return tsarBreakFinished;
    }

    public void markTsarBreakFinished() {
        tsarBreakFinished = true;
    }

    public boolean isTsarDrainFinished() {
        return tsarDrainFinished;
    }

    public void markTsarDrainFinished() {
        tsarDrainFinished = true;
    }

    public int getTsarDrainRing() {
        return tsarDrainRing;
    }

    public void setTsarDrainRing(int tsarDrainRing) {
        this.tsarDrainRing = tsarDrainRing;
    }

    public int getTsarMaskRing() {
        return tsarMaskRing;
    }

    public void setTsarMaskRing(int tsarMaskRing) {
        this.tsarMaskRing = tsarMaskRing;
    }

    public LongOpenHashSet getTsarMaskKeys() {
        return tsarMaskKeys;
    }

    public LongOpenHashSet getTsarMedallionKeys() {
        return tsarMedallionKeys;
    }

    public boolean isTsarMedallionQueued() {
        return tsarMedallionQueued;
    }

    public void markTsarMedallionQueued() {
        tsarMedallionQueued = true;
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

    public ExplosionJobDiagnostics getDiagnostics() {
        return diagnostics;
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
