package bm.b0b0b0.SoulBlast;

import bm.b0b0b0.SoulBlast.command.SoulBlastCommand;
import bm.b0b0b0.SoulBlast.command.SoulGrimoireCommand;
import bm.b0b0b0.SoulBlast.config.*;
import bm.b0b0b0.SoulBlast.config.menu.MenuFileConfig;
import bm.b0b0b0.SoulBlast.decay.DecayModule;
import bm.b0b0b0.SoulBlast.decay.command.DecayBlocksCommand;
import bm.b0b0b0.SoulBlast.decay.service.DecayExplosionBridge;
import bm.b0b0b0.SoulBlast.gui.MenuItemGuard;
import bm.b0b0b0.SoulBlast.gui.menu.MenuIconFactory;
import bm.b0b0b0.SoulBlast.gui.menu.SoulGrimoireMenuService;
import bm.b0b0b0.SoulBlast.integration.DeferredIntegrationsListener;
import bm.b0b0b0.SoulBlast.integration.PluginIntegrationsReporter;
import bm.b0b0b0.SoulBlast.integration.coreprotect.CoreProtectBridge;
import bm.b0b0b0.SoulBlast.integration.worldguard.WorldGuardRegionBackend;
import bm.b0b0b0.SoulBlast.integration.worldguard.WorldGuardSoulblastEventBridge;
import bm.b0b0b0.SoulBlast.listener.*;
import bm.b0b0b0.SoulBlast.message.MessageService;
import bm.b0b0b0.SoulBlast.message.MessagesConfig;
import bm.b0b0b0.SoulBlast.message.SoulBlastStartupBanner;
import bm.b0b0b0.SoulBlast.ps.PsModule;
import bm.b0b0b0.SoulBlast.ps.service.PsExplosionBridge;
import bm.b0b0b0.SoulBlast.repository.DynamiteRegistry;
import bm.b0b0b0.SoulBlast.repository.PlayerProfileRepository;
import bm.b0b0b0.SoulBlast.repository.SqlitePlayerProfileRepository;
import bm.b0b0b0.SoulBlast.service.*;
import bm.b0b0b0.SoulBlast.service.economy.ExperienceCostService;
import bm.b0b0b0.SoulBlast.service.economy.VaultEconomyBridge;
import bm.b0b0b0.SoulBlast.service.region.DisabledRegionBackend;
import bm.b0b0b0.SoulBlast.service.region.RegionBackend;
import bm.b0b0b0.SoulBlast.service.region.RegionProtectionMessenger;
import bm.b0b0b0.SoulBlast.service.region.RegionProtectionService;
import bm.b0b0b0.SoulBlast.util.PluginKeys;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicBoolean;

public final class SoulBlast extends JavaPlugin {

    private PluginConfig pluginConfig;
    private MaterialGroupsFileConfig materialGroupsConfig;
    private DynamitesFileConfig dynamitesConfig;
    private MenuFileConfig menuConfig;
    private MessagesConfig messagesConfig;
    private YamlConfigLoader configLoader;
    private DynamiteRegistry dynamiteRegistry;
    private MessageService messageService;
    private PluginKeys pluginKeys;
    private DynamiteItemFactory itemFactory;
    private BlastResistanceService blastResistanceService;
    private CraftingRegistrar craftingRegistrar;
    private DynamiteVisualService dynamiteVisualService;
    private PrimedDynamiteService primedDynamiteService;
    private PrimedDynamiteRecallService primedDynamiteRecallService;
    private FuseMisfireResolver fuseMisfireResolver;
    private PrimedDynamiteMisfireService primedDynamiteMisfireService;
    private PrimedDynamiteDetonationService primedDynamiteDetonationService;
    private DecayModule decayModule;
    private PsModule psModule;
    private PlacedDynamiteTracker placedDynamiteTracker;
    private final AtomicBoolean integrationsBootstrapped = new AtomicBoolean(false);
    private ExplosionQueueService explosionQueueService;
    private TsarExplosionGate tsarExplosionGate;
    private SoulGrimoireMenuService grimoireMenuService;
    private SoulGrimoireCommand grimoireCommand;
    private PlayerProfileRepository playerProfileRepository;
    private PlayerProfileService playerProfileService;
    private VaultEconomyBridge vaultEconomyBridge;
    private ExperienceCostService experienceCostService;
    private DynamitePurchaseService dynamitePurchaseService;
    private VanillaTntInventoryService vanillaTntInventoryService;
    private MenuIconFactory menuIconFactory;
    private MenuItemGuard menuItemGuard;
    private RegionProtectionService regionProtectionService;
    private CoreProtectBridge coreProtectBridge;
    private RegionProtectionMessenger regionProtectionMessenger;
    private DynamiteCooldownService dynamiteCooldownService;
    private DynamiteCooldownMessenger dynamiteCooldownMessenger;
    private BukkitTask explosionTask;
    private BukkitTask hologramTask;
    private SoulBlastStartupBanner startupBanner;

