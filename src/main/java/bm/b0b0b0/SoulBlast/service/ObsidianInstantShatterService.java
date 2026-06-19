package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.BlockExplosionRule;
import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.config.ExplosionAlgorithmSettings;
import bm.b0b0b0.SoulBlast.decay.service.DecayDamageResolver;
import bm.b0b0b0.SoulBlast.decay.service.DecayExplosionBridge;
import bm.b0b0b0.SoulBlast.integration.coreprotect.CoreProtectBridge;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public final class ObsidianInstantShatterService {

    private static final String OBSIDIAN_GROUP = "obsidian_hard";

    private final BlastResistanceService resistanceService;
    private final DecayDamageResolver damageResolver;
    private final DecayExplosionBridge decayBridge;
    private final CoreProtectBridge coreProtect;

    public ObsidianInstantShatterService(
            BlastResistanceService resistanceService,
            DecayDamageResolver damageResolver,
            DecayExplosionBridge decayBridge,
            CoreProtectBridge coreProtect
    ) {
        this.resistanceService = resistanceService;
        this.damageResolver = damageResolver;
        this.decayBridge = decayBridge;
        this.coreProtect = coreProtect;
    }

    public boolean isEnabled(ExplosionAlgorithmSettings algorithm) {
        return algorithm != null && algorithm.obsidianInstantShatter;
    }

    public boolean matchesObsidianTarget(Block block) {
        return resistanceService.matchesTarget(OBSIDIAN_GROUP, block);
    }

    public boolean tryShatter(
            Block block,
            ExplosionJob job,
            BlockExplosionRule rule,
            ExplosionAlgorithmSettings algorithm,
            boolean edgePhysics
    ) {
        if (!isEnabled(algorithm) || !matchesObsidianTarget(block)) {
            return false;
        }
        DynamiteDefinition dynamite = job.getDynamite();
        Location center = job.getCenter();
        float radius = dynamite.explosion.radius;
        float proximity = resolveProximity(center, block, radius);
        float required = requiredProximity(block.getType(), algorithm);
        if (proximity < required) {
            return false;
        }
        if (decayBridge != null) {
            decayBridge.clearTracked(block);
        }
        Material material = block.getType();
        if (coreProtect != null) {
            coreProtect.logBreak(job, block);
        }
        applyBreakRule(block, rule, algorithm, edgePhysics);
        ExplosionPresentationEffects.playObsidianBlockShatter(
                block.getLocation().add(0.5, 0.5, 0.5),
                material,
                proximity,
                dynamite
        );
        return true;
    }

    private float resolveProximity(Location center, Block block, float radius) {
        if (damageResolver != null) {
            return damageResolver.explosionDistanceMultiplier(center, block, radius);
        }
        if (center == null || center.getWorld() == null || !center.getWorld().equals(block.getWorld()) || radius <= 0.01f) {
            return 1.0f;
        }
        double blockX = block.getX() + 0.5;
        double blockY = block.getY() + 0.5;
        double blockZ = block.getZ() + 0.5;
        double dx = blockX - center.getX();
        double dy = blockY - center.getY();
        double dz = blockZ - center.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double normalized = Math.min(1.0, distance / radius);
        double inverted = 1.0 - normalized;
        return (float) (inverted * inverted);
    }

    private static float requiredProximity(Material material, ExplosionAlgorithmSettings algorithm) {
        if (material == Material.CRYING_OBSIDIAN) {
            return Math.clamp(algorithm.obsidianShatterCryingProximity, 0.0f, 1.0f);
        }
        return Math.clamp(algorithm.obsidianShatterObsidianProximity, 0.0f, 1.0f);
    }

    private static void applyBreakRule(
            Block block,
            BlockExplosionRule rule,
            ExplosionAlgorithmSettings algorithm,
            boolean edgePhysics
    ) {
        if (rule != null && "DROP".equalsIgnoreCase(rule.mode)) {
            ExplosionContainerBreak.applyDropRule(block, rule.dropMaterial, edgePhysics);
            return;
        }
        ExplosionContainerBreak.breakWithAlgorithm(block, algorithm, edgePhysics);
    }

}
