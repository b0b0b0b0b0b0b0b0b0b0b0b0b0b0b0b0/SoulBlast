package bm.b0b0b0.SoulBlast.config;

import java.util.ArrayList;

final class DynamiteDefinitionCloner {

    private DynamiteDefinitionCloner() {
    }

    static void copyInto(DynamiteDefinition source, DynamiteDefinition target) {
        target.id = source.id;
        target.fuseTicks = source.fuseTicks;
        target.autoIgniteOnPlace = source.autoIgniteOnPlace;
        target.disableGravity = source.disableGravity;
        target.upwardVelocity = source.upwardVelocity;
        copyItem(source.item, target.item);
        copyIgnition(source.ignition, target.ignition);
        copyCrafting(source.crafting, target.crafting);
        copyGlow(source.glow, target.glow);
        copyHologram(source.hologram, target.hologram);
        copyExplosion(source.explosion, target.explosion);
        copyPurchase(source.purchase, target.purchase);
        copyFuseMisfire(source.fuseMisfire, target.fuseMisfire);
    }

    private static void copyFuseMisfire(FuseMisfireSettings source, FuseMisfireSettings target) {
        target.enabled = source.enabled;
        target.endChance = source.endChance;
        target.warningTicks = source.warningTicks;
        target.activateRelightChance = source.activateRelightChance;
        target.activateDetonateChance = source.activateDetonateChance;
        target.dudSound = source.dudSound;
        target.dudSoundVolume = source.dudSoundVolume;
        target.dudSoundPitch = source.dudSoundPitch;
        target.dudAmbientParticles = source.dudAmbientParticles;
    }

    private static void copyItem(DynamiteItemSettings source, DynamiteItemSettings target) {
        target.material = source.material;
        target.displayName = source.displayName;
        target.lore = new ArrayList<>(source.lore);
        target.customModelData = source.customModelData;
        target.glow = source.glow;
    }

    private static void copyIgnition(IgnitionSettings source, IgnitionSettings target) {
        target.allowFlintAndSteel = source.allowFlintAndSteel;
        target.allowFire = source.allowFire;
        target.allowFlameBow = source.allowFlameBow;
        target.allowRedstone = source.allowRedstone;
        target.allowExplosion = source.allowExplosion;
        target.allowOtherPrimed = source.allowOtherPrimed;
    }

    private static void copyCrafting(CraftingSettings source, CraftingSettings target) {
        target.enabled = source.enabled;
        target.recipeKey = source.recipeKey;
        target.shape = new ArrayList<>(source.shape);
        target.ingredients = new ArrayList<>(source.ingredients);
        target.resultAmount = source.resultAmount;
    }

    private static void copyGlow(GlowSettings source, GlowSettings target) {
        target.enabled = source.enabled;
        target.colorRgb = source.colorRgb;
        target.animation = source.animation;
        target.animationIntervalTicks = source.animationIntervalTicks;
        target.useTeamColor = source.useTeamColor;
        target.spawnParticles = source.spawnParticles;
    }

    private static void copyHologram(HologramSettings source, HologramSettings target) {
        target.enabled = source.enabled;
        target.lineName = source.lineName;
        target.lineTimer = source.lineTimer;
        target.offsetY = source.offsetY;
    }

    private static void copyPurchase(PurchaseSettings source, PurchaseSettings target) {
        target.type = source.type;
        target.amount = source.amount;
        target.money = source.money;
        target.experience = source.experience;
        target.vanillaTnt = source.vanillaTnt;
    }

