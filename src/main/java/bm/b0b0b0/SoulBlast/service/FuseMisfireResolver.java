package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.config.FuseMisfireGlobalSettings;
import bm.b0b0b0.SoulBlast.config.FuseMisfireSettings;

import java.util.concurrent.ThreadLocalRandom;

public final class FuseMisfireResolver {

    private FuseMisfireGlobalSettings global = new FuseMisfireGlobalSettings();

    public void reload(FuseMisfireGlobalSettings global) {
        this.global = global;
    }

    public FuseMisfireSettings resolve(DynamiteDefinition definition) {
        if (definition == null || !global.enabled) {
            return disabled();
        }
        FuseMisfireSettings settings = definition.fuseMisfire;
        if (settings == null || !settings.enabled || settings.endChance <= 0.0) {
            return disabled();
        }
        return settings;
    }

    public boolean rollPendingDud(DynamiteDefinition definition) {
        FuseMisfireSettings settings = resolve(definition);
        if (!settings.enabled) {
            return false;
        }
        return ThreadLocalRandom.current().nextDouble() < clampChance(settings.endChance);
    }

    public int dudDisposalTicks() {
        if (!global.enabled || global.dudDisposalSeconds <= 0) {
            return 0;
        }
        return global.dudDisposalSeconds * 20;
    }

    public ActivateOutcome rollActivate(DynamiteDefinition definition) {
        FuseMisfireSettings settings = resolve(definition);
        if (!settings.enabled) {
            return ActivateOutcome.FIZZLE;
        }
        double detonate = clampChance(settings.activateDetonateChance);
        double relight = clampChance(settings.activateRelightChance);
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll < detonate) {
            return ActivateOutcome.DETONATE;
        }
        if (roll < detonate + relight) {
            return ActivateOutcome.RELIGHT;
        }
        return ActivateOutcome.FIZZLE;
    }

    private static FuseMisfireSettings disabled() {
        FuseMisfireSettings settings = new FuseMisfireSettings();
        settings.enabled = false;
        return settings;
    }

    private static double clampChance(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    public enum ActivateOutcome {
        DETONATE,
        RELIGHT,
        FIZZLE
    }

}
