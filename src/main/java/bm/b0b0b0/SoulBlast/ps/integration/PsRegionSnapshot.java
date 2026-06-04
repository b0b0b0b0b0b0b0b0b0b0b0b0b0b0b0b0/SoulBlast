package bm.b0b0b0.SoulBlast.ps.integration;

import java.util.UUID;

public record PsRegionSnapshot(
        String alias,
        UUID ownerId,
        String ownerName,
        int radiusX,
        int radiusY,
        int radiusZ
) {
}
