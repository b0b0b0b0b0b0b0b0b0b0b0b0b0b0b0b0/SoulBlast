package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.config.PlayerCooldownDynamiteOverride;
import bm.b0b0b0.SoulBlast.config.PlayerCooldownSettings;
import bm.b0b0b0.SoulBlast.config.PlayerCooldownTier;
import bm.b0b0b0.SoulBlast.config.PurchaseSettings;
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

    public void reload(PlayerCooldownSettings settings) {
        this.settings = settings == null ? new PlayerCooldownSettings() : settings;
        List<PlayerCooldownTier> tiers = new ArrayList<>();
        for (PlayerCooldownTier tier : this.settings.tiers) {
            tiers.add(tier);
        }
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
        if (player.hasPermission(settings.bypassPermission)) {
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
        if (remaining <= 0) {
            return CooldownStatus.allowed();
        }
        return new CooldownStatus(true, remaining, resolved.tierId());
    }

    public void record(Player player, DynamiteDefinition dynamite, CooldownKind kind) {
        if (!settings.enabled || player == null || dynamite == null) {
            return;
        }
        if (player.hasPermission(settings.bypassPermission)) {
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
        if (settings.sharedTierCooldown) {
            return tierId + ":" + kind.name();
        }
        return dynamiteId + ":" + kind.name();
    }

    private record ResolvedCooldown(int seconds, String tierId, String storageKey) {
    }

}
