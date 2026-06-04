package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.config.FuseRecallSettings;
import bm.b0b0b0.SoulBlast.config.GlowSettings;
import bm.b0b0b0.SoulBlast.config.HologramSettings;
import bm.b0b0b0.SoulBlast.message.MessageService;
import bm.b0b0b0.SoulBlast.model.PrimedDynamiteSession;
import bm.b0b0b0.SoulBlast.repository.DynamiteRegistry;
import bm.b0b0b0.SoulBlast.util.PluginKeys;
import bm.b0b0b0.SoulBlast.util.TextUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PrimedDynamiteService {

    private static final ThreadLocal<DynamiteDefinition> pendingSpawnDefinition = new ThreadLocal<>();

    public static Optional<DynamiteDefinition> pendingSpawnDefinition() {
        return Optional.ofNullable(pendingSpawnDefinition.get());
    }

    private final JavaPlugin plugin;
    private final PluginKeys keys;
    private final DynamiteRegistry registry;
    private final DynamiteVisualService visualService;
    private final FuseLightningService fuseLightningService;
    private final MessageService messages;
    private final FuseRecallSettings fuseRecall;
    private PrimedDynamiteMisfireService misfireService;
    private PrimedDynamiteDetonationService detonationService;
    private TsarWarheadService warheadService;
    private final Map<UUID, PrimedDynamiteSession> sessions = new HashMap<>();

    public PrimedDynamiteService(
            JavaPlugin plugin,
            PluginKeys keys,
            DynamiteRegistry registry,
            DynamiteVisualService visualService,
            FuseLightningService fuseLightningService,
            MessageService messages,
            FuseRecallSettings fuseRecall
    ) {
        this.plugin = plugin;
        this.keys = keys;
        this.registry = registry;
        this.visualService = visualService;
        this.fuseLightningService = fuseLightningService;
        this.messages = messages;
        this.fuseRecall = fuseRecall;
    }

    public void bindMisfireService(PrimedDynamiteMisfireService misfireService) {
        this.misfireService = misfireService;
    }

    public void bindDetonationService(PrimedDynamiteDetonationService detonationService) {
        this.detonationService = detonationService;
    }

    public void bindWarheadService(TsarWarheadService warheadService) {
        this.warheadService = warheadService;
    }

    public Optional<PrimedDynamiteSession> session(UUID entityId) {
        return Optional.ofNullable(sessions.get(entityId));
    }

    public CollectionView sessions() {
        return new CollectionView(sessions.values());
    }

    public TNTPrimed spawnPrimed(Location location, DynamiteDefinition definition, org.bukkit.entity.Entity source) {
        World world = location.getWorld();
        if (world == null) {
            return null;
        }
        pendingSpawnDefinition.set(definition);
        TNTPrimed primed;
        try {
            primed = (TNTPrimed) world.spawnEntity(location, EntityType.TNT);
        } finally {
            pendingSpawnDefinition.remove();
        }
        primed.setFuseTicks(definition.fuseTicks);
        primed.getPersistentDataContainer().set(keys.primedDynamiteId, PersistentDataType.STRING, definition.id);
        if (definition.disableGravity) {
            primed.setGravity(false);
            Vector velocity = primed.getVelocity();
            velocity.setY(definition.upwardVelocity);
            primed.setVelocity(velocity);
        }
        if (definition.glow.enabled) {
            visualService.applyPrimedLook(primed, definition);
        }
        UUID placerId = resolvePlacerId(source);
        if (source instanceof Entity entity) {
            primed.setSource(entity);
        }
        if (placerId != null) {
            primed.getPersistentDataContainer().set(keys.primedPlacerId, PersistentDataType.STRING, placerId.toString());
        }
        PrimedDynamiteSession session = new PrimedDynamiteSession(primed, definition.id, definition, placerId);
        if (misfireService != null) {
            misfireService.rollPendingDudOnSpawn(session, definition);
        }
        attachHologram(session, primed);
        sessions.put(primed.getUniqueId(), session);
        return primed;
    }

    public TNTPrimed spawnWarhead(
            Location location,
            DynamiteDefinition definition,
            org.bukkit.entity.Entity source,
            Vector velocity,
            int fuseTicks
    ) {
        TNTPrimed primed = spawnPrimed(location, definition, source);
        if (primed == null) {
            return null;
        }
        primed.setFuseTicks(fuseTicks);
        primed.setVelocity(velocity);
        return primed;
    }

    public void respawnDudEntity(PrimedDynamiteSession session) {
        Location at = session.lastLocation();
        if (at == null || at.getWorld() == null) {
            return;
        }
        DynamiteDefinition definition = session.getDefinition();
        UUID oldId = session.getEntityId();
        sessions.remove(oldId);
        TNTPrimed primed = (TNTPrimed) at.getWorld().spawnEntity(at, EntityType.TNT);
        primed.getPersistentDataContainer().set(keys.primedDynamiteId, PersistentDataType.STRING, definition.id);
        if (session.getPlacerId() != null) {
            primed.getPersistentDataContainer().set(keys.primedPlacerId, PersistentDataType.STRING, session.getPlacerId().toString());
        }
        primed.getPersistentDataContainer().set(keys.primedDud, PersistentDataType.BYTE, (byte) 1);
        if (definition.glow.enabled) {
            visualService.applyPrimedLook(primed, definition);
        }
        session.updateEntityId(primed.getUniqueId());
        session.rememberLocation(at);
        sessions.put(primed.getUniqueId(), session);
        if (misfireService != null) {
            misfireService.enterDudState(primed, session);
        }
    }

    public void removeSession(UUID entityId) {
        PrimedDynamiteSession session = sessions.remove(entityId);
        if (session == null) {
            return;
        }
        fuseLightningService.cancelSession(session);
        if (session.getHologram() != null && !session.getHologram().isDead()) {
            session.getHologram().remove();
        }
    }

    public void tickGlowAndHologram() {
        for (PrimedDynamiteSession session : sessions.values()) {
            org.bukkit.entity.Entity entity = plugin.getServer().getEntity(session.getEntityId());
            if (!(entity instanceof TNTPrimed primed) || primed.isDead()) {
                removeSession(session.getEntityId());
                continue;
            }
            session.rememberLocation(primed.getLocation());
            if (misfireService != null) {
                misfireService.maintainDudFuse(primed, session);
            }
            updateHologram(session, primed);
            applyGlowAnimation(session, primed);
            visualService.tickParticles(primed, session.getDefinition(), session.getGlowPhase());
            if (!session.isDudActive()
                    && misfireService != null
                    && misfireService.isWarningPhase(session, primed, session.getDefinition())
                    && session.getGlowPhase() % 4 == 0) {
                FuseMisfireEffects.playMisfireWarning(primed.getLocation());
            }
            fuseLightningService.tickPrimed(
                    session,
                    primed,
                    session.getDefinition().explosion.effects.fuseLightning
            );
            if (warheadService != null) {
                warheadService.tickSeparation(session, primed);
            }
            if (detonationService != null
                    && !session.isDudActive()
                    && primed.getFuseTicks() <= 0) {
                detonationService.detonate(primed, session.getDefinition());
            }
        }
    }

    private void attachHologram(PrimedDynamiteSession session, TNTPrimed primed) {
        HologramSettings hologram = session.getDefinition().hologram;
        if (!hologram.enabled) {
            return;
        }
        Location at = primed.getLocation().clone().add(0, hologram.offsetY, 0);
        TextDisplay display = (TextDisplay) primed.getWorld().spawnEntity(at, EntityType.TEXT_DISPLAY);
        display.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
        display.setSeeThrough(true);
        display.setShadowed(false);
        display.text(LegacyComponentSerializer.legacyAmpersand().deserialize(formatHologram(session, primed)));
        session.setHologram(display);
    }

    private void updateHologram(PrimedDynamiteSession session, TNTPrimed primed) {
        TextDisplay display = session.getHologram();
        if (display == null || display.isDead()) {
            return;
        }
        Location target = primed.getLocation().clone().add(0, session.getDefinition().hologram.offsetY, 0);
        display.teleport(target);
        display.text(LegacyComponentSerializer.legacyAmpersand().deserialize(
                formatHologram(session, primed)
        ));
    }

    private String formatHologram(PrimedDynamiteSession session, TNTPrimed primed) {
        DynamiteDefinition definition = session.getDefinition();
        HologramSettings settings = definition.hologram;
        Map<String, String> placeholders = Map.of(
                "fuse", String.format("%.1f", primed.getFuseTicks() / 20.0),
                "display", TextUtil.apply(definition.item.displayName, Map.of("dyn_id", definition.id))
        );
        if (session.isDudActive()) {
            StringBuilder dud = new StringBuilder(messages.format("fuse-hologram-misfire-active", placeholders));
            dud.append('\n').append(messages.format("fuse-hologram-misfire-idle", Map.of()));
            dud.append('\n').append(messages.format("fuse-hologram-misfire-hint", Map.of()));
            if (fuseRecall.enabled && session.getPlacerId() != null) {
                dud.append('\n').append(messages.format("fuse-hologram-misfire-recall", Map.of()));
            }
            return dud.toString();
        }
        String lineName = pickLine(settings.lineName, "fuse-hologram-name", placeholders);
        String lineTimer = pickLine(settings.lineTimer, "fuse-hologram-timer", placeholders);
        StringBuilder text = new StringBuilder(lineName).append('\n').append(lineTimer);
        if (misfireService != null && misfireService.isWarningPhase(session, primed, definition)) {
            text.append('\n').append(messages.format("fuse-hologram-misfire-warning", Map.of()));
        }
        if (fuseRecall.enabled && session.getPlacerId() != null) {
            text.append('\n').append(messages.format("fuse-hologram-recall", Map.of()));
        }
        return text.toString();
    }

    private String pickLine(String override, String messageKey, Map<String, String> placeholders) {
        if (override != null && !override.isBlank()) {
            return TextUtil.apply(override, placeholders);
        }
        return messages.format(messageKey, placeholders);
    }

    private UUID resolvePlacerId(Entity source) {
        if (source instanceof Player player) {
            return player.getUniqueId();
        }
        if (source instanceof TNTPrimed primed) {
            String raw = primed.getPersistentDataContainer().get(keys.primedPlacerId, PersistentDataType.STRING);
            if (raw != null) {
                try {
                    return UUID.fromString(raw);
                } catch (IllegalArgumentException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private void applyGlowAnimation(PrimedDynamiteSession session, TNTPrimed primed) {
        GlowSettings glow = session.getDefinition().glow;
        if (!glow.enabled || "NONE".equalsIgnoreCase(glow.animation)) {
            return;
        }
        session.setGlowPhase(session.getGlowPhase() + 1);
        if (session.getGlowPhase() % Math.max(1, glow.animationIntervalTicks) != 0) {
            return;
        }
        if ("RAINBOW".equalsIgnoreCase(glow.animation)) {
            primed.setGlowing(true);
            return;
        }
        if ("PULSE".equalsIgnoreCase(glow.animation)) {
            primed.setGlowing(session.getGlowPhase() % 2 == 0);
        }
    }

    public Optional<DynamiteDefinition> resolvePrimed(TNTPrimed primed) {
        String id = primed.getPersistentDataContainer().get(keys.primedDynamiteId, PersistentDataType.STRING);
        if (id == null) {
            return Optional.empty();
        }
        return registry.find(id);
    }

    public record CollectionView(java.util.Collection<PrimedDynamiteSession> values) {
    }

}
