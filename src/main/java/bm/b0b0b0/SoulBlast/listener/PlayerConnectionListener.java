package bm.b0b0b0.SoulBlast.listener;

import bm.b0b0b0.SoulBlast.service.DynamiteCooldownService;
import bm.b0b0b0.SoulBlast.service.PlayerProfileService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerConnectionListener implements Listener {

    private final PlayerProfileService profileService;
    private final DynamiteCooldownService cooldownService;

    public PlayerConnectionListener(PlayerProfileService profileService, DynamiteCooldownService cooldownService) {
        this.profileService = profileService;
        this.cooldownService = cooldownService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        profileService.preload(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldownService.clearPlayer(event.getPlayer().getUniqueId());
        profileService.unload(event.getPlayer());
    }

}
