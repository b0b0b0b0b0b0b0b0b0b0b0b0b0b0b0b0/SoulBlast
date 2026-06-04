package bm.b0b0b0.SoulBlast.ps.service;

import bm.b0b0b0.SoulBlast.ps.config.PsHologramDisplaySettings;
import bm.b0b0b0.SoulBlast.ps.config.PsHologramTypeSettings;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Locale;

public final class PsTextDisplayConfigurer {

    private PsTextDisplayConfigurer() {
    }

    public static void apply(TextDisplay display, PsHologramTypeSettings hologram) {
        if (display == null || hologram == null) {
            return;
        }
        PsHologramDisplaySettings settings = hologram.display;
        if (settings == null) {
            settings = new PsHologramDisplaySettings();
        }
        display.setViewRange(Math.max(0.0f, settings.viewRange));
        display.setSeeThrough(settings.seeThrough);
        display.setShadowed(settings.shadowed);
        display.setBillboard(parseBillboard(settings.billboard));
        display.setAlignment(parseAlignment(settings.alignment));
        display.setDefaultBackground(settings.defaultBackground);
        if (settings.lineWidth > 0) {
            display.setLineWidth(settings.lineWidth);
        }
        if (settings.textOpacity >= 0) {
            display.setTextOpacity((byte) Math.min(255, settings.textOpacity));
        }
        if (settings.backgroundColor != 0) {
            display.setBackgroundColor(Color.fromARGB(settings.backgroundColor));
        }
        float shadowRadius = Math.max(0.0f, settings.shadowRadius);
        float shadowStrength = Math.max(0.0f, settings.shadowStrength);
        display.setShadowRadius(shadowRadius);
        display.setShadowStrength(shadowStrength);
        float scale = settings.scale <= 0.0f ? 1.0f : settings.scale;
        display.setTransformation(new Transformation(
                new Vector3f(0.0f, 0.0f, 0.0f),
                new Quaternionf(),
                new Vector3f(scale, scale, scale),
                new Quaternionf()
        ));
        display.setPersistent(false);
    }

    private static Display.Billboard parseBillboard(String value) {
        if (value == null) {
            return Display.Billboard.CENTER;
        }
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "fixed" -> Display.Billboard.FIXED;
            case "horizontal" -> Display.Billboard.HORIZONTAL;
            case "vertical" -> Display.Billboard.VERTICAL;
            default -> Display.Billboard.CENTER;
        };
    }

    private static TextDisplay.TextAlignment parseAlignment(String value) {
        if (value == null) {
            return TextDisplay.TextAlignment.CENTER;
        }
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "left" -> TextDisplay.TextAlignment.LEFT;
            case "right" -> TextDisplay.TextAlignment.RIGHT;
            default -> TextDisplay.TextAlignment.CENTER;
        };
    }

}
