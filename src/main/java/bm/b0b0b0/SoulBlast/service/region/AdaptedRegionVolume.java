package bm.b0b0b0.SoulBlast.service.region;

public final class AdaptedRegionVolume implements RegionVolume {

    private final String name;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private final RegionPointCheck containsCheck;

    public AdaptedRegionVolume(
            String name,
            int minX,
            int minY,
            int minZ,
            int maxX,
            int maxY,
            int maxZ,
            RegionPointCheck containsCheck
    ) {
        this.name = name;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.containsCheck = containsCheck;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return containsCheck.contains(x, y, z);
    }

    @Override
    public double distanceToBoundary(int x, int y, int z) {
        if (contains(x, y, z)) {
            return 0.0;
        }
        int closestX = clamp(x, minX, maxX);
        int closestY = clamp(y, minY, maxY);
        int closestZ = clamp(z, minZ, maxZ);
        double dx = x - closestX;
        double dy = y - closestY;
        double dz = z - closestZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static int clamp(int value, int min, int max) {
        return Math.clamp(value, min, max);
    }

}