    @Override
    public void onEnable() {
        startupBanner = SoulBlastStartupBanner.open(this);
        configLoader = new YamlConfigLoader(this);
        pluginKeys = new PluginKeys(this);
        menuItemGuard = new MenuItemGuard(pluginKeys);
        dynamiteRegistry = new DynamiteRegistry();
        blastResistanceService = new BlastResistanceService();
        itemFactory = new DynamiteItemFactory(pluginKeys);
        menuIconFactory = new MenuIconFactory(pluginKeys);
        decayModule = new DecayModule(this);
        decayModule.enable();
        placedDynamiteTracker = new PlacedDynamiteTracker();
        psModule = new PsModule(this);
        reloadPlugin(false);
        grimoireMenuService = new SoulGrimoireMenuService(this, dynamiteRegistry, menuIconFactory, playerProfileService);
        grimoireMenuService.reload(menuConfig);
        grimoireCommand = new SoulGrimoireCommand(messageService, grimoireMenuService);
        grimoireCommand.reload(menuConfig);
        registerListeners();
        registerCommands();
        startTasks();
        getServer().getPluginManager().registerEvents(new DeferredIntegrationsListener(this), this);
        if (startupBanner != null) {
            startupBanner.afterCoreLoad(this);
        }
        scheduleIntegrationBootstrap();
    }

    private void scheduleIntegrationBootstrap() {
        getServer().getScheduler().runTask(this, this::bootstrapIntegrations);
    }

    public void bootstrapIntegrations() {
        if (pluginConfig == null) {
            return;
        }
        if (!PluginIntegrationsReporter.isPluginActive(this, "WorldGuard")) {
            return;
        }
        reloadPlugin(startupBanner == null);
        integrationsBootstrapped.set(true);
        if (!integrationStartupComplete()) {
            return;
        }
        finishStartupBanner();
    }

    private boolean integrationStartupComplete() {
        ProtectionStonesIntegrationSettings psIntegration = pluginConfig.protectionStonesIntegration;
        if (!psIntegration.enabled) {
            return true;
        }
        if (!PluginIntegrationsReporter.isPluginInstalled(this, "ProtectionStones")) {
            return true;
        }
        return PluginIntegrationsReporter.isPluginActive(this, "ProtectionStones");
    }

    private void finishStartupBanner() {
        if (startupBanner == null) {
            return;
        }
        startupBanner.finishIntegrations(this);
        startupBanner = null;
    }

    @Override
    public void onDisable() {
        if (startupBanner != null) {
            startupBanner.printDisable();
        } else {
            SoulBlastStartupBanner.printShutdown();
        }
        if (craftingRegistrar != null) {
            craftingRegistrar.unregisterAll();
        }
        if (primedDynamiteService != null) {
            primedDynamiteService.shutdown();
        }
        if (explosionQueueService != null) {
            explosionQueueService.shutdown();
        }
        if (dynamiteVisualService != null) {
            dynamiteVisualService.shutdown();
        }
        if (playerProfileService != null) {
            playerProfileService.shutdown();
        }
        if (playerProfileRepository != null) {
            playerProfileRepository.shutdown();
        }
        if (decayModule != null) {
            decayModule.disable();
        }
        if (psModule != null) {
            psModule.disable();
        }
        cancelTasks();
    }

