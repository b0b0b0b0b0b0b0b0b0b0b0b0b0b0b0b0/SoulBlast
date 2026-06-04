package bm.b0b0b0.SoulBlast.decay.model;

import bm.b0b0b0.SoulBlast.decay.config.DecayBlockTypeDefinition;
import bm.b0b0b0.SoulBlast.decay.service.DecayCrackSourceIds;
import org.bukkit.Material;

public final class DecayingBlockState {

    private final DecayingBlockKey key;
    private final Material material;
    private final DecayBlockTypeDefinition type;
    private final String breakMode;
    private final String dropMaterial;
    private final String transformInto;
    private final int crackSourceId;
    private float damage;
    private float lastSentClientProgress;
    private long lastDamageGameTime;
    private long lastRegenerationGameTime;
    private boolean crackDirty;

    public DecayingBlockState(
            DecayingBlockKey key,
            Material material,
            DecayBlockTypeDefinition type,
            String breakMode,
            String dropMaterial,
            String transformInto
    ) {
        this.key = key;
        this.material = material;
        this.type = type;
        this.breakMode = breakMode;
        this.dropMaterial = dropMaterial;
        this.transformInto = transformInto;
        this.crackSourceId = DecayCrackSourceIds.allocate();
        this.lastDamageGameTime = System.currentTimeMillis();
        this.lastRegenerationGameTime = lastDamageGameTime;
        this.crackDirty = true;
    }

    public int crackSourceId() {
        return crackSourceId;
    }

    public float lastSentClientProgress() {
        return lastSentClientProgress;
    }

    public void recordSentClientProgress(float progress) {
        lastSentClientProgress = Math.max(0.0f, Math.min(1.0f, progress));
    }

    public void resetClientProgress() {
        lastSentClientProgress = 0.0f;
    }

    public DecayingBlockKey key() {
        return key;
    }

    public Material material() {
        return material;
    }

    public DecayBlockTypeDefinition type() {
        return type;
    }

    public String breakMode() {
        return breakMode;
    }

    public String dropMaterial() {
        return dropMaterial;
    }

    public String transformInto() {
        return transformInto;
    }

    public float damage() {
        return damage;
    }

    public void addDamage(float amount) {
        damage = Math.min(1.0f, damage + amount);
        long now = System.currentTimeMillis();
        lastDamageGameTime = now;
        lastRegenerationGameTime = now;
        crackDirty = true;
    }

    public void reduceDamage(float amount) {
        damage = Math.max(0.0f, damage - amount);
        crackDirty = true;
    }

    public boolean isCrackDirty() {
        return crackDirty;
    }

    public void clearCrackDirty() {
        crackDirty = false;
    }

    public void markCrackDirty() {
        crackDirty = true;
    }

    public long lastDamageGameTime() {
        return lastDamageGameTime;
    }

    public long lastRegenerationGameTime() {
        return lastRegenerationGameTime;
    }

    public void touchRegeneration() {
        lastRegenerationGameTime = System.currentTimeMillis();
    }

    public boolean isBroken() {
        return damage >= 1.0f;
    }

}
