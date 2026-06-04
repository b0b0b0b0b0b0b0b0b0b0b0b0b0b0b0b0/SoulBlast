package bm.b0b0b0.SoulBlast.service.region;

import bm.b0b0b0.SoulBlast.config.RegionProtectionSettings;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public final class DisabledRegionBackend implements RegionBackend {

    public static final DisabledRegionBackend INSTANCE = new DisabledRegionBackend();

    private DisabledRegionBackend() {
    }

    @Override
    public boolean available() {
        return false;
    }

    @Override
    public void registerIntegration() {
    }

    @Override
    public List<RegionVolume> protectedVolumes(
            World world,
            Location sample,
            Player player,
            RegionProtectionSettings settings
    ) {
        return List.of();
    }

    @Override
    public List<RegionVolume> dynamitePlacementVolumes(
            World world,
            Location sample,
            Player player,
            RegionProtectionSettings settings
    ) {
        return List.of();
    }

}
