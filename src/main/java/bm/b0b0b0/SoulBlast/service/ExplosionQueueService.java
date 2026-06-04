package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.SoulBlast;
import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.ps.PsModule;
import bm.b0b0b0.SoulBlast.ps.service.PsExplosionBridge;
import bm.b0b0b0.SoulBlast.config.ExplosionLimits;
import bm.b0b0b0.SoulBlast.config.GeneralSettings;
import bm.b0b0b0.SoulBlast.config.PluginConfig;
import bm.b0b0b0.SoulBlast.model.ExplosionBlockAction;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import bm.b0b0b0.SoulBlast.model.ExplosionJobPhase;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.ArrayDeque;
import java.util.Queue;

public final class ExplosionQueueService {

    private final SoulBlast plugin;
    private final ExplosionRaySampler raySampler;
    private final ExplosionWaveSampler waveSampler;
    private final ExplosionVolumeSampler volumeSampler;
    private final ExplosionLiquidSampler liquidSampler;
    private final CraterFillPlanner craterFillPlanner;
    private final BlockExplosionApplier blockApplier;
    private final EntityExplosionDamageService entityDamageService;
    private final ExplosionEntityEffectsService entityEffectsService;
    private final PostExplosionActionRunner postActionRunner;
    private final Queue<ExplosionJob> samplingQueue = new ArrayDeque<>();
    private final Queue<ExplosionJob> queue = new ArrayDeque<>();
    private GeneralSettings general = new GeneralSettings();
    private ExplosionLimits limits = ExplosionLimits.from(general);
    private final ExplosionChunkContext chunkContext = new ExplosionChunkContext();
    private final ExplosionFireSupport fireSupport = new ExplosionFireSupport();

    public ExplosionQueueService(
            SoulBlast plugin,
            ExplosionRaySampler raySampler,
            ExplosionWaveSampler waveSampler,
            ExplosionVolumeSampler volumeSampler,
            ExplosionLiquidSampler liquidSampler,
            CraterFillPlanner craterFillPlanner,
            BlockExplosionApplier blockApplier,
            EntityExplosionDamageService entityDamageService,
            ExplosionEntityEffectsService entityEffectsService,
            PostExplosionActionRunner postActionRunner
    ) {
        this.plugin = plugin;
        this.raySampler = raySampler;
        this.waveSampler = waveSampler;
        this.volumeSampler = volumeSampler;
        this.liquidSampler = liquidSampler;
        this.craterFillPlanner = craterFillPlanner;
        this.blockApplier = blockApplier;
        this.entityDamageService = entityDamageService;
        this.entityEffectsService = entityEffectsService;
        this.postActionRunner = postActionRunner;
    }

    public void reload(PluginConfig config) {
        general = config.general;
        limits = ExplosionLimits.from(general);
    }

    public void enqueue(Location center, DynamiteDefinition dynamite, Entity source) {
        enqueue(center, dynamite, source, false);
    }

    public void enqueue(Location center, DynamiteDefinition dynamite, Entity source, boolean fuseTriggered) {
        if (samplingQueue.size() + queue.size() >= general.maxQueuedExplosions) {
            return;
        }
        if (ExplosionLiquidSampler.drainsLiquids(dynamite)) {
            liquidSampler.clearLiquidsImmediately(center, dynamite, limits, general);
            if (DrainVortexEffectService.usesDrainVortex(dynamite)) {
                DrainVortexEffectService.start(plugin, center, dynamite);
            } else {
                ExplosionPresentationEffects.playChargeBurst(center, dynamite);
            }
        }
        ExplosionJob job = new ExplosionJob(center, dynamite, source);
        if (!dynamite.explosion.breakBlocks) {
            complete(job);
            return;
        }
        beginBlockSampling(job);
        boolean immediate = fuseTriggered || !dynamite.explosion.spreadAcrossTicks;
        if (immediate) {
            finishSampling(job);
            runJobSync(job);
            return;
        }
        samplingQueue.offer(job);
    }

    private void beginBlockSampling(ExplosionJob job) {
        if (ExplosionWaveSampler.usesWave(job.getDynamite().explosion)) {
            waveSampler.beginSampling(job, limits, general);
            return;
        }
        raySampler.beginSampling(job, limits);
    }

