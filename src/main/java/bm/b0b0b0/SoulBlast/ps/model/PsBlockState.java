package bm.b0b0b0.SoulBlast.ps.model;

import java.util.UUID;

public final class PsBlockState {

    private final PsBlockKey key;
    private String typeAlias;
    private int durability;
    private final int maximum;
    private String ownerName;
    private String ownerPrefix;
    private String ownerSuffix;
    private UUID ownerId;
    private int radiusX;
    private int radiusY;
    private int radiusZ;
    private boolean hologramHidden;

    public PsBlockState(
            PsBlockKey key,
            String typeAlias,
            int durability,
            int maximum,
            String ownerName,
            String ownerPrefix,
            String ownerSuffix,
            int radiusX,
            int radiusY,
            int radiusZ
    ) {
        this(
                key,
                typeAlias,
                durability,
                maximum,
                ownerName,
                ownerPrefix,
                ownerSuffix,
                null,
                radiusX,
                radiusY,
                radiusZ,
                false
        );
    }

    public PsBlockState(
            PsBlockKey key,
            String typeAlias,
            int durability,
            int maximum,
            String ownerName,
            String ownerPrefix,
            String ownerSuffix,
            UUID ownerId,
            int radiusX,
            int radiusY,
            int radiusZ,
            boolean hologramHidden
    ) {
        this.key = key;
        this.typeAlias = typeAlias;
        this.durability = durability;
        this.maximum = maximum;
        this.ownerName = ownerName;
        this.ownerPrefix = ownerPrefix;
        this.ownerSuffix = ownerSuffix;
        this.ownerId = ownerId;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.radiusZ = radiusZ;
        this.hologramHidden = hologramHidden;
    }

    public PsBlockKey key() {
        return key;
    }

    public String typeAlias() {
        return typeAlias;
    }

    public int durability() {
        return durability;
    }

    public int maximum() {
        return maximum;
    }

    public String ownerName() {
        return ownerName;
    }

    public String ownerPrefix() {
        return ownerPrefix;
    }

    public String ownerSuffix() {
        return ownerSuffix;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public int radiusX() {
        return radiusX;
    }

    public int radiusY() {
        return radiusY;
    }

    public int radiusZ() {
        return radiusZ;
    }

    public boolean hologramHidden() {
        return hologramHidden;
    }

    public void applyDamage(int amount) {
        if (amount <= 0) {
            return;
        }
        durability = Math.max(0, durability - amount);
    }

    public boolean isBroken() {
        return durability <= 0;
    }

    public void updateOwnerMeta(String ownerName, String ownerPrefix, String ownerSuffix) {
        this.ownerName = ownerName;
        this.ownerPrefix = ownerPrefix;
        this.ownerSuffix = ownerSuffix;
    }

    public void updateOwnerId(UUID ownerId) {
        if (ownerId != null) {
            this.ownerId = ownerId;
        }
    }

    public void updateRadii(int radiusX, int radiusY, int radiusZ) {
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.radiusZ = radiusZ;
    }

    public void updateTypeAlias(String typeAlias) {
        if (typeAlias != null && !typeAlias.isBlank()) {
            this.typeAlias = typeAlias;
        }
    }

    public void setHologramHidden(boolean hologramHidden) {
        this.hologramHidden = hologramHidden;
    }

    public PsBlockState withKey(PsBlockKey newKey) {
        return new PsBlockState(
                newKey,
                typeAlias,
                durability,
                maximum,
                ownerName,
                ownerPrefix,
                ownerSuffix,
                ownerId,
                radiusX,
                radiusY,
                radiusZ,
                hologramHidden
        );
    }

}
