package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.FuseMisfireSettings;
import bm.b0b0b0.SoulBlast.util.ColorUtil;
import org.bukkit.*;

import java.util.Locale;

public final class FuseMisfireEffects {

    private FuseMisfireEffects() {
    }

    public static void playDudTrigger(Location center, FuseMisfireSettings settings) {
        if (center == null || settings == null) {
            return;
        }
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        playSound(world, center, settings);
        play(world, center, "entity.tnt.primed", 0.9f, 0.65f);
        burstParticles(world, center.clone().add(0.0, 0.55, 0.0));
    }

    public static void playDudAmbient(Location center) {
        if (center == null) {
            return;
        }
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        Location at = center.clone().add(0.0, 0.45, 0.0);
        world.spawnParticle(Particle.SMOKE, at, 6, 0.22, 0.14, 0.22, 0.012);
        world.spawnParticle(Particle.SMALL_FLAME, at, 4, 0.16, 0.11, 0.16, 0.006);
        world.spawnParticle(Particle.CRIT, at, 8, 0.26, 0.18, 0.26, 0.14);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, at, 3, 0.12, 0.1, 0.12, 0.004);
        world.spawnParticle(Particle.ENCHANT, at, 4, 0.2, 0.14, 0.2, 0.35);
    }

    public static void playMisfireWarning(Location center) {
        if (center == null) {
            return;
        }
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        Location at = center.clone().add(0.0, 0.5, 0.0);
        world.spawnParticle(Particle.SMOKE, at, 5, 0.14, 0.1, 0.14, 0.008);
        world.spawnParticle(Particle.CRIT, at, 6, 0.18, 0.12, 0.18, 0.16);
        world.spawnParticle(Particle.ELECTRIC_SPARK, at, 4, 0.12, 0.1, 0.12, 0.02);
        play(world, center, "block.note_block.pling", 0.55f, 0.45f);
    }

    public static void playFuseTick(Location center, int fuseTicks, String colorRgb) {
        if (center == null || fuseTicks > 60) {
            return;
        }
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        Location at = center.clone().add(0.0, 0.2, 0.0);
        int urgency = Math.max(1, 4 - fuseTicks / 15);
        world.spawnParticle(Particle.SMALL_FLAME, at, urgency, 0.1, 0.08, 0.1, 0.003);
        world.spawnParticle(Particle.SMOKE, at, urgency + 1, 0.12, 0.09, 0.12, 0.005);
        if (fuseTicks <= 30) {
            world.spawnParticle(Particle.CRIT, at, urgency + 2, 0.14, 0.1, 0.14, 0.12);
            ColorUtil.parseRgb(colorRgb).ifPresent(rgb -> world.spawnParticle(
                    Particle.DUST,
                    at,
                    urgency + 3,
                    0.14,
                    0.1,
                    0.14,
                    0.0,
                    ColorUtil.toDust(rgb, 1.1f)
            ));
        }
        if (fuseTicks <= 15 && fuseTicks % 5 == 0) {
            play(world, center, "block.note_block.hat", 0.35f, 1.2f + (15 - fuseTicks) * 0.04f);
        }
    }

    private static void playSound(World world, Location center, FuseMisfireSettings settings) {
        Sound sound = resolveSound(settings.dudSound);
        if (sound == null) {
            sound = Registry.SOUNDS.get(NamespacedKey.minecraft("entity.creeper.primed"));
        }
        world.playSound(center, sound, settings.dudSoundVolume, settings.dudSoundPitch);
    }

    private static void burstParticles(World world, Location at) {
        world.spawnParticle(Particle.EXPLOSION_EMITTER, at, 1, 0.0, 0.0, 0.0, 0.0);
        world.spawnParticle(Particle.EXPLOSION, at, 2, 0.08, 0.08, 0.08, 0.0);
        world.spawnParticle(Particle.SMOKE, at, 42, 0.38, 0.3, 0.38, 0.045);
        world.spawnParticle(Particle.LARGE_SMOKE, at, 12, 0.28, 0.22, 0.28, 0.025);
        world.spawnParticle(Particle.FLAME, at, 22, 0.32, 0.24, 0.32, 0.018);
        world.spawnParticle(Particle.CRIT, at, 32, 0.42, 0.32, 0.42, 0.2);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, at, 16, 0.3, 0.22, 0.3, 0.012);
        world.spawnParticle(Particle.ASH, at, 24, 0.34, 0.26, 0.34, 0.07);
        world.spawnParticle(Particle.ENCHANT, at, 18, 0.36, 0.28, 0.36, 0.55);
        world.spawnParticle(Particle.ELECTRIC_SPARK, at, 10, 0.25, 0.2, 0.25, 0.03);
    }

    private static void play(World world, Location at, String soundKey, float volume, float pitch) {
        Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundKey));
        if (sound != null) {
            world.playSound(at, sound, volume, pitch);
        }
    }

    private static Sound resolveSound(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        NamespacedKey namespacedKey = toSoundKey(key.trim());
        if (namespacedKey == null) {
            return null;
        }
        return Registry.SOUNDS.get(namespacedKey);
    }

    private static NamespacedKey toSoundKey(String raw) {
        if (raw.contains(":")) {
            String[] parts = raw.split(":", 2);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                return null;
            }
            return new NamespacedKey(parts[0].toLowerCase(Locale.ROOT), parts[1].toLowerCase(Locale.ROOT));
        }
        String value = raw.toLowerCase(Locale.ROOT);
        if (!value.contains(".")) {
            value = value.replace('_', '.');
        }
        return NamespacedKey.minecraft(value);
    }

}
