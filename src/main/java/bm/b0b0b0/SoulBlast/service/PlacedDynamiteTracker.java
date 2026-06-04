package bm.b0b0b0.SoulBlast.service;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlacedDynamiteTracker {

    private final ConcurrentHashMap<BlockKey, String> placed = new ConcurrentHashMap<>();

    public void track(Block block, String dynamiteId) {
        if (block.getWorld() == null || dynamiteId == null || dynamiteId.isBlank()) {
            return;
        }
        placed.put(key(block), dynamiteId);
    }

    public void untrack(Block block) {
        if (block.getWorld() == null) {
            return;
        }
        placed.remove(key(block));
    }

    public Optional<String> dynamiteId(Block block) {
        if (block.getWorld() == null || block.getType() != Material.TNT) {
            untrack(block);
            return Optional.empty();
        }
        return Optional.ofNullable(placed.get(key(block)));
    }

    private static BlockKey key(Block block) {
        return new BlockKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
    }

    private record BlockKey(UUID worldId, int x, int y, int z) {
    }

}
