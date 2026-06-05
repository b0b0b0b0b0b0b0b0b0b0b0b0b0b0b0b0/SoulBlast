package bm.b0b0b0.SoulBlast.listener;

import bm.b0b0b0.SoulBlast.SoulBlast;
import bm.b0b0b0.SoulBlast.service.TsarDecoyBurstService;
import bm.b0b0b0.SoulBlast.util.PluginKeys;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.persistence.PersistentDataType;

public final class TsarDecoyTntListener implements Listener {

    private final PluginKeys keys;

    public TsarDecoyTntListener(SoulBlast plugin) {
        this.keys = plugin.pluginKeys();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrime(ExplosionPrimeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed primed)) {
            return;
        }
        if (!isDecoy(primed)) {
            return;
        }
        event.setCancelled(true);
        TsarDecoyBurstService.fizzle(primed);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed primed)) {
            return;
        }
        if (!isDecoy(primed)) {
            return;
        }
        event.setCancelled(true);
        TsarDecoyBurstService.fizzle(primed);
    }

    private boolean isDecoy(TNTPrimed primed) {
        return primed.getPersistentDataContainer().has(keys.tsarDecoy, PersistentDataType.BYTE);
    }

}
