package bm.b0b0b0.SoulBlast.decay.service;

import bm.b0b0b0.SoulBlast.decay.config.DecayDamageSourcesFileConfig;
import bm.b0b0b0.SoulBlast.decay.config.DecayGeneralSettings;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class DecayDamageResolver {

    private Map<String, String> sources = Map.of();
    private float maxDamagePerHit = 0.16f;
    private DecayGeneralSettings general = new DecayGeneralSettings();

    public void reload(DecayDamageSourcesFileConfig config, DecayGeneralSettings general) {
        this.general = general;
        sources = Map.copyOf(config.types);
        maxDamagePerHit = Math.max(0.06f, Math.min(0.22f, general.maxDamagePerHit));
    }

    public float rollHit(String dynamiteId, float resistance, Location explosionCenter, Block block, float explosionRadius) {
        float distanceMultiplier = explosionDistanceMultiplier(explosionCenter, block, explosionRadius);
        float multiplier = rollMultiplier(resolveSpec(dynamiteId));
        float resistanceFactor = 1.0f / (0.25f + Math.max(0.1f, resistance) * 0.85f);
        float scatter = blockScatterBias(block) * (0.9f + ThreadLocalRandom.current().nextFloat() * 0.2f);
        float raw = multiplier * maxDamagePerHit * resistanceFactor * distanceMultiplier * scatter;
        return Math.min(0.2f, raw);
    }

    private static float blockScatterBias(Block block) {
        int mixed = block.getX() * 7340033 ^ block.getY() * 28657 ^ block.getZ() * 7691;
        return 0.88f + (mixed & 0xFF) / 255.0f * 0.24f;
    }

    public float explosionDistanceMultiplier(Location explosionCenter, Block block, float explosionRadius) {
        if (!general.explosionDistanceFalloffEnabled || explosionCenter == null || block == null || explosionRadius <= 0.01f) {
            return 1.0f;
        }
        if (explosionCenter.getWorld() == null || !explosionCenter.getWorld().equals(block.getWorld())) {
            return 1.0f;
        }
        double blockX = block.getX() + 0.5;
        double blockY = block.getY() + 0.5;
        double blockZ = block.getZ() + 0.5;
        double dx = blockX - explosionCenter.getX();
        double dy = blockY - explosionCenter.getY();
        double dz = blockZ - explosionCenter.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double normalized = Math.min(1.0, distance / explosionRadius);
        float minAtEdge = Math.max(0.0f, Math.min(1.0f, general.explosionMinDamageMultiplierAtEdge));
        float span = 1.0f - minAtEdge;
        float proximity = falloffProximity(normalized);
        return minAtEdge + span * proximity;
    }

    private float falloffProximity(double normalized) {
        if ("LINEAR".equalsIgnoreCase(general.explosionDistanceFalloffCurve)) {
            return (float) (1.0 - normalized);
        }
        double inverted = 1.0 - normalized;
        return (float) (inverted * inverted);
    }

    private String resolveSpec(String dynamiteId) {
        if (dynamiteId != null && sources.containsKey(dynamiteId)) {
            return sources.get(dynamiteId);
        }
        return sources.getOrDefault("DEFAULT", "1");
    }

    private float rollMultiplier(String spec) {
        if (spec == null || spec.isBlank()) {
            return 1.0f;
        }
        String trimmed = spec.trim();
        int dash = trimmed.indexOf('-');
        if (dash > 0) {
            float min = parseFloat(trimmed.substring(0, dash), 1.0f);
            float max = parseFloat(trimmed.substring(dash + 1), min);
            if (max < min) {
                float swap = min;
                min = max;
                max = swap;
            }
            return min + ThreadLocalRandom.current().nextFloat() * (max - min);
        }
        return parseFloat(trimmed, 1.0f);
    }

    private float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

}
