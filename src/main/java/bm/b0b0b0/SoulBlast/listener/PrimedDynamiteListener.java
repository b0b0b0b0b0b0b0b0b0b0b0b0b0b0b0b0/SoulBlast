package bm.b0b0b0.SoulBlast.listener;

import bm.b0b0b0.SoulBlast.SoulBlast;
import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.model.PrimedDynamiteSession;
import bm.b0b0b0.SoulBlast.service.PrimedDynamiteDetonationService;
import bm.b0b0b0.SoulBlast.service.PrimedDynamiteMisfireService;
import bm.b0b0b0.SoulBlast.service.PrimedDynamiteService;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import java.util.Optional;

public final class PrimedDynamiteListener implements Listener {

    private final SoulBlast plugin;

    public PrimedDynamiteListener(SoulBlast plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrime(ExplosionPrimeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed primed)) {
            return;
        }
        PrimedDynamiteService primedService = plugin.getPrimedDynamiteService();
        PrimedDynamiteMisfireService misfireService = plugin.getPrimedDynamiteMisfireService();
        PrimedDynamiteDetonationService detonationService = plugin.getPrimedDynamiteDetonationService();
        if (primedService == null || misfireService == null || detonationService == null) {
            return;
        }
        Optional<DynamiteDefinition> definition = primedService.resolvePrimed(primed);
        if (definition.isEmpty()) {
            return;
        }
        event.setCancelled(true);
        Optional<PrimedDynamiteSession> session = primedService.session(primed.getUniqueId());
        if (session.isEmpty()) {
            primed.remove();
            return;
        }
        PrimedDynamiteSession active = session.get();
        if (misfireService.shouldEnterDudOnPrime(active, definition.get())) {
            if (!active.isDudActive()) {
                misfireService.enterDudState(primed, active);
            } else {
                misfireService.maintainDudFuse(primed, active);
            }
            return;
        }
        detonationService.detonate(primed, definition.get());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed primed)) {
            return;
        }
        PrimedDynamiteService primedService = plugin.getPrimedDynamiteService();
        if (primedService == null) {
            return;
        }
        if (primedService.resolvePrimed(primed).isPresent()) {
            event.setCancelled(true);
        }
    }

}
