package bm.b0b0b0.SoulBlast.ps.service;

import bm.b0b0b0.SoulBlast.ps.model.PsBlockKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Optional;
import java.util.UUID;

public final class PsWorldLookup {

    private PsWorldLookup() {
    }

    public static Optional<World> resolveWorld(Server server, UUID worldId, int x, int y, int z) {
        if (server == null) {
            return Optional.empty();
        }
        if (worldId != null) {
            World byId = server.getWorld(worldId);
            if (byId != null) {
                return Optional.of(byId);
            }
        }
        return findWorldWithBlock(server, x, y, z);
    }

    public static Optional<Block> resolveBlock(Server server, PsBlockKey key) {
        if (server == null || key == null) {
            return Optional.empty();
        }
        return resolveWorld(server, key.worldId(), key.x(), key.y(), key.z())
                .map(world -> world.getBlockAt(key.x(), key.y(), key.z()));
    }

    public static Optional<World> findWorldWithBlock(Server server, int x, int y, int z) {
        if (server == null) {
            return Optional.empty();
        }
        for (World world : server.getWorlds()) {
            Block block = world.getBlockAt(x, y, z);
            if (!block.getType().isAir()) {
                return Optional.of(world);
            }
        }
        return Optional.empty();
    }

}