    public void reloadPlugin() {
        reloadPlugin(true);
    }

    public void reloadPlugin(boolean logIntegrations) {
        if (craftingRegistrar != null) {
            craftingRegistrar.unregisterAll();
        }
        pluginConfig = configLoader.load("config.yml", "config.yml", new PluginConfig());
        GeneralSettingsSafety.apply(this, pluginConfig.general);
        dynamitesConfig = configLoader.load(
                ConfigPaths.DYNAMITES,
                ConfigPaths.DYNAMITES,
                new DynamitesFileConfig(),
                "dynamites.yml"
        );
        materialGroupsConfig = configLoader.load(
                ConfigPaths.MATERIAL_GROUPS,
                ConfigPaths.MATERIAL_GROUPS,
                new MaterialGroupsFileConfig(),
                "material-groups.yml"
        );
        menuConfig = configLoader.load(
                ConfigPaths.MENU,
                ConfigPaths.MENU,
                new MenuFileConfig(),
                "menu.yml"
        );
        messagesConfig = configLoader.load(
                ConfigPaths.MESSAGES,
                ConfigPaths.MESSAGES,
                new MessagesConfig(),
                "messages.yml"
        );
        messageService = new MessageService(messagesConfig);
        if (coreProtectBridge == null) {
            coreProtectBridge = new CoreProtectBridge(this);
        }
        coreProtectBridge.reload(pluginConfig.coreProtectIntegration);
        if (psModule != null) {
            psModule.reload();
            psModule.enable();
            psModule.logIntegrationProblems();
        }
        boolean quietStartup = psModule == null || psModule.silentStartup();
        if (decayModule != null) {
            decayModule.reload(quietStartup);
            if (decayModule.explosionBridge() != null) {
                decayModule.explosionBridge().bindCoreProtect(coreProtectBridge);
            }
        }
        if (logIntegrations && !quietStartup && startupBanner == null) {
            getLogger().info("[Интеграции] Подключение WorldGuard / ProtectionStones после полного старта сервера");
            if (psModule != null) {
                psModule.logIntegrationSuccess();
            }
        }
        initPlayerCooldown();
        initRegionProtection();
        dynamiteRegistry.reload(DynamiteCatalog.build(dynamitesConfig));
        blastResistanceService.reload(pluginConfig, materialGroupsConfig);
        initPlayerData();
        initEconomy();
        EntityExplosionDamageService entityExplosionDamageService = new EntityExplosionDamageService();
        entityExplosionDamageService.reload(pluginConfig);
        ExplosionEntityEffectsService explosionEntityEffectsService = new ExplosionEntityEffectsService(coreProtectBridge);
        ExplosionBlockInclusion blockInclusion = new ExplosionBlockInclusion(blastResistanceService);
        ExplosionRaySampler raySampler = new ExplosionRaySampler(blastResistanceService, blockInclusion);
        ExplosionWaveSampler waveSampler = new ExplosionWaveSampler(blockInclusion);
        ExplosionVolumeSampler volumeSampler = new ExplosionVolumeSampler();
        CraterFillPlanner craterFillPlanner = new CraterFillPlanner();
        DecayExplosionBridge decayBridge = decayModule == null ? null : decayModule.explosionBridge();
        ExplosionLiquidSampler liquidSampler = new ExplosionLiquidSampler(coreProtectBridge);
        TsarGradualDrainService tsarGradualDrain = new TsarGradualDrainService(coreProtectBridge);
        ObsidianInstantShatterService obsidianShatter = new ObsidianInstantShatterService(
                blastResistanceService,
                decayModule == null ? null : decayModule.damageResolver(),
                decayBridge,
                coreProtectBridge
        );
        PsExplosionBridge psExplosionBridge = resolvePsExplosionBridge();
        ExplosionFireSupport explosionFireSupport = new ExplosionFireSupport(coreProtectBridge);
        BlockExplosionApplier blockApplier = new BlockExplosionApplier(
                blastResistanceService,
                explosionEntityEffectsService,
                regionProtectionService,
                decayBridge,
                obsidianShatter,
                psExplosionBridge,
                coreProtectBridge,
                explosionFireSupport
        );
        if (tsarExplosionGate == null) {
            tsarExplosionGate = new TsarExplosionGate(this);
        }
        if (primedDynamiteService != null) {
            primedDynamiteService.shutdown();
        }
        if (explosionQueueService != null) {
            explosionQueueService.shutdown();
        }
        ExplosionDebugTrace explosionDebugTrace = new ExplosionDebugTrace(this);
        explosionQueueService = new ExplosionQueueService(
                this,
                raySampler,
                waveSampler,
                volumeSampler,
                liquidSampler,
                craterFillPlanner,
                blockApplier,
                entityExplosionDamageService,
                explosionEntityEffectsService,
                new PostExplosionActionRunner(),
                tsarExplosionGate,
                explosionDebugTrace,
                explosionFireSupport,
                tsarGradualDrain
        );
        explosionQueueService.reload(pluginConfig);
        dynamiteVisualService = new DynamiteVisualService(this);
        FuseLightningService fuseLightningService = new FuseLightningService(this);
        primedDynamiteService = new PrimedDynamiteService(
                this,
                pluginKeys,
                dynamiteRegistry,
                dynamiteVisualService,
                fuseLightningService,
                messageService,
                pluginConfig.fuseRecall
        );
        primedDynamiteDetonationService = new PrimedDynamiteDetonationService(
                this,
                regionProtectionService,
                regionProtectionMessenger,
                dynamiteCooldownService,
                dynamiteCooldownMessenger,
                primedDynamiteService
        );
        fuseMisfireResolver = new FuseMisfireResolver();
        fuseMisfireResolver.reload(pluginConfig.fuseMisfire);
        primedDynamiteMisfireService = new PrimedDynamiteMisfireService(
                this,
                fuseMisfireResolver,
                primedDynamiteService,
                primedDynamiteDetonationService,
                messageService,
                pluginKeys
        );
        primedDynamiteService.bindMisfireService(primedDynamiteMisfireService);
        primedDynamiteService.bindDetonationService(primedDynamiteDetonationService);
        primedDynamiteRecallService = new PrimedDynamiteRecallService(
                pluginConfig.fuseRecall,
                primedDynamiteService,
                primedDynamiteMisfireService,
                itemFactory,
                messageService,
                pluginKeys
        );
        TsarWarheadService tsarWarheadService = new TsarWarheadService(
                pluginKeys,
                dynamiteRegistry,
                primedDynamiteService
        );
        primedDynamiteService.bindWarheadService(tsarWarheadService);
        craftingRegistrar = new CraftingRegistrar(this, dynamiteRegistry, itemFactory);
        craftingRegistrar.registerAll();
        menuIconFactory.setPurchaseService(dynamitePurchaseService);
        if (grimoireMenuService != null) {
            grimoireMenuService.reload(menuConfig);
        }
        if (grimoireCommand != null) {
            grimoireCommand.reload(menuConfig);
        }
        if (logIntegrations && !quietStartup && startupBanner == null) {
            PluginIntegrationsReporter.report(
                    this,
                    pluginConfig.regionProtection,
                    regionProtectionService == null ? DisabledRegionBackend.INSTANCE : regionProtectionService.regionBackend(),
                    pluginConfig.economy,
                    vaultEconomyBridge,
                    pluginConfig.coreProtectIntegration,
                    coreProtectBridge
            );
        }
        startTasks();
    }

