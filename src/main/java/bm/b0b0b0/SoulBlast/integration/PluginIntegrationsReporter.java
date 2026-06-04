package bm.b0b0b0.SoulBlast.integration;

import bm.b0b0b0.SoulBlast.config.EconomySettings;
import bm.b0b0b0.SoulBlast.config.RegionProtectionSettings;
import bm.b0b0b0.SoulBlast.integration.worldguard.WorldGuardRegionBackend;
import bm.b0b0b0.SoulBlast.message.SoulBlastConsole;
import bm.b0b0b0.SoulBlast.service.economy.VaultEconomyBridge;
import bm.b0b0b0.SoulBlast.service.region.DisabledRegionBackend;
import bm.b0b0b0.SoulBlast.service.region.RegionBackend;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginIntegrationsReporter {

    private PluginIntegrationsReporter() {
    }

    public static void report(
            JavaPlugin plugin,
            RegionProtectionSettings regionProtection,
            RegionBackend regionBackend,
            EconomySettings economy,
            VaultEconomyBridge vault
    ) {
        report(plugin, regionProtection, regionBackend, economy, vault, null);
    }

    public static void report(
            JavaPlugin plugin,
            RegionProtectionSettings regionProtection,
            RegionBackend regionBackend,
            EconomySettings economy,
            VaultEconomyBridge vault,
            SoulBlastConsole console
    ) {
        RegionBackend backend = regionBackend == null ? DisabledRegionBackend.INSTANCE : regionBackend;
        reportWorldGuard(plugin, regionProtection, backend, console);
        reportVault(plugin, economy, vault, console);
    }

    private static void reportWorldGuard(
            JavaPlugin plugin,
            RegionProtectionSettings regionProtection,
            RegionBackend regionBackend,
            SoulBlastConsole console
    ) {
        if (!regionProtection.enabled) {
            log(console, plugin, "Защита регионов выключена в config.yml", false);
            return;
        }
        boolean active = WorldGuardRegionBackend.isWorldGuardPresent(plugin);
        if (!active) {
            if (isPluginInstalled(plugin, "WorldGuard")) {
                return;
            }
            if (regionProtection.requireWorldGuard) {
                log(console, plugin, "WorldGuard не установлен — защита спавна/регионов не работает", true);
            } else {
                log(console, plugin, "WorldGuard не установлен — ограничения WG не применяются", false);
            }
            return;
        }
        if (regionBackend.available()) {
            log(console, plugin, "WorldGuard — подключён", false, true);
            return;
        }
        log(console, plugin, "WorldGuard установлен, но API недоступен — проверь версию", true);
    }

    private static void reportVault(
            JavaPlugin plugin,
            EconomySettings economy,
            VaultEconomyBridge vault,
            SoulBlastConsole console
    ) {
        if (!economy.useVaultIfPresent) {
            log(console, plugin, "Vault в config выключен — монеты в гримуаре через опыт/TNT", false);
            return;
        }
        if (!isPluginActive(plugin, "Vault")) {
            log(console, plugin, "Vault не установлен — монеты в гримуаре недоступны (опыт и TNT работают)", false);
            return;
        }
        if (vault != null && vault.isActive()) {
            log(console, plugin, "Vault — экономика подключена", false, true);
            return;
        }
        log(console, plugin, "Vault без провайдера Economy — поставь EssentialsX и т.п.", true);
    }

    private static void log(JavaPlugin plugin, String message, boolean warning) {
        log(null, plugin, message, warning);
    }

    private static void log(JavaPlugin plugin, String message, boolean warning, boolean success) {
        log(null, plugin, message, warning, success);
    }

    private static void log(SoulBlastConsole console, JavaPlugin plugin, String message, boolean warning) {
        log(console, plugin, message, warning, false);
    }

    private static void log(
            SoulBlastConsole console,
            JavaPlugin plugin,
            String message,
            boolean warning,
            boolean success
    ) {
        if (console != null) {
            if (warning) {
                console.warn(message);
            } else if (success) {
                console.ok(message);
            } else {
                console.info(message);
            }
            return;
        }
        String line = "[Интеграции] " + message;
        if (warning) {
            plugin.getLogger().warning(line);
        } else {
            plugin.getLogger().info(line);
        }
    }

    public static boolean isPluginActive(JavaPlugin plugin, String name) {
        Plugin dependency = plugin.getServer().getPluginManager().getPlugin(name);
        return dependency != null && dependency.isEnabled();
    }

    public static boolean isPluginInstalled(JavaPlugin plugin, String name) {
        return plugin.getServer().getPluginManager().getPlugin(name) != null;
    }

}
