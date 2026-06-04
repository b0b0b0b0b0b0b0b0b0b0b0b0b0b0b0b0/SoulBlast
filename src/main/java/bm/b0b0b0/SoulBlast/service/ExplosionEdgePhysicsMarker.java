package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.model.ExplosionBlockAction;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import bm.b0b0b0.SoulBlast.util.BlockCoordPacker;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public final class ExplosionEdgePhysicsMarker {

    private ExplosionEdgePhysicsMarker() {
    }

    public static void markShell(ExplosionJob job, ExplosionBlockAction action, boolean enabled) {
        job.clearPhysicsEdgeKeys();
        if (!enabled) {
            return;
        }
        LongOpenHashSet volume = new LongOpenHashSet();
        for (ExplosionJob.BlockTarget target : job.getPendingBlocks()) {
            if (target.action() == action) {
                volume.add(BlockCoordPacker.pack(target.x(), target.y(), target.z()));
            }
        }
        for (ExplosionJob.BlockTarget target : job.getPendingBlocks()) {
            if (target.action() != action) {
                continue;
            }
            if (isShell(target.x(), target.y(), target.z(), volume)) {
                job.addPhysicsEdgeKey(BlockCoordPacker.pack(target.x(), target.y(), target.z()));
            }
        }
    }

    private static boolean isShell(int x, int y, int z, LongOpenHashSet volume) {
        return !volume.contains(BlockCoordPacker.pack(x + 1, y, z))
                || !volume.contains(BlockCoordPacker.pack(x - 1, y, z))
                || !volume.contains(BlockCoordPacker.pack(x, y + 1, z))
                || !volume.contains(BlockCoordPacker.pack(x, y - 1, z))
                || !volume.contains(BlockCoordPacker.pack(x, y, z + 1))
                || !volume.contains(BlockCoordPacker.pack(x, y, z - 1));
    }

}
