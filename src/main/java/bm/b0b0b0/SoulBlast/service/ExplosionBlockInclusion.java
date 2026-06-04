package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.BlockExplosionRule;
import bm.b0b0b0.SoulBlast.config.ExplosionBlockPolicy;
import bm.b0b0b0.SoulBlast.config.ExplosionSettings;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import org.bukkit.block.Block;

public final class ExplosionBlockInclusion {

    private final BlastResistanceService resistanceService;

    public ExplosionBlockInclusion(BlastResistanceService resistanceService) {
        this.resistanceService = resistanceService;
    }

    public boolean includes(ExplosionJob job, Block block) {
        if (block.getType().isAir()) {
            return false;
        }
        ExplosionSettings settings = job.getDynamite().explosion;
        ExplosionBlockPolicy policy = ExplosionBlockPolicy.parse(settings.blockPolicy);
        if (policy != ExplosionBlockPolicy.WHITELIST) {
            return true;
        }
        for (BlockExplosionRule rule : settings.blockRules) {
            if (!resistanceService.matchesTarget(rule.target, block)) {
                continue;
            }
            return !"KEEP".equalsIgnoreCase(rule.mode);
        }
        return false;
    }

}
