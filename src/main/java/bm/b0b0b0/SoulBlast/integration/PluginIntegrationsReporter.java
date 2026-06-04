package bm.b0b0b0.SoulBlast.integration;

import bm.b0b0b0.SoulBlast.config.EconomySettings;
import bm.b0b0b0.SoulBlast.config.RegionProtectionSettings;
import bm.b0b0b0.SoulBlast.integration.worldguard.WorldGuardRegionBackend;
import bm.b0b0b0.SoulBlast.service.economy.VaultEconomyBridge;
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
        reportWorldGuard(plugin, regionProtection, regionBackend);
        reportVault(plugin, economy, vault);
    }

    private static void reportWorldGuard(
            JavaPlugin plugin,
            RegionProtectionSettings regionProtection,
            RegionBackend regionBackend
    ) {
        if (!regionProtection.enabled) {
            plugin.getLogger().info("[Интеграции] Защита регионов выключена в config.yml");
            return;
        }
        boolean active = WorldGuardRegionBackend.isWorldGuardPresent(plugin);
        if (!active) {
            if (isPluginInstalled(plugin, "WorldGuard")) {
                return;
            }
            if (regionProtection.requireWorldGuard) {
                plugin.getLogger().warning(
                        "[Интеграции] WorldGuard не установлен — защита спавна/регионов для динамита не работает"
                );
            } else {
                plugin.getLogger().info(
                        "[Интеграции] WorldGuard не установлен — ограничения WG не применяются"
                );
            }
            return;
        }
        if (regionBackend.available()) {
            plugin.getLogger().info("[Интеграции] WorldGuard — подключён");
            return;
        }
        plugin.getLogger().warning(
                "[Интеграции] WorldGuard установлен, но API недоступен — проверьте версию WorldGuard"
        );
    }

    private static void reportVault(JavaPlugin plugin, EconomySettings economy, VaultEconomyBridge vault) {
        if (!economy.useVaultIfPresent) {
            return;
        }
        if (!isPluginActive(plugin, "Vault")) {
            plugin.getLogger().info(
                    "[Интеграции] Vault не установлен — покупки за монеты через Vault недоступны (опыт и TNT работают)"
            );
            return;
        }
        if (vault != null && vault.isActive()) {
            plugin.getLogger().info("[Интеграции] Vault — экономика подключена");
            return;
        }
        plugin.getLogger().warning(
                "[Интеграции] Vault установлен, но провайдер Economy не найден — подключите плагин экономики (EssentialsX и т.д.)"
        );
    }

    public static boolean isPluginActive(JavaPlugin plugin, String name) {
        Plugin dependency = plugin.getServer().getPluginManager().getPlugin(name);
        return dependency != null && dependency.isEnabled();
    }

    public static boolean isPluginInstalled(JavaPlugin plugin, String name) {
        return plugin.getServer().getPluginManager().getPlugin(name) != null;
    }

}
