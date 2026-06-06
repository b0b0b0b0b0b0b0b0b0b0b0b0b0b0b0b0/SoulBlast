package bm.b0b0b0.SoulBlast.ps;

import bm.b0b0b0.SoulBlast.SoulBlast;
import bm.b0b0b0.SoulBlast.integration.PluginIntegrationsReporter;
import bm.b0b0b0.SoulBlast.integration.worldguard.WorldGuardRegionScan;
import bm.b0b0b0.SoulBlast.message.SoulBlastConsole;
import bm.b0b0b0.SoulBlast.ps.integration.PsConfiguredBlockInfo;
import bm.b0b0b0.SoulBlast.ps.config.PsConfigLoader;
import bm.b0b0b0.SoulBlast.ps.config.PsSettings;
import bm.b0b0b0.SoulBlast.ps.config.PsSettingsFileConfig;
import bm.b0b0b0.SoulBlast.ps.integration.LuckPermsBridge;
import bm.b0b0b0.SoulBlast.ps.integration.ProtectionStonesBridge;
import bm.b0b0b0.SoulBlast.ps.config.PsProtectionTypeDefinition;
import bm.b0b0b0.SoulBlast.message.MessageService;
import bm.b0b0b0.SoulBlast.ps.command.PsHologramCommand;
import bm.b0b0b0.SoulBlast.ps.listener.PsHologramHideListener;
import bm.b0b0b0.SoulBlast.ps.listener.PsEventRegistrar;
import bm.b0b0b0.SoulBlast.ps.listener.PsRegionRestoreListener;
import bm.b0b0b0.SoulBlast.ps.listener.PsLifecycleListener;
import bm.b0b0b0.SoulBlast.ps.listener.PsProtectionItemGlowListener;
import bm.b0b0b0.SoulBlast.ps.listener.PsRegionAllowanceListener;
import bm.b0b0b0.SoulBlast.ps.listener.PsSoulblastDynamiteWorldGuardListener;
import bm.b0b0b0.SoulBlast.ps.listener.PsWitherProtectionListener;
import bm.b0b0b0.SoulBlast.ps.repository.PsBlockPersistence;
import bm.b0b0b0.SoulBlast.ps.repository.PsBlockStore;
import bm.b0b0b0.SoulBlast.ps.repository.PsTypesDirectory;
import bm.b0b0b0.SoulBlast.ps.service.PsRegionRestoreService;
import bm.b0b0b0.SoulBlast.ps.service.PsDynamiteRaidService;
import bm.b0b0b0.SoulBlast.ps.service.PsExplosionBridge;
import bm.b0b0b0.SoulBlast.config.RegionProtectionSettings;
import bm.b0b0b0.SoulBlast.service.region.PsDynamiteRaidBypass;
import bm.b0b0b0.SoulBlast.service.region.RegionBackend;
import bm.b0b0b0.SoulBlast.ps.service.PsHologramHideSession;
import bm.b0b0b0.SoulBlast.ps.service.PsHologramService;
import bm.b0b0b0.SoulBlast.ps.service.PsHologramVisibilityService;
import bm.b0b0b0.SoulBlast.ps.service.PsLifecycleService;
import bm.b0b0b0.SoulBlast.ps.service.PsDebugLog;
import bm.b0b0b0.SoulBlast.ps.service.PsDurabilityTrace;
import bm.b0b0b0.SoulBlast.ps.service.PsTypeRegistry;
import bm.b0b0b0.SoulBlast.ps.service.PsTypesFileSynchronizer;
import bm.b0b0b0.SoulBlast.ps.service.PsTypesMerger;
import bm.b0b0b0.SoulBlast.ps.service.PsWitherBreakService;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class PsModule {

    private final SoulBlast plugin;
    private final PsConfigLoader configLoader;
    private final PsBlockStore store = new PsBlockStore();
    private PsBlockPersistence blockPersistence;
    private PsTypesDirectory typesDirectory;
    private final PsHologramService holograms = new PsHologramService();
    private final PsHologramHideSession hologramHideSession = new PsHologramHideSession();

    private PsSettings settings = new PsSettings();
    private PsHologramVisibilityService hologramVisibilityService;
    private ProtectionStonesBridge protectionStones;
    private LuckPermsBridge luckPerms;
    private PsTypeRegistry typeRegistry;
    private PsLifecycleService lifecycleService;
    private PsLifecycleListener lifecycleListener;
    private PsExplosionBridge explosionBridge;
    private PsDurabilityTrace durabilityTrace;
    private PsWitherBreakService witherBreakService;
    private PsEventRegistrar eventRegistrar;
    private Listener allowanceListener;
    private Listener witherListener;
    private Listener itemGlowListener;
    private Listener soulblastWgOverrideListener;
    private Listener regionRestoreListener;
    private Listener hologramHideListener;
    private boolean listenersRegistered;
    private boolean integrationEnabled;
    private int configuredTypeCount;
    private int autoDiscoveredTypeCount;
    private String startupStatusMessage = "";

    public PsModule(SoulBlast plugin) {
        this.plugin = plugin;
        this.configLoader = new PsConfigLoader(plugin);
        this.protectionStones = ProtectionStonesBridge.disabled();
        this.luckPerms = LuckPermsBridge.disabled();
    }

    public void enable() {
        if (!active()) {
            return;
        }
        if (!listenersRegistered) {
            registerListeners();
            listenersRegistered = true;
        }
    }

    public void reload() {
        try {
            reloadInternal();
        } catch (Throwable failure) {
            startupStatusMessage = "[Интеграции] ProtectionStones+ — ошибка загрузки, модуль деактивирован";
            plugin.getLogger().warning(startupStatusMessage + ": " + failure.getMessage());
            protectionStones = ProtectionStonesBridge.disabled();
            luckPerms = LuckPermsBridge.disabled();
        }
    }

    private void reloadInternal() {
        integrationEnabled = plugin.getPluginConfig().protectionStonesIntegration.enabled;
        if (!integrationEnabled) {
            protectionStones = ProtectionStonesBridge.disabled();
            luckPerms = LuckPermsBridge.disabled();
            explosionBridge = null;
            startupStatusMessage = resolveStartupStatusMessage();
            holograms.clearAll();
            store.clear();
            return;
        }
        PsSettingsFileConfig settingsConfig = configLoader.load(
                "ps/settings.yml",
                "settings.yml",
                new PsSettingsFileConfig()
        );
        settings = settingsConfig.settings;
        if (!settings.supportSoulblast) {
            plugin.getLogger().warning(
                    "[ProtectionStones+] support-soulblast=false — урон динамитов по приватам выключен. "
                            + "В ps/settings.yml: settings.support-soulblast: true"
            );
        }
        protectionStones = ProtectionStonesBridge.tryCreate(plugin);
        luckPerms = LuckPermsBridge.tryCreate(plugin);
        startupStatusMessage = resolveStartupStatusMessage();
        if (!active()) {
            explosionBridge = null;
            return;
        }
        if (typesDirectory == null) {
            typesDirectory = new PsTypesDirectory(plugin, configLoader.folder());
        }
        Map<String, PsProtectionTypeDefinition> loadedTypes = typesDirectory.loadAll();
        int newlyWritten = PsTypesFileSynchronizer.syncAndSave(
                plugin,
                typesDirectory,
                loadedTypes,
                protectionStones
        );
        if (newlyWritten > 0) {
            loadedTypes = typesDirectory.loadAll();
        }
        configuredTypeCount = loadedTypes.size();
        autoDiscoveredTypeCount = protectionStones.listConfiguredBlocks().size();
        typeRegistry = new PsTypeRegistry(protectionStones);
        Map<String, PsProtectionTypeDefinition> registryTypes = PsTypesMerger.forRegistry(loadedTypes);
        if (registryTypes.isEmpty()) {
            registryTypes = PsTypesMerger.runtimeDefaults(protectionStones);
        }
        typeRegistry.reload(PsTypesMerger.normalizeKeys(registryTypes));
        if (blockPersistence == null) {
            blockPersistence = new PsBlockPersistence(plugin, configLoader);
        }
        blockPersistence.loadInto(store);
        hologramVisibilityService = new PsHologramVisibilityService(
                protectionStones,
                typeRegistry,
                store,
                holograms,
                blockPersistence
        );
        PsDebugLog debugLog = new PsDebugLog(plugin, settings);
        durabilityTrace = new PsDurabilityTrace(plugin, settings);
        debugLog.logReload(protectionStones, loadedTypes, newlyWritten);
        lifecycleService = new PsLifecycleService(
                plugin,
                settings,
                protectionStones,
                luckPerms,
                typeRegistry,
                store,
                holograms,
                debugLog,
                blockPersistence
        );
        lifecycleListener = new PsLifecycleListener(
                settings,
                protectionStones,
                typeRegistry,
                lifecycleService,
                debugLog
        );
        explosionBridge = new PsExplosionBridge(
                plugin,
                settings,
                typeRegistry,
                store,
                holograms,
                protectionStones,
                lifecycleService,
                blockPersistence,
                debugLog,
                durabilityTrace,
                plugin.getCoreProtectBridge()
        );
        scheduleRegionRestore(debugLog);
        witherBreakService = new PsWitherBreakService(
                typeRegistry,
                store,
                protectionStones,
                lifecycleService
        );
        ensureSoulblastWgOverrideListener();
    }

    public void logIntegrationProblems() {
        logIntegrationProblems(null);
    }

    public void logIntegrationProblems(SoulBlastConsole console) {
        if (!integrationEnabled) {
            return;
        }
        if (startupStatusMessage == null || startupStatusMessage.isBlank()) {
            return;
        }
        if (console != null) {
            console.warn(startupStatusMessage);
            return;
        }
        plugin.getLogger().warning(startupStatusMessage);
    }

    public void logIntegrationSuccess() {
        logIntegrationSuccess(null);
    }

    public void logIntegrationSuccess(SoulBlastConsole console) {
        if (!integrationEnabled || !active()) {
            return;
        }
        if (settings.silentStartup && console == null) {
            return;
        }
        if (console != null) {
            logIntegrationSuccessToConsole(console);
            return;
        }
        String line = "ProtectionStones+ — типов в ps/types/: "
                + configuredTypeCount
                + ", блоков PS: "
                + autoDiscoveredTypeCount
                + ", support-soulblast="
                + settings.supportSoulblast;
        plugin.getLogger().info("[Интеграции] " + line);
        if (!luckPermsAvailable()) {
            plugin.getLogger().info(
                    "[Интеграции] LuckPerms не установлен — %owner_prefix% / %owner_suffix% в голограммах пустые"
            );
        }
    }

    private void logIntegrationSuccessToConsole(SoulBlastConsole console) {
        int psRegionCount = WorldGuardRegionScan.countProtectionStoneRegions(plugin, protectionStones);
        int trackedRegions = store.size();
        console.ok("ProtectionStones+ — активен");
        List<PsConfiguredBlockInfo> blocks = protectionStones.listConfiguredBlocks().stream()
                .sorted(Comparator.comparing(PsConfiguredBlockInfo::alias))
                .toList();
        if (blocks.isEmpty()) {
            console.detail("Блоки привата: (нет в конфиге ProtectionStones)");
        } else {
            console.info("Блоки привата (ProtectionStones):");
            for (PsConfiguredBlockInfo block : blocks) {
                String material = block.material() == null ? "?" : block.material().name();
                console.detail("  - " + material + " (" + block.alias() + ")");
            }
        }
        console.info(
                "Регионов PS на сервере: \u001B[90m"
                        + psRegionCount
                        + "\u001B[0m | записей SoulBlast (ps/regions/): \u001B[90m"
                        + trackedRegions
                        + "\u001B[0m | типов ps/types/: \u001B[90m"
                        + configuredTypeCount
                        + "\u001B[0m"
        );
        if (!settings.supportSoulblast) {
            console.warn("support-soulblast=false — урон динамитов по приватам выключен");
        }
        if (!luckPermsAvailable()) {
            console.detail("LuckPerms не установлен — префиксы в голограммах пустые");
        }
    }

    public PsDynamiteRaidBypass createDynamiteRaidBypass(
            RegionBackend regionBackend,
            RegionProtectionSettings regionSettings
    ) {
        if (!active()) {
            return null;
        }
        return new PsDynamiteRaidService(
                settings,
                typeRegistry,
                protectionStones,
                regionBackend,
                regionSettings
        );
    }

    public boolean integrationEnabled() {
        return integrationEnabled;
    }

    public int configuredTypeCount() {
        return configuredTypeCount;
    }

    public int autoDiscoveredTypeCount() {
        return autoDiscoveredTypeCount;
    }

    public boolean silentStartup() {
        return settings.silentStartup;
    }

    public PsSettings settings() {
        return settings;
    }

    public PsHologramHideSession hologramHideSession() {
        return hologramHideSession;
    }

    public PsHologramVisibilityService hologramVisibilityService() {
        return hologramVisibilityService;
    }

    public void registerCommands(MessageService messages) {
        PluginCommand command = plugin.getCommand("psholo");
        if (command == null) {
            return;
        }
        PsHologramCommand executor = new PsHologramCommand(this, messages);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    public boolean debugEnabled() {
        return settings.debug;
    }

    public boolean luckPermsAvailable() {
        return luckPerms != null && luckPerms.available();
    }

    public String startupStatusMessage() {
        return startupStatusMessage;
    }

    private String resolveStartupStatusMessage() {
        if (!integrationEnabled) {
            return null;
        }
        if (!PluginIntegrationsReporter.isPluginActive(plugin, "ProtectionStones")) {
            if (PluginIntegrationsReporter.isPluginInstalled(plugin, "ProtectionStones")) {
                return null;
            }
            return "[Интеграции] ProtectionStones не найден — модуль деактивирован";
        }
        if (!protectionStones.available()) {
            return "[Интеграции] ProtectionStones: API 2.10+ недоступен — модуль деактивирован";
        }
        return null;
    }

    public void disable() {
        hologramHideSession.clearAll();
        if (hologramHideListener != null) {
            HandlerList.unregisterAll(hologramHideListener);
            hologramHideListener = null;
        }
        if (blockPersistence != null) {
            blockPersistence.saveFrom(store);
        }
        holograms.clearAll();
        store.clear();
        if (allowanceListener != null) {
            HandlerList.unregisterAll(allowanceListener);
        }
        if (witherListener != null) {
            HandlerList.unregisterAll(witherListener);
        }
        if (itemGlowListener != null) {
            HandlerList.unregisterAll(itemGlowListener);
        }
        if (soulblastWgOverrideListener != null) {
            HandlerList.unregisterAll(soulblastWgOverrideListener);
            soulblastWgOverrideListener = null;
        }
        if (regionRestoreListener != null) {
            HandlerList.unregisterAll(regionRestoreListener);
            regionRestoreListener = null;
        }
        listenersRegistered = false;
    }

    public void restoreRegionsInWorld(World world) {
        if (!active() || typeRegistry == null || blockPersistence == null) {
            return;
        }
        createRegionRestoreService().restoreInWorld(world);
    }

    public void restoreRegionsInChunk(Chunk chunk) {
        if (!active() || typeRegistry == null || blockPersistence == null) {
            return;
        }
        createRegionRestoreService().restoreInChunk(chunk);
    }

    public boolean active() {
        return integrationEnabled && protectionStones.available();
    }

    public boolean isProtectBlock(Block block) {
        return active() && protectionStones.isProtectBlock(block);
    }

    public PsExplosionBridge explosionBridge() {
        return explosionBridge;
    }

    public PsDurabilityTrace durabilityTrace() {
        return durabilityTrace;
    }

    public PsBlockStore blockStore() {
        return store;
    }

    public PsLifecycleListener lifecycleListener() {
        return lifecycleListener;
    }

    private void registerListeners() {
        eventRegistrar = new PsEventRegistrar(plugin, this);
        eventRegistrar.registerProtectionStonesEvents();
        if (anyAllowanceEnabled()) {
            allowanceListener = new PsRegionAllowanceListener(settings, protectionStones);
            plugin.getServer().getPluginManager().registerEvents(allowanceListener, plugin);
        }
        if (settings.allowInRegion.breakBlockWithWither
                && typeRegistry != null) {
            witherListener = new PsWitherProtectionListener(settings, witherBreakService);
            plugin.getServer().getPluginManager().registerEvents(witherListener, plugin);
        }
        if (anyItemGlowEnabled()) {
            itemGlowListener = new PsProtectionItemGlowListener(plugin, protectionStones, typeRegistry);
            plugin.getServer().getPluginManager().registerEvents(itemGlowListener, plugin);
        }
        ensureSoulblastWgOverrideListener();
        regionRestoreListener = new PsRegionRestoreListener(this);
        plugin.getServer().getPluginManager().registerEvents(regionRestoreListener, plugin);
        if (settings.hologramHide.enabled && hologramVisibilityService != null) {
            if (hologramHideListener != null) {
                HandlerList.unregisterAll(hologramHideListener);
            }
            MessageService messages = plugin.messageService();
            if (messages != null) {
                hologramHideListener = new PsHologramHideListener(this, messages);
                plugin.getServer().getPluginManager().registerEvents(hologramHideListener, plugin);
            }
        }
    }

    private PsRegionRestoreService createRegionRestoreService() {
        return new PsRegionRestoreService(
                plugin,
                protectionStones,
                luckPerms,
                typeRegistry,
                store,
                holograms,
                blockPersistence
        );
    }

    private void scheduleRegionRestore(PsDebugLog debugLog) {
        PsRegionRestoreService restore = createRegionRestoreService();
        scheduleRestorePass(restore, debugLog, 1L);
        scheduleRestorePass(restore, debugLog, 20L);
        scheduleRestorePass(restore, debugLog, 40L);
        scheduleRestorePass(restore, debugLog, 100L);
        scheduleRestorePass(restore, debugLog, 200L);
    }

    private void scheduleRestorePass(PsRegionRestoreService restore, PsDebugLog debugLog, long delayTicks) {
        Runnable task = () -> {
            int fromDisk = restore.restoreFromDisk();
            int discovered = restore.discoverMissing();
            if (fromDisk > 0) {
                debugLog.line("восстановлено голограмм из ps/regions/: " + fromDisk);
            }
            if (discovered > 0) {
                debugLog.line("обнаружено PS-блоков без файла в ps/regions/: " + discovered);
            }
        };
        if (delayTicks <= 1L) {
            plugin.getServer().getScheduler().runTask(plugin, task);
        } else {
            plugin.getServer().getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public void ensureSoulblastWgOverrideListener() {
        if (!plugin.regionProtectionReady()
                || !plugin.regionProtectionService().regionBackend().available()) {
            if (soulblastWgOverrideListener != null) {
                HandlerList.unregisterAll(soulblastWgOverrideListener);
                soulblastWgOverrideListener = null;
            }
            return;
        }
        if (soulblastWgOverrideListener != null) {
            return;
        }
        soulblastWgOverrideListener = new PsSoulblastDynamiteWorldGuardListener(
                plugin.dynamiteRegistry(),
                plugin.dynamiteItemFactory(),
                plugin.placedDynamiteTracker(),
                plugin.pluginKeys()
        );
        plugin.getServer().getPluginManager().registerEvents(soulblastWgOverrideListener, plugin);
    }

    private boolean anyItemGlowEnabled() {
        if (typeRegistry == null) {
            return false;
        }
        for (PsProtectionTypeDefinition type : typeRegistry.allTypes()) {
            if (type.itemGlow.enabled) {
                return true;
            }
        }
        return false;
    }

    private boolean anyAllowanceEnabled() {
        if (!integrationEnabled) {
            return false;
        }
        var allow = settings.allowInRegion;
        return allow.fallingBlock
                || allow.breakBlockWithWither
                || allow.useFireArrowToIgniteTnt
                || allow.usePiston
                || allow.useSpawnEggs
                || allow.mineCart.open
                || allow.mineCart.hookUp;
    }

}
