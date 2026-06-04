package bm.b0b0b0.SoulBlast.listener;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.model.PrimedDynamiteSession;
import bm.b0b0b0.SoulBlast.message.MessageService;
import bm.b0b0b0.SoulBlast.service.PrimedDynamiteMisfireService;
import bm.b0b0b0.SoulBlast.service.PrimedDynamiteService;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PrimedDynamiteMisfireListener implements Listener {

    private final PrimedDynamiteService primedService;
    private final PrimedDynamiteMisfireService misfireService;
    private final MessageService messages;
    private final ConcurrentHashMap<UUID, Long> activateCooldown = new ConcurrentHashMap<>();

    public PrimedDynamiteMisfireListener(
            PrimedDynamiteService primedService,
            PrimedDynamiteMisfireService misfireService,
            MessageService messages
    ) {
        this.primedService = primedService;
        this.misfireService = misfireService;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        handleActivate(event.getPlayer(), event.getRightClicked(), event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        handleActivate(event.getPlayer(), event.getRightClicked(), event);
    }

    private void handleActivate(Player player, org.bukkit.entity.Entity clicked, Event event) {
        if (!(clicked instanceof TNTPrimed primed)) {
            return;
        }
        Optional<DynamiteDefinition> definition = primedService.resolvePrimed(primed);
        if (definition.isEmpty()) {
            return;
        }
        Optional<PrimedDynamiteSession> session = primedService.session(primed.getUniqueId());
        if (session.isEmpty() || !session.get().isDudActive()) {
            return;
        }
        long now = System.currentTimeMillis();
        Long last = activateCooldown.get(player.getUniqueId());
        if (last != null && now - last < 350L) {
            return;
        }
        activateCooldown.put(player.getUniqueId(), now);
        if (event instanceof Cancellable cancellable) {
            cancellable.setCancelled(true);
        }
        PrimedDynamiteMisfireService.ActivateAttemptResult result = misfireService.tryActivate(player, primed);
        notify(player, result, definition.get());
    }

    private void notify(Player player, PrimedDynamiteMisfireService.ActivateAttemptResult result, DynamiteDefinition definition) {
        switch (result) {
            case NOT_OWNER -> messages.send(player, "fuse-misfire-not-owner");
            case FIZZLE -> messages.send(player, "fuse-misfire-fizzle", Map.of("display", definition.item.displayName));
            default -> {
            }
        }
    }

}
