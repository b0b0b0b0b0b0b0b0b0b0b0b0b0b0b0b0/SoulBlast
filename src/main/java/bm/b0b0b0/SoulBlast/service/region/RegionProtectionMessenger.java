package bm.b0b0b0.SoulBlast.service.region;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.message.MessageService;
import org.bukkit.entity.Player;

import java.util.Map;

public final class RegionProtectionMessenger {

    private final RegionProtectionService protection;
    private final MessageService messages;

    public RegionProtectionMessenger(RegionProtectionService protection, MessageService messages) {
        this.protection = protection;
        this.messages = messages;
    }

    public void sendBlocked(Player player, RegionProtectionService.RegionCheckResult result, DynamiteDefinition dynamite) {
        if (player == null) {
            return;
        }
        if ("worldguard-missing".equals(result.reason())) {
            messages.send(player, "region-worldguard-missing");
            return;
        }
        String region = result.regionName().isBlank() ? "?" : result.regionName();
        if ("blast".equals(result.reason())) {
            messages.send(player, "region-protected-blast", Map.of("region", region));
            return;
        }
        messages.send(player, "region-protected", Map.of(
                "region", region,
                "margin", String.valueOf(protection.marginBlocks(dynamite))
        ));
    }

}
