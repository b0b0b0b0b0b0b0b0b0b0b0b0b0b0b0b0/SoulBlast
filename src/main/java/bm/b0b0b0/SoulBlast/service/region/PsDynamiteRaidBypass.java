package bm.b0b0b0.SoulBlast.service.region;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface PsDynamiteRaidBypass {

    boolean permits(Location location, Player player);

}
