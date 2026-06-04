package bm.b0b0b0.SoulBlast.command;

import bm.b0b0b0.SoulBlast.config.menu.MenuFileConfig;
import bm.b0b0b0.SoulBlast.gui.menu.SoulGrimoireMenuService;
import bm.b0b0b0.SoulBlast.message.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SoulGrimoireCommand implements CommandExecutor {

    private final MessageService messages;
    private final SoulGrimoireMenuService menuService;
    private MenuFileConfig menuConfig = new MenuFileConfig();

    public SoulGrimoireCommand(MessageService messages, SoulGrimoireMenuService menuService) {
        this.messages = messages;
        this.menuService = menuService;
    }

    public void reload(MenuFileConfig config) {
        menuConfig = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!menuConfig.command.enabled) {
            return true;
        }
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player-only");
            return true;
        }
        String permission = resolvePermission();
        if (permission != null && !player.hasPermission(permission)) {
            messages.send(sender, "no-permission");
            return true;
        }
        menuService.open(player);
        return true;
    }

    private String resolvePermission() {
        String configured = menuConfig.command.permission;
        if (configured == null || configured.isBlank()) {
            return null;
        }
        return configured;
    }

}
