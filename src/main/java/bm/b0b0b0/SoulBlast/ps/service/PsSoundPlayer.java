package bm.b0b0b0.SoulBlast.ps.service;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.World;

import java.util.Locale;

public final class PsSoundPlayer {

    private PsSoundPlayer() {
    }

    public static void play(World world, Location at, String soundKey) {
        if (world == null || at == null || soundKey == null || soundKey.isBlank()) {
            return;
        }
        Sound sound = resolve(soundKey);
        if (sound == null) {
            return;
        }
        world.playSound(at, sound, 1.0f, 1.0f);
    }

    private static Sound resolve(String soundKey) {
        String trimmed = soundKey.trim();
        Sound fromKey = Registry.SOUNDS.get(NamespacedKey.minecraft(trimmed.toLowerCase(Locale.ROOT)));
        if (fromKey != null) {
            return fromKey;
        }
        if (trimmed.contains(".")) {
            return null;
        }
        String dotted = trimmed.toLowerCase(Locale.ROOT).replace('_', '.');
        return Registry.SOUNDS.get(NamespacedKey.minecraft(dotted));
    }

}
