package bm.b0b0b0.SoulBlast.service;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class ExplosionWaveLineOfSight {

    private ExplosionWaveLineOfSight() {
    }

    public static boolean canReach(World world, Location origin, Block target) {
        if (world == null || origin == null || target == null) {
            return false;
        }
        Location end = target.getLocation().add(0.5, 0.5, 0.5);
        Vector delta = end.toVector().subtract(origin.toVector());
        double distance = delta.length();
        if (distance < 1.0E-4) {
            return true;
        }
        Vector direction = delta.clone().normalize();
        Location rayStart = origin.clone().add(direction.clone().multiply(0.35));
        RayTraceResult trace = world.rayTraceBlocks(
                rayStart,
                direction,
                Math.max(0.0, distance - 0.4),
                FluidCollisionMode.NEVER,
                false
        );
        if (trace == null || trace.getHitBlock() == null) {
            return true;
        }
        Block hit = trace.getHitBlock();
        return hit.getX() == target.getX()
                && hit.getY() == target.getY()
                && hit.getZ() == target.getZ();
    }

}
