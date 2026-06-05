package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.ExplosionLimits;
import bm.b0b0b0.SoulBlast.config.GeneralSettings;
import bm.b0b0b0.SoulBlast.model.ExplosionBlockAction;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import org.bukkit.Particle;

public final class TsarGradualMaskService {

    private final HellscapeMaskPlanner maskPlanner;
    private final BlockExplosionApplier blockApplier;
    private final ExplosionChunkContext chunkContext = new ExplosionChunkContext();

    public TsarGradualMaskService(HellscapeMaskPlanner maskPlanner, BlockExplosionApplier blockApplier) {
        this.maskPlanner = maskPlanner;
        this.blockApplier = blockApplier;
    }

    public boolean tick(ExplosionJob job, int budget, ExplosionLimits limits, GeneralSettings general) {
        int reach = maskPlanner.resolveReach(job);
        int ring = job.getTsarMaskRing();
        if (ring > reach && maskQueueEmpty(job)) {
            return true;
        }
        int maxOps = TsarBombRules.craterCap(limits, job.getDynamite());
        if (job.getTsarMaskKeys().size() >= maxOps && maskQueueEmpty(job)) {
            return true;
        }
        if (maskQueueEmpty(job)) {
            if (ring > reach) {
                return true;
            }
            int plannedBefore = job.getTsarMaskKeys().size();
            maskPlanner.planHorizontalRing(job, ring, limits, general);
            job.getDiagnostics().setPlannedMask(job.getTsarMaskKeys().size());
            if (job.getTsarMaskKeys().size() == plannedBefore && ring < reach) {
                job.setTsarMaskRing(ring + 1);
                return false;
            }
        }
        chunkContext.reset();
        int applied = 0;
        while (budget > 0 && !job.getPendingBlocks().isEmpty()) {
            ExplosionJob.BlockTarget peek = job.getPendingBlocks().peekFirst();
            if (peek == null || !isMaskAction(peek.action())) {
                break;
            }
            ExplosionJob.BlockTarget target = job.getPendingBlocks().pollFirst();
            blockApplier.applyBlock(job, target, chunkContext);
            applied++;
            budget--;
        }
        if (applied > 0 && ring % 3 == 0) {
            playMaskPulse(job, ring);
        }
        if (maskQueueEmpty(job)) {
            job.setTsarMaskRing(ring + 1);
        }
        int nextRing = job.getTsarMaskRing();
        return nextRing > reach && maskQueueEmpty(job);
    }

    private static boolean maskQueueEmpty(ExplosionJob job) {
        for (ExplosionJob.BlockTarget target : job.getPendingBlocks()) {
            if (isMaskAction(target.action())) {
                return false;
            }
        }
        return true;
    }

    public void initMaskMeta(ExplosionJob job) {
        int reach = maskPlanner.resolveReach(job);
        int columns = maskPlanner.estimateColumns(reach);
        job.getDiagnostics().setMaskMeta(reach, columns);
    }

    public int resolveReach(ExplosionJob job) {
        return maskPlanner.resolveReach(job);
    }

    public boolean isComplete(ExplosionJob job, ExplosionLimits limits) {
        if (!job.isHellMaskQueued()) {
            return false;
        }
        int reach = maskPlanner.resolveReach(job);
        int maxOps = TsarBombRules.craterCap(limits, job.getDynamite());
        if (job.getTsarMaskKeys().size() >= maxOps) {
            return maskQueueEmpty(job);
        }
        return job.getTsarMaskRing() > reach && maskQueueEmpty(job);
    }

    private static void playMaskPulse(ExplosionJob job, int ring) {
        var world = job.getCenter().getWorld();
        if (world == null) {
            return;
        }
        double angle = ring * 0.41;
        double x = job.getCenter().getX() + Math.cos(angle) * ring;
        double z = job.getCenter().getZ() + Math.sin(angle) * ring;
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, x, job.getCenter().getY() + 1.4, z, 6, 0.25, 0.35, 0.25, 0.02);
        world.spawnParticle(Particle.LAVA, x, job.getCenter().getY() + 0.2, z, 2, 0.15, 0.1, 0.15, 0.01);
    }

    private static boolean isMaskAction(ExplosionBlockAction action) {
        return action == ExplosionBlockAction.REPLACE || action == ExplosionBlockAction.PLACE;
    }

}
