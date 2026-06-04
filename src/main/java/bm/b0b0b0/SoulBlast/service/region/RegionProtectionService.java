package bm.b0b0b0.SoulBlast.service.region;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.config.RegionProtectionSettings;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public final class RegionProtectionService {

    private RegionProtectionSettings settings;
    private RegionBackend backend;
    private PsDynamiteRaidBypass psRaidBypass;

    public RegionProtectionService(RegionProtectionSettings settings, RegionBackend backend) {
        this.settings = settings;
        this.backend = backend;
    }

    public void reload(RegionProtectionSettings settings, RegionBackend backend) {
        this.settings = settings;
        this.backend = backend;
    }

    public void setPsRaidBypass(PsDynamiteRaidBypass psRaidBypass) {
        this.psRaidBypass = psRaidBypass;
    }

    public RegionBackend regionBackend() {
        return backend;
    }

    public boolean worldGuardRequiredButMissing() {
        return settings.enabled && settings.requireWorldGuard && !backend.available();
    }

    public boolean allowsPlacement(Location location, DynamiteDefinition dynamite, Player player) {
        return evaluate(location, dynamite, player).permitted();
    }

    public boolean allowsExplosionBlock(Location blockLocation, DynamiteDefinition dynamite, Player player) {
        if (!settings.enabled || !worldAllowed(blockLocation.getWorld())) {
            return true;
        }
        if (hasBypass(player)) {
            return true;
        }
        if (permitsPsRaid(blockLocation, player)) {
            return true;
        }
        if (!backend.available()) {
            return !settings.requireWorldGuard;
        }
        List<RegionVolume> volumes;
        try {
            volumes = backend.protectedVolumes(
                    blockLocation.getWorld(),
                    blockLocation,
                    player,
                    settings
            );
        } catch (RuntimeException exception) {
            return true;
        }
        if (volumes.isEmpty()) {
            return true;
        }
        int x = blockLocation.getBlockX();
        int y = blockLocation.getBlockY();
        int z = blockLocation.getBlockZ();
        for (RegionVolume volume : volumes) {
            if (volume.contains(x, y, z)) {
                return false;
            }
        }
        return true;
    }

    public RegionCheckResult evaluate(Location location, DynamiteDefinition dynamite, Player player) {
        if (!settings.enabled || !worldAllowed(location.getWorld())) {
            return RegionCheckResult.permit();
        }
        if (hasBypass(player)) {
            return RegionCheckResult.permit();
        }
        if (permitsPsRaid(location, player)) {
            return RegionCheckResult.permit();
        }
        if (!backend.available()) {
            if (settings.requireWorldGuard) {
                return RegionCheckResult.denied("worldguard-missing", "");
            }
            return RegionCheckResult.permit();
        }
        List<RegionVolume> volumes;
        try {
            volumes = backend.dynamitePlacementVolumes(
                    location.getWorld(),
                    location,
                    player,
                    settings
            );
        } catch (RuntimeException exception) {
            return RegionCheckResult.permit();
        }
        if (volumes.isEmpty()) {
            return RegionCheckResult.permit();
        }
        int margin = marginBlocks(dynamite);
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        for (RegionVolume volume : volumes) {
            if (volume.contains(x, y, z)) {
                return RegionCheckResult.denied("inside", volume.name());
            }
            double distance = volume.distanceToBoundary(x, y, z);
            if (distance < margin) {
                return RegionCheckResult.denied("buffer", volume.name());
            }
        }
        if (settings.checkExplosionFootprint && explosionTouchesProtected(location, dynamite, volumes)) {
            return RegionCheckResult.denied("blast", nearestTouchingRegion(location, dynamite, volumes));
        }
        return RegionCheckResult.permit();
    }

    public int marginBlocks(DynamiteDefinition dynamite) {
        float radius = Math.max(0.0f, dynamite.explosion.radius);
        double margin = radius * Math.max(0.0, settings.marginRadiusMultiplier);
        return (int) Math.ceil(margin);
    }

    private boolean explosionTouchesProtected(Location center, DynamiteDefinition dynamite, List<RegionVolume> volumes) {
        int radius = Math.max(1, (int) Math.ceil(dynamite.explosion.radius));
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int radiusSquared = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radiusSquared) {
                        continue;
                    }
                    int x = cx + dx;
                    int y = cy + dy;
                    int z = cz + dz;
                    for (RegionVolume volume : volumes) {
                        if (volume.contains(x, y, z)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private String nearestTouchingRegion(Location center, DynamiteDefinition dynamite, List<RegionVolume> volumes) {
        int radius = Math.max(1, (int) Math.ceil(dynamite.explosion.radius));
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int radiusSquared = radius * radius;
        String name = "";
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radiusSquared) {
                        continue;
                    }
                    int x = cx + dx;
                    int y = cy + dy;
                    int z = cz + dz;
                    for (RegionVolume volume : volumes) {
                        if (volume.contains(x, y, z)) {
                            name = volume.name();
                        }
                    }
                }
            }
        }
        return name;
    }

    private boolean worldAllowed(World world) {
        if (settings.worlds.isEmpty()) {
            return true;
        }
        String name = world.getName().toLowerCase(Locale.ROOT);
        for (String allowed : settings.worlds) {
            if (allowed != null && allowed.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasBypass(Player player) {
        return player != null && player.hasPermission(settings.bypassPermission);
    }

    public boolean permitsPsRaid(Location location, Player player) {
        return psRaidBypass != null && psRaidBypass.permits(location, player);
    }

    public record RegionCheckResult(boolean permitted, String reason, String regionName) {

        public static RegionCheckResult permit() {
            return new RegionCheckResult(true, "", "");
        }

        public static RegionCheckResult denied(String reason, String regionName) {
            return new RegionCheckResult(false, reason, regionName == null ? "" : regionName);
        }

    }

}
