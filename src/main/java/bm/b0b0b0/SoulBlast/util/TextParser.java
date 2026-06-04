package bm.b0b0b0.SoulBlast.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Pattern;

public final class TextParser {

    private static final Pattern BARE_HEX = Pattern.compile("(?<![&])#([0-9A-Fa-f]{6})");
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();
    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private TextParser() {
    }

    public static Component parse(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Component.empty();
        }
        String normalized = BARE_HEX.matcher(raw).replaceAll("&#$1");
        if (normalized.indexOf('<') >= 0) {
            return MINI.deserialize(normalized);
        }
        return LEGACY.deserialize(normalized);
    }

}
