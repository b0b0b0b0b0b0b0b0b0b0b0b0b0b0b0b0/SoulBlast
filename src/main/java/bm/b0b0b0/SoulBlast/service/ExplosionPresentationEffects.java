package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.config.ExplosionPresentationSettings;
import bm.b0b0b0.SoulBlast.config.ExplosionSettings;
import bm.b0b0b0.SoulBlast.util.ColorUtil;
import org.bukkit.*;

import java.util.Optional;

public final class ExplosionPresentationEffects {

    private ExplosionPresentationEffects() {
    }

    public static void playFuseEnd(Location center, DynamiteDefinition dynamite) {
        if (center == null || center.getWorld() == null) {
            return;
        }
        World world = center.getWorld();
        Location at = center.clone().add(0.0, 0.35, 0.0);
        play(world, center, "entity.tnt.primed", 0.75f, 0.55f);
        if (DrainVortexEffectService.usesDrainVortex(dynamite)) {
            play(world, center, "block.bubble_column.whirlpool_ambient", 0.8f, 0.85f);
            play(world, center, "block.conduit.ambient", 0.55f, 1.45f);
        } else if (ExplosionLiquidSampler.drainsLiquids(dynamite)) {
            play(world, center, "block.bubble_column.whirlpool_ambient", 0.65f, 1.1f);
        } else {
            play(world, center, "entity.firework_rocket.launch", 0.5f, 1.35f);
        }
        spawnFlash(world, at, dynamite);
        world.spawnParticle(Particle.SMOKE, at, 10, 0.2, 0.15, 0.2, 0.02);
        spawnColoredSoulHaze(world, at, dynamite, 2, 1.0f);
    }

