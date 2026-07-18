package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DynamiteCooldownService {

    public enum CooldownKind {
        PURCHASE,
        USE
    }

    public record CooldownStatus(
            boolean blocked,
            long remainingSeconds,
            String tierId
    ) {
        public static CooldownStatus allowed() {
            return new CooldownStatus(false, 0L, "");
        }
    }

    private final Map<UUID, Map<String, Long>> purchaseExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Long>> useExpiry = new ConcurrentHashMap<>();
    private PlayerCooldownSettings settings = new PlayerCooldownSettings();
    private List<PlayerCooldownTier> sortedTiers = List.of();
    private TsarExplosionGate tsarGate;

    public void bindTsarGate(TsarExplosionGate tsarGate) {
        this.tsarGate = tsarGate;
    }

    public void reload(PlayerCooldownSettings settings) {
        this.settings = settings == null ? new PlayerCooldownSettings() : settings;
        List<PlayerCooldownTier> tiers = new ArrayList<>();
        tiers.addAll(this.settings.tiers);
        tiers.sort((a, b) -> Float.compare(b.minRadius, a.minRadius));
        this.sortedTiers = List.copyOf(tiers);
    }

    public void clearPlayer(UUID playerId) {
        purchaseExpiry.remove(playerId);
        useExpiry.remove(playerId);
    }

    public CooldownStatus status(Player player, DynamiteDefinition dynamite, CooldownKind kind) {
        if (!settings.enabled || player == null || dynamite == null) {
            return CooldownStatus.allowed();
        }
        if (bypassAllowed(player, dynamite)) {
            return CooldownStatus.allowed();
        }
        if (kind == CooldownKind.PURCHASE && !settings.applyOnPurchase) {
            return CooldownStatus.allowed();
        }
        if (kind == CooldownKind.USE && !settings.applyOnUse) {
            return CooldownStatus.allowed();
        }
        ResolvedCooldown resolved = resolve(dynamite, kind);
        if (resolved.seconds() <= 0) {
            return CooldownStatus.allowed();
        }
        long remaining = remainingSeconds(player.getUniqueId(), resolved.storageKey(), kind);
        if (TsarBombRules.isTsar(dynamite) && kind == CooldownKind.USE && tsarGate != null) {
            if (tsarGate.isActive()) {
                return new CooldownStatus(true, Math.max(remaining, TsarExplosionGate.COOLDOWN_SECONDS), resolved.tierId());
            }
            remaining = Math.max(remaining, serverLastPyreRemainingSeconds());
        }
        if (remaining <= 0) {
            return CooldownStatus.allowed();
        }
        return new CooldownStatus(true, remaining, resolved.tierId());
    }

    public void record(Player player, DynamiteDefinition dynamite, CooldownKind kind) {
        if (!settings.enabled || player == null || dynamite == null) {
            return;
        }
        if (bypassAllowed(player, dynamite)) {
            return;
        }
        if (kind == CooldownKind.PURCHASE && !settings.applyOnPurchase) {
            return;
        }
        if (kind == CooldownKind.USE && !settings.applyOnUse) {
            return;
        }
        ResolvedCooldown resolved = resolve(dynamite, kind);
        if (resolved.seconds() <= 0) {
            return;
        }
        long expiresAt = System.currentTimeMillis() + resolved.seconds() * 1000L;
        Map<UUID, Map<String, Long>> store = kind == CooldownKind.PURCHASE ? purchaseExpiry : useExpiry;
        store.computeIfAbsent(player.getUniqueId(), ignored -> new ConcurrentHashMap<>())
                .put(resolved.storageKey(), expiresAt);
    }

    private long serverLastPyreRemainingSeconds() {
        if (tsarGate == null) {
            return 0L;
        }
        return tsarGate.remainingSeconds();
    }

    public static String formatRemaining(long seconds) {
        if (seconds < 60) {
            return seconds + "с";
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            long sec = seconds % 60;
            if (sec == 0) {
                return minutes + "м";
            }
            return minutes + "м " + sec + "с";
        }
        long hours = minutes / 60;
        long min = minutes % 60;
        if (min == 0) {
            return hours + "ч";
        }
        return hours + "ч " + min + "м";
    }

    private long remainingSeconds(UUID playerId, String key, CooldownKind kind) {
        Map<UUID, Map<String, Long>> store = kind == CooldownKind.PURCHASE ? purchaseExpiry : useExpiry;
        Map<String, Long> playerMap = store.get(playerId);
        if (playerMap == null) {
            return 0L;
        }
        Long expiresAt = playerMap.get(key);
        if (expiresAt == null) {
            return 0L;
        }
        long remainingMs = expiresAt - System.currentTimeMillis();
        if (remainingMs <= 0) {
            playerMap.remove(key);
            if (playerMap.isEmpty()) {
                store.remove(playerId);
            }
            return 0L;
        }
        return (remainingMs + 999L) / 1000L;
    }

    private boolean bypassAllowed(Player player, DynamiteDefinition dynamite) {
        if (player == null || dynamite == null) {
            return false;
        }
        if (TsarBombRules.ID.equals(dynamite.id) || dynamiteOverride(dynamite.id).noBypass) {
            return false;
        }
        return player.hasPermission(settings.bypassPermission);
    }

    private PlayerCooldownDynamiteOverride dynamiteOverride(String dynamiteId) {
        if (dynamiteId == null) {
            return new PlayerCooldownDynamiteOverride();
        }
        PlayerCooldownDynamiteOverride override = settings.dynamites.get(dynamiteId);
        return override == null ? new PlayerCooldownDynamiteOverride() : override;
    }

    private ResolvedCooldown resolve(DynamiteDefinition dynamite, CooldownKind kind) {
        PlayerCooldownTier tier = matchTier(dynamite);
        String tierId = tier == null ? "light" : tier.id;
        int seconds = secondsFor(dynamite, kind, tier);
        String storageKey = storageKey(dynamite.id, tierId, kind);
        return new ResolvedCooldown(seconds, tierId, storageKey);
    }

    private int secondsFor(DynamiteDefinition dynamite, CooldownKind kind, PlayerCooldownTier tier) {
        PurchaseSettings purchase = dynamite.purchase;
        int override = kind == CooldownKind.PURCHASE
                ? purchase.purchaseCooldownSeconds
                : purchase.useCooldownSeconds;
        if (override > 0) {
            return override;
        }
        if (override == -1) {
            return 0;
        }
        PlayerCooldownDynamiteOverride dynamiteOverride = settings.dynamites.get(dynamite.id);
        if (dynamiteOverride != null) {
            int fromMap = kind == CooldownKind.PURCHASE
                    ? dynamiteOverride.purchaseSeconds
                    : dynamiteOverride.useSeconds;
            if (fromMap >= 0) {
                return fromMap;
            }
        }
        if (tier == null) {
            return 0;
        }
        return kind == CooldownKind.PURCHASE ? tier.purchaseSeconds : tier.useSeconds;
    }

    private PlayerCooldownTier matchTier(DynamiteDefinition dynamite) {
        float radius = dynamite.explosion.radius;
        String quality = dynamite.explosion.quality;
        for (PlayerCooldownTier tier : sortedTiers) {
            if (tier.matchExtremeQuality && "EXTREME".equalsIgnoreCase(quality)) {
                return tier;
            }
            if (radius >= tier.minRadius) {
                return tier;
            }
        }
        return null;
    }

    private String storageKey(String dynamiteId, String tierId, CooldownKind kind) {
        if (TsarBombRules.ID.equals(dynamiteId)) {
            return dynamiteId + ":" + kind.name();
        }
        if (settings.sharedTierCooldown) {
            return tierId + ":" + kind.name();
        }
        return dynamiteId + ":" + kind.name();
    }

    private record ResolvedCooldown(int seconds, String tierId, String storageKey) {
    }

}
