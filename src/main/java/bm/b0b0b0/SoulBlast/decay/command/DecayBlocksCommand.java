package bm.b0b0b0.SoulBlast.decay.command;

import bm.b0b0b0.SoulBlast.decay.DecayModule;
import bm.b0b0b0.SoulBlast.decay.gui.DecayBlocksSortMode;
import bm.b0b0b0.SoulBlast.message.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class DecayBlocksCommand implements CommandExecutor {

    private final DecayModule module;
    private final MessageService messages;

    public DecayBlocksCommand(DecayModule module, MessageService messages) {
        this.module = module;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player-only");
            return true;
        }
        if (!player.hasPermission("soulblast.decay.menu")) {
            messages.send(sender, "no-permission");
            return true;
        }
        if (!module.isEnabled()) {
            return true;
        }
        module.menuService().open(player, 0, DecayBlocksSortMode.HIGHEST_RESISTANCE);
        return true;
    }

}
