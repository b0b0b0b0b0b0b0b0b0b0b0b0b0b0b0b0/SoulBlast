package bm.b0b0b0.SoulBlast.message;

import bm.b0b0b0.SoulBlast.SoulBlast;
import bm.b0b0b0.SoulBlast.config.EconomySettings;
import bm.b0b0b0.SoulBlast.config.RegionProtectionSettings;
import bm.b0b0b0.SoulBlast.integration.PluginIntegrationsReporter;
import bm.b0b0b0.SoulBlast.integration.worldguard.WorldGuardRegionBackend;
import bm.b0b0b0.SoulBlast.ps.PsModule;
import bm.b0b0b0.SoulBlast.service.economy.VaultEconomyBridge;
import bm.b0b0b0.SoulBlast.service.region.RegionBackend;
import org.bukkit.plugin.java.JavaPlugin;

public final class SoulBlastStartupBanner {

    private final SoulBlastConsole console;
    private final String version;

    private SoulBlastStartupBanner(JavaPlugin plugin) {
        this.console = new SoulBlastConsole();
        this.version = plugin.getPluginMeta().getVersion();
    }

    public static SoulBlastStartupBanner open(JavaPlugin plugin) {
        SoulBlastStartupBanner banner = new SoulBlastStartupBanner(plugin);
        banner.printHeader();
        return banner;
    }

    public SoulBlastConsole console() {
        return console;
    }

    public void step(String message) {
        console.info(message);
    }

    public void printHeader() {
        console.blank();
        console.separator();
        console.info("SoulBlast запускается на твоём сервере");
        console.info("Version:\u001B[90m " + version + " \u001B[0m| Author: \u001B[90mb0b0b0\u001B[0m");
        console.blank();
        console.info("Инициализация:");
    }

    public void afterCoreLoad(SoulBlast plugin) {
        int dynamites = plugin.dynamiteRegistry().all().size();
        step("Конфиги YAML загружены");
        step("Зарядов в реестре: \u001B[90m" + dynamites + "\u001B[0m");
        if (plugin.getDecayModule() != null) {
            String decayState = plugin.getDecayModule().isEnabled() ? "\u001B[32mвкл\u001B[0m" : "\u001B[90mвыкл\u001B[0m";
            step("Decay (постепенное разрушение): " + decayState);
        }
        step("Гримуар, команды, слушатели, взрывная очередь");
        step("Ожидание WorldGuard / ProtectionStones…");
    }

    public void finishIntegrations(SoulBlast plugin) {
        console.blank();
        console.info("Интеграции:");
        plugin.getCoreProtectBridge().reload(plugin.getPluginConfig().coreProtectIntegration);
        PluginIntegrationsReporter.report(
                plugin,
                plugin.getPluginConfig().regionProtection,
                plugin.regionProtectionService() == null
                        ? null
                        : plugin.regionProtectionService().regionBackend(),
                plugin.getPluginConfig().economy,
                plugin.vaultEconomyBridge(),
                plugin.getPluginConfig().coreProtectIntegration,
                plugin.getCoreProtectBridge(),
                console
        );
        PsModule psModule = plugin.getPsModule();
        if (psModule != null) {
            psModule.logIntegrationProblems(console);
            psModule.logIntegrationSuccess(console);
        }
        console.blank();
        console.ok("SoulBlast успешно загружен");
        console.separator();
        console.blank();
    }

    public void printDisable() {
        printShutdown();
    }

    public static void printShutdown() {
        SoulBlastConsole console = new SoulBlastConsole();
        console.blank();
        console.info("SoulBlast выгружен");
        console.blank();
    }

}
