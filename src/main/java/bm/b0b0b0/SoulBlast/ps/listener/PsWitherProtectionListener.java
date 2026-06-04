package bm.b0b0b0.SoulBlast.ps.listener;

import bm.b0b0b0.SoulBlast.ps.config.PsSettings;
import bm.b0b0b0.SoulBlast.ps.service.PsWitherBreakService;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public final class PsWitherProtectionListener implements Listener {

    private final PsSettings settings;
    private final PsWitherBreakService witherBreak;

    public PsWitherProtectionListener(PsSettings settings, PsWitherBreakService witherBreak) {
        this.settings = settings;
        this.witherBreak = witherBreak;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWitherBreakProtectionBlock(EntityChangeBlockEvent event) {
        if (!settings.allowInRegion.breakBlockWithWither) {
            return;
        }
        if (event.getEntityType() != EntityType.WITHER) {
            return;
        }
        if (witherBreak.tryBreak(event.getBlock())) {
            event.setCancelled(true);
        }
    }

}
