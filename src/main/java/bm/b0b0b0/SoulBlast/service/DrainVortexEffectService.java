package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.config.ExplosionEffectsSettings;
import bm.b0b0b0.SoulBlast.util.ColorUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public final class DrainVortexEffectService {

    private static final int ARMS = 3;

    private DrainVortexEffectService() {
    }

    public static void start(JavaPlugin plugin, Location center, DynamiteDefinition dynamite) {
        if (plugin == null || center == null || dynamite == null || center.getWorld() == null) {
            return;
        }
        ExplosionEffectsSettings effects = dynamite.explosion.effects;
        if (!effects.drainVortex) {
            return;
        }
        World world = center.getWorld();
        float radius = resolveRadius(dynamite);
        float intensity = Math.max(0.5f, effects.drainVortexIntensity * effects.presentation.intensity);
        int totalTicks = Math.max(12, Math.min(64, effects.drainVortexTicks));
        playOpening(world, center, radius, intensity);
        for (int tick = 0; tick < totalTicks; tick++) {
            int frame = tick;
            plugin.getServer().getScheduler().runTaskLater(
                    plugin,
                    () -> spawnFrame(world, center, frame, totalTicks, radius, intensity, dynamite),
                    tick
            );
        }
        plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> spawnFinale(world, center, radius, intensity, dynamite),
                totalTicks + 1
        );
    }

    public static boolean usesDrainVortex(DynamiteDefinition dynamite) {
        return dynamite != null && dynamite.explosion.effects.drainVortex;
    }

    private static float resolveRadius(DynamiteDefinition dynamite) {
        float liquid = dynamite.explosion.effects.liquidRadius;
        if (liquid > 0.01f) {
            return liquid;
        }
        return Math.max(1.0f, dynamite.explosion.radius);
    }

    private static void playOpening(World world, Location center, float radius, float intensity) {
        Location core = center.clone().add(0.0, 0.55, 0.0);
        play(world, center, "block.bubble_column.whirlpool_ambient", 1.15f * intensity, 0.55f);
        play(world, center, "block.conduit.activate", 0.9f * intensity, 1.55f);
        play(world, center, "entity.phantom.swoop", 0.45f * intensity, 0.35f);
        world.spawnParticle(Particle.REVERSE_PORTAL, core, 6, radius * 0.2, 0.15, radius * 0.2, 0.35);
        world.spawnParticle(Particle.SCULK_SOUL, core, 4, radius * 0.15, 0.12, radius * 0.15, 0.02);
    }

    private static void spawnFrame(
            World world,
            Location center,
            int tick,
            int totalTicks,
            float radius,
            float intensity,
            DynamiteDefinition dynamite
    ) {
        if (world == null || center == null) {
            return;
        }
        double progress = totalTicks <= 1 ? 1.0 : (double) tick / (totalTicks - 1);
        double spin = tick * 0.42;
        double ringRadius = radius * (0.92 - progress * 0.78);
        double coreY = center.getY() + 0.45 + (1.0 - progress) * radius * 0.35;
        double centerX = center.getX() + 0.5;
        double centerZ = center.getZ() + 0.5;
        int points = Math.min(36, 10 + Math.round(radius * 2.2f * intensity));
        int layers = 2 + (int) Math.round(progress * 2.0);
        float pullSpeed = (float) (0.28 + progress * 0.55) * intensity;
        for (int layer = 0; layer < layers; layer++) {
            double layerOffset = layer * 0.55;
            double layerRadius = ringRadius * (1.0 - layer * 0.14);
            double layerY = coreY + layerOffset;
            for (int arm = 0; arm < ARMS; arm++) {
                double armBase = spin + arm * (Math.PI * 2.0 / ARMS) + layer * 0.35;
                for (int point = 0; point < points; point++) {
                    double angle = armBase + (Math.PI * 2.0 * point) / points;
                    double px = centerX + Math.cos(angle) * layerRadius;
                    double pz = centerZ + Math.sin(angle) * layerRadius;
                    Location at = new Location(world, px, layerY, pz);
                    pullToward(world, at, centerX, coreY, centerZ, pullSpeed);
                    if (point % 3 == 0) {
                        world.spawnParticle(Particle.BUBBLE, at, 1, 0.02, 0.02, 0.02, 0.04);
                        world.spawnParticle(Particle.SPLASH, at, 1, 0.02, 0.01, 0.02, 0.08);
                    }
                    if (point % 2 == 0) {
                        world.spawnParticle(Particle.SCULK_SOUL, at, 1, 0.03, 0.03, 0.03, 0.01);
                    }
                    if (point % 4 == 0) {
                        world.spawnParticle(Particle.REVERSE_PORTAL, at, 1, 0.04, 0.04, 0.04, 0.08);
                    }
                }
            }
        }
        spawnColoredVortexDust(world, center, dynamite, tick, totalTicks, radius, intensity);
        if (tick % 6 == 0) {
            play(world, center, "block.bubble_column.whirlpool_ambient", 0.35f + (float) progress * 0.5f, 0.7f + (float) progress * 0.35f);
        }
        if (tick % 10 == 0) {
            play(world, center, "block.conduit.ambient", 0.25f + (float) progress * 0.4f, 1.1f + (float) progress * 0.25f);
        }
    }

    private static void spawnFinale(
            World world,
            Location center,
            float radius,
            float intensity,
            DynamiteDefinition dynamite
    ) {
        Location core = center.clone().add(0.5, 0.5, 0.5);
        play(world, center, "block.bubble_column.whirlpool_ambient", 1.35f * intensity, 0.45f);
        play(world, center, "block.conduit.deactivate", 1.0f * intensity, 0.65f);
        play(world, center, "entity.enderman.teleport", 0.75f * intensity, 0.55f);
        play(world, center, "entity.illusioner.mirror_move", 0.5f * intensity, 1.2f);
        world.spawnParticle(Particle.SONIC_BOOM, core, 1, 0.0, 0.0, 0.0, 0.0);
        world.spawnParticle(Particle.REVERSE_PORTAL, core, 28, radius * 0.22, 0.18, radius * 0.22, 0.45);
        world.spawnParticle(Particle.SCULK_SOUL, core, 22, radius * 0.2, 0.16, radius * 0.2, 0.03);
        world.spawnParticle(Particle.ENCHANT, core, 40, radius * 0.25, 0.2, radius * 0.25, 1.2);
        world.spawnParticle(Particle.SQUID_INK, core, 12, radius * 0.15, 0.12, radius * 0.15, 0.02);
        spawnFlash(world, core, dynamite);
        spawnColoredVortexDust(world, center, dynamite, 1, 1, radius, intensity * 1.4f);
    }

    private static void pullToward(
            World world,
            Location from,
            double targetX,
            double targetY,
            double targetZ,
            float speed
    ) {
        double dx = targetX - from.getX();
        double dy = targetY - from.getY();
        double dz = targetZ - from.getZ();
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 0.001) {
            return;
        }
        world.spawnParticle(
                Particle.ENCHANT,
                from,
                0,
                dx / length,
                dy / length,
                dz / length,
                speed
        );
        world.spawnParticle(
                Particle.PORTAL,
                from,
                0,
                dx / length,
                dy / length,
                dz / length,
                speed * 0.65f
        );
    }

    private static void spawnColoredVortexDust(
            World world,
            Location center,
            DynamiteDefinition dynamite,
            int tick,
            int totalTicks,
            float radius,
            float intensity
    ) {
        Optional<ColorUtil.RgbColor> rgb = ColorUtil.parseRgb(dynamite.glow.colorRgb);
        if (rgb.isEmpty()) {
            return;
        }
        double progress = totalTicks <= 1 ? 1.0 : (double) tick / (totalTicks - 1);
        Location core = center.clone().add(0.5, 0.5 + (1.0 - progress) * 0.4, 0.5);
        int count = Math.max(6, Math.round((14 + radius * 4.0f) * intensity * (1.0f - (float) progress * 0.35f)));
        float spread = radius * (0.35f - (float) progress * 0.22f);
        world.spawnParticle(
                Particle.DUST,
                core,
                count,
                spread,
                spread * 0.7f,
                spread,
                0.0,
                ColorUtil.toDust(rgb.get(), 1.1f * intensity)
        );
    }

    private static void spawnFlash(World world, Location at, DynamiteDefinition dynamite) {
        Color color = ColorUtil.parseRgb(dynamite.glow.colorRgb)
                .map(ColorUtil::toBukkitColor)
                .orElse(Color.fromRGB(25, 35, 110));
        world.spawnParticle(Particle.FLASH, at, 1, 0.0, 0.0, 0.0, 0.0, color);
    }

    private static void play(World world, Location at, String soundKey, float volume, float pitch) {
        Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundKey));
        if (sound != null) {
            world.playSound(at, sound, volume, pitch);
        }
    }

}
