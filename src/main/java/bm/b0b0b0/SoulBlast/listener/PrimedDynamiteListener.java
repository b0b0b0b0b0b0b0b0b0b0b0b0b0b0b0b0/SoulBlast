package bm.b0b0b0.SoulBlast.listener;

import bm.b0b0b0.SoulBlast.SoulBlast;
import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.model.PrimedDynamiteSession;
import bm.b0b0b0.SoulBlast.service.PrimedDynamiteDetonationService;
import bm.b0b0b0.SoulBlast.service.PrimedDynamiteMisfireService;
import bm.b0b0b0.SoulBlast.service.PrimedDynamiteService;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import java.util.Optional;

public final class PrimedDynamiteListener implements Listener {

    private final PrimedDynamiteService primedService;
    private final PrimedDynamiteMisfireService misfireService;
    private final PrimedDynamiteDetonationService detonationService;

    public PrimedDynamiteListener(
            SoulBlast plugin,
            PrimedDynamiteService primedService,
            PrimedDynamiteMisfireService misfireService,
            PrimedDynamiteDetonationService detonationService
    ) {
        this.primedService = primedService;
        this.misfireService = misfireService;
        this.detonationService = detonationService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrime(ExplosionPrimeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed primed)) {
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
        if (primedService.resolvePrimed(primed).isPresent()) {
            event.setCancelled(true);
        }
    }

}
