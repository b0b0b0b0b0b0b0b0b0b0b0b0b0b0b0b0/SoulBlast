package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.model.ExplosionBlockAction;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ExplosionBlockOrder {

    private ExplosionBlockOrder() {
    }

    public static void sortByChunk(ExplosionJob job) {
        sort(job, Comparator.comparingLong(ExplosionBlockOrder::chunkKey));
    }

    public static void prioritizeHellscape(ExplosionJob job) {
        sort(job, Comparator
                .comparingInt((ExplosionJob.BlockTarget target) -> actionRank(target.action()))
                .thenComparingLong(target -> maskSortKey(job, target)));
    }

    private static long maskSortKey(ExplosionJob job, ExplosionJob.BlockTarget target) {
        if (target.action() == ExplosionBlockAction.REPLACE || target.action() == ExplosionBlockAction.PLACE) {
            return horizontalDistSq(job, target);
        }
        return chunkKey(target);
    }

    private static long horizontalDistSq(ExplosionJob job, ExplosionJob.BlockTarget target) {
        int dx = target.x() - job.getCenter().getBlockX();
        int dz = target.z() - job.getCenter().getBlockZ();
        return (long) dx * dx + (long) dz * dz;
    }

    private static void sort(ExplosionJob job, Comparator<ExplosionJob.BlockTarget> comparator) {
        ArrayDeque<ExplosionJob.BlockTarget> pending = job.getPendingBlocks();
        if (pending.size() < 2) {
            return;
        }
        List<ExplosionJob.BlockTarget> list = new ArrayList<>(pending);
        pending.clear();
        list.sort(comparator);
        for (ExplosionJob.BlockTarget target : list) {
            pending.addLast(target);
        }
    }

    private static int actionRank(ExplosionBlockAction action) {
        return switch (action) {
            case REPLACE -> 0;
            case PLACE -> 1;
            case CLEAR_LIQUID -> 2;
            case BREAK -> 3;
        };
    }

    private static long chunkKey(ExplosionJob.BlockTarget target) {
        return ((long) (target.x() >> 4) << 32) | (target.z() >> 4 & 0xFFFFFFFFL);
    }

}
