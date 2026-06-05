package bm.b0b0b0.SoulBlast.model;

import org.bukkit.Material;

import java.util.EnumMap;
import java.util.Map;

public final class ExplosionJobDiagnostics {

    private int plannedBreak;
    private int plannedMask;
    private int maskReach;
    private int maskColumns;
    private int applied;
    private int regionBlocked;
    private int skippedUnloaded;
    private int skippedOutOfWorld;
    private int skippedOther;
    private final EnumMap<ExplosionBlockAction, Integer> appliedByAction = new EnumMap<>(ExplosionBlockAction.class);
    private final EnumMap<Material, Integer> replacedMaterials = new EnumMap<>(Material.class);

    public void setPlannedBreak(int count) {
        plannedBreak = count;
    }

    public void setPlannedMask(int count) {
        plannedMask = count;
    }

    public void setMaskMeta(int reach, int columns) {
        maskReach = reach;
        maskColumns = columns;
    }

    public int maskReach() {
        return maskReach;
    }

    public int maskColumns() {
        return maskColumns;
    }

    public void recordApplied(ExplosionBlockAction action, Material material) {
        applied++;
        appliedByAction.merge(action, 1, Integer::sum);
        if (action == ExplosionBlockAction.REPLACE || action == ExplosionBlockAction.PLACE) {
            replacedMaterials.merge(material, 1, Integer::sum);
        }
    }

    public void recordRegionBlocked() {
        regionBlocked++;
    }

    public void recordSkippedUnloaded() {
        skippedUnloaded++;
    }

    public void recordSkippedOutOfWorld() {
        skippedOutOfWorld++;
    }

    public void recordSkippedOther() {
        skippedOther++;
    }

    public int plannedBreak() {
        return plannedBreak;
    }

    public int plannedMask() {
        return plannedMask;
    }

    public int applied() {
        return applied;
    }

    public int regionBlocked() {
        return regionBlocked;
    }

    public int skippedUnloaded() {
        return skippedUnloaded;
    }

    public int skippedOutOfWorld() {
        return skippedOutOfWorld;
    }

    public int skippedOther() {
        return skippedOther;
    }

    public int applied(ExplosionBlockAction action) {
        return appliedByAction.getOrDefault(action, 0);
    }

    public String maskMaterialSummary() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Material, Integer> entry : replacedMaterials.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(entry.getKey().name()).append('=').append(entry.getValue());
        }
        return builder.length() == 0 ? "none" : builder.toString();
    }

}