    private void initPlayerData() {
        if (playerProfileRepository == null) {
            playerProfileRepository = new SqlitePlayerProfileRepository(this, pluginConfig.database);
            playerProfileRepository.initialize();
        }
        if (playerProfileService == null) {
            playerProfileService = new PlayerProfileService(
                    playerProfileRepository,
                    pluginConfig.general.defaultAutoIgnite
            );
        }
    }

    private void initPlayerCooldown() {
        if (dynamiteCooldownService == null) {
            dynamiteCooldownService = new DynamiteCooldownService();
        }
        if (tsarExplosionGate != null) {
            dynamiteCooldownService.bindTsarGate(tsarExplosionGate);
        }
        dynamiteCooldownService.reload(pluginConfig.playerCooldown);
        if (dynamiteCooldownMessenger == null) {
            dynamiteCooldownMessenger = new DynamiteCooldownMessenger(dynamiteCooldownService, messageService);
        }
    }

    private void initRegionProtection() {
        RegionBackend backend = WorldGuardRegionBackend.tryCreate(this, pluginConfig.regionProtection);
        if (backend == null) {
            backend = DisabledRegionBackend.INSTANCE;
        } else {
            backend.registerIntegration();
        }
        if (regionProtectionService == null) {
            regionProtectionService = new RegionProtectionService(pluginConfig.regionProtection, backend);
            regionProtectionMessenger = new RegionProtectionMessenger(regionProtectionService, messageService);
        } else {
            regionProtectionService.reload(pluginConfig.regionProtection, backend);
        }
        regionProtectionService.setPsRaidBypass(
                psModule == null ? null : psModule.createDynamiteRaidBypass(backend, pluginConfig.regionProtection)
        );
        if (backend.available()) {
            WorldGuardSoulblastEventBridge.register(
                    this,
                    itemFactory,
                    dynamiteRegistry,
                    placedDynamiteTracker,
                    pluginKeys
            );
            if (psModule != null) {
                psModule.ensureSoulblastWgOverrideListener();
            }
        }
    }

