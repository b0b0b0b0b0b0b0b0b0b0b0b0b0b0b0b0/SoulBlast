package bm.b0b0b0.SoulBlast.command;

import bm.b0b0b0.SoulBlast.SoulBlast;
import bm.b0b0b0.SoulBlast.integration.coreprotect.CoreProtectBridge;
import bm.b0b0b0.SoulBlast.message.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;

public final class CoreProtectRollbackCommand {

    private final SoulBlast plugin;
    private final CoreProtectBridge coreProtect;
    private final MessageService messages;

    public CoreProtectRollbackCommand(SoulBlast plugin, CoreProtectBridge coreProtect, MessageService messages) {
        this.plugin = plugin;
        this.coreProtect = coreProtect;
        this.messages = messages;
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("soulblast.rollback")) {
            messages.send(sender, "no-permission");
            return true;
        }
        if (!coreProtect.active()) {
            messages.send(sender, "coreprotect-missing");
            return true;
        }
        if (args.length < 4) {
            messages.send(sender, "command-usage");
            return true;
        }
        String user = args[1];
        int radius = parsePositiveInt(args[2]);
        int timeSeconds = parseTimeSeconds(args[3]);
        if (radius <= 0 || timeSeconds <= 0) {
            messages.send(sender, "coreprotect-rollback-invalid");
            return true;
        }
        Location center = resolveCenter(sender, user);
        if (center == null) {
            messages.send(sender, "coreprotect-rollback-center");
            return true;
        }
        messages.send(sender, "coreprotect-rollback-started", Map.of(
                "user", user,
                "radius", String.valueOf(radius),
                "time", formatTime(timeSeconds)
        ));
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            CoreProtectBridge.RollbackOutcome outcome = coreProtect.rollbackAround(user, center, radius, timeSeconds);
            plugin.getServer().getScheduler().runTask(plugin, () -> deliverOutcome(sender, outcome));
        });
        return true;
    }

    private void deliverOutcome(CommandSender sender, CoreProtectBridge.RollbackOutcome outcome) {
        switch (outcome.type()) {
            case SUCCESS -> {
                if (outcome.changes() <= 0) {
                    messages.send(sender, "coreprotect-rollback-empty", Map.of(
                            "user", coreProtect.logUser()
                    ));
                } else {
                    messages.send(sender, "coreprotect-rollback-done", Map.of(
                            "changes", String.valueOf(outcome.changes())
                    ));
                }
            }
            case UNAVAILABLE -> messages.send(sender, "coreprotect-missing");
            case INVALID_USER -> messages.send(sender, "coreprotect-rollback-invalid");
            case INVALID_CENTER -> messages.send(sender, "coreprotect-rollback-center");
            case INVALID_ARGUMENTS -> messages.send(sender, "coreprotect-rollback-invalid");
            case FAILED -> messages.send(sender, "coreprotect-rollback-failed", Map.of(
                    "detail", outcome.detail().isBlank() ? "unknown" : outcome.detail()
            ));
        }
    }

    private static Location resolveCenter(CommandSender sender, String user) {
        if (sender instanceof Player player) {
            return player.getLocation();
        }
        Player target = Bukkit.getPlayerExact(user);
        if (target != null) {
            return target.getLocation();
        }
        return null;
    }

    private static int parsePositiveInt(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private static int parseTimeSeconds(String raw) {
        if (raw == null || raw.isBlank()) {
            return -1;
        }
        String value = raw.trim().toLowerCase(Locale.ROOT);
        try {
            if (value.endsWith("h")) {
                return Integer.parseInt(value.substring(0, value.length() - 1)) * 3600;
            }
            if (value.endsWith("m")) {
                return Integer.parseInt(value.substring(0, value.length() - 1)) * 60;
            }
            if (value.endsWith("s")) {
                return Integer.parseInt(value.substring(0, value.length() - 1));
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private static String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + "с";
        }
        int minutes = seconds / 60;
        if (minutes < 60) {
            int sec = seconds % 60;
            return sec == 0 ? minutes + "м" : minutes + "м " + sec + "с";
        }
        int hours = minutes / 60;
        int min = minutes % 60;
        return min == 0 ? hours + "ч" : hours + "ч " + min + "м";
    }

}
