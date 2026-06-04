package bm.b0b0b0.SoulBlast.service.region;

public interface RegionVolume {

    String name();

    boolean contains(int x, int y, int z);

    double distanceToBoundary(int x, int y, int z);

}
