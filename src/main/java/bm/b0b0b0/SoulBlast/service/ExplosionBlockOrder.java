package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.model.ExplosionJob;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ExplosionBlockOrder {

    private ExplosionBlockOrder() {
    }

    public static void sortByChunk(ExplosionJob job) {
        ArrayDeque<ExplosionJob.BlockTarget> pending = job.getPendingBlocks();
        if (pending.size() < 2) {
            return;
        }
        List<ExplosionJob.BlockTarget> list = new ArrayList<>(pending);
        pending.clear();
        list.sort(Comparator.comparingLong(ExplosionBlockOrder::chunkKey));
        for (ExplosionJob.BlockTarget target : list) {
            pending.addLast(target);
        }
    }

    private static long chunkKey(ExplosionJob.BlockTarget target) {
        return ((long) (target.x() >> 4) << 32) | (target.z() >> 4 & 0xFFFFFFFFL);
    }

}