    public static void playObsidianBlockShatter(
            Location at,
            Material material,
            float proximity,
            DynamiteDefinition dynamite
    ) {
        if (at == null || at.getWorld() == null || material == null || !material.isBlock()) {
            return;
        }
        World world = at.getWorld();
        int scale = Math.max(2, Math.round(4.0f + proximity * 10.0f));
        float spread = 0.12f + proximity * 0.08f;
        world.spawnParticle(Particle.BLOCK, at, scale, spread, spread, spread, 0.08, material.createBlockData());
        world.spawnParticle(Particle.REVERSE_PORTAL, at, 3 + scale / 2, spread, spread, spread, 0.06);
        world.spawnParticle(Particle.SOUL, at, 2 + scale, spread * 0.9f, spread * 0.9f, spread * 0.9f, 0.02);
        if (proximity >= 0.65f) {
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, at, 2 + scale / 2, spread * 0.8f, spread * 0.8f, spread * 0.8f, 0.01);
            world.spawnParticle(Particle.CRIT, at, scale, spread, spread, spread, 0.18);
        }
        if (proximity >= 0.85f) {
            play(world, at, "block.amethyst_cluster.break", 0.55f, 0.9f + proximity * 0.2f);
        }
        spawnColoredSoulHaze(world, at, dynamite, Math.max(1, scale / 3), 0.65f + proximity * 0.5f);
    }

    public static void playObsidianSiegeBurst(Location center, DynamiteDefinition dynamite) {
        if (center == null || center.getWorld() == null) {
            return;
        }
        World world = center.getWorld();
        Location at = center.clone().add(0.0, 0.45, 0.0);
        float radius = dynamite.explosion.radius;
        play(world, center, "block.amethyst_block.break", 1.0f, 0.75f);
        play(world, center, "block.respawn_anchor.charge", 0.85f, 1.15f);
        world.spawnParticle(Particle.REVERSE_PORTAL, at, 24, radius * 0.3, 0.2, radius * 0.3, 0.15);
        world.spawnParticle(Particle.SOUL, at, 20, radius * 0.28, 0.18, radius * 0.28, 0.04);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, at, 16, radius * 0.25, 0.16, radius * 0.25, 0.01);
        world.spawnParticle(Particle.CRIT, at, 18, radius * 0.32, 0.22, radius * 0.32, 0.2);
        spawnColoredSoulHaze(world, at, dynamite, Math.max(2, Math.round(radius)), 1.3f);
    }

    public static void playDrainVortexDetonation(Location center, DynamiteDefinition dynamite) {
        if (center == null || dynamite == null || center.getWorld() == null) {
            return;
        }
        World world = center.getWorld();
        float radius = dynamite.explosion.effects.liquidRadius > 0.01f
                ? dynamite.explosion.effects.liquidRadius
                : dynamite.explosion.radius;
        float intensity = Math.max(0.5f, dynamite.explosion.effects.presentation.intensity);
        Location core = center.clone().add(0.0, 0.4, 0.0);
        int scale = Math.max(2, Math.round(radius * intensity * 0.85f));
        play(world, center, "block.conduit.deactivate", Math.min(3.0f, 0.7f + radius * 0.06f) * intensity, 0.7f);
        play(world, center, "entity.phantom.bite", 0.6f * intensity, 0.5f);
        world.spawnParticle(Particle.SONIC_BOOM, core, 1, 0.0, 0.0, 0.0, 0.0);
        world.spawnParticle(Particle.REVERSE_PORTAL, core, 14 + scale * 4, radius * 0.32, 0.22, radius * 0.32, 0.2);
        world.spawnParticle(Particle.SCULK_SOUL, core, 10 + scale * 3, radius * 0.28, 0.18, radius * 0.28, 0.025);
        world.spawnParticle(Particle.SQUID_INK, core, 8 + scale * 2, radius * 0.2, 0.14, radius * 0.2, 0.015);
        world.spawnParticle(Particle.BUBBLE, core, 12 + scale * 5, radius * 0.3, 0.2, radius * 0.3, 0.06);
        spawnColoredSoulHaze(world, core, dynamite, scale, intensity * 1.15f);
    }

    public static void playChargeBurst(Location center, DynamiteDefinition dynamite) {
        if (center == null || dynamite == null || center.getWorld() == null) {
            return;
        }
        if (!ExplosionLiquidSampler.drainsLiquids(dynamite)) {
            return;
        }
        World world = center.getWorld();
        Location at = center.clone().add(0.0, 0.4, 0.0);
        float radius = dynamite.explosion.effects.liquidRadius > 0
                ? dynamite.explosion.effects.liquidRadius
                : dynamite.explosion.radius;
        int scale = Math.max(1, Math.round(radius * 0.9f));
        play(world, center, "block.bubble_column.upwards_inside", 1.1f, 0.7f);
        play(world, center, "block.conduit.ambient", 0.9f, 1.4f);
        play(world, center, "entity.player.splash", 0.85f, 0.55f);
        world.spawnParticle(Particle.BUBBLE, at, 30 + scale * 8, radius * 0.35, 0.2, radius * 0.35, 0.08);
        world.spawnParticle(Particle.BUBBLE_POP, at, 16 + scale * 4, radius * 0.28, 0.16, radius * 0.28, 0.04);
        world.spawnParticle(Particle.FALLING_WATER, at, 12 + scale * 3, radius * 0.3, 0.25, radius * 0.3, 0.0);
        world.spawnParticle(Particle.SPLASH, at, 10 + scale * 2, radius * 0.25, 0.12, radius * 0.25, 0.12);
        spawnColoredSoulHaze(world, at, dynamite, scale, 1.2f);
    }

    public static void playDetonation(Location center, DynamiteDefinition dynamite) {
        if (center == null || dynamite == null) {
            return;
        }
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        ExplosionPresentationSettings presentation = dynamite.explosion.effects.presentation;
        if (!presentation.enabled) {
            return;
        }
        if (DrainVortexEffectService.usesDrainVortex(dynamite)) {
            playDrainVortexDetonation(center, dynamite);
            return;
        }
        ExplosionSettings explosion = dynamite.explosion;
        float intensity = Math.max(0.35f, presentation.intensity);
        float radius = Math.max(1.0f, explosion.radius);
        int scale = Math.max(1, Math.round(radius * intensity));
        Location core = center.clone().add(0.0, 0.35, 0.0);
        playDetonationSounds(world, center, radius, intensity);
        spawnCoreBurst(world, core, scale, intensity);
        spawnShockRings(world, center, radius, scale, intensity);
        spawnColoredSoulHaze(world, core, dynamite, scale, intensity);
        if ("WAVE".equalsIgnoreCase(explosion.quality)) {
            spawnWaveFlare(world, center, radius, scale, intensity);
        }
    }

    private static void playDetonationSounds(World world, Location center, float radius, float intensity) {
        float volume = Math.min(4.0f, 0.65f + radius * 0.09f) * intensity;
        float pitch = 0.82f + Math.min(0.22f, radius * 0.012f);
        play(world, center, "entity.generic.explode", volume, pitch);
        play(world, center, "entity.firework_rocket.blast", volume * 0.7f, 1.15f + pitch * 0.08f);
        play(world, center, "block.beacon.deactivate", volume * 0.55f, 1.35f);
        if (radius >= 6.0f) {
            play(world, center, "entity.lightning_bolt.thunder", volume * 0.35f, 1.55f);
        }
    }

    private static void spawnCoreBurst(World world, Location core, int scale, float intensity) {
        int smoke = 18 + scale * 10;
        int flame = 8 + scale * 4;
        world.spawnParticle(Particle.EXPLOSION_EMITTER, core, 1, 0.0, 0.0, 0.0, 0.0);
        world.spawnParticle(Particle.EXPLOSION, core, Math.max(1, scale / 3), 0.12, 0.12, 0.12, 0.0);
        world.spawnParticle(Particle.LARGE_SMOKE, core, smoke / 3, 0.42, 0.32, 0.42, 0.03 * intensity);
        world.spawnParticle(Particle.SMOKE, core, smoke, 0.55, 0.4, 0.55, 0.05 * intensity);
        world.spawnParticle(Particle.FLAME, core, flame, 0.38, 0.28, 0.38, 0.02);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, core, flame / 2 + 2, 0.34, 0.26, 0.34, 0.012);
        world.spawnParticle(Particle.CRIT, core, 12 + scale * 6, 0.5, 0.36, 0.5, 0.22);
        world.spawnParticle(Particle.ENCHANT, core, 16 + scale * 8, 0.62, 0.48, 0.62, 0.65);
        world.spawnParticle(Particle.ASH, core, 10 + scale * 4, 0.45, 0.34, 0.45, 0.07);
    }

    private static void spawnShockRings(World world, Location center, float radius, int scale, float intensity) {
        double ringRadius = Math.max(1.2, radius * 0.55);
        int points = Math.min(48, 12 + scale * 3);
        int layers = Math.min(5, 2 + scale / 4);
        for (int layer = 0; layer < layers; layer++) {
            double y = 0.25 + layer * 0.55;
            double layerRadius = ringRadius * (0.75 + layer * 0.12);
            for (int i = 0; i < points; i++) {
                double angle = (Math.PI * 2.0 * i) / points;
                Location at = center.clone().add(
                        Math.cos(angle) * layerRadius,
                        y,
                        Math.sin(angle) * layerRadius
                );
                world.spawnParticle(Particle.SMOKE, at, 2, 0.06, 0.05, 0.06, 0.01 * intensity);
                world.spawnParticle(Particle.END_ROD, at, 1, 0.04, 0.04, 0.04, 0.01);
                if (layer == 0) {
                    world.spawnParticle(Particle.CRIT, at, 2, 0.08, 0.06, 0.08, 0.14);
                }
            }
        }
    }

    private static void spawnColoredSoulHaze(
            World world,
            Location core,
            DynamiteDefinition dynamite,
            int scale,
            float intensity
    ) {
        Optional<ColorUtil.RgbColor> rgb = ColorUtil.parseRgb(dynamite.glow.colorRgb);
        if (rgb.isEmpty()) {
            return;
        }
        int count = 14 + scale * 8;
        world.spawnParticle(
                Particle.DUST,
                core,
                count,
                0.65,
                0.5,
                0.65,
                0.0,
                ColorUtil.toDust(rgb.get(), 1.15f * intensity)
        );
        world.spawnParticle(
                Particle.DUST,
                core,
                count / 2,
                0.35,
                0.55,
                0.35,
                0.0,
                ColorUtil.toDust(rgb.get(), 0.75f)
        );
    }

    private static void spawnWaveFlare(World world, Location center, float radius, int scale, float intensity) {
        Location at = center.clone().add(0.0, 0.2, 0.0);
        world.spawnParticle(Particle.SONIC_BOOM, at, 1, 0.0, 0.0, 0.0, 0.0);
        world.spawnParticle(Particle.SCULK_SOUL, at, 10 + scale * 5, radius * 0.35, 0.25, radius * 0.35, 0.02);
        world.spawnParticle(Particle.REVERSE_PORTAL, at, 8 + scale * 4, radius * 0.28, 0.2, radius * 0.28, 0.12);
        world.spawnParticle(Particle.WITCH, at, 6 + scale * 3, radius * 0.22, 0.18, radius * 0.22, 0.0);
        play(world, center, "block.conduit.ambient", Math.min(3.0f, 0.5f + radius * 0.08f) * intensity, 0.75f);
    }

    private static void spawnFlash(World world, Location at, DynamiteDefinition dynamite) {
        Color color = ColorUtil.parseRgb(dynamite.glow.colorRgb)
                .map(ColorUtil::toBukkitColor)
                .orElse(Color.WHITE);
        world.spawnParticle(Particle.FLASH, at, 1, 0.0, 0.0, 0.0, 0.0, color);
    }

    private static void play(World world, Location at, String soundKey, float volume, float pitch) {
        Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundKey));
        if (sound != null) {
            world.playSound(at, sound, volume, pitch);
        }
    }

}
