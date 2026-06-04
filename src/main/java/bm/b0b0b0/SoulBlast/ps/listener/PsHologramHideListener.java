package bm.b0b0b0.SoulBlast.ps.listener;

import bm.b0b0b0.SoulBlast.message.MessageService;
import bm.b0b0b0.SoulBlast.ps.PsModule;
import bm.b0b0b0.SoulBlast.ps.service.PsHologramHideSession;
import bm.b0b0b0.SoulBlast.ps.service.PsHologramVisibilityService;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PsHologramHideListener implements Listener {

    private final PsModule module;
    private final MessageService messages;

    public PsHologramHideListener(PsModule module, MessageService messages) {
        this.module = module;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        PsHologramHideSession session = module.hologramHideSession();
        if (!session.isArmed(player.getUniqueId())) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        PsHologramHideSession.Mode mode = session.consume(player.getUniqueId()).orElse(null);
        if (mode == null) {
            return;
        }
        if (!module.active() || !module.settings().hologramHide.enabled) {
            return;
        }
        event.setCancelled(true);
        PsHologramVisibilityService.Result result = module.hologramVisibilityService().applyHit(player, block, mode);
        String messageKey = switch (result) {
            case HIDDEN -> "ps-hologram-hidden";
            case SHOWN -> "ps-hologram-shown";
            case ALREADY_HIDDEN -> "ps-hologram-already-hidden";
            case ALREADY_VISIBLE -> "ps-hologram-already-visible";
            case NOT_OWNER -> "ps-hologram-not-owner";
            case NOT_TRACKED -> "ps-hologram-not-tracked";
            case NOT_PROTECT_BLOCK -> "ps-hologram-not-protect-block";
        };
        messages.send(player, messageKey);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        module.hologramHideSession().disarm(event.getPlayer().getUniqueId());
    }

}
