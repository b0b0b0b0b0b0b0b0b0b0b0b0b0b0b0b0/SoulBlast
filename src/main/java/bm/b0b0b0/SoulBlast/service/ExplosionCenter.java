package bm.b0b0b0.SoulBlast.service;

import org.bukkit.Location;
import org.bukkit.block.Block;

public final class ExplosionCenter {

    private ExplosionCenter() {
    }

    public static Location snap(Location raw) {
        if (raw == null) {
            return null;
        }
        Location center = raw.clone();
        center.setX(Math.floor(center.getX()) + 0.5);
        center.setY(Math.floor(center.getY()) + 0.5);
        center.setZ(Math.floor(center.getZ()) + 0.5);
        return center;
    }

    public static boolean isWithinRadius(Block block, Location center, float radius) {
        if (block == null || center == null || center.getWorld() == null) {
            return false;
        }
        if (!center.getWorld().equals(block.getWorld())) {
            return false;
        }
        double limit = radius + 0.5;
        Location blockCenter = block.getLocation().add(0.5, 0.5, 0.5);
        return blockCenter.distanceSquared(center) <= limit * limit;
    }

    public static double blockCenterDistanceSquared(Block block, Location center) {
        Location blockCenter = block.getLocation().add(0.5, 0.5, 0.5);
        double dx = blockCenter.getX() - center.getX();
        double dy = blockCenter.getY() - center.getY();
        double dz = blockCenter.getZ() - center.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

}
