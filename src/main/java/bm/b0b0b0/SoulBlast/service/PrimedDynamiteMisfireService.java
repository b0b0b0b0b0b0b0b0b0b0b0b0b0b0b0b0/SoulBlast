package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.config.FuseMisfireSettings;
import bm.b0b0b0.SoulBlast.message.MessageService;
import bm.b0b0b0.SoulBlast.model.PrimedDynamiteSession;
import bm.b0b0b0.SoulBlast.util.PluginKeys;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PrimedDynamiteMisfireService {

    private static final int DUD_FUSE_TICKS = 72000;

    private final JavaPlugin plugin;
    private final FuseMisfireResolver resolver;
    private final PrimedDynamiteService primedService;
    private final PrimedDynamiteDetonationService detonationService;
    private final MessageService messages;
    private final PluginKeys keys;

    public PrimedDynamiteMisfireService(
            JavaPlugin plugin,
            FuseMisfireResolver resolver,
            PrimedDynamiteService primedService,
            PrimedDynamiteDetonationService detonationService,
            MessageService messages,
            PluginKeys keys
    ) {
        this.plugin = plugin;
        this.resolver = resolver;
        this.primedService = primedService;
        this.detonationService = detonationService;
        this.messages = messages;
        this.keys = keys;
    }

    public boolean shouldEnterDudOnPrime(PrimedDynamiteSession session, DynamiteDefinition definition) {
        if (session.isDudActive()) {
            return true;
        }
        if (!session.isPendingDud()) {
            return false;
        }
        return resolver.resolve(definition).enabled;
    }

    public void enterDudState(TNTPrimed primed, PrimedDynamiteSession session) {
        session.setDudActive(true);
        session.setPendingDud(false);
        session.setDudDisposalTicksRemaining(resolver.dudDisposalTicks());
        session.rememberLocation(primed.getLocation());
        applyDudEntityState(primed);
        FuseMisfireSettings settings = resolver.resolve(session.getDefinition());
        FuseMisfireEffects.playDudTrigger(primed.getLocation(), settings);
        scheduleDudReinforce(session);
    }

    public void restoreDudEntity(TNTPrimed primed, PrimedDynamiteSession session) {
        session.rememberLocation(primed.getLocation());
        applyDudEntityState(primed);
    }

    public void disposeDud(PrimedDynamiteSession session) {
        session.setDudActive(false);
        session.setPendingDud(false);
        session.setDudDisposalTicksRemaining(0);
        session.bumpDudReinforceGeneration();
    }

    public boolean tickDudDisposal(TNTPrimed primed, PrimedDynamiteSession session) {
        if (!session.isDudActive() || session.getDudDisposalTicksRemaining() <= 0) {
            return false;
        }
        if (session.tickDudDisposal() > 0) {
            return false;
        }
        expireDud(primed, session);
        return true;
    }

    public void maintainDudFuse(TNTPrimed primed, PrimedDynamiteSession session) {
        if (!session.isDudActive()) {
            return;
        }
        session.rememberLocation(primed.getLocation());
        if (primed.getFuseTicks() < 6000) {
            primed.setFuseTicks(DUD_FUSE_TICKS);
        }
        primed.getPersistentDataContainer().set(keys.primedDud, PersistentDataType.BYTE, (byte) 1);
        FuseMisfireSettings settings = resolver.resolve(session.getDefinition());
        if (settings.dudAmbientParticles && primed.getTicksLived() % 3 == 0) {
            FuseMisfireEffects.playDudAmbient(primed.getLocation());
        }
    }

    public boolean isWarningPhase(PrimedDynamiteSession session, TNTPrimed primed, DynamiteDefinition definition) {
        if (session.isDudActive() || !session.isPendingDud()) {
            return false;
        }
        FuseMisfireSettings settings = resolver.resolve(definition);
        if (!settings.enabled) {
            return false;
        }
        return primed.getFuseTicks() <= Math.max(1, settings.warningTicks);
    }

    public ActivateAttemptResult tryActivate(Player player, TNTPrimed primed) {
        Optional<PrimedDynamiteSession> sessionOpt = primedService.session(primed.getUniqueId());
        if (sessionOpt.isEmpty()) {
            return ActivateAttemptResult.NOT_PRIMED;
        }
        PrimedDynamiteSession session = sessionOpt.get();
        if (!session.isDudActive()) {
            return ActivateAttemptResult.NOT_DUD;
        }
        DynamiteDefinition definition = session.getDefinition();
        UUID placerId = resolvePlacerId(primed, session);
        if (placerId == null) {
            return ActivateAttemptResult.NO_OWNER;
        }
        if (!placerId.equals(player.getUniqueId())) {
            return ActivateAttemptResult.NOT_OWNER;
        }
        FuseMisfireResolver.ActivateOutcome outcome = resolver.rollActivate(definition);
        if (outcome == FuseMisfireResolver.ActivateOutcome.DETONATE) {
            clearDudEntityState(primed);
            PrimedDynamiteDetonationService.DetonationResult detonation = detonationService.detonate(primed, definition);
            if (detonation == PrimedDynamiteDetonationService.DetonationResult.QUEUED) {
                notify(player, "fuse-misfire-detonate", definition);
                return ActivateAttemptResult.DETONATED;
            }
            return ActivateAttemptResult.BLOCKED;
        }
        if (outcome == FuseMisfireResolver.ActivateOutcome.RELIGHT) {
            relight(primed, session, definition);
            notify(player, "fuse-misfire-relight", definition);
            return ActivateAttemptResult.RELIGHT;
        }
        notify(player, "fuse-misfire-fizzle", definition);
        return ActivateAttemptResult.FIZZLE;
    }

    public void rollPendingDudOnSpawn(PrimedDynamiteSession session, DynamiteDefinition definition) {
        session.setPendingDud(resolver.rollPendingDud(definition));
        session.setDudActive(false);
    }

    private void relight(TNTPrimed primed, PrimedDynamiteSession session, DynamiteDefinition definition) {
        disposeDud(session);
        session.setPendingDud(resolver.rollPendingDud(definition));
        clearDudEntityState(primed);
        primed.setFuseTicks(definition.fuseTicks);
        session.setFuseTicksRemaining(definition.fuseTicks);
        if (definition.disableGravity) {
            primed.setGravity(false);
        }
    }

    private void applyDudEntityState(TNTPrimed primed) {
        primed.setFuseTicks(DUD_FUSE_TICKS);
        primed.setVelocity(new Vector(0, 0, 0));
        primed.setGravity(false);
        primed.getPersistentDataContainer().set(keys.primedDud, PersistentDataType.BYTE, (byte) 1);
    }

    private void clearDudEntityState(TNTPrimed primed) {
        primed.getPersistentDataContainer().remove(keys.primedDud);
        DynamiteDefinition definition = primedService.resolvePrimed(primed).orElse(null);
        if (definition != null && !definition.disableGravity) {
            primed.setGravity(true);
        }
    }

    private void scheduleDudReinforce(PrimedDynamiteSession session) {
        int generation = session.bumpDudReinforceGeneration();
        plugin.getServer().getScheduler().runTask(plugin, () -> reinforceDudEntity(session, generation));
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> reinforceDudEntity(session, generation), 1L);
    }

    private void reinforceDudEntity(PrimedDynamiteSession session, int generation) {
        if (!session.isDudActive() || session.getDudReinforceGeneration() != generation) {
            return;
        }
        Entity entity = plugin.getServer().getEntity(session.getEntityId());
        if (entity instanceof TNTPrimed primed && primed.isValid() && !primed.isDead()) {
            applyDudEntityState(primed);
            return;
        }
        primedService.respawnDudEntity(session);
    }

    private UUID resolvePlacerId(TNTPrimed primed, PrimedDynamiteSession session) {
        if (session.getPlacerId() != null) {
            return session.getPlacerId();
        }
        String raw = primed.getPersistentDataContainer().get(keys.primedPlacerId, PersistentDataType.STRING);
        if (raw == null) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private void expireDud(TNTPrimed primed, PrimedDynamiteSession session) {
        DynamiteDefinition definition = session.getDefinition();
        UUID placerId = session.getPlacerId();
        disposeDud(session);
        if (!primed.isDead()) {
            primed.remove();
        }
        primedService.disposeSession(session);
        if (placerId == null) {
            return;
        }
        Player player = plugin.getServer().getPlayer(placerId);
        if (player != null) {
            notify(player, "fuse-misfire-expired", definition);
        }
    }

    private void notify(Player player, String key, DynamiteDefinition definition) {
        messages.send(player, key, Map.of("display", definition.item.displayName));
    }

    public enum ActivateAttemptResult {
        DETONATED,
        RELIGHT,
        FIZZLE,
        NOT_DUD,
        NOT_PRIMED,
        NOT_OWNER,
        NO_OWNER,
        BLOCKED
    }

}
