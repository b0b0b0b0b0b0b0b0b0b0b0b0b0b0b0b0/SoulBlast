package bm.b0b0b0.SoulBlast.ps.model;

import org.bukkit.World;

import java.util.UUID;

public record PsBlockKey(UUID worldId, int x, int y, int z) {

    public static PsBlockKey of(World world, int x, int y, int z) {
        return new PsBlockKey(world.getUID(), x, y, z);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PsBlockKey(UUID id, int x1, int y1, int z1))) {
            return false;
        }
        return x == x1 && y == y1 && z == z1 && worldId.equals(id);
    }

}
