package bm.b0b0b0.SoulBlast.integration;

import bm.b0b0b0.SoulBlast.SoulBlast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

public final class DeferredIntegrationsListener implements Listener {

    private final SoulBlast plugin;

    public DeferredIntegrationsListener(SoulBlast plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event) {
        String name = event.getPlugin().getName();
        if ("WorldGuard".equals(name) || "ProtectionStones".equals(name)) {
            plugin.bootstrapIntegrations();
        }
    }

}
