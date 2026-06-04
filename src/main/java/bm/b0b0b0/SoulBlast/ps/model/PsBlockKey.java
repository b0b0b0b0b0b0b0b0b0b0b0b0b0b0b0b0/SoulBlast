package bm.b0b0b0.SoulBlast.ps.model;

import org.bukkit.World;

import java.util.Objects;
import java.util.UUID;

public final class PsBlockKey {

    private final UUID worldId;
    private final int x;
    private final int y;
    private final int z;

    public PsBlockKey(UUID worldId, int x, int y, int z) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static PsBlockKey of(World world, int x, int y, int z) {
        return new PsBlockKey(world.getUID(), x, y, z);
    }

    public UUID worldId() {
        return worldId;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PsBlockKey key)) {
            return false;
        }
        return x == key.x && y == key.y && z == key.z && worldId.equals(key.worldId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldId, x, y, z);
    }

}
