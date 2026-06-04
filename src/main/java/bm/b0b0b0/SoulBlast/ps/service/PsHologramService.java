package bm.b0b0b0.SoulBlast.ps.service;

import bm.b0b0b0.SoulBlast.ps.config.PsHologramTypeSettings;
import bm.b0b0b0.SoulBlast.ps.config.PsProtectionTypeDefinition;
import bm.b0b0b0.SoulBlast.ps.model.PsBlockKey;
import bm.b0b0b0.SoulBlast.ps.model.PsBlockState;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.TextDisplay;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PsHologramService {

    private final Map<PsBlockKey, TextDisplay> displays = new ConcurrentHashMap<>();

    public void attach(
            World world,
            PsBlockState state,
            PsProtectionTypeDefinition type
    ) {
        if (world == null || type == null || state == null || state.hologramHidden()) {
            if (state != null) {
                remove(state.key());
            }
            return;
        }
        PsHologramTypeSettings hologram = type.hologram;
        if (!hologram.enabled) {
            return;
        }
        remove(state.key());
        Location at = new Location(world, state.key().x() + 0.5, state.key().y() + hologram.offsetY, state.key().z() + 0.5);
        TextDisplay display = world.spawn(at, TextDisplay.class, spawned -> {
            spawned.text(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    PsPlaceholderResolver.joinLines(hologram.lines, state)
            ));
            PsTextDisplayConfigurer.apply(spawned, hologram);
        });
        displays.put(state.key(), display);
    }

    public void refresh(PsBlockState state, PsProtectionTypeDefinition type) {
        if (state == null || type == null || state.hologramHidden() || !type.hologram.enabled) {
            if (state != null && state.hologramHidden()) {
                remove(state.key());
            }
            return;
        }
        TextDisplay display = displays.get(state.key());
        if (display == null || display.isDead()) {
            World world = Bukkit.getWorld(state.key().worldId());
            if (world != null) {
                attach(world, state, type);
            }
            return;
        }
        PsHologramTypeSettings hologram = type.hologram;
        display.text(LegacyComponentSerializer.legacyAmpersand().deserialize(
                PsPlaceholderResolver.joinLines(hologram.lines, state)
        ));
        PsTextDisplayConfigurer.apply(display, hologram);
        Location target = new Location(
                display.getWorld(),
                state.key().x() + 0.5,
                state.key().y() + hologram.offsetY,
                state.key().z() + 0.5
        );
        display.teleport(target);
    }

    public void remove(PsBlockKey key) {
        TextDisplay display = displays.remove(key);
        if (display != null && !display.isDead()) {
            display.remove();
        }
    }

    public void clearAll() {
        for (TextDisplay display : displays.values()) {
            if (display != null && !display.isDead()) {
                display.remove();
            }
        }
        displays.clear();
    }

}
