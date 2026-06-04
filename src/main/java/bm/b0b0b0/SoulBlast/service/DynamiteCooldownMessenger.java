package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.message.MessageService;
import bm.b0b0b0.SoulBlast.util.TextUtil;
import org.bukkit.entity.Player;

import java.util.Map;

public final class DynamiteCooldownMessenger {

    private final DynamiteCooldownService cooldownService;
    private final MessageService messageService;

    public DynamiteCooldownMessenger(DynamiteCooldownService cooldownService, MessageService messageService) {
        this.cooldownService = cooldownService;
        this.messageService = messageService;
    }

    public boolean blockPurchase(Player player, DynamiteDefinition dynamite) {
        DynamiteCooldownService.CooldownStatus status = cooldownService.status(
                player,
                dynamite,
                DynamiteCooldownService.CooldownKind.PURCHASE
        );
        if (!status.blocked()) {
            return false;
        }
        messageService.send(player, "cooldown-purchase-active", Map.of(
                "display", displayName(dynamite),
                "remaining", DynamiteCooldownService.formatRemaining(status.remainingSeconds())
        ));
        return true;
    }

    public boolean blockUse(Player player, DynamiteDefinition dynamite) {
        DynamiteCooldownService.CooldownStatus status = cooldownService.status(
                player,
                dynamite,
                DynamiteCooldownService.CooldownKind.USE
        );
        if (!status.blocked()) {
            return false;
        }
        messageService.send(player, "cooldown-use-active", Map.of(
                "display", displayName(dynamite),
                "remaining", DynamiteCooldownService.formatRemaining(status.remainingSeconds())
        ));
        return true;
    }

    private static String displayName(DynamiteDefinition dynamite) {
        return TextUtil.stripColor(TextUtil.apply(dynamite.item.displayName, Map.of()));
    }

}
