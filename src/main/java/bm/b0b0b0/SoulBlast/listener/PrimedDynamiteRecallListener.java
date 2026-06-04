package bm.b0b0b0.SoulBlast.listener;

import bm.b0b0b0.SoulBlast.SoulBlast;
import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.service.PrimedDynamiteRecallService;
import bm.b0b0b0.SoulBlast.service.PrimedDynamiteService;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Optional;

public final class PrimedDynamiteRecallListener implements Listener {

    private final SoulBlast plugin;

    public PrimedDynamiteRecallListener(SoulBlast plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPreAttack(PrePlayerAttackEntityEvent event) {
        if (!(event.getAttacked() instanceof TNTPrimed primed)) {
            return;
        }
        PrimedDynamiteRecallService recallService = plugin.getPrimedDynamiteRecallService();
        PrimedDynamiteService primedService = plugin.getPrimedDynamiteService();
        if (recallService == null || primedService == null) {
            return;
        }
        Optional<DynamiteDefinition> definition = primedService.resolvePrimed(primed);
        if (definition.isEmpty()) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        PrimedDynamiteRecallService.RecallResult result = recallService.tryRecall(player, primed);
        if (result != PrimedDynamiteRecallService.RecallResult.NOT_PRIMED) {
            recallService.notify(player, result, definition.get());
        }
    }

}
