package bm.b0b0b0.SoulBlast.decay.service;

import bm.b0b0b0.SoulBlast.SoulBlast;
import bm.b0b0b0.SoulBlast.decay.config.DecayBlockTypeDefinition;
import bm.b0b0b0.SoulBlast.decay.config.DecayGeneralSettings;
import bm.b0b0b0.SoulBlast.decay.config.DecayManualRepairSettings;
import bm.b0b0b0.SoulBlast.decay.message.DecayMessageService;
import bm.b0b0b0.SoulBlast.decay.model.DecayingBlockKey;
import bm.b0b0b0.SoulBlast.decay.model.DecayingBlockState;
import bm.b0b0b0.SoulBlast.decay.repository.DecayingBlockStore;
import bm.b0b0b0.SoulBlast.ps.PsModule;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DecayManualRepairService {

    public enum Result {
        SUCCESS,
        ALREADY_HEALTHY,
        NOT_DECAYING,
        NO_MATERIAL,
        BLOCKED,
        COOLDOWN,
        TOO_FAR,
        MODULE_OFF
    }

    private final SoulBlast plugin;
    private final DecayGeneralSettings general;
    private final DecayingBlockStore store;
    private final DecayBlockRegistry registry;
    private final DecayCrackBroadcaster crackBroadcaster;
    private final DecayMessageService messages;
    private final Map<UUID, Long> lastRepairAt = new ConcurrentHashMap<>();

    public DecayManualRepairService(
            SoulBlast plugin,
            DecayGeneralSettings general,
            DecayingBlockStore store,
            DecayBlockRegistry registry,
            DecayCrackBroadcaster crackBroadcaster,
            DecayMessageService messages
    ) {
        this.plugin = plugin;
        this.general = general;
        this.store = store;
        this.registry = registry;
        this.crackBroadcaster = crackBroadcaster;
        this.messages = messages;
    }

    public Result tryRepair(Player player, Block block) {
        if (player == null || block == null) {
            return Result.NOT_DECAYING;
        }
        if (!general.enabled) {
            return Result.MODULE_OFF;
        }
        DecayManualRepairSettings settings = general.manualRepair;
        if (settings == null || !settings.enabled) {
            return Result.MODULE_OFF;
        }
        if (settings.permission != null
                && !settings.permission.isBlank()
                && !player.hasPermission(settings.permission)) {
            return Result.BLOCKED;
        }
        if (player.getLocation().distanceSquared(block.getLocation().add(0.5, 0.5, 0.5)) > settings.reach * settings.reach) {
            return Result.TOO_FAR;
        }
        if (isOnCooldown(player.getUniqueId(), settings.cooldownMs)) {
            return Result.COOLDOWN;
        }
        if (isProtectionStone(block)) {
            return Result.BLOCKED;
        }
        DecayingBlockKey key = DecayingBlockKey.of(block.getWorld(), block.getX(), block.getY(), block.getZ());
        DecayingBlockState state = store.find(key).orElse(null);
        if (state == null || block.getType() != state.material() || state.damage() <= 0.0f) {
            return Result.NOT_DECAYING;
        }
        DecayBlockTypeDefinition type = state.type();
        if (type == null || !type.options.canManualRepair) {
            return Result.BLOCKED;
        }
        List<DecayRepairMaterialCost> costs = DecayRepairMaterialParser.costsFor(type, settings);
        DecayRepairMaterialCost affordable = DecayInventoryMaterials.findAffordable(player, costs);
        if (affordable == null) {
            messages.chat(player, "repair-no-material", materialLabel(costs));
            return Result.NO_MATERIAL;
        }
        if (!DecayInventoryMaterials.consume(player, affordable)) {
            messages.chat(player, "repair-no-material", materialLabel(costs));
            return Result.NO_MATERIAL;
        }
        float repairAmount = Math.max(0.01f, settings.repairPerClick);
        state.reduceDamage(repairAmount);
        state.touchRegeneration();
        lastRepairAt.put(player.getUniqueId(), System.currentTimeMillis());
        int percent = Math.round(state.damage() * 100.0f);
        if (state.damage() <= 0.0f) {
            crackBroadcaster.clearBlock(state);
            store.remove(key);
            messages.actionBar(player, "repair-success", DecayMessageService.percentPlaceholders(0));
        } else {
            crackBroadcaster.broadcastState(state, true);
            messages.actionBar(player, "repair-success", DecayMessageService.percentPlaceholders(percent));
        }
        playFeedback(player, block, settings);
        return Result.SUCCESS;
    }

    private boolean isOnCooldown(UUID playerId, int cooldownMs) {
        if (cooldownMs <= 0) {
            return false;
        }
        Long last = lastRepairAt.get(playerId);
        return last != null && System.currentTimeMillis() - last < cooldownMs;
    }

    private boolean isProtectionStone(Block block) {
        PsModule psModule = plugin.getPsModule();
        return psModule != null && psModule.isProtectBlock(block);
    }

    private void playFeedback(Player player, Block block, DecayManualRepairSettings settings) {
        Location at = block.getLocation().add(0.5, 0.5, 0.5);
        if (settings.particles) {
            player.getWorld().spawnParticle(
                    Particle.HAPPY_VILLAGER,
                    at,
                    6,
                    0.25,
                    0.25,
                    0.25,
                    0.02
            );
            player.getWorld().spawnParticle(
                    Particle.BLOCK,
                    at,
                    12,
                    0.2,
                    0.2,
                    0.2,
                    0.05,
                    block.getBlockData()
            );
        }
        if (settings.sound == null || settings.sound.isBlank()) {
            return;
        }
        Sound sound = resolveSound(settings.sound);
        if (sound != null) {
            player.playSound(at, sound, 1.0f, 1.15f);
        }
    }

    private static Sound resolveSound(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        String normalized = key.toLowerCase().replace(' ', '_');
        if (normalized.contains(":")) {
            NamespacedKey namespaced = NamespacedKey.fromString(normalized);
            if (namespaced != null) {
                return Registry.SOUNDS.get(namespaced);
            }
        }
        return Registry.SOUNDS.get(NamespacedKey.minecraft(normalized));
    }

    private Map<String, String> materialLabel(List<DecayRepairMaterialCost> costs) {
        if (costs == null || costs.isEmpty()) {
            return DecayMessageService.materialPlaceholders("?");
        }
        StringBuilder label = new StringBuilder();
        for (DecayRepairMaterialCost cost : costs) {
            if (!label.isEmpty()) {
                label.append(" / ");
            }
            label.append(formatMaterial(cost.material()));
        }
        return DecayMessageService.materialPlaceholders(label.toString());
    }

    private static String formatMaterial(Material material) {
        if (material == null) {
            return "?";
        }
        String name = material.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

}
