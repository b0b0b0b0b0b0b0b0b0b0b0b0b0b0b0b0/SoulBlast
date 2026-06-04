package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.ExplosionAlgorithmSettings;
import bm.b0b0b0.SoulBlast.util.BukkitKeys;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public final class ExplosionContainerBreak {

    private static final Random RANDOM = new Random();

    private ExplosionContainerBreak() {
    }

    public static boolean isLootContainer(Block block) {
        if (!(block.getState() instanceof Container)) {
            return false;
        }
        Material type = block.getType();
        return type != Material.ENDER_CHEST;
    }

    public static void breakNaturally(Block block, boolean edgePhysics) {
        if (isLootContainer(block)) {
            block.breakNaturally();
            return;
        }
        block.setType(Material.AIR, edgePhysics);
    }

    public static void breakWithAlgorithm(Block block, ExplosionAlgorithmSettings algorithm, boolean edgePhysics) {
        if (isLootContainer(block)) {
            block.breakNaturally();
            return;
        }
        if (algorithm.dropChanceMultiplier <= 0) {
            block.setType(Material.AIR, edgePhysics);
        } else if (RANDOM.nextFloat() > algorithm.dropChanceMultiplier * 0.3f) {
            block.breakNaturally();
        } else {
            block.setType(Material.AIR, edgePhysics);
        }
    }

    public static void applyDropRule(Block block, String dropMaterialRaw, boolean edgePhysics) {
        if (isLootContainer(block)) {
            block.breakNaturally();
            return;
        }
        Material drop = dropMaterialRaw == null || dropMaterialRaw.isBlank()
                ? block.getType()
                : BukkitKeys.material(dropMaterialRaw);
        if (drop != null && drop != Material.AIR) {
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(drop));
        }
        block.setType(Material.AIR, edgePhysics);
    }

}
