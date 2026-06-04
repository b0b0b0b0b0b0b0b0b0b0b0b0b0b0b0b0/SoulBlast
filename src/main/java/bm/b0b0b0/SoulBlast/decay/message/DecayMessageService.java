package bm.b0b0b0.SoulBlast.decay.message;

import bm.b0b0b0.SoulBlast.decay.config.DecayMessagesFileConfig;
import bm.b0b0b0.SoulBlast.util.TextParser;
import bm.b0b0b0.SoulBlast.util.TextUtil;
import org.bukkit.entity.Player;

import java.util.Map;

public final class DecayMessageService {

    private final DecayMessagesFileConfig messages;

    public DecayMessageService(DecayMessagesFileConfig messages) {
        this.messages = messages;
    }

    public void chat(Player player, String key, Map<String, String> placeholders) {
        String raw = resolve(key);
        if (raw == null || raw.isBlank()) {
            return;
        }
        player.sendMessage(TextParser.parse(apply(raw, placeholders)));
    }

    public void actionBar(Player player, String key, Map<String, String> placeholders) {
        String raw = resolve(key);
        if (raw == null || raw.isBlank()) {
            return;
        }
        player.sendActionBar(TextParser.parse(apply(raw, placeholders)));
    }

    private String apply(String template, Map<String, String> placeholders) {
        String result = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return TextUtil.colorize(result);
    }

    public static Map<String, String> percentPlaceholders(int percent) {
        return Map.of("percent", Integer.toString(Math.max(0, Math.min(100, percent))));
    }

    public static Map<String, String> materialPlaceholders(String materialLabel) {
        return Map.of("material", materialLabel);
    }

    private String resolve(String key) {
        return switch (key) {
            case "repair-success" -> messages.repairSuccess;
            case "repair-already-healthy" -> messages.repairAlreadyHealthy;
            case "repair-no-material" -> messages.repairNoMaterial;
            case "repair-not-decaying" -> messages.repairNotDecaying;
            case "repair-blocked" -> messages.repairBlocked;
            case "repair-too-far" -> messages.repairTooFar;
            default -> null;
        };
    }

}
