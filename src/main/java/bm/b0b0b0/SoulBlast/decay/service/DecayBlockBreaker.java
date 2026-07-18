package bm.b0b0b0.SoulBlast.decay.service;

import bm.b0b0b0.SoulBlast.config.ExplosionAlgorithmSettings;
import bm.b0b0b0.SoulBlast.decay.model.DecayingBlockState;
import bm.b0b0b0.SoulBlast.service.ExplosionContainerBreak;
import bm.b0b0b0.SoulBlast.util.BukkitKeys;
import org.bukkit.Material;
import org.bukkit.block.Block;

public final class DecayBlockBreaker {

    public void finalizeBreak(Block block, DecayingBlockState state, ExplosionAlgorithmSettings algorithm, boolean edgePhysics) {
        if (block.getType() != state.material()) {
            return;
        }
        String mode = state.breakMode() == null ? "BREAK" : state.breakMode().toUpperCase();
        switch (mode) {
            case "DROP" -> applyDrop(block, state, edgePhysics);
            case "TRANSFORM" -> applyTransform(block, state, edgePhysics);
            case "KEEP" -> {
            }
            default -> breakVanilla(block, algorithm, edgePhysics);
        }
    }

    private void applyDrop(Block block, DecayingBlockState state, boolean edgePhysics) {
        ExplosionContainerBreak.applyDropRule(block, state.dropMaterial(), edgePhysics);
    }

    private void applyTransform(Block block, DecayingBlockState state, boolean edgePhysics) {
        Material into = BukkitKeys.material(state.transformInto());
        if (into != null) {
            block.setType(into, edgePhysics);
        } else {
            block.setType(Material.AIR, edgePhysics);
        }
    }

    private void breakVanilla(Block block, ExplosionAlgorithmSettings algorithm, boolean edgePhysics) {
        ExplosionContainerBreak.breakWithAlgorithm(block, algorithm, edgePhysics);
    }

}
