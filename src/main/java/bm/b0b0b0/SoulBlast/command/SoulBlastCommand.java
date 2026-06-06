package bm.b0b0b0.SoulBlast.command;

import bm.b0b0b0.SoulBlast.SoulBlast;
import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.gui.menu.SoulGrimoireMenuService;
import bm.b0b0b0.SoulBlast.message.MessageService;
import bm.b0b0b0.SoulBlast.repository.DynamiteRegistry;
import bm.b0b0b0.SoulBlast.service.DynamiteItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class SoulBlastCommand implements CommandExecutor, TabCompleter {

    private final SoulBlast plugin;
    private final DynamiteRegistry registry;
    private final DynamiteItemFactory itemFactory;
    private final MessageService messages;
    private final SoulGrimoireMenuService menuService;
    private final CoreProtectRollbackCommand rollbackCommand;

    public SoulBlastCommand(
            SoulBlast plugin,
            DynamiteRegistry registry,
            DynamiteItemFactory itemFactory,
            MessageService messages,
            SoulGrimoireMenuService menuService
    ) {
        this.plugin = plugin;
        this.registry = registry;
        this.itemFactory = itemFactory;
        this.messages = messages;
        this.menuService = menuService;
        this.rollbackCommand = new CoreProtectRollbackCommand(
                plugin,
                plugin.getCoreProtectBridge(),
                messages
        );
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            messages.send(sender, "command-usage");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        return switch (sub) {
            case "reload" -> handleReload(sender);
            case "menu" -> handleMenu(sender);
            case "give" -> handleGive(sender, args);
            case "rollback" -> rollbackCommand.handle(sender, args);
            default -> {
                messages.send(sender, "command-usage");
                yield true;
            }
        };
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("soulblast.reload")) {
            messages.send(sender, "no-permission");
            return true;
        }
        plugin.reloadPlugin();
        messages.send(sender, "reload-done");
        return true;
    }

    private boolean handleMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player-only");
            return true;
        }
        String permission = plugin.getMenuConfig().command.permission;
        if (permission != null && !permission.isBlank() && !player.hasPermission(permission)) {
            messages.send(sender, "no-permission");
            return true;
        }
        menuService.open(player);
        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("soulblast.give")) {
            messages.send(sender, "no-permission");
            return true;
        }
        if (args.length < 3) {
            messages.send(sender, "command-usage");
            return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            messages.send(sender, "command-usage");
            return true;
        }
        String id = args[2];
        DynamiteDefinition definition = registry.find(id).orElse(null);
        if (definition == null) {
            messages.send(sender, "unknown-dynamite", Map.of("id", id));
            return true;
        }
        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException ignored) {
                amount = 1;
            }
        }
        ItemStack stack = itemFactory.create(definition, amount);
        target.getInventory().addItem(stack);
        messages.send(sender, "dynamite-given", Map.of(
                "player", target.getName(),
                "id", definition.id,
                "amount", String.valueOf(amount)
        ));
        messages.send(target, "dynamite-received", Map.of(
                "display", definition.item.displayName,
                "amount", String.valueOf(amount)
        ));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("give", "menu", "reload", "rollback"), args[0]);
        }
        if (args.length == 2 && "give".equalsIgnoreCase(args[0])) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        if (args.length == 3 && "give".equalsIgnoreCase(args[0])) {
            return filter(registry.all().stream().map(d -> d.id).collect(Collectors.toList()), args[2]);
        }
        if (args.length == 2 && "rollback".equalsIgnoreCase(args[0])) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(option);
            }
        }
        return result;
    }

}