    private void initEconomy() {
        if (vaultEconomyBridge == null) {
            vaultEconomyBridge = new VaultEconomyBridge(this, pluginConfig.economy.useVaultIfPresent);
        }
        vaultEconomyBridge.reload();
        if (experienceCostService == null) {
            experienceCostService = new ExperienceCostService();
        }
        if (vanillaTntInventoryService == null) {
            vanillaTntInventoryService = new VanillaTntInventoryService(itemFactory);
        }
        if (dynamitePurchaseService == null) {
            dynamitePurchaseService = new DynamitePurchaseService(
                    vaultEconomyBridge,
                    experienceCostService,
                    pluginConfig.economy
            );
        } else {
            dynamitePurchaseService.reload(pluginConfig.economy);
        }
    }

    private void registerCommands() {
        PluginCommand main = getCommand("soulblast");
        if (main != null) {
            SoulBlastCommand executor = new SoulBlastCommand(
                    this,
                    dynamiteRegistry,
                    itemFactory,
                    messageService,
                    grimoireMenuService
            );
            main.setExecutor(executor);
            main.setTabCompleter(executor);
        }
        PluginCommand grimoire = getCommand("soulgrimoire");
        if (grimoire != null) {
            grimoire.setExecutor(grimoireCommand);
        }
        PluginCommand decayBlocks = getCommand("decayblocks");
        if (decayBlocks != null && decayModule != null) {
            decayBlocks.setExecutor(new DecayBlocksCommand(decayModule, messageService));
        }
        if (psModule != null) {
            psModule.registerCommands(messageService);
        }
    }

