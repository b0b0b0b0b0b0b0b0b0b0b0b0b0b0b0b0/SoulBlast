package bm.b0b0b0.SoulBlast.ps.service;

import bm.b0b0b0.SoulBlast.config.RegionProtectionSettings;
import bm.b0b0b0.SoulBlast.ps.config.PsProtectionTypeDefinition;
import bm.b0b0b0.SoulBlast.ps.config.PsSettings;
import bm.b0b0b0.SoulBlast.ps.integration.ProtectionStonesBridge;
import bm.b0b0b0.SoulBlast.ps.integration.PsRegionSnapshot;
import bm.b0b0b0.SoulBlast.service.region.PsDynamiteRaidBypass;
import bm.b0b0b0.SoulBlast.service.region.RegionBackend;
import bm.b0b0b0.SoulBlast.service.region.RegionVolume;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public final class PsDynamiteRaidService implements PsDynamiteRaidBypass {

    private final PsSettings psSettings;
    private final PsTypeRegistry typeRegistry;
    private final ProtectionStonesBridge protectionStones;
    private final RegionBackend regionBackend;
    private final RegionProtectionSettings regionSettings;

    public PsDynamiteRaidService(
            PsSettings psSettings,
            PsTypeRegistry typeRegistry,
            ProtectionStonesBridge protectionStones,
            RegionBackend regionBackend,
            RegionProtectionSettings regionSettings
    ) {
        this.psSettings = psSettings;
        this.typeRegistry = typeRegistry;
        this.protectionStones = protectionStones;
        this.regionBackend = regionBackend;
        this.regionSettings = regionSettings;
    }

    @Override
    public boolean permits(Location location, Player player) {
        if (!psSettings.supportSoulblast) {
            return false;
        }
        if (location == null || location.getWorld() == null) {
            return false;
        }
        if (!protectionStones.available() || typeRegistry == null) {
            return false;
        }
        Optional<PsRegionSnapshot> snapshot = resolveRegionSnapshot(location);
        if (snapshot.isEmpty()) {
            return false;
        }
        String alias = protectionStones.canonicalAlias(snapshot.get().alias());
        Optional<PsProtectionTypeDefinition> type = typeRegistry.findAlias(alias);
        boolean allowed = type.map(definition -> definition.allowSoulblastDynamiteInForeignClaims).orElse(true);
        if (!allowed) {
            return false;
        }
        return !insideSpawnBlacklist(location);
    }

    private Optional<PsRegionSnapshot> resolveRegionSnapshot(Location location) {
        Optional<PsRegionSnapshot> snapshot = protectionStones.snapshotAt(location);
        if (snapshot.isPresent()) {
            return snapshot;
        }
        if (location.getWorld() == null) {
            return Optional.empty();
        }
        Location blockCenter = new Location(
                location.getWorld(),
                location.getBlockX() + 0.5,
                location.getBlockY() + 0.5,
                location.getBlockZ() + 0.5
        );
        return protectionStones.snapshotAt(blockCenter);
    }

    private boolean insideSpawnBlacklist(Location location) {
        if (!regionSettings.enabled || !regionBackend.available()) {
            return false;
        }
        Set<String> blacklist = lowered(regionSettings.regionNames);
        if (blacklist.isEmpty()) {
            return false;
        }
        List<RegionVolume> volumes = regionBackend.dynamitePlacementVolumes(
                location.getWorld(),
                location,
                null,
                regionSettings
        );
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        for (RegionVolume volume : volumes) {
            if (volume.name() == null) {
                continue;
            }
            if (!blacklist.contains(volume.name().toLowerCase(Locale.ROOT))) {
                continue;
            }
            if (volume.contains(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> lowered(List<String> names) {
        Set<String> result = new HashSet<>();
        if (names == null) {
            return result;
        }
        for (String name : names) {
            if (name != null && !name.isBlank()) {
                result.add(name.toLowerCase(Locale.ROOT));
            }
        }
        return result;
    }

}
