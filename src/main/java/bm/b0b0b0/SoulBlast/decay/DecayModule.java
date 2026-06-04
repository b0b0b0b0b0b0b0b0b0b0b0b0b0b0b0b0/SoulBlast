package bm.b0b0b0.SoulBlast.decay;

import bm.b0b0b0.SoulBlast.SoulBlast;
import bm.b0b0b0.SoulBlast.decay.config.DecayBlocksFileConfig;
import bm.b0b0b0.SoulBlast.decay.config.DecayConfigLoader;
import bm.b0b0b0.SoulBlast.decay.config.DecayDamageSourcesFileConfig;
import bm.b0b0b0.SoulBlast.decay.config.DecayGeneralFileConfig;
import bm.b0b0b0.SoulBlast.decay.config.DecayGeneralSettings;
import bm.b0b0b0.SoulBlast.decay.config.DecayMessagesFileConfig;
import bm.b0b0b0.SoulBlast.decay.gui.DecayBlocksMenuService;
import bm.b0b0b0.SoulBlast.decay.listener.DecayBlocksMenuListener;
import bm.b0b0b0.SoulBlast.decay.listener.DecayProtectionListener;
import bm.b0b0b0.SoulBlast.decay.repository.DecayingBlockStore;
import bm.b0b0b0.SoulBlast.decay.service.DecayBlockBreaker;
import bm.b0b0b0.SoulBlast.decay.service.DecayBlockRegistry;
import bm.b0b0b0.SoulBlast.decay.service.DecayCrackBroadcaster;
import bm.b0b0b0.SoulBlast.decay.service.DecayDamageResolver;
import bm.b0b0b0.SoulBlast.decay.service.DecayExplosionBridge;
import bm.b0b0b0.SoulBlast.decay.service.DecayRegenerationService;
import bm.b0b0b0.SoulBlast.decay.service.DecayTickService;
import org.bukkit.scheduler.BukkitTask;

public final class DecayModule {

    private final SoulBlast plugin;
    private final DecayConfigLoader configLoader;
    private final DecayingBlockStore store = new DecayingBlockStore();
    private final DecayBlockRegistry registry = new DecayBlockRegistry();
    private final DecayDamageResolver damageResolver = new DecayDamageResolver();
    private final DecayBlockBreaker breaker = new DecayBlockBreaker();
    private final DecayCrackBroadcaster crackBroadcaster = new DecayCrackBroadcaster(store);

    private DecayGeneralSettings general = new DecayGeneralSettings();
    private DecayMessagesFileConfig messages = new DecayMessagesFileConfig();
    private DecayExplosionBridge explosionBridge;
    private DecayTickService tickService;
    private DecayRegenerationService regenerationService;
    private DecayBlocksMenuService menuService;
    private BukkitTask tickTask;
    private boolean listenersRegistered;

    public DecayModule(SoulBlast plugin) {
        this.plugin = plugin;
        this.configLoader = new DecayConfigLoader(plugin);
    }

    public void enable() {
        if (!listenersRegistered) {
            plugin.getServer().getPluginManager().registerEvents(new DecayProtectionListener(this), plugin);
            plugin.getServer().getPluginManager().registerEvents(new DecayBlocksMenuListener(this), plugin);
            listenersRegistered = true;
        }
    }

    public void reload() {
        reload(false);
    }

    public void reload(boolean quietStartup) {
        DecayGeneralFileConfig generalConfig = configLoader.load(
                "decay/general.yml",
                "general.yml",
                new DecayGeneralFileConfig()
        );
        DecayBlocksFileConfig blocksConfig = configLoader.load(
                "decay/blocks.yml",
                "blocks.yml",
                new DecayBlocksFileConfig()
        );
        DecayDamageSourcesFileConfig sourcesConfig = configLoader.load(
                "decay/damage-sources.yml",
                "damage-sources.yml",
                new DecayDamageSourcesFileConfig()
        );
        messages = configLoader.load(
                "decay/messages.yml",
                "messages.yml",
                new DecayMessagesFileConfig()
        );
        general = generalConfig.general;
        registry.reload(blocksConfig);
        damageResolver.reload(sourcesConfig, general);
        crackBroadcaster.reload(general);
        regenerationService = new DecayRegenerationService(store, crackBroadcaster);
        explosionBridge = new DecayExplosionBridge(
                general,
                registry,
                damageResolver,
                store,
                breaker,
                crackBroadcaster
        );
        if (!quietStartup) {
            plugin.getLogger().info(
                    "Decay: " + registry.menuEntries().size() + " menu types, "
                            + registry.explosionMaterialCount() + " explosion materials, enabled=" + general.enabled
            );
        }
        tickService = new DecayTickService(general, store, crackBroadcaster, regenerationService);
        menuService = new DecayBlocksMenuService(plugin, messages, registry);
        crackBroadcaster.refreshAll();
        restartTickTask();
    }

    public void disable() {
        cancelTickTask();
        crackBroadcaster.clearAllViewers();
        store.clear();
    }

    public boolean isEnabled() {
        return general.enabled;
    }

    public DecayExplosionBridge explosionBridge() {
        return explosionBridge;
    }

    public DecayDamageResolver damageResolver() {
        return damageResolver;
    }

    public DecayBlockRegistry registry() {
        return registry;
    }

    public DecayingBlockStore store() {
        return store;
    }

    public DecayBlocksMenuService menuService() {
        return menuService;
    }

    private void restartTickTask() {
        cancelTickTask();
        int interval = Math.max(1, general.tickInterval);
        tickTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (tickService != null) {
                tickService.tick();
            }
        }, interval, interval);
    }

    private void cancelTickTask() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
    }

}
