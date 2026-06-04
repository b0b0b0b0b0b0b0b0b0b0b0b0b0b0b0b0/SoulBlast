package bm.b0b0b0.SoulBlast.model;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import org.bukkit.Location;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PrimedDynamiteSession {

    private UUID entityId;
    private Location lastLocation;
    private final String dynamiteId;
    private final DynamiteDefinition definition;
    private final UUID placerId;
    private TextDisplay hologram;
    private int glowPhase;
    private boolean fuseLightningTriggered;
    private boolean warheadsLaunched;
    private boolean pendingDud;
    private boolean dudActive;
    private boolean detonationTriggered;
    private final List<BukkitTask> fuseLightningTasks = new ArrayList<>();

    public PrimedDynamiteSession(TNTPrimed entity, String dynamiteId, DynamiteDefinition definition, UUID placerId) {
        this.entityId = entity.getUniqueId();
        this.dynamiteId = dynamiteId;
        this.definition = definition;
        this.placerId = placerId;
        this.glowPhase = 0;
    }

    public UUID getPlacerId() {
        return placerId;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void updateEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public void rememberLocation(Location location) {
        if (location != null) {
            this.lastLocation = location.clone();
        }
    }

    public Location lastLocation() {
        return lastLocation == null ? null : lastLocation.clone();
    }

    public String getDynamiteId() {
        return dynamiteId;
    }

    public DynamiteDefinition getDefinition() {
        return definition;
    }

    public TextDisplay getHologram() {
        return hologram;
    }

    public void setHologram(TextDisplay hologram) {
        this.hologram = hologram;
    }

    public int getGlowPhase() {
        return glowPhase;
    }

    public void setGlowPhase(int glowPhase) {
        this.glowPhase = glowPhase;
    }

    public boolean isFuseLightningTriggered() {
        return fuseLightningTriggered;
    }

    public void setFuseLightningTriggered(boolean fuseLightningTriggered) {
        this.fuseLightningTriggered = fuseLightningTriggered;
    }

    public List<BukkitTask> getFuseLightningTasks() {
        return fuseLightningTasks;
    }

    public void setFuseLightningTasks(List<BukkitTask> tasks) {
        fuseLightningTasks.clear();
        if (tasks != null) {
            fuseLightningTasks.addAll(tasks);
        }
    }

    public boolean isWarheadsLaunched() {
        return warheadsLaunched;
    }

    public void setWarheadsLaunched(boolean warheadsLaunched) {
        this.warheadsLaunched = warheadsLaunched;
    }

    public boolean isPendingDud() {
        return pendingDud;
    }

    public void setPendingDud(boolean pendingDud) {
        this.pendingDud = pendingDud;
    }

    public boolean isDudActive() {
        return dudActive;
    }

    public void setDudActive(boolean dudActive) {
        this.dudActive = dudActive;
    }

    public boolean tryMarkDetonation() {
        if (detonationTriggered) {
            return false;
        }
        detonationTriggered = true;
        return true;
    }

}
