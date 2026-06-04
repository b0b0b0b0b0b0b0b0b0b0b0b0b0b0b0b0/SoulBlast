package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.SoulBlast;
import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.ps.PsModule;
import bm.b0b0b0.SoulBlast.ps.service.PsDurabilityTrace;
import bm.b0b0b0.SoulBlast.service.region.RegionProtectionMessenger;
import bm.b0b0b0.SoulBlast.service.region.RegionProtectionService;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;

public final class PrimedDynamiteDetonationService {

    public enum DetonationResult {
        QUEUED,
        REGION_BLOCKED,
        COOLDOWN_BLOCKED,
        NO_QUEUE,
        ALREADY_TRIGGERED
    }

    private final SoulBlast plugin;
    private final RegionProtectionService regionProtection;
    private final RegionProtectionMessenger regionMessages;
    private final DynamiteCooldownService cooldownService;
    private final DynamiteCooldownMessenger cooldownMessages;
    private final PrimedDynamiteService primedService;

    public PrimedDynamiteDetonationService(
            SoulBlast plugin,
            RegionProtectionService regionProtection,
            RegionProtectionMessenger regionMessages,
            DynamiteCooldownService cooldownService,
            DynamiteCooldownMessenger cooldownMessages,
            PrimedDynamiteService primedService
    ) {
        this.plugin = plugin;
        this.regionProtection = regionProtection;
        this.regionMessages = regionMessages;
        this.cooldownService = cooldownService;
        this.cooldownMessages = cooldownMessages;
        this.primedService = primedService;
    }

    public DetonationResult detonate(TNTPrimed primed, DynamiteDefinition definition) {
        var sessionOpt = primedService.session(primed.getUniqueId());
        if (sessionOpt.isPresent() && !sessionOpt.get().tryMarkDetonation()) {
            return DetonationResult.ALREADY_TRIGGERED;
        }
        Player player = primed.getSource() instanceof Player igniter ? igniter : null;
        RegionProtectionService.RegionCheckResult regionCheck = regionProtection.evaluate(
                primed.getLocation(),
                definition,
                player
        );
        if (!regionCheck.permitted()) {
            if (player != null) {
                regionMessages.sendBlocked(player, regionCheck, definition);
            }
            return DetonationResult.REGION_BLOCKED;
        }
        if (player != null && cooldownMessages.blockUse(player, definition)) {
            return DetonationResult.COOLDOWN_BLOCKED;
        }
        ExplosionQueueService queue = plugin.getExplosionQueueService();
        if (queue == null) {
            return DetonationResult.NO_QUEUE;
        }
        Location blastCenter = ExplosionCenter.snap(primed.getLocation());
        traceDetonation(player, blastCenter, definition.id);
        Entity source = primed.getSource();
        try {
            ExplosionPresentationEffects.playFuseEnd(blastCenter, definition);
            if ("crying_souls".equals(definition.id)) {
                ExplosionPresentationEffects.playObsidianSiegeBurst(blastCenter, definition);
            }
        } catch (Throwable failure) {
            plugin.getLogger().warning("Detonation effects skipped: " + failure.getMessage());
        }
        queue.enqueue(blastCenter, definition, source, true);
        if (player != null) {
            cooldownService.record(player, definition, DynamiteCooldownService.CooldownKind.USE);
        }
        primed.remove();
        primedService.removeSession(primed.getUniqueId());
        return DetonationResult.QUEUED;
    }

    private void traceDetonation(Player player, Location center, String dynamiteId) {
        PsModule psModule = plugin.getPsModule();
        if (psModule == null || !psModule.active()) {
            return;
        }
        PsDurabilityTrace trace = psModule.durabilityTrace();
        if (trace != null) {
            trace.dynamiteDetonated(player, center, dynamiteId, psModule.blockStore());
        }
    }

}
