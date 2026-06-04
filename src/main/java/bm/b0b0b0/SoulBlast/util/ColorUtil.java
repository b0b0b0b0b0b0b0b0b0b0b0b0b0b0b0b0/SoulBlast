package bm.b0b0b0.SoulBlast.util;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Particle;

import java.util.Optional;

public final class ColorUtil {

    private ColorUtil() {
    }

    public static Optional<RgbColor> parseRgb(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String[] parts = raw.split(",");
        if (parts.length < 3) {
            return Optional.empty();
        }
        try {
            int red = clamp(Integer.parseInt(parts[0].trim()));
            int green = clamp(Integer.parseInt(parts[1].trim()));
            int blue = clamp(Integer.parseInt(parts[2].trim()));
            return Optional.of(new RgbColor(red, green, blue));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    public static TextColor toTextColor(RgbColor rgb) {
        return TextColor.color(rgb.red(), rgb.green(), rgb.blue());
    }

    public static Color toBukkitColor(RgbColor rgb) {
        return Color.fromRGB(rgb.red(), rgb.green(), rgb.blue());
    }

    public static Particle.DustOptions toDust(RgbColor rgb, float size) {
        return new Particle.DustOptions(toBukkitColor(rgb), size);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    public record RgbColor(int red, int green, int blue) {
        public String teamKey() {
            return red + "_" + green + "_" + blue;
        }
    }

}
