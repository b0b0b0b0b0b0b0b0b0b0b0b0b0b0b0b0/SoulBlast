package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.config.GlowSettings;
import bm.b0b0b0.SoulBlast.util.ColorUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DynamiteVisualService {

    private final JavaPlugin plugin;
    private final Map<String, Team> teamsByColor = new HashMap<>();

    public DynamiteVisualService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void applyPrimedLook(TNTPrimed primed, DynamiteDefinition definition) {
        GlowSettings glow = definition.glow;
        if (!glow.enabled) {
            return;
        }
        primed.setGlowing(true);
        if (glow.useTeamColor) {
            ColorUtil.parseRgb(glow.colorRgb).ifPresent(rgb -> applyTeamColor(primed, rgb));
        }
    }

    public void tickParticles(TNTPrimed primed, DynamiteDefinition definition, int glowPhase) {
        GlowSettings glow = definition.glow;
        if (!glow.enabled || !glow.spawnParticles) {
            return;
        }
        Optional<ColorUtil.RgbColor> rgb = ColorUtil.parseRgb(glow.colorRgb);
        if (rgb.isEmpty() || primed.getWorld() == null) {
            return;
        }
        Location at = primed.getLocation().add(0, 0.15, 0);
        primed.getWorld().spawnParticle(
                Particle.DUST,
                at,
                6,
                0.18,
                0.16,
                0.18,
                0.0,
                ColorUtil.toDust(rgb.get(), 1.05f)
        );
        if (glowPhase % 2 == 0) {
            primed.getWorld().spawnParticle(Particle.END_ROD, at, 2, 0.1, 0.12, 0.1, 0.008);
            primed.getWorld().spawnParticle(Particle.SMOKE, at, 2, 0.1, 0.08, 0.1, 0.004);
        }
        if (glowPhase % 3 == 0) {
            primed.getWorld().spawnParticle(Particle.SMALL_FLAME, at, 1, 0.08, 0.06, 0.08, 0.002);
            primed.getWorld().spawnParticle(Particle.ENCHANT, at, 3, 0.14, 0.1, 0.14, 0.4);
        }
        FuseMisfireEffects.playFuseTick(at, primed.getFuseTicks(), glow.colorRgb);
    }

    private void applyTeamColor(Entity entity, ColorUtil.RgbColor rgb) {
        Team team = teamsByColor.computeIfAbsent(rgb.teamKey(), key -> registerTeam(key, rgb));
        team.addEntity(entity);
    }

    private Team registerTeam(String key, ColorUtil.RgbColor rgb) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamId = "sb_" + key;
        Team existing = scoreboard.getTeam(teamId);
        if (existing != null) {
            return existing;
        }
        Team team = scoreboard.registerNewTeam(teamId);
        team.color(NamedTextColor.nearestTo(ColorUtil.toTextColor(rgb)));
        return team;
    }

    public void shutdown() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        for (String key : teamsByColor.keySet()) {
            Team team = scoreboard.getTeam("sb_" + key);
            if (team != null) {
                team.unregister();
            }
        }
        teamsByColor.clear();
    }

}
