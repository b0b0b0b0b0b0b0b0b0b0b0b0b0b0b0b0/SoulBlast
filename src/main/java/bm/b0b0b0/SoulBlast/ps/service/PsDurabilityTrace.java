package bm.b0b0b0.SoulBlast.ps.service;

import bm.b0b0b0.SoulBlast.ps.config.PsSettings;
import bm.b0b0b0.SoulBlast.ps.model.PsBlockState;
import bm.b0b0b0.SoulBlast.ps.repository.PsBlockStore;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PsDurabilityTrace {

    private final JavaPlugin plugin;
    private final PsSettings settings;

    public PsDurabilityTrace(JavaPlugin plugin, PsSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public boolean enabled() {
        return settings.debug || settings.durabilityTrace;
    }

    public void dynamitePlaced(Player player, Location at, String dynamiteId, PsBlockStore store) {
        if (!enabled()) {
            return;
        }
        String who = player == null ? "?" : player.getName();
        line("динамит поставлен: " + dynamiteId + " игрок=" + who + " @ " + formatLocation(at));
        logNearbyRegions(store, at, "сейчас");
    }

    public void dynamiteDetonated(Player player, Location at, String dynamiteId, PsBlockStore store) {
        if (!enabled()) {
            return;
        }
        String who = player == null ? "?" : player.getName();
        line("взрыв запущен: " + dynamiteId + " игрок=" + who + " центр=" + formatLocation(at));
        logNearbyRegions(store, at, "до взрыва");
    }

    private void logNearbyRegions(PsBlockStore store, Location at, String moment) {
        if (store == null || at == null || at.getWorld() == null) {
            return;
        }
        double maxDistSq = 96.0 * 96.0;
        int count = 0;
        for (PsBlockState state : store.all()) {
            double dx = (state.key().x() + 0.5) - at.getX();
            double dy = (state.key().y() + 0.5) - at.getY();
            double dz = (state.key().z() + 0.5) - at.getZ();
            if (dx * dx + dy * dy + dz * dz > maxDistSq) {
                continue;
            }
            count++;
            line("приват " + moment + " " + formatBlock(state) + " прочность="
                    + state.durability() + "/" + state.maximum()
                    + " world-id=" + state.key().worldId());
        }
        if (count == 0) {
            line("приват " + moment + ": в store нет регионов в радиусе 96 блоков от взрыва");
        }
    }

    public void explosionPassStart(Location center, String dynamiteId, int trackedRegions) {
        if (!enabled()) {
            return;
        }
        line("обработка прочности: динамит=" + dynamiteId + " центр=" + formatLocation(center)
                + " записей в store=" + trackedRegions);
    }

    public void blockBefore(PsBlockState state, String dynamiteId) {
        if (!enabled()) {
            return;
        }
        line("блок привата " + formatBlock(state) + " ДО взрыва " + dynamiteId + ": "
                + state.durability() + "/" + state.maximum());
    }

    public void blockAfter(PsBlockState state, int damage, String dynamiteId) {
        if (!enabled()) {
            return;
        }
        line("блок привата " + formatBlock(state) + " ПОСЛЕ взрыва " + dynamiteId
                + " (-" + damage + "): " + state.durability() + "/" + state.maximum());
    }

    public void skipped(String message) {
        if (!enabled()) {
            return;
        }
        line("пропуск: " + message);
    }

    public void moduleInactive(String reason) {
        if (!enabled()) {
            return;
        }
        line("модуль не обработал взрыв: " + reason);
    }

    private void line(String message) {
        plugin.getLogger().info("[PS-Durability] " + message);
    }

    private static String formatBlock(PsBlockState state) {
        return state.key().x() + "," + state.key().y() + "," + state.key().z()
                + " alias=" + state.typeAlias();
    }

    private static String formatLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return "?";
        }
        return location.getWorld().getName() + " "
                + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

}
