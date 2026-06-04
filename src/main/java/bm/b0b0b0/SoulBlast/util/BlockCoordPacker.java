package bm.b0b0b0.SoulBlast.util;

public final class BlockCoordPacker {

    private BlockCoordPacker() {
    }

    public static long pack(int x, int y, int z) {
        return ((long) x & 0x3FFFFF) << 42 | ((long) y & 0xFFF) << 30 | ((long) z & 0x3FFFFF);
    }

}
