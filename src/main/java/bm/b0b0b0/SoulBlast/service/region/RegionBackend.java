package bm.b0b0b0.SoulBlast.service.region;

import bm.b0b0b0.SoulBlast.config.RegionProtectionSettings;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public interface RegionBackend {

    boolean available();

    void registerIntegration();

    List<RegionVolume> protectedVolumes(World world, Location sample, Player player, RegionProtectionSettings settings);

    List<RegionVolume> dynamitePlacementVolumes(
            World world,
            Location sample,
            Player player,
            RegionProtectionSettings settings
    );

}