    private int continueBlockSampling(ExplosionJob job, int stepBudget) {
        if (ExplosionWaveSampler.usesWave(job.getDynamite().explosion)) {
            if (job.getRaySampling().active) {
                return waveSampler.continueSampling(job, limits, general, stepBudget);
            }
            if (waveRayOverlayEnabled(job)) {
                if (!job.getRaySampling().rayOverlayActive) {
                    raySampler.beginWaveOverlay(job, limits);
                }
                return raySampler.continueWaveOverlay(job, limits, stepBudget);
            }
            return stepBudget;
        }
        return raySampler.continueSampling(job, limits, stepBudget);
    }

    private static boolean waveRayOverlayEnabled(ExplosionJob job) {
        return job.getDynamite().explosion.algorithm.waveRayOverlay;
    }

    private void finishSampling(ExplosionJob job) {
        int budget = Math.max(500, limits.maxSamplingStepsPerTick() * 40);
        while (job.isRaySampling() && budget > 0) {
            budget = continueBlockSampling(job, budget);
        }
        prepareBreakPhase(job);
    }

    private void prepareBreakPhase(ExplosionJob job) {
        volumeSampler.fillIntoJob(job, limits, general);
        ExplosionBlockOrder.sortByChunk(job);
        ExplosionEdgePhysicsMarker.markShell(
                job,
                ExplosionBlockAction.BREAK,
                job.getDynamite().explosion.algorithm.edgePhysicsOnly
        );
    }

    private void prepareLiquidPhase(ExplosionJob job) {
        ExplosionBlockOrder.sortByChunk(job);
        ExplosionEdgePhysicsMarker.markShell(
                job,
                ExplosionBlockAction.CLEAR_LIQUID,
                job.getDynamite().explosion.algorithm.edgePhysicsOnly
        );
    }

    private void prepareCraterPhase(ExplosionJob job) {
        ExplosionBlockOrder.sortByChunk(job);
        ExplosionEdgePhysicsMarker.markShell(
                job,
                ExplosionBlockAction.PLACE,
                job.getDynamite().explosion.algorithm.edgePhysicsOnly
        );
    }

    private void promoteSampledJob(ExplosionJob job, DynamiteDefinition dynamite) {
        if (job.getCenter().getWorld() == null) {
            return;
        }
        if (queue.size() >= general.maxQueuedExplosions) {
            return;
        }
        if (dynamite.explosion.spreadAcrossTicks) {
            queue.offer(job);
            return;
        }
        runJobSync(job);
    }

    private void runJobSync(ExplosionJob job) {
        try {
            if (hasLiquidPhase(job)) {
                liquidSampler.sampleIntoJob(job, limits, general);
                prepareLiquidPhase(job);
                drainPending(job);
            }
            prepareBreakPhase(job);
            drainPending(job);
            entityEffectsService.apply(job);
            if (hasCraterPhase(job)) {
                craterFillPlanner.planIntoJob(job, limits, general);
                prepareCraterPhase(job);
                drainPending(job);
            }
        } finally {
            applyPsDurability(job);
        }
        complete(job);
    }

    private void drainPending(ExplosionJob job) {
        chunkContext.reset();
        ExplosionJob.BlockTarget target;
        while ((target = job.getPendingBlocks().pollFirst()) != null) {
            blockApplier.applyBlock(job, target, chunkContext);
        }
    }

    public void processTick() {
        int budget = limits.maxBlocksPerExplosionTick();
        budget = processSampling(budget);
        processBreaking(budget);
    }

    private int processSampling(int budget) {
        int samplingBudget = Math.min(budget, limits.maxSamplingStepsPerTick());
        int remaining = budget;
        while (samplingBudget > 0 && !samplingQueue.isEmpty()) {
            ExplosionJob job = samplingQueue.peek();
            if (job == null) {
                break;
            }
            int before = samplingBudget;
            samplingBudget = continueBlockSampling(job, samplingBudget);
            remaining -= before - samplingBudget;
            if (!job.isRaySampling()) {
                samplingQueue.poll();
                if (hasLiquidPhase(job)) {
                    liquidSampler.sampleIntoJob(job, limits, general);
                    prepareLiquidPhase(job);
                }
                prepareBreakPhase(job);
                promoteSampledJob(job, job.getDynamite());
            }
        }
        return remaining;
    }

