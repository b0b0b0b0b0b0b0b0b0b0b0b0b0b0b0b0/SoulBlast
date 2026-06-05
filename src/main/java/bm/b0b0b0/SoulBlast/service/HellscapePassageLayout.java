package bm.b0b0b0.SoulBlast.service;

public final class HellscapePassageLayout {

    private static final int RAY_COUNT = 10;
    private static final double CENTER_HUB_RADIUS = 2.8;
    private static final double BASE_HALF_WIDTH = 1.65;
    private static final double WIDTH_GROWTH = 0.048;

    private HellscapePassageLayout() {
    }

    public static boolean isPassage(int centerX, int centerZ, int x, int z) {
        int dx = x - centerX;
        int dz = z - centerZ;
        double distSq = (double) dx * dx + dz * dz;
        if (distSq <= CENTER_HUB_RADIUS * CENTER_HUB_RADIUS) {
            return true;
        }
        double dist = Math.sqrt(distSq);
        double angle = Math.atan2(dz, dx);
        if (angle < 0.0) {
            angle += Math.PI * 2.0;
        }
        double slice = Math.PI * 2.0 / RAY_COUNT;
        double offset = angle % slice;
        double edgeDist = Math.min(offset, slice - offset) * dist;
        double halfWidth = BASE_HALF_WIDTH + dist * WIDTH_GROWTH;
        return edgeDist <= halfWidth;
    }

}
