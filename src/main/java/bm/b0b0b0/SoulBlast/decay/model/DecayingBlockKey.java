package bm.b0b0b0.SoulBlast.decay.model;

import org.bukkit.World;

import java.util.UUID;

public record DecayingBlockKey(UUID worldId, int x, int y, int z) {

    public static DecayingBlockKey of(World world, int x, int y, int z) {
        return new DecayingBlockKey(world.getUID(), x, y, z);
    }

}
