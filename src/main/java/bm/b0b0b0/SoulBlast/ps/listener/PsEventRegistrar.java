package bm.b0b0b0.SoulBlast.ps.listener;

import bm.b0b0b0.SoulBlast.ps.PsModule;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class PsEventRegistrar {

    private static final String CREATE_EVENT = "dev.espi.protectionstones.event.PSCreateEvent";
    private static final String REMOVE_EVENT = "dev.espi.protectionstones.event.PSRemoveEvent";

    private final JavaPlugin plugin;
    private final PsModule module;

    public PsEventRegistrar(JavaPlugin plugin, PsModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    public void registerProtectionStonesEvents() {
        boolean createRegistered = register(
                CREATE_EVENT,
                EventPriority.HIGH,
                false,
                module.lifecycleListener()::handleCreate
        );
        boolean removeRegistered = register(
                REMOVE_EVENT,
                EventPriority.MONITOR,
                false,
                module.lifecycleListener()::handleRemove
        );
        if (!createRegistered || !removeRegistered) {
            plugin.getLogger().warning(
                    "[Интеграции] События ProtectionStones не найдены — голограммы при создании/снятии привата не работают"
            );
        } else if (module.debugEnabled()) {
            plugin.getLogger().info(
                    "[ProtectionStones+/debug] Слушатели PSCreateEvent и PSRemoveEvent зарегистрированы"
            );
        }
    }

    private boolean register(
            String className,
            EventPriority priority,
            boolean ignoreCancelled,
            java.util.function.Consumer<Event> handler
    ) {
        try {
            Class<? extends Event> eventClass = eventClass(className);
            Listener listener = new Listener() {
            };
            plugin.getServer().getPluginManager().registerEvent(
                    eventClass,
                    listener,
                    priority,
                    (registered, event) -> handler.accept(event),
                    plugin,
                    ignoreCancelled
            );
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Event> eventClass(String className) throws ClassNotFoundException {
        return (Class<? extends Event>) Class.forName(className);
    }

}
