package bm.b0b0b0.SoulBlast.ps.command;

import bm.b0b0b0.SoulBlast.message.MessageService;
import bm.b0b0b0.SoulBlast.ps.PsModule;
import bm.b0b0b0.SoulBlast.ps.service.PsHologramHideSession;
import bm.b0b0b0.SoulBlast.ps.service.PsHologramVisibilityService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PsHologramCommand implements CommandExecutor, TabCompleter {

    private final PsModule module;
    private final MessageService messages;

    public PsHologramCommand(PsModule module, MessageService messages) {
        this.module = module;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player-only");
            return true;
        }
        if (!player.hasPermission(PsHologramVisibilityService.PERMISSION)
                && !player.hasPermission(PsHologramVisibilityService.ADMIN_PERMISSION)) {
            messages.send(sender, "no-permission");
            return true;
        }
        if (!module.active()) {
            messages.send(sender, "ps-hologram-module-off");
            return true;
        }
        if (!module.settings().hologramHide.enabled) {
            messages.send(sender, "ps-hologram-disabled");
            return true;
        }
        if (args.length == 0) {
            messages.send(sender, "ps-hologram-usage");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        PsHologramHideSession.Mode mode = switch (sub) {
            case "hide", "скрыть" -> PsHologramHideSession.Mode.HIDE;
            case "show", "показать" -> PsHologramHideSession.Mode.SHOW;
            case "toggle", "переключить" -> PsHologramHideSession.Mode.TOGGLE;
            default -> null;
        };
        if (mode == null) {
            messages.send(sender, "ps-hologram-usage");
            return true;
        }
        int timeoutSeconds = module.settings().hologramHide.armTimeoutSeconds;
        module.hologramHideSession().arm(player.getUniqueId(), mode, timeoutSeconds);
        Map<String, String> placeholders = Map.of("seconds", String.valueOf(Math.max(5, timeoutSeconds)));
        messages.send(sender, switch (mode) {
            case HIDE -> "ps-hologram-armed-hide";
            case SHOW -> "ps-hologram-armed-show";
            case TOGGLE -> "ps-hologram-armed-toggle";
        }, placeholders);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> options = List.of("hide", "show", "toggle");
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.startsWith(prefix)) {
                result.add(option);
            }
        }
        return result;
    }

}
