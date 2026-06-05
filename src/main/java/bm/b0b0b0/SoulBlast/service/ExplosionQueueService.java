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
    private final ExplosionChunkScope chunkScope = new ExplosionChunkScope();
    private final TsarExplosionGate tsarGate;
    private final ExplosionDebugTrace explosionDebug;
    private final TsarGradualDrainService tsarGradualDrain = new TsarGradualDrainService();
    private final HellscapeLightningService hellscapeLightning = new HellscapeLightningService();
    private final TsarGradualMaskService tsarGradualMask;

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
            PostExplosionActionRunner postActionRunner,
            TsarExplosionGate tsarGate,
            ExplosionDebugTrace explosionDebug
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
        this.tsarGate = tsarGate;
        this.explosionDebug = explosionDebug;
        this.tsarGradualMask = new TsarGradualMaskService(new HellscapeMaskPlanner(), blockApplier);
        tsarGate.bindLauncher(this::launchTsar);
    }

    public void reload(PluginConfig config) {
        general = config.general;
        limits = ExplosionLimits.from(general);
        explosionDebug.setEnabled(general.explosionDebug);
    }

    public void enqueue(Location center, DynamiteDefinition dynamite, Entity source) {
        enqueue(center, dynamite, source, false);
    }

    public void enqueue(Location center, DynamiteDefinition dynamite, Entity source, boolean fuseTriggered) {
        if (samplingQueue.size() + queue.size() >= general.maxQueuedExplosions) {
            explosionDebug.warn("queue full, dropped id=" + dynamite.id + " at " + format(center));
            return;
        }
        chunkScope.pin(center, dynamite);
        if (ExplosionLiquidSampler.drainsLiquids(dynamite)) {
            if (TsarBombRules.isTsar(dynamite)) {
                int innerDrain = Math.max(0, general.griefLastPyreInnerDrainRadius);
                liquidSampler.clearInnerLiquidsImmediately(center, dynamite, general, innerDrain);
                ExplosionPresentationEffects.playChargeBurst(center, dynamite);
            } else {
                liquidSampler.clearLiquidsImmediately(center, dynamite, limits, general);
                if (DrainVortexEffectService.usesDrainVortex(dynamite)) {
                    DrainVortexEffectService.start(plugin, center, dynamite);
                } else {
                    ExplosionPresentationEffects.playChargeBurst(center, dynamite);
                }
            }
        }
        ExplosionJob job = new ExplosionJob(center, dynamite, source);
        if (TsarBombRules.isTsar(dynamite)) {
            if (!dynamite.explosion.breakBlocks) {
                TsarExplosionGate.AdmitOutcome outcome = tsarGate.admit(job);
                explosionDebug.enqueue(center, dynamite, outcome);
                if (outcome == TsarExplosionGate.AdmitOutcome.STARTED) {
                    complete(job);
                }
                return;
            }
            TsarExplosionGate.AdmitOutcome outcome = tsarGate.admit(job);
            explosionDebug.enqueue(center, dynamite, outcome);
            return;
        }
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

    private void launchTsar(ExplosionJob job) {
        if (!job.getDynamite().explosion.breakBlocks) {
            complete(job);
            return;
        }
        prepareBreakPhase(job);
        job.markTsarOrchestrated();
        job.setTsarDrainRing(Math.max(0, general.griefLastPyreInnerDrainRadius) + 1);
        job.getDiagnostics().setPlannedBreak(job.getPendingBlocks().size());
        if (queue.size() >= general.maxQueuedExplosions) {
            playTsarPresentation(job);
            finalizeTsarPhases(job);
            return;
        }
        pushTsarFront(job);
        playTsarPresentation(job);
    }

    private void pushTsarFront(ExplosionJob job) {
        queue.remove(job);
        if (queue instanceof ArrayDeque<ExplosionJob> deque) {
            deque.addFirst(job);
        } else {
            queue.offer(job);
        }
    }

    private void finalizeTsarPhases(ExplosionJob job) {
        if (!job.isTsarOrchestrated()) {
            job.markTsarOrchestrated();
        }
        makeQueueRoomForTsar(job);
        entityEffectsService.apply(job);
        if (!job.getPendingBlocks().isEmpty()) {
            pushTsarFront(job);
            return;
        }
        complete(job);
    }

    private void makeQueueRoomForTsar(ExplosionJob job) {
        while (queue.size() >= general.maxQueuedExplosions && !queue.isEmpty()) {
            ExplosionJob dropped = queue.poll();
            explosionDebug.warn("queue full, dropped queued explosion to keep last_pyre");
            if (dropped != null && dropped.getPendingBlocks().isEmpty()) {
                complete(dropped);
            }
        }
    }

    private int tsarTickBudget(int baseBudget) {
        float multiplier = Math.max(1.0f, general.griefLastPyreQueueTickMultiplier);
        if (multiplier <= 1.001f) {
            return baseBudget;
        }
        return Math.min(
                limits.maxBlocksPerExplosionTick() * 2,
                Math.max(baseBudget, (int) (limits.maxBlocksPerExplosionTick() * multiplier))
        );
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
        if (TsarBombRules.isTsar(job.getDynamite()) && job.isHellMaskQueued()) {
            ExplosionBlockOrder.prioritizeHellscape(job);
        } else {
            ExplosionBlockOrder.sortByChunk(job);
        }
        ExplosionEdgePhysicsMarker.markPlacementShell(
                job,
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
        if (TsarBombRules.isTsar(dynamite) || dynamite.explosion.spreadAcrossTicks) {
            queue.offer(job);
            return;
        }
        runJobSync(job);
    }

    private void runJobSync(ExplosionJob job) {
        if (hasLiquidPhase(job)) {
            liquidSampler.sampleIntoJob(job, limits, general);
            prepareLiquidPhase(job);
            drainPending(job);
        }
        prepareBreakPhase(job);
        drainPending(job);
        entityEffectsService.apply(job);
        if (hasCraterPhase(job)) {
            planAndApplyMask(job);
            drainPending(job);
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
        ExplosionJob tsar = findOrchestratedTsar();
        if (tsar != null) {
            processTsarOrchestration(tsar, tsarTickBudget(budget));
            return;
        }
        budget = processSampling(budget);
        processBreaking(budget);
    }

    private ExplosionJob findOrchestratedTsar() {
        for (ExplosionJob job : queue) {
            if (TsarBombRules.isTsar(job.getDynamite()) && job.isTsarOrchestrated()) {
                return job;
            }
        }
        return null;
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
        ExplosionJob job = queue.peek();
        if (job == null) {
            return;
        }
        chunkContext.reset();
        if (TsarBombRules.isTsar(job.getDynamite())) {
            budget = tsarTickBudget(budget);
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

    private void processTsarOrchestration(ExplosionJob job, int budget) {
        int breakBudget = Math.max(1, budget * 2 / 5);
        int drainBudget = Math.max(1, budget * 2 / 5);
        int maskBudget = Math.max(0, budget - breakBudget - drainBudget);
        if (!job.isTsarBreakFinished()) {
            processTsarBreakChunk(job, breakBudget);
        }
        if (!job.isTsarDrainFinished()) {
            if (tsarGradualDrain.tick(job, drainBudget, general)) {
                job.markTsarDrainFinished();
            }
        }
        if (shouldStartTsarMask(job)) {
            startTsarMask(job);
        }
        if (job.isHellMaskQueued()) {
            hellscapeLightning.tickDuringMask(job, general);
            if (maskBudget > 0) {
                tsarGradualMask.tick(job, maskBudget, limits, general);
            }
        }
        if (job.isTsarBreakFinished()
                && job.isTsarDrainFinished()
                && tsarGradualMask.isComplete(job, limits)) {
            finishTsarJob(job);
        }
    }

    private void processTsarBreakChunk(ExplosionJob job, int budget) {
        chunkContext.reset();
        while (budget > 0 && !job.getPendingBlocks().isEmpty()) {
            ExplosionJob.BlockTarget target = job.getPendingBlocks().pollFirst();
            if (target != null) {
                blockApplier.applyBlock(job, target, chunkContext);
            }
            budget--;
        }
        if (job.getPendingBlocks().isEmpty()) {
            entityEffectsService.apply(job);
            job.markTsarBreakFinished();
            explosionDebug.phase(job, ExplosionJobPhase.BREAK, ExplosionJobPhase.LIQUID, 0);
        }
    }

    private boolean shouldStartTsarMask(ExplosionJob job) {
        if (job.isHellMaskQueued()) {
            return true;
        }
        int reach = (int) Math.ceil(resolveTsarLiquidRadius(job));
        int threshold = Math.max(6, reach / 5);
        return job.getTsarDrainRing() >= threshold || job.isTsarDrainFinished();
    }

    private void startTsarMask(ExplosionJob job) {
        if (job.isHellMaskQueued()) {
            return;
        }
        job.setPhase(ExplosionJobPhase.CRATER);
        job.setTsarMaskRing(Math.max(0, general.griefLastPyreInnerDrainRadius));
        job.getTsarMaskKeys().clear();
        job.markHellMaskQueued();
        tsarGradualMask.initMaskMeta(job);
        explosionDebug.phase(job, ExplosionJobPhase.LIQUID, ExplosionJobPhase.CRATER, 0);
        explosionDebug.maskPlanned(
                job,
                0,
                job.getDiagnostics().maskReach(),
                TsarBombRules.craterCap(limits, job.getDynamite()),
                job.getDiagnostics().maskColumns()
        );
    }

    private void finishTsarJob(ExplosionJob job) {
        queue.remove(job);
        complete(job);
    }

    private float resolveTsarLiquidRadius(ExplosionJob job) {
        var effects = job.getDynamite().explosion.effects;
        return effects.liquidRadius > 0 ? effects.liquidRadius : job.getDynamite().explosion.radius;
    }

    private void advancePhase(ExplosionJob job) {
        ExplosionJobPhase phase = job.getPhase();
        switch (phase) {
            case BREAK -> {
                entityEffectsService.apply(job);
                job.setPhase(ExplosionJobPhase.LIQUID);
                explosionDebug.phase(job, phase, ExplosionJobPhase.LIQUID, job.getPendingBlocks().size());
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
                explosionDebug.phase(job, phase, ExplosionJobPhase.CRATER, job.getPendingBlocks().size());
                if (hasCraterPhase(job) && !job.isHellMaskQueued()) {
                    planAndApplyMask(job);
                    job.markHellMaskQueued();
                    ExplosionBlockOrder.prioritizeHellscape(job);
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

    private void planAndApplyMask(ExplosionJob job) {
        craterFillPlanner.planIntoJob(job, limits, general);
        prepareCraterPhase(job);
        explosionDebug.maskPlanned(
                job,
                job.getDiagnostics().plannedMask(),
                job.getDiagnostics().maskReach(),
                TsarBombRules.craterCap(limits, job.getDynamite()),
                job.getDiagnostics().maskColumns()
        );
    }

    private boolean hasLiquidPhase(ExplosionJob job) {
        var effects = job.getDynamite().explosion.effects;
        return effects.removeWater || effects.removeLava;
    }

    private boolean hasCraterPhase(ExplosionJob job) {
        if (TsarBombRules.isTsar(job.getDynamite())) {
            return true;
        }
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
        if (!job.isPresentationPlayed()) {
            applyPresentation(job);
        }
        if (TsarBombRules.isTsar(dynamite)) {
            fireSupport.igniteHellscape(center, dynamite);
        }
        if (dynamite.explosion.createFire) {
            fireSupport.removeUnsupportedFire(center, dynamite.explosion.radius);
        }
        if (!TsarBombRules.isTsar(dynamite)) {
            postActionRunner.scheduleActions(plugin, center, dynamite.explosion.postActions);
        }
        explosionDebug.complete(job);
        if (TsarBombRules.isTsar(dynamite)) {
            tsarGate.onExplosionFinished();
        }
    }

    private static String format(Location center) {
        if (center == null || center.getWorld() == null) {
            return "null";
        }
        return center.getWorld().getName() + '@'
                + center.getBlockX() + ',' + center.getBlockY() + ',' + center.getBlockZ();
    }

    private void playTsarPresentation(ExplosionJob job) {
        applyPresentation(job);
        postActionRunner.scheduleActions(plugin, job.getCenter(), job.getDynamite().explosion.postActions);
        job.markPresentationPlayed();
    }

    private void applyPresentation(ExplosionJob job) {
        Location center = job.getCenter();
        DynamiteDefinition dynamite = job.getDynamite();
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
        tsarGate.shutdown();
    }

}
