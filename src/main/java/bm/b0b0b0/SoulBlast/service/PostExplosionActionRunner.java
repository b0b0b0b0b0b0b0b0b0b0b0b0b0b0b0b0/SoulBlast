package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.PostExplosionAction;
import org.bukkit.*;

import java.util.List;

public final class PostExplosionActionRunner {

    public void scheduleActions(
            org.bukkit.plugin.java.JavaPlugin plugin,
            Location center,
            List<PostExplosionAction> actions
    ) {
        for (PostExplosionAction action : actions) {
            if ("LIGHTNING".equalsIgnoreCase(action.type)) {
                continue;
            }
            int delay = Math.max(0, action.delayTicks);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> run(center, action), delay);
        }
    }

    private void run(Location center, PostExplosionAction action) {
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        String type = action.type.toUpperCase();
        switch (type) {
            case "LIGHTNING" -> world.strikeLightningEffect(center);
            case "SOUND" -> {
                Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(action.param.toLowerCase()));
                if (sound != null) {
                    world.playSound(center, sound, 1.0f, 1.0f);
                }
            }
            case "COMMAND" -> {
                String command = action.param;
                for (String arg : action.args) {
                    command = command.replace("{arg}", arg);
                }
                org.bukkit.Bukkit.dispatchCommand(
                        org.bukkit.Bukkit.getConsoleSender(),
                        command.replace("{x}", String.valueOf(center.getBlockX()))
                                .replace("{y}", String.valueOf(center.getBlockY()))
                                .replace("{z}", String.valueOf(center.getBlockZ()))
                );
            }
            default -> {
                try {
                    Particle particle = Particle.valueOf(action.param.toUpperCase());
                    int count = parseIntArg(action.args, 0, 120);
                    double spread = parseDoubleArg(action.args, 1, 12.0);
                    world.spawnParticle(particle, center, count, spread, spread, spread, 0.05);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private int parseIntArg(List<String> args, int index, int fallback) {
        if (args.size() <= index) {
            return fallback;
        }
        try {
            return Integer.parseInt(args.get(index));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private double parseDoubleArg(List<String> args, int index, double fallback) {
        if (args.size() <= index) {
            return fallback;
        }
        try {
            return Double.parseDouble(args.get(index));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

}