    private void processBreaking(int budget) {
        chunkContext.reset();
        ExplosionJob job = queue.peek();
        if (job == null) {
            return;
        }
        while (budget > 0 && !job.getPendingBlocks().isEmpty()) {
            ExplosionJob.BlockTarget target = job.getPendingBlocks().pollFirst();
            if (target != null) {
                blockApplier.applyBlock(job, target, chunkContext);
            }
            budget--;
        }
        if (job.getPendingBlocks().isEmpty()) {
            advancePhase(job);
        }
    }

    private void advancePhase(ExplosionJob job) {
        switch (job.getPhase()) {
            case BREAK -> {
                entityEffectsService.apply(job);
                job.setPhase(ExplosionJobPhase.LIQUID);
                if (hasLiquidPhase(job)) {
                    liquidSampler.sampleIntoJob(job, limits, general);
                    prepareLiquidPhase(job);
                    if (!job.getPendingBlocks().isEmpty()) {
                        return;
                    }
                }
                advancePhase(job);
            }
            case LIQUID -> {
                job.setPhase(ExplosionJobPhase.CRATER);
                if (hasCraterPhase(job)) {
                    craterFillPlanner.planIntoJob(job, limits, general);
                    prepareCraterPhase(job);
                    if (!job.getPendingBlocks().isEmpty()) {
                        return;
                    }
                }
                advancePhase(job);
            }
            case CRATER -> {
                queue.poll();
                complete(job);
            }
            default -> queue.poll();
        }
    }

    private boolean hasLiquidPhase(ExplosionJob job) {
        var effects = job.getDynamite().explosion.effects;
        return effects.removeWater || effects.removeLava;
    }

    private boolean hasCraterPhase(ExplosionJob job) {
        return job.getDynamite().explosion.effects.craterFill.enabled;
    }

    private void applyPsDurability(ExplosionJob job) {
        PsModule psModule = plugin.getPsModule();
        if (psModule == null || !psModule.active()) {
            if (psModule != null && psModule.durabilityTrace() != null) {
                psModule.durabilityTrace().moduleInactive("PsModule не active");
            }
            return;
        }
        PsExplosionBridge psBridge = psModule.explosionBridge();
        if (psBridge == null) {
            if (psModule.durabilityTrace() != null) {
                psModule.durabilityTrace().moduleInactive("explosionBridge=null после reload");
            }
            return;
        }
        psBridge.applyProximityDamage(job);
    }

    private void complete(ExplosionJob job) {
        Location center = job.getCenter();
        DynamiteDefinition dynamite = job.getDynamite();
        applyPsDurability(job);
        if (center.getWorld() != null) {
            entityDamageService.applyDamage(
                    dynamite.explosion,
                    job.getSource(),
                    center.getWorld().getNearbyEntities(
                            center,
                            dynamite.explosion.radius * 2,
                            dynamite.explosion.radius * 2,
                            dynamite.explosion.radius * 2
                    ),
                    center
            );
        }
        playEffects(center, dynamite);
        if (dynamite.explosion.createFire) {
            fireSupport.removeUnsupportedFire(center, dynamite.explosion.radius);
        }
        postActionRunner.scheduleActions(plugin, center, dynamite.explosion.postActions);
    }

    private void playEffects(Location center, DynamiteDefinition dynamite) {
        if (center.getWorld() == null) {
            return;
        }
        float visual = TsarBombRules.visualExplosionPower(dynamite);
        if (visual <= 0f) {
            float radius = dynamite.explosion.radius;
            visual = Math.min(12.0f, 1.5f + radius * 0.14f + dynamite.explosion.power * 0.08f);
        }
        if (DrainVortexEffectService.usesDrainVortex(dynamite)) {
            visual = Math.min(visual, 1.8f);
        }
        center.getWorld().createExplosion(
                center,
                visual,
                false,
                false
        );
        ExplosionPresentationEffects.playDetonation(center, dynamite);
    }

    public void shutdown() {
        samplingQueue.clear();
        queue.clear();
    }

}
