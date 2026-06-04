package bm.b0b0b0.SoulBlast.ps.service;

import bm.b0b0b0.SoulBlast.ps.model.PsBlockKey;

import java.util.UUID;

public final class PsRegionId {

    private PsRegionId() {
    }

    public static String fileName(PsBlockKey key) {
        return key.worldId().toString() + "_" + key.x() + "_" + key.y() + "_" + key.z() + ".yml";
    }

    public static PsBlockKey parseFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        String base = fileName.endsWith(".yml") ? fileName.substring(0, fileName.length() - 4) : fileName;
        String[] parts = base.split("_");
        if (parts.length < 4) {
            return null;
        }
        int zIndex = parts.length - 1;
        int yIndex = parts.length - 2;
        int xIndex = parts.length - 3;
        try {
            UUID worldId = UUID.fromString(String.join("_", java.util.Arrays.copyOf(parts, xIndex)));
            int x = Integer.parseInt(parts[xIndex]);
            int y = Integer.parseInt(parts[yIndex]);
            int z = Integer.parseInt(parts[zIndex]);
            return new PsBlockKey(worldId, x, y, z);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

}
