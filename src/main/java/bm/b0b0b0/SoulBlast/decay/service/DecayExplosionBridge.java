package bm.b0b0b0.SoulBlast.decay.service;

import bm.b0b0b0.SoulBlast.config.BlockExplosionRule;
import bm.b0b0b0.SoulBlast.config.ExplosionAlgorithmSettings;
import bm.b0b0b0.SoulBlast.decay.config.DecayBlockTypeDefinition;
import bm.b0b0b0.SoulBlast.decay.config.DecayGeneralSettings;
import bm.b0b0b0.SoulBlast.decay.model.DecayingBlockKey;
import bm.b0b0b0.SoulBlast.decay.model.DecayingBlockState;
import bm.b0b0b0.SoulBlast.decay.repository.DecayingBlockStore;
import bm.b0b0b0.SoulBlast.integration.coreprotect.CoreProtectBridge;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import org.bukkit.block.Block;

import java.util.Optional;

public final class DecayExplosionBridge {

    private final DecayGeneralSettings general;
    private final DecayBlockRegistry registry;
    private final DecayDamageResolver damageResolver;
    private final DecayingBlockStore store;
    private final DecayBlockBreaker breaker;
    private final DecayCrackBroadcaster crackBroadcaster;
    private CoreProtectBridge coreProtect;

    public DecayExplosionBridge(
            DecayGeneralSettings general,
            DecayBlockRegistry registry,
            DecayDamageResolver damageResolver,
            DecayingBlockStore store,
            DecayBlockBreaker breaker,
            DecayCrackBroadcaster crackBroadcaster
    ) {
        this.general = general;
        this.registry = registry;
        this.damageResolver = damageResolver;
        this.store = store;
        this.breaker = breaker;
        this.crackBroadcaster = crackBroadcaster;
    }

    public void bindCoreProtect(CoreProtectBridge coreProtect) {
        this.coreProtect = coreProtect;
    }

    public boolean isEnabled() {
        return general.enabled;
    }

    public boolean supports(Block block) {
        return general.enabled && registry.find(block).isPresent();
    }

    public void clearTracked(Block block) {
        if (!general.enabled || block == null) {
            return;
        }
        DecayingBlockKey key = DecayingBlockKey.of(block.getWorld(), block.getX(), block.getY(), block.getZ());
        DecayingBlockState state = store.find(key).orElse(null);
        if (state == null) {
            return;
        }
        crackBroadcaster.clearBlock(state);
        store.remove(key);
    }

    public boolean tryApplyExplosionDamage(
            Block block,
            ExplosionJob job,
            BlockExplosionRule rule,
            ExplosionAlgorithmSettings algorithm,
            boolean edgePhysics,
            boolean rayBoosted
    ) {
        if (!general.enabled) {
            return false;
        }
        Optional<DecayBlockTypeDefinition> type = registry.find(block);
        if (type.isEmpty()) {
            return false;
        }
        DecayingBlockKey key = DecayingBlockKey.of(block.getWorld(), block.getX(), block.getY(), block.getZ());
        DecayingBlockState existing = store.find(key).orElse(null);
        if (existing == null && store.size() >= general.maxActiveBlocks) {
            return true;
        }
        DecayingBlockState state = existing != null
                ? existing
                : createState(key, block, type.get(), rule);
        float hit = damageResolver.rollHit(
                job.getDynamite().id,
                type.get().resistance,
                job.getCenter(),
                block,
                job.getDynamite().explosion.radius
        );
        if (rayBoosted && algorithm.waveRayOverlayDecayMultiplier > 1.0f) {
            float bonus = hit * (algorithm.waveRayOverlayDecayMultiplier - 1.0f);
            hit = Math.min(0.2f, hit + bonus);
        }
        state.addDamage(hit);
        if (state.isBroken()) {
            crackBroadcaster.clearBlock(state);
            store.remove(key);
            if (coreProtect != null) {
                coreProtect.logBreak(job, block);
            }
            breaker.finalizeBreak(block, state, algorithm, edgePhysics);
            return true;
        }
        store.put(state);
        crackBroadcaster.broadcastState(state);
        return true;
    }

    private DecayingBlockState createState(
            DecayingBlockKey key,
            Block block,
            DecayBlockTypeDefinition type,
            BlockExplosionRule rule
    ) {
        String mode = "BREAK";
        String drop = "";
        String transform = "";
        if (rule != null) {
            mode = rule.mode;
            drop = rule.dropMaterial;
            transform = rule.transformInto;
        }
        return new DecayingBlockState(key, block.getType(), type, mode, drop, transform);
    }

}
