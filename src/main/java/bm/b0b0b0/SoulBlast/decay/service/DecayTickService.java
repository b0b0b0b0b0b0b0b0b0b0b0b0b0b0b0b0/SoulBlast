package bm.b0b0b0.SoulBlast.decay.service;

import bm.b0b0b0.SoulBlast.decay.config.DecayGeneralSettings;
import bm.b0b0b0.SoulBlast.decay.repository.DecayingBlockStore;

public final class DecayTickService {

    private final DecayGeneralSettings general;
    private final DecayingBlockStore store;
    private final DecayCrackBroadcaster crackBroadcaster;
    private final DecayRegenerationService regenerationService;
    private int regenerationCounter;
    private int crackRefreshCounter;

    public DecayTickService(
            DecayGeneralSettings general,
            DecayingBlockStore store,
            DecayCrackBroadcaster crackBroadcaster,
            DecayRegenerationService regenerationService
    ) {
        this.general = general;
        this.store = store;
        this.crackBroadcaster = crackBroadcaster;
        this.regenerationService = regenerationService;
    }

    public void tick() {
        if (!general.enabled || store.isEmpty()) {
            return;
        }
        crackRefreshCounter++;
        if (crackRefreshCounter >= general.crackRefreshTicks) {
            crackRefreshCounter = 0;
            crackBroadcaster.refreshAll();
        }
        crackBroadcaster.broadcastTick();
        regenerationCounter++;
        if (regenerationCounter >= general.regenerationIntervalTicks) {
            regenerationCounter = 0;
            regenerationService.tick();
        }
    }

}
