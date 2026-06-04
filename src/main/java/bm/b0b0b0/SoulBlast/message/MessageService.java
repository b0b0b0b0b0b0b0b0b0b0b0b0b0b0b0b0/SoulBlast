package bm.b0b0b0.SoulBlast.message;

import bm.b0b0b0.SoulBlast.util.TextParser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class MessageService {

    private final MessagesConfig messages;

    public MessageService(MessagesConfig messages) {
        this.messages = messages;
    }

    public void send(CommandSender sender, String templateKey, Map<String, String> placeholders) {
        String raw = resolveTemplate(templateKey);
        if (raw == null) {
            return;
        }
        Map<String, String> all = new HashMap<>(placeholders);
        all.putIfAbsent("prefix", colorize(messages.prefix));
        String text = applyPlaceholders(colorize(raw), all);
        sender.sendMessage(TextParser.parse(text));
    }

    public void send(CommandSender sender, String templateKey) {
        send(sender, templateKey, Map.of());
    }

    public String format(String templateKey, Map<String, String> placeholders) {
        String raw = resolveTemplate(templateKey);
        if (raw == null) {
            return "";
        }
        Map<String, String> all = new HashMap<>(placeholders);
        all.putIfAbsent("prefix", colorize(messages.prefix));
        return applyPlaceholders(colorize(raw), all);
    }

    private String resolveTemplate(String key) {
        return switch (key) {
            case "no-permission" -> messages.noPermission;
            case "player-only" -> messages.playerOnly;
            case "unknown-dynamite" -> messages.unknownDynamite;
            case "dynamite-given" -> messages.dynamiteGiven;
            case "dynamite-received" -> messages.dynamiteReceived;
            case "reload-done" -> messages.reloadDone;
            case "ignite-denied" -> messages.igniteDenied;
            case "command-usage" -> messages.commandUsage;
            case "goal-selected" -> messages.goalSelected;
            case "goal-cleared" -> messages.goalCleared;
            case "goal-not-selected" -> messages.goalNotSelected;
            case "auto-ignite-on" -> messages.autoIgniteOn;
            case "auto-ignite-off" -> messages.autoIgniteOff;
            case "purchase-success" -> messages.purchaseSuccess;
            case "purchase-failed" -> messages.purchaseFailed;
            case "purchase-failed-money" -> messages.purchaseFailedMoney;
            case "purchase-failed-experience" -> messages.purchaseFailedExperience;
            case "purchase-failed-tnt" -> messages.purchaseFailedTnt;
            case "purchase-failed-inventory" -> messages.purchaseFailedInventory;
            case "purchase-requirements" -> messages.purchaseRequirements;
            case "catalog-pay-not-required" -> messages.catalogPayNotRequired;
            case "copilka-ready" -> messages.copilkaReady;
            case "copilka-tnt-full" -> messages.copilkaTntFull;
            case "tnt-deposited" -> messages.tntDeposited;
            case "tnt-deposit-none" -> messages.tntDepositNone;
            case "tnt-deposit-complete" -> messages.tntDepositComplete;
            case "tnt-deposit-not-required" -> messages.tntDepositNotRequired;
            case "region-protected" -> messages.regionProtected;
            case "region-protected-blast" -> messages.regionProtectedBlast;
            case "region-worldguard-missing" -> messages.regionWorldguardMissing;
            case "cooldown-purchase-active" -> messages.cooldownPurchaseActive;
            case "cooldown-use-active" -> messages.cooldownUseActive;
            case "fuse-recall-success" -> messages.fuseRecallSuccess;
            case "fuse-recall-not-owner" -> messages.fuseRecallNotOwner;
            case "fuse-recall-no-owner" -> messages.fuseRecallNoOwner;
            case "fuse-recall-warhead" -> messages.fuseRecallWarhead;
            case "fuse-recall-disabled" -> messages.fuseRecallDisabled;
            case "fuse-hologram-name" -> messages.fuseHologramName;
            case "fuse-hologram-timer" -> messages.fuseHologramTimer;
            case "fuse-hologram-recall" -> messages.fuseHologramRecall;
            case "fuse-hologram-misfire-warning" -> messages.fuseHologramMisfireWarning;
            case "fuse-hologram-misfire-active" -> messages.fuseHologramMisfireActive;
            case "fuse-hologram-misfire-idle" -> messages.fuseHologramMisfireIdle;
            case "fuse-hologram-misfire-hint" -> messages.fuseHologramMisfireHint;
            case "fuse-hologram-misfire-recall" -> messages.fuseHologramMisfireRecall;
            case "fuse-misfire-relight" -> messages.fuseMisfireRelight;
            case "fuse-misfire-detonate" -> messages.fuseMisfireDetonate;
            case "fuse-misfire-fizzle" -> messages.fuseMisfireFizzle;
            case "fuse-misfire-not-owner" -> messages.fuseMisfireNotOwner;
            case "ps-hologram-usage" -> messages.psHologramUsage;
            case "ps-hologram-armed-hide" -> messages.psHologramArmedHide;
            case "ps-hologram-armed-show" -> messages.psHologramArmedShow;
            case "ps-hologram-armed-toggle" -> messages.psHologramArmedToggle;
            case "ps-hologram-hidden" -> messages.psHologramHidden;
            case "ps-hologram-shown" -> messages.psHologramShown;
            case "ps-hologram-already-hidden" -> messages.psHologramAlreadyHidden;
            case "ps-hologram-already-visible" -> messages.psHologramAlreadyVisible;
            case "ps-hologram-not-owner" -> messages.psHologramNotOwner;
            case "ps-hologram-not-tracked" -> messages.psHologramNotTracked;
            case "ps-hologram-not-protect-block" -> messages.psHologramNotProtectBlock;
            case "ps-hologram-module-off" -> messages.psHologramModuleOff;
            case "ps-hologram-disabled" -> messages.psHologramDisabled;
            default -> null;
        };
    }

    private String applyPlaceholders(String text, Map<String, String> placeholders) {
        String result = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    private String colorize(String input) {
        return input == null ? "" : input.replace('§', '&');
    }

    public static Map<String, String> playerPlaceholders(Player player) {
        return Map.of("player", player.getName());
    }

}