    private static void copyExplosion(ExplosionSettings source, ExplosionSettings target) {
        target.radius = source.radius;
        target.entityDamage = source.entityDamage;
        target.damagePlayers = source.damagePlayers;
        target.power = source.power;
        target.quality = source.quality;
        target.samplingRayOverride = source.samplingRayOverride;
        target.blockBudgetMultiplier = source.blockBudgetMultiplier;
        target.createFire = source.createFire;
        target.breakBlocks = source.breakBlocks;
        target.spreadAcrossTicks = source.spreadAcrossTicks;
        target.blockPolicy = source.blockPolicy;
        copyAlgorithm(source.algorithm, target.algorithm);
        copyEffects(source.effects, target.effects);
        target.blockRules = new ArrayList<>(source.blockRules);
        target.postActions = new ArrayList<>(source.postActions);
    }

    private static void copyAlgorithm(ExplosionAlgorithmSettings source, ExplosionAlgorithmSettings target) {
        target.rayRandomness = source.rayRandomness;
        target.rayStep = source.rayStep;
        target.dropChanceMultiplier = source.dropChanceMultiplier;
        target.edgePhysicsOnly = source.edgePhysicsOnly;
        target.fireChance = source.fireChance;
        target.resistanceMultiplier = source.resistanceMultiplier;
        target.minimumRayPower = source.minimumRayPower;
        target.waveLineOfSight = source.waveLineOfSight;
        target.waveRayOverlay = source.waveRayOverlay;
        target.waveRayOverlayRays = source.waveRayOverlayRays;
        target.waveRayOverlayDecayMultiplier = source.waveRayOverlayDecayMultiplier;
    }

    private static void copyEffects(ExplosionEffectsSettings source, ExplosionEffectsSettings target) {
        target.removeWater = source.removeWater;
        target.removeLava = source.removeLava;
        target.liquidRadius = source.liquidRadius;
        target.liquidDrainMaxBlocks = source.liquidDrainMaxBlocks;
        target.drainVortex = source.drainVortex;
        target.drainVortexTicks = source.drainVortexTicks;
        target.drainVortexIntensity = source.drainVortexIntensity;
        target.destroyTntBlocks = source.destroyTntBlocks;
        target.detonateOtherPrimed = source.detonateOtherPrimed;
        copyCraterFill(source.craterFill, target.craterFill);
        copyFuseLightning(source.fuseLightning, target.fuseLightning);
        copyWarheads(source.warheads, target.warheads);
        copyPresentation(source.presentation, target.presentation);
    }

    private static void copyPresentation(ExplosionPresentationSettings source, ExplosionPresentationSettings target) {
        target.enabled = source.enabled;
        target.intensity = source.intensity;
    }

    private static void copyWarheads(TsarWarheadSettings source, TsarWarheadSettings target) {
        target.enabled = source.enabled;
        target.launchTicksBeforeEnd = source.launchTicksBeforeEnd;
        target.launchSpeed = source.launchSpeed;
        target.upwardBoost = source.upwardBoost;
        target.warheadFuseTicks = source.warheadFuseTicks;
        target.warheadIds = new ArrayList<>(source.warheadIds);
    }

    private static void copyFuseLightning(FuseLightningSettings source, FuseLightningSettings target) {
        target.enabled = source.enabled;
        target.ticksBeforeEnd = source.ticksBeforeEnd;
        target.boltCount = source.boltCount;
        target.boltIntervalTicks = source.boltIntervalTicks;
        target.spreadRadius = source.spreadRadius;
        target.realLightning = source.realLightning;
    }

    private static void copyCraterFill(CraterFillSettings source, CraterFillSettings target) {
        target.enabled = source.enabled;
        target.radius = source.radius;
        target.floorMaterial = source.floorMaterial;
        target.coatMaterial = source.coatMaterial;
        target.floorDepth = source.floorDepth;
        target.lavaChance = source.lavaChance;
        target.allowLavaCoat = source.allowLavaCoat;
        target.magmaShell = source.magmaShell;
        target.magmaShellWidth = source.magmaShellWidth;
        target.magmaShellLayers = source.magmaShellLayers;
        target.shellMaterial = source.shellMaterial;
        target.spreadAcrossTicks = source.spreadAcrossTicks;
    }

}
