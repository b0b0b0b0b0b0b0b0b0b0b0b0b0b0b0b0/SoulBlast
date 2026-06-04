package bm.b0b0b0.SoulBlast.config;

public record ExplosionLimits(
        int maxBlocksPerExplosion,
        int maxBlocksPerExplosionTick,
        int maxSamplingRays,
        int maxSamplingStepsPerTick,
        int maxCraterFillBlocksPerExplosion
) {

    public static ExplosionLimits from(GeneralSettings general) {
        if ("GRIEF".equalsIgnoreCase(general.destructionMode)) {
            return new ExplosionLimits(
                    pick(general.griefMaxBlocksPerExplosion, 3200),
                    pick(general.griefMaxBlocksPerExplosionTick, 450),
                    pick(general.griefMaxSamplingRays, 8192),
                    pick(general.griefMaxSamplingStepsPerTick, 16_000),
                    craterFillBudget(general, true)
            );
        }
        return new ExplosionLimits(
                general.maxBlocksPerExplosion,
                general.maxBlocksPerExplosionTick,
                general.maxSamplingRays,
                general.maxSamplingStepsPerTick,
                craterFillBudget(general, false)
        );
    }

    private static int craterFillBudget(GeneralSettings general, boolean grief) {
        if (general.maxCraterFillBlocksPerExplosion > 0) {
            return general.maxCraterFillBlocksPerExplosion;
        }
        if (grief && general.griefMaxCraterFillBlocksPerExplosion > 0) {
            return general.griefMaxCraterFillBlocksPerExplosion;
        }
        int explosionCap = grief
                ? pick(general.griefMaxBlocksPerExplosion, 3200)
                : general.maxBlocksPerExplosion;
        return Math.max(4000, explosionCap / 2);
    }

    private static int pick(int value, int fallback) {
        return value > 0 ? value : fallback;
    }

}
