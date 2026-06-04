package bm.b0b0b0.SoulBlast.config;

import bm.b0b0b0.SoulBlast.service.TsarBombRules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DynamiteDefaultsMerger {

    private static final String GENERIC_DISPLAY = "&cКастомный динамит";

    private static final Set<String> LEGACY_DISPLAY_NAMES = Set.of(
            "&c&lУголёк погребального огня",
            "&b&lОтлив пепельной волны",
            "&8&lЦепная элегия",
            "&9&lПолумесяц бездны",
            "&d&lСверхновая поглощённых",
            "&5&lОбсидиановый обряд",
            "&5&lПлачущие души",
            "&4&lСердце затмения",
            "&1&lКоллапс бездны",
            "&6&lПоследний костёр душ",
            "&c&l☢ ЦАРЬ-БОМБА"
    );

    public void merge(Map<String, DynamiteDefinition> dynamites) {
        Map<String, DynamiteDefinition> template = DynamitePresets.createDefaults();
        for (Map.Entry<String, DynamiteDefinition> templateEntry : template.entrySet()) {
            String id = templateEntry.getKey();
            DynamiteDefinition defaults = templateEntry.getValue();
            DynamiteDefinition current = dynamites.get(id);
            if (current == null) {
                dynamites.put(id, defaults);
                continue;
            }
            applyTemplate(current, defaults);
            migrateExplosionRoles(current, defaults);
            sanitizeGriefLoot(current);
        }
        dynamites.remove("burial_ember");
        dynamites.remove("ash_tide");
        migrateDynamiteId(dynamites, "obsidian_rite", "crying_souls");
        dynamites.remove("chain_elegy");
        for (DynamiteDefinition definition : dynamites.values()) {
            definition.explosion.effects.warheads.warheadIds.removeIf("chain_elegy"::equals);
        }
    }

    private void migrateDynamiteId(Map<String, DynamiteDefinition> dynamites, String oldId, String newId) {
        if (!dynamites.containsKey(oldId) || dynamites.containsKey(newId)) {
            return;
        }
        DynamiteDefinition definition = dynamites.remove(oldId);
        definition.id = newId;
        dynamites.put(newId, definition);
    }

    private void applyTemplate(DynamiteDefinition current, DynamiteDefinition defaults) {
        if (GENERIC_DISPLAY.equals(current.item.displayName) || LEGACY_DISPLAY_NAMES.contains(current.item.displayName)) {
            current.item.displayName = defaults.item.displayName;
            current.item.lore = new ArrayList<>(defaults.item.lore);
        }
        if (isGenericLore(current.item.lore)) {
            current.item.lore = new ArrayList<>(defaults.item.lore);
        }
        if (current.glow.colorRgb.equals("255,80,40") && !defaults.glow.colorRgb.equals("255,80,40")) {
            current.glow.colorRgb = defaults.glow.colorRgb;
        }
        if (Math.abs(current.explosion.radius - 4.0f) < 0.01f && defaults.explosion.radius != 4.0f) {
            current.fuseTicks = defaults.fuseTicks;
            copyExplosion(current, defaults);
        } else if (current.explosion.radius < defaults.explosion.radius * 0.85f) {
            copyExplosion(current, defaults);
        } else if (current.explosion.radius > defaults.explosion.radius * 1.25f
                && !"last_pyre".equals(current.id)) {
            applyOversizedExplosionDowngrade(current, defaults);
        } else if ("last_pyre".equals(current.id) && current.explosion.radius < 88f) {
            applyLastPyreUpgrade(current, defaults);
        }
        if (current.purchase.isFree() && !defaults.purchase.isFree()) {
            current.purchase.type = defaults.purchase.type;
            current.purchase.amount = defaults.purchase.amount;
        }
        sanitizePurchaseCooldowns(current, defaults);
    }

    private void applyOversizedExplosionDowngrade(DynamiteDefinition current, DynamiteDefinition defaults) {
        copyExplosion(current, defaults);
        current.explosion.blockRules = copyBlockRules(defaults);
        current.explosion.postActions = new ArrayList<>(defaults.explosion.postActions);
    }

    private void applyLastPyreUpgrade(DynamiteDefinition current, DynamiteDefinition defaults) {
        copyExplosion(current, defaults);
        current.explosion.blockRules = copyBlockRules(defaults);
        current.explosion.postActions = new ArrayList<>(defaults.explosion.postActions);
        current.fuseTicks = defaults.fuseTicks;
        current.item.displayName = defaults.item.displayName;
        current.item.lore = new ArrayList<>(defaults.item.lore);
        current.glow.colorRgb = defaults.glow.colorRgb;
        current.hologram.offsetY = defaults.hologram.offsetY;
        current.explosion.blockBudgetMultiplier = defaults.explosion.blockBudgetMultiplier;
        current.purchase.purchaseCooldownSeconds = defaults.purchase.purchaseCooldownSeconds;
        current.purchase.useCooldownSeconds = defaults.purchase.useCooldownSeconds;
    }

    private void migrateExplosionRoles(DynamiteDefinition current, DynamiteDefinition defaults) {
        if ("void_crescent".equals(current.id)) {
            current.explosion.blockPolicy = defaults.explosion.blockPolicy;
            current.explosion.blockRules = copyBlockRules(defaults);
            current.explosion.quality = defaults.explosion.quality;
            current.explosion.algorithm.resistanceMultiplier = defaults.explosion.algorithm.resistanceMultiplier;
            current.explosion.algorithm.minimumRayPower = defaults.explosion.algorithm.minimumRayPower;
            current.explosion.algorithm.waveLineOfSight = defaults.explosion.algorithm.waveLineOfSight;
            current.explosion.algorithm.waveRayOverlay = defaults.explosion.algorithm.waveRayOverlay;
            current.explosion.algorithm.waveRayOverlayRays = defaults.explosion.algorithm.waveRayOverlayRays;
            current.explosion.algorithm.waveRayOverlayDecayMultiplier = defaults.explosion.algorithm.waveRayOverlayDecayMultiplier;
            current.fuseMisfire.enabled = defaults.fuseMisfire.enabled;
            current.fuseMisfire.endChance = defaults.fuseMisfire.endChance;
            current.fuseMisfire.warningTicks = defaults.fuseMisfire.warningTicks;
            current.fuseMisfire.activateRelightChance = defaults.fuseMisfire.activateRelightChance;
            current.fuseMisfire.activateDetonateChance = defaults.fuseMisfire.activateDetonateChance;
            current.fuseMisfire.dudSound = defaults.fuseMisfire.dudSound;
            current.fuseMisfire.dudSoundVolume = defaults.fuseMisfire.dudSoundVolume;
            current.fuseMisfire.dudSoundPitch = defaults.fuseMisfire.dudSoundPitch;
            current.fuseMisfire.dudAmbientParticles = defaults.fuseMisfire.dudAmbientParticles;
            current.item.lore = new ArrayList<>(defaults.item.lore);
        }
        if ("abyss_collapse".equals(current.id)) {
            current.explosion.blockPolicy = defaults.explosion.blockPolicy;
            current.explosion.blockRules = copyBlockRules(defaults);
            current.explosion.quality = defaults.explosion.quality;
            current.explosion.spreadAcrossTicks = defaults.explosion.spreadAcrossTicks;
            current.explosion.algorithm.resistanceMultiplier = defaults.explosion.algorithm.resistanceMultiplier;
            current.explosion.algorithm.minimumRayPower = defaults.explosion.algorithm.minimumRayPower;
            current.explosion.algorithm.waveLineOfSight = defaults.explosion.algorithm.waveLineOfSight;
            current.explosion.effects.removeWater = defaults.explosion.effects.removeWater;
            current.explosion.effects.removeLava = defaults.explosion.effects.removeLava;
            current.explosion.effects.liquidRadius = defaults.explosion.effects.liquidRadius;
            current.explosion.effects.liquidDrainMaxBlocks = defaults.explosion.effects.liquidDrainMaxBlocks;
            current.explosion.radius = defaults.explosion.radius;
            current.explosion.effects.drainVortex = defaults.explosion.effects.drainVortex;
            current.explosion.effects.drainVortexTicks = defaults.explosion.effects.drainVortexTicks;
            current.explosion.effects.drainVortexIntensity = defaults.explosion.effects.drainVortexIntensity;
            current.explosion.effects.presentation.enabled = defaults.explosion.effects.presentation.enabled;
            current.explosion.effects.presentation.intensity = defaults.explosion.effects.presentation.intensity;
            current.explosion.entityDamage = defaults.explosion.entityDamage;
            current.explosion.damagePlayers = defaults.explosion.damagePlayers;
            current.item.lore = new ArrayList<>(defaults.item.lore);
        }
        if ("crying_souls".equals(current.id)) {
            current.explosion.blockPolicy = defaults.explosion.blockPolicy;
            current.explosion.blockRules = copyBlockRules(defaults);
            current.explosion.quality = defaults.explosion.quality;
            current.explosion.spreadAcrossTicks = defaults.explosion.spreadAcrossTicks;
            current.explosion.entityDamage = defaults.explosion.entityDamage;
            current.explosion.damagePlayers = defaults.explosion.damagePlayers;
            current.explosion.algorithm.resistanceMultiplier = defaults.explosion.algorithm.resistanceMultiplier;
            current.explosion.algorithm.minimumRayPower = defaults.explosion.algorithm.minimumRayPower;
            current.explosion.algorithm.waveLineOfSight = defaults.explosion.algorithm.waveLineOfSight;
            current.explosion.algorithm.obsidianInstantShatter = defaults.explosion.algorithm.obsidianInstantShatter;
            current.explosion.algorithm.obsidianShatterObsidianProximity = defaults.explosion.algorithm.obsidianShatterObsidianProximity;
            current.explosion.algorithm.obsidianShatterCryingProximity = defaults.explosion.algorithm.obsidianShatterCryingProximity;
            current.explosion.effects.presentation.intensity = defaults.explosion.effects.presentation.intensity;
            current.item.lore = new ArrayList<>(defaults.item.lore);
        }
    }

    private List<BlockExplosionRule> copyBlockRules(DynamiteDefinition defaults) {
        List<BlockExplosionRule> rules = new ArrayList<>();
        for (BlockExplosionRule source : defaults.explosion.blockRules) {
            BlockExplosionRule rule = new BlockExplosionRule();
            rule.target = source.target;
            rule.mode = source.mode;
            rule.chance = source.chance;
            rule.dropMaterial = source.dropMaterial;
            rule.transformInto = source.transformInto;
            rules.add(rule);
        }
        return rules;
    }

    private void sanitizePurchaseCooldowns(DynamiteDefinition current, DynamiteDefinition defaults) {
        if ("last_pyre".equals(current.id) && defaults.purchase.purchaseCooldownSeconds > 0) {
            current.purchase.purchaseCooldownSeconds = defaults.purchase.purchaseCooldownSeconds;
            current.purchase.useCooldownSeconds = defaults.purchase.useCooldownSeconds;
        }
        if (current.purchase.purchaseCooldownSeconds > 600) {
            current.purchase.purchaseCooldownSeconds = defaults.purchase.purchaseCooldownSeconds;
        }
        if (current.purchase.useCooldownSeconds > 600) {
            current.purchase.useCooldownSeconds = defaults.purchase.useCooldownSeconds;
        }
        if (current.purchase.purchaseCooldownSeconds <= 0 && defaults.purchase.purchaseCooldownSeconds > 0) {
            current.purchase.purchaseCooldownSeconds = defaults.purchase.purchaseCooldownSeconds;
        }
        if (current.purchase.useCooldownSeconds <= 0 && defaults.purchase.useCooldownSeconds > 0) {
            current.purchase.useCooldownSeconds = defaults.purchase.useCooldownSeconds;
        }
    }

    private void copyExplosion(DynamiteDefinition current, DynamiteDefinition defaults) {
        current.explosion.radius = defaults.explosion.radius;
        current.explosion.power = defaults.explosion.power;
        current.explosion.quality = defaults.explosion.quality;
        current.explosion.entityDamage = defaults.explosion.entityDamage;
        current.explosion.damagePlayers = defaults.explosion.damagePlayers;
        current.explosion.createFire = defaults.explosion.createFire;
        copyEffects(current.explosion.effects, defaults.explosion.effects);
        current.explosion.algorithm.dropChanceMultiplier = defaults.explosion.algorithm.dropChanceMultiplier;
        current.explosion.algorithm.resistanceMultiplier = defaults.explosion.algorithm.resistanceMultiplier;
        current.explosion.algorithm.minimumRayPower = defaults.explosion.algorithm.minimumRayPower;
        current.explosion.algorithm.rayStep = defaults.explosion.algorithm.rayStep;
        current.explosion.algorithm.rayRandomness = defaults.explosion.algorithm.rayRandomness;
        current.explosion.algorithm.fireChance = defaults.explosion.algorithm.fireChance;
        current.explosion.quality = defaults.explosion.quality;
        current.explosion.samplingRayOverride = defaults.explosion.samplingRayOverride;
        current.explosion.blockBudgetMultiplier = defaults.explosion.blockBudgetMultiplier;
        current.explosion.blockPolicy = defaults.explosion.blockPolicy;
        if (current.explosion.blockRules.isEmpty() && !defaults.explosion.blockRules.isEmpty()) {
            current.explosion.blockRules = new ArrayList<>(defaults.explosion.blockRules);
        }
        if (current.explosion.postActions.isEmpty() && !defaults.explosion.postActions.isEmpty()) {
            current.explosion.postActions = new ArrayList<>(defaults.explosion.postActions);
        }
    }

    private void copyEffects(ExplosionEffectsSettings current, ExplosionEffectsSettings defaults) {
        current.removeWater = defaults.removeWater;
        current.removeLava = defaults.removeLava;
        current.liquidRadius = defaults.liquidRadius;
        current.liquidDrainMaxBlocks = defaults.liquidDrainMaxBlocks;
        current.drainVortex = defaults.drainVortex;
        current.drainVortexTicks = defaults.drainVortexTicks;
        current.drainVortexIntensity = defaults.drainVortexIntensity;
        current.destroyTntBlocks = defaults.destroyTntBlocks;
        current.detonateOtherPrimed = defaults.detonateOtherPrimed;
        current.craterFill.enabled = defaults.craterFill.enabled;
        current.craterFill.radius = defaults.craterFill.radius;
        current.craterFill.floorMaterial = defaults.craterFill.floorMaterial;
        current.craterFill.coatMaterial = defaults.craterFill.coatMaterial;
        current.craterFill.floorDepth = defaults.craterFill.floorDepth;
        current.craterFill.lavaChance = defaults.craterFill.lavaChance;
        current.craterFill.allowLavaCoat = defaults.craterFill.allowLavaCoat;
        current.craterFill.magmaShell = defaults.craterFill.magmaShell;
        current.craterFill.magmaShellWidth = defaults.craterFill.magmaShellWidth;
        current.craterFill.magmaShellLayers = defaults.craterFill.magmaShellLayers;
        current.craterFill.shellMaterial = defaults.craterFill.shellMaterial;
        copyFuseLightning(current.fuseLightning, defaults.fuseLightning);
        copyWarheads(current.warheads, defaults.warheads);
        current.presentation.enabled = defaults.presentation.enabled;
        current.presentation.intensity = defaults.presentation.intensity;
    }

    private void copyWarheads(TsarWarheadSettings current, TsarWarheadSettings defaults) {
        if (!defaults.enabled) {
            return;
        }
        if (!current.enabled || current.warheadIds.isEmpty()) {
            current.enabled = defaults.enabled;
            current.launchTicksBeforeEnd = defaults.launchTicksBeforeEnd;
            current.launchSpeed = defaults.launchSpeed;
            current.upwardBoost = defaults.upwardBoost;
            current.warheadFuseTicks = defaults.warheadFuseTicks;
            current.warheadIds = new ArrayList<>(defaults.warheadIds);
        }
    }

    private void copyFuseLightning(FuseLightningSettings current, FuseLightningSettings defaults) {
        if (!defaults.enabled) {
            return;
        }
        if (!current.enabled) {
            current.enabled = defaults.enabled;
            current.ticksBeforeEnd = defaults.ticksBeforeEnd;
            current.boltCount = defaults.boltCount;
            current.boltIntervalTicks = defaults.boltIntervalTicks;
            current.spreadRadius = defaults.spreadRadius;
            current.realLightning = defaults.realLightning;
        }
    }

    private void sanitizeGriefLoot(DynamiteDefinition dynamite) {
        if (TsarBombRules.ID.equals(dynamite.id)) {
            return;
        }
        CraterFillSettings fill = dynamite.explosion.effects.craterFill;
        fill.allowLavaCoat = false;
        fill.coatMaterial = "";
        fill.lavaChance = 0.0;
        fill.magmaShell = false;
        fill.hellFloorScatter = false;
        if (dynamite.explosion.algorithm.dropChanceMultiplier <= 0.0) {
            dynamite.explosion.algorithm.dropChanceMultiplier = 1.0;
        }
    }

    private boolean isGenericLore(java.util.List<String> lore) {
        if (lore.isEmpty()) {
            return true;
        }
        return lore.stream().anyMatch(line ->
                line.contains("{dyn_id}") || line.contains("Уникальный взрывчатый")
        );
    }

}
