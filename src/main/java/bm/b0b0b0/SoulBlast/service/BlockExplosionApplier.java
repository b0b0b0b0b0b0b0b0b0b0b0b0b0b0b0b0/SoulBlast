package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.*;
import bm.b0b0b0.SoulBlast.decay.service.DecayExplosionBridge;
import bm.b0b0b0.SoulBlast.integration.coreprotect.CoreProtectBridge;
import bm.b0b0b0.SoulBlast.model.ExplosionBlockAction;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import bm.b0b0b0.SoulBlast.ps.service.PsExplosionBridge;
import bm.b0b0b0.SoulBlast.service.region.RegionProtectionService;
import bm.b0b0b0.SoulBlast.util.BukkitKeys;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public final class BlockExplosionApplier {

    private final BlastResistanceService resistanceService;
    private final ExplosionEntityEffectsService entityEffectsService;
    private final RegionProtectionService regionProtection;
    private final DecayExplosionBridge decayBridge;
    private final ObsidianInstantShatterService obsidianShatter;
    private final PsExplosionBridge psExplosionBridge;
    private final CoreProtectBridge coreProtect;
    private final ExplosionFireSupport fireSupport;
    private final java.util.Random random = new java.util.Random();

    public BlockExplosionApplier(
            BlastResistanceService resistanceService,
            ExplosionEntityEffectsService entityEffectsService,
            RegionProtectionService regionProtection,
            DecayExplosionBridge decayBridge,
            ObsidianInstantShatterService obsidianShatter,
            PsExplosionBridge psExplosionBridge,
            CoreProtectBridge coreProtect,
            ExplosionFireSupport fireSupport
    ) {
        this.resistanceService = resistanceService;
        this.entityEffectsService = entityEffectsService;
        this.regionProtection = regionProtection;
        this.decayBridge = decayBridge;
        this.obsidianShatter = obsidianShatter;
        this.psExplosionBridge = psExplosionBridge;
        this.coreProtect = coreProtect;
        this.fireSupport = fireSupport;
    }

    public void applyBlock(ExplosionJob job, ExplosionJob.BlockTarget target, ExplosionChunkContext chunkContext) {
        World world = job.getCenter().getWorld();
        if (world == null) {
            job.getDiagnostics().recordSkippedOther();
            return;
        }
        world.getChunkAt(target.x() >> 4, target.z() >> 4);
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 1;
        if (target.y() < minY || target.y() > maxY) {
            job.getDiagnostics().recordSkippedOutOfWorld();
            return;
        }
        Player player = job.getSource() instanceof Player sourcePlayer ? sourcePlayer : null;
        Location blockLocation = new Location(world, target.x(), target.y(), target.z());
        if (!regionProtection.allowsExplosionBlock(blockLocation, job.getDynamite(), player)) {
            job.getDiagnostics().recordRegionBlocked();
            return;
        }
        Block block = chunkContext.blockAt(world, target.x(), target.y(), target.z());
        boolean edgePhysics = job.requiresPhysics(target);
        if (target.action() == ExplosionBlockAction.CLEAR_LIQUID) {
            if (block.isLiquid() || block.getType() == Material.KELP || block.getType() == Material.SEAGRASS) {
                coreProtect.logLiquidClear(job, block);
                block.setType(Material.AIR, edgePhysics);
                job.getDiagnostics().recordApplied(ExplosionBlockAction.CLEAR_LIQUID, Material.AIR);
            } else {
                job.getDiagnostics().recordSkippedOther();
            }
            return;
        }
        if (target.action() == ExplosionBlockAction.REPLACE) {
            Material type = block.getType();
            if (type == Material.BEDROCK || type == Material.BARRIER) {
                job.getDiagnostics().recordSkippedOther();
                return;
            }
            coreProtect.logBreak(job, block);
            block.setType(target.placeMaterial(), edgePhysics);
            coreProtect.logPlace(job, block);
            job.getDiagnostics().recordApplied(ExplosionBlockAction.REPLACE, target.placeMaterial());
            return;
        }
        if (target.action() == ExplosionBlockAction.PLACE) {
            Material type = block.getType();
            if (type.isAir() || block.isLiquid() || type == Material.FIRE || type == Material.SOUL_FIRE) {
                if (!type.isAir() && !block.isLiquid()) {
                    coreProtect.logBreak(job, block);
                }
                block.setType(target.placeMaterial(), edgePhysics);
                coreProtect.logPlace(job, block);
                job.getDiagnostics().recordApplied(ExplosionBlockAction.PLACE, target.placeMaterial());
            } else {
                job.getDiagnostics().recordSkippedOther();
            }
            return;
        }
        if (block.getType().isAir()) {
            job.getDiagnostics().recordSkippedOther();
            return;
        }
        if (!ExplosionCenter.isWithinRadius(block, job.getCenter(), job.getDynamite().explosion.radius)) {
            job.getDiagnostics().recordSkippedOther();
            return;
        }
        if (block.getType() == Material.TNT) {
            entityEffectsService.breakTntBlocks(job, target);
            return;
        }
        if (psExplosionBridge != null && psExplosionBridge.tryAbsorbExplosion(block, job.getDynamite(), job)) {
            return;
        }
        ExplosionSettings settings = job.getDynamite().explosion;
        ExplosionBlockPolicy policy = resolvePolicy(settings, job.getDynamite());
        Optional<BlockExplosionRule> rule = findRule(settings.blockRules, block);
        ExplosionAlgorithmSettings algorithm = settings.algorithm;
        if (policy == ExplosionBlockPolicy.WHITELIST) {
            if (rule.isEmpty() || isKeepMode(rule.get())) {
                return;
            }
            fireSupport.igniteOnSolidBeforeBreak(job, block, settings, algorithm);
            applyBreak(block, job, rule.get(), algorithm, edgePhysics);
            return;
        }
        if (rule.isPresent()) {
            if (isKeepMode(rule.get())) {
                return;
            }
            fireSupport.igniteOnSolidBeforeBreak(job, block, settings, algorithm);
            applyBreak(block, job, rule.get(), algorithm, edgePhysics);
            return;
        }
        if (policy == ExplosionBlockPolicy.OMNIVORE) {
            if (block.isLiquid()) {
                coreProtect.logLiquidClear(job, block);
                block.setType(Material.AIR, edgePhysics);
                job.getDiagnostics().recordApplied(ExplosionBlockAction.BREAK, Material.AIR);
                return;
            }
            fireSupport.igniteOnSolidBeforeBreak(job, block, settings, algorithm);
            applyBreak(block, job, null, algorithm, edgePhysics);
            job.getDiagnostics().recordApplied(ExplosionBlockAction.BREAK, block.getType());
            return;
        }
        if (block.isLiquid()) {
            coreProtect.logLiquidClear(job, block);
            block.setType(Material.AIR, edgePhysics);
            return;
        }
        fireSupport.igniteOnSolidBeforeBreak(job, block, settings, algorithm);
        applyBreak(block, job, null, algorithm, edgePhysics);
    }

    private void applyBreak(
            Block block,
            ExplosionJob job,
            BlockExplosionRule rule,
            ExplosionAlgorithmSettings algorithm,
            boolean edgePhysics
    ) {
        DynamiteDefinition dynamite = job.getDynamite();
        if (psExplosionBridge != null && psExplosionBridge.tryAbsorbExplosion(block, dynamite, job)) {
            return;
        }
        if (obsidianShatter != null
                && obsidianShatter.isEnabled(algorithm)
                && obsidianShatter.tryShatter(block, job, rule, algorithm, edgePhysics)) {
            return;
        }
        if (obsidianShatter != null
                && obsidianShatter.isEnabled(algorithm)
                && obsidianShatter.matchesObsidianTarget(block)) {
            return;
        }
        if (decayBridge != null && decayBridge.supports(block) && !TsarBombRules.isTsar(dynamite)) {
            decayBridge.tryApplyExplosionDamage(
                    block,
                    job,
                    rule,
                    algorithm,
                    edgePhysics,
                    job.isRayBoosted(block.getX(), block.getY(), block.getZ())
            );
            return;
        }
        if (rule != null) {
            applyRule(block, job, rule, algorithm, edgePhysics);
            return;
        }
        breakBlock(block, job, algorithm, edgePhysics);
    }

    private ExplosionBlockPolicy resolvePolicy(ExplosionSettings settings, DynamiteDefinition dynamite) {
        ExplosionBlockPolicy policy = ExplosionBlockPolicy.parse(settings.blockPolicy);
        if (policy == ExplosionBlockPolicy.STANDARD && TsarBombRules.isTsar(dynamite)) {
            return ExplosionBlockPolicy.OMNIVORE;
        }
        return policy;
    }

    private static boolean isKeepMode(BlockExplosionRule rule) {
        return "KEEP".equalsIgnoreCase(rule.mode);
    }

    private void breakBlock(
            Block block,
            ExplosionJob job,
            ExplosionAlgorithmSettings algorithm,
            boolean edgePhysics
    ) {
        coreProtect.logBreak(job, block);
        ExplosionContainerBreak.breakWithAlgorithm(block, algorithm, edgePhysics);
    }

    private Optional<BlockExplosionRule> findRule(List<BlockExplosionRule> rules, Block block) {
        for (BlockExplosionRule rule : rules) {
            if (rule.chance < 1.0 && random.nextDouble() > rule.chance) {
                continue;
            }
            if (resistanceService.matchesTarget(rule.target, block)) {
                return Optional.of(rule);
            }
        }
        return Optional.empty();
    }

    private void applyRule(
            Block block,
            ExplosionJob job,
            BlockExplosionRule rule,
            ExplosionAlgorithmSettings algorithm,
            boolean edgePhysics
    ) {
        String mode = rule.mode.toUpperCase();
        switch (mode) {
            case "KEEP" -> {
            }
            case "DROP" -> {
                coreProtect.logBreak(job, block);
                ExplosionContainerBreak.applyDropRule(block, rule.dropMaterial, edgePhysics);
            }
            case "BREAK" -> breakBlock(block, job, algorithm, edgePhysics);
            case "TRANSFORM" -> {
                Material into = BukkitKeys.material(rule.transformInto);
                if (into != null) {
                    coreProtect.logBreak(job, block);
                    block.setType(into, edgePhysics);
                    coreProtect.logPlace(job, block);
                }
            }
            default -> breakBlock(block, job, algorithm, edgePhysics);
        }
    }

}