    private void registerListeners() {
        if (psModule != null) {
            psModule.ensureSoulblastWgOverrideListener();
        }
        getServer().getPluginManager().registerEvents(
                new SoulGrimoireMenuListener(
                        pluginKeys,
                        grimoireMenuService,
                        dynamiteRegistry,
                        itemFactory,
                        playerProfileService,
                        dynamitePurchaseService,
                        vanillaTntInventoryService,
                        messageService,
                        menuItemGuard,
                        dynamiteCooldownService,
                        dynamiteCooldownMessenger
                ),
                this
        );
        getServer().getPluginManager().registerEvents(
                new DynamitePlaceListener(
                        this,
                        dynamiteRegistry,
                        itemFactory,
                        playerProfileService,
                        placedDynamiteTracker,
                        regionProtectionService,
                        regionProtectionMessenger,
                        dynamiteCooldownMessenger
                ),
                this
        );
        getServer().getPluginManager().registerEvents(
                new DynamiteIgniteListener(
                        this,
                        pluginKeys,
                        dynamiteRegistry,
                        itemFactory,
                        placedDynamiteTracker,
                        messageService,
                        regionProtectionService,
                        regionProtectionMessenger,
                        dynamiteCooldownMessenger
                ),
                this
        );
        getServer().getPluginManager().registerEvents(new PrimedDynamiteRecallListener(this), this);
        getServer().getPluginManager().registerEvents(new PrimedDynamiteMisfireListener(this), this);
        getServer().getPluginManager().registerEvents(new PrimedDynamiteListener(this), this);
        getServer().getPluginManager().registerEvents(new TsarDecoyTntListener(this), this);
        getServer().getPluginManager().registerEvents(
                new PlayerConnectionListener(playerProfileService, dynamiteCooldownService),
                this
        );
    }

    private void startTasks() {
        if (explosionTask == null) {
            int interval = Math.max(1, pluginConfig.general.explosionQueueIntervalTicks);
            explosionTask = getServer().getScheduler().runTaskTimer(
                    this,
                    () -> {
                        ExplosionQueueService service = explosionQueueService;
                        if (service != null) {
                            service.processTick();
                        }
                    },
                    interval,
                    interval
            );
        }
        if (hologramTask == null) {
            hologramTask = getServer().getScheduler().runTaskTimer(
                    this,
                    () -> {
                        PrimedDynamiteService service = primedDynamiteService;
                        if (service != null) {
                            service.tickGlowAndHologram();
                        }
                    },
                    1L,
                    1L
            );
        }
    }

    public MenuFileConfig getMenuConfig() {
        return menuConfig;
    }

    public TsarExplosionGate getTsarExplosionGate() {
        return tsarExplosionGate;
    }

    public PrimedDynamiteService getPrimedDynamiteService() {
        return primedDynamiteService;
    }

    public PrimedDynamiteRecallService getPrimedDynamiteRecallService() {
        return primedDynamiteRecallService;
    }

    public PrimedDynamiteMisfireService getPrimedDynamiteMisfireService() {
        return primedDynamiteMisfireService;
    }

    public PrimedDynamiteDetonationService getPrimedDynamiteDetonationService() {
        return primedDynamiteDetonationService;
    }

    public DecayModule getDecayModule() {
        return decayModule;
    }

    public CoreProtectBridge getCoreProtectBridge() {
        if (coreProtectBridge == null) {
            coreProtectBridge = new CoreProtectBridge(this);
            if (pluginConfig != null) {
                coreProtectBridge.reload(pluginConfig.coreProtectIntegration);
            }
        }
        return coreProtectBridge;
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public PsModule getPsModule() {
        return psModule;
    }

    public MessageService messageService() {
        return messageService;
    }

    public boolean regionProtectionReady() {
        return regionProtectionService != null;
    }

    public RegionProtectionService regionProtectionService() {
        return regionProtectionService;
    }

    public DynamiteRegistry dynamiteRegistry() {
        return dynamiteRegistry;
    }

    public VaultEconomyBridge vaultEconomyBridge() {
        return vaultEconomyBridge;
    }

    public DynamiteItemFactory dynamiteItemFactory() {
        return itemFactory;
    }

    public PlacedDynamiteTracker placedDynamiteTracker() {
        return placedDynamiteTracker;
    }

    public PluginKeys pluginKeys() {
        return pluginKeys;
    }

    private PsExplosionBridge resolvePsExplosionBridge() {
        if (psModule == null || !psModule.active()) {
            return null;
        }
        return psModule.explosionBridge();
    }

    public ExplosionQueueService getExplosionQueueService() {
        return explosionQueueService;
    }

    private void cancelTasks() {
        if (explosionTask != null) {
            explosionTask.cancel();
            explosionTask = null;
        }
        if (hologramTask != null) {
            hologramTask.cancel();
            hologramTask = null;
        }
    }

}
