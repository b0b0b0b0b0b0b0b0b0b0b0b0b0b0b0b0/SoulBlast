package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.config.ExplosionLimits;
import bm.b0b0b0.SoulBlast.config.GeneralSettings;

public final class TsarBombRules {

    public static final String ID = "last_pyre";

    private TsarBombRules() {
    }

    public static boolean isTsar(DynamiteDefinition dynamite) {
        return dynamite != null && ID.equals(dynamite.id);
    }

    public static boolean allowsHellFill(DynamiteDefinition dynamite) {
        return isTsar(dynamite);
    }

    public static int blockCap(ExplosionLimits limits, DynamiteDefinition dynamite, GeneralSettings general) {
        int base = limits.maxBlocksPerExplosion();
        if (!isTsar(dynamite)) {
            return base;
        }
        int scaled = Math.max(base, (int) (base * dynamite.explosion.blockBudgetMultiplier));
        if (!"GRIEF".equalsIgnoreCase(general.destructionMode)) {
            return scaled;
        }
        if (general.griefUnlimitedBlocks) {
            float radius = dynamite.explosion.radius * 1.05f;
            return Math.max(scaled, (int) Math.ceil((4.0 / 3.0) * Math.PI * radius * radius * radius));
        }
        return scaled;
    }

    public static int craterCap(ExplosionLimits limits, DynamiteDefinition dynamite) {
        int base = limits.maxCraterFillBlocksPerExplosion();
        if (!isTsar(dynamite)) {
            return base;
        }
        return (int) (base * 1.35f);
    }

    public static float visualExplosionPower(DynamiteDefinition dynamite) {
        if (!isTsar(dynamite)) {
            return 0f;
        }
        float radius = dynamite.explosion.radius;
        float power = dynamite.explosion.power;
        return Math.min(18.0f, 4.0f + radius * 0.16f + power * 0.1f);
    }

}
