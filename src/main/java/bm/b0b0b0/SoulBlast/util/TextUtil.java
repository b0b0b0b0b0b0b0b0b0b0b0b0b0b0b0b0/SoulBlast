package bm.b0b0b0.SoulBlast.util;

import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TextUtil {

    private TextUtil() {
    }

    public static String colorize(String input) {
        if (input == null) {
            return "";
        }
        return input.replace('§', '&');
    }

    public static String apply(String template, Map<String, String> placeholders) {
        if (template == null) {
            return "";
        }
        String result = colorize(template);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() == null ? "" : entry.getValue();
            result = result.replace("{" + key + "}", value);
            result = result.replace("%" + key + "%", value);
        }
        return result;
    }

    public static List<String> colorizeLore(List<String> lines, Map<String, String> placeholders) {
        return lines.stream()
                .map(line -> apply(line, placeholders))
                .collect(Collectors.toList());
    }

    public static void setDisplayName(ItemMeta meta, String name) {
        meta.displayName(TextParser.parse(name));
    }

    public static void setLore(ItemMeta meta, List<String> lore) {
        meta.lore(lore.stream()
                .map(TextParser::parse)
                .collect(Collectors.toList()));
    }

    public static String stripColor(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("(?i)&#[0-9a-f]{6}", "")
                .replaceAll("(?i)[&§][0-9a-fk-or]", "");
    }

}
