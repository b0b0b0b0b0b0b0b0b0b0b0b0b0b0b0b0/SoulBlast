package bm.b0b0b0.SoulBlast.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DynamitePresets {

    private DynamitePresets() {
    }

    public static Map<String, DynamiteDefinition> createDefaults() {
        Map<String, DynamiteDefinition> map = new LinkedHashMap<>();
        map.put("void_crescent", voidCrescent());
        map.put("soul_supernova", soulSupernova());
        map.put("crying_souls", cryingSouls());
        map.put("eclipse_heart", eclipseHeart());
        map.put("abyss_collapse", abyssCollapse());
        map.put("last_pyre", lastPyre());
        return map;
    }

    private static DynamiteDefinition voidCrescent() {
        DynamiteDefinition d = base("void_crescent", "&b&lСвет Холодной Луны", List.of(
                "&bРоль: &7рассечение построек",
                "&7Дерево, кирпич, стекло в радиусе",
                "&7Обсидиан стачивается, но медленно",
                "&7Землю и траву не трогает"
        ), "120,160,255", 80, 10.0f, 6.0f, "WAVE");
        d.explosion.blockPolicy = ExplosionBlockPolicy.WHITELIST.name();
        d.explosion.algorithm.resistanceMultiplier = 0.16;
        d.explosion.algorithm.minimumRayPower = 0.008f;
        d.explosion.algorithm.waveLineOfSight = false;
        d.explosion.algorithm.waveRayOverlay = true;
        d.explosion.algorithm.waveRayOverlayRays = 144;
        d.explosion.algorithm.waveRayOverlayDecayMultiplier = 1.9f;
        d.fuseMisfire.enabled = true;
        d.fuseMisfire.endChance = 0.1;
        d.fuseMisfire.warningTicks = 40;
        d.fuseMisfire.activateRelightChance = 0.52;
        d.fuseMisfire.activateDetonateChance = 0.33;
        d.fuseMisfire.dudSound = "entity.creeper.primed";
        d.fuseMisfire.dudSoundVolume = 1.0f;
        d.fuseMisfire.dudSoundPitch = 1.25f;
        d.fuseMisfire.dudAmbientParticles = true;
        d.explosion.effects.presentation.intensity = 1.45f;
        d.explosion.blockRules.add(blockRule("BEDROCK", "KEEP", 1.0));
        d.explosion.blockRules.add(blockRule("REINFORCED_DEEPSLATE", "KEEP", 1.0));
        d.explosion.blockRules.add(blockRule("obsidian_hard", "BREAK", 1.0));
        d.explosion.blockRules.add(blockRule("CHEST", "DROP", 1.0));
        d.explosion.blockRules.add(blockRule("TRAPPED_CHEST", "DROP", 1.0));
        d.explosion.blockRules.add(blockRule("BARREL", "DROP", 1.0));
        d.explosion.blockRules.add(blockRule("ENDER_CHEST", "DROP", 1.0));
        d.explosion.blockRules.add(blockRule("storage_shulker", "DROP", 1.0));
        d.explosion.blockRules.add(blockRule("raid_timber", "BREAK", 1.0));
        d.explosion.blockRules.add(blockRule("raid_masonry", "BREAK", 1.0));
        d.explosion.blockRules.add(blockRule("base_raid", "BREAK", 1.0));
        d.explosion.blockRules.add(blockRule("fragile_trap", "BREAK", 1.0));
        applyPurchase(d, PurchaseType.MONEY, 350);
        return d;
    }

    private static DynamiteDefinition soulSupernova() {
        DynamiteDefinition d = base("soul_supernova", "&d&lГром Великой Руны", List.of(
                "&dРоль: &7штурм базы",
                "&7Ломает постройки, сундуки — лут",
                "&7Обсидиан только &5Плачущие души"
        ), "200,80,220", 80, 10.0f, 6.5f, "HIGH");
        d.explosion.entityDamage = 14.0;
        d.explosion.createFire = false;
        d.explosion.algorithm.fireChance = 0.0;
        d.explosion.effects.removeWater = true;
        d.explosion.effects.removeLava = true;
        d.explosion.effects.liquidRadius = 10.0f;
        d.explosion.blockPolicy = ExplosionBlockPolicy.STANDARD.name();
        d.explosion.blockRules.add(blockRule("obsidian_hard", "KEEP", 1.0));
        d.explosion.blockRules.add(blockRule("REINFORCED_DEEPSLATE", "KEEP", 1.0));
        d.explosion.blockRules.add(blockRule("BEDROCK", "KEEP", 1.0));
        d.explosion.blockRules.add(blockRule("CHEST", "DROP", 1.0));
        d.explosion.blockRules.add(blockRule("TRAPPED_CHEST", "DROP", 1.0));
        d.explosion.blockRules.add(blockRule("BARREL", "DROP", 1.0));
        d.explosion.blockRules.add(blockRule("ENDER_CHEST", "DROP", 1.0));
        d.explosion.postActions.add(soundAction("entity_generic_explode", 0));
        applyPurchase(d, PurchaseType.EXPERIENCE, 1500);
        return d;
    }

    private static DynamiteDefinition cryingSouls() {
        DynamiteDefinition d = base("crying_souls", "&5&lПлачущие души", List.of(
                "&5Роль: &7штурм обсидиана",
                "&7Только обсидиан и плачущий",
                "&7Ближе к заряду — мгновенный разлом",
                "&7Постройки из дерева/кирпича не трогает"
        ), "90,20,120", 90, 10.0f, 5.0f, "WAVE");
        d.explosion.blockPolicy = ExplosionBlockPolicy.WHITELIST.name();
        d.explosion.spreadAcrossTicks = false;
        d.explosion.entityDamage = 4.0;
        d.explosion.damagePlayers = false;
        d.explosion.algorithm.resistanceMultiplier = 0.04;
        d.explosion.algorithm.minimumRayPower = 0.001f;
        d.explosion.algorithm.waveLineOfSight = true;
        d.explosion.algorithm.obsidianInstantShatter = true;
        d.explosion.algorithm.obsidianShatterObsidianProximity = 0.26f;
        d.explosion.algorithm.obsidianShatterCryingProximity = 0.40f;
        d.explosion.effects.presentation.intensity = 1.55f;
        d.explosion.blockRules.clear();
        d.explosion.blockRules.add(blockRule("obsidian_hard", "BREAK", 1.0));
        d.explosion.postActions.add(soundAction("block_amethyst_block_break", 0));
        applyPurchase(d, PurchaseType.MONEY, 450);
        return d;
    }

    private static DynamiteDefinition eclipseHeart() {
        DynamiteDefinition d = base("eclipse_heart", "&4&lПламя Затменного Владыки", List.of(
                "&cРоль: &7поджог и сжигание",
                "&7Огонь в радиусе, блоки как у штурма",
                "&7Обсидиан не рвёт"
        ), "160,25,35", 80, 10.0f, 6.0f, "HIGH");
        d.explosion.entityDamage = 12.0;
        d.explosion.createFire = true;
        d.explosion.algorithm.fireChance = 0.45;
        d.explosion.effects.removeWater = true;
        d.explosion.blockPolicy = ExplosionBlockPolicy.STANDARD.name();
        d.explosion.blockRules.add(blockRule("obsidian_hard", "KEEP", 1.0));
        d.explosion.blockRules.add(blockRule("BEDROCK", "KEEP", 1.0));
        applyPurchase(d, PurchaseType.EXPERIENCE, 950);
        return d;
    }

    private static DynamiteDefinition abyssCollapse() {
        DynamiteDefinition d = base("abyss_collapse", "&1&lВздох Пустоты", List.of(
                "&9Роль: &7осушение",
                "&7Засасывающая воронка пустоты",
                "&7Сразу вытягивает воду и лаву",
                "&7Сфера осушения &f18 &7блоков"
        ), "25,35,110", 80, 18.0f, 5.0f, "WAVE");
        d.explosion.blockPolicy = ExplosionBlockPolicy.WHITELIST.name();
        d.explosion.spreadAcrossTicks = false;
        d.explosion.algorithm.resistanceMultiplier = 0.02f;
        d.explosion.algorithm.minimumRayPower = 0.001f;
        d.explosion.algorithm.waveLineOfSight = false;
        d.explosion.effects.removeWater = true;
        d.explosion.effects.removeLava = true;
        d.explosion.effects.liquidRadius = 18.0f;
        d.explosion.effects.liquidDrainMaxBlocks = 0;
        d.explosion.effects.drainVortex = true;
        d.explosion.effects.drainVortexTicks = 36;
        d.explosion.effects.drainVortexIntensity = 1.4f;
        d.explosion.effects.presentation.intensity = 1.85f;
        d.explosion.entityDamage = 6.0;
        d.explosion.damagePlayers = false;
        d.explosion.blockRules.add(blockRule("liquid_sources", "BREAK", 1.0));
        applyPurchase(d, PurchaseType.MONEY, 650);
        return d;
    }

    private static DynamiteDefinition lastPyre() {
        DynamiteDefinition d = base("last_pyre", "&6&lПоследний Разжигатель Пламени", List.of(
                "&6Роль: &cсудный приговор",
                "&7Ломает &fвсё&7 (кроме бедрока)",
                "&7Ядро + 7 боеголовок, ад и лава"
        ), "240,160,40", 220, 96.0f, 52.0f, "EXTREME");
        d.explosion.blockPolicy = ExplosionBlockPolicy.OMNIVORE.name();
        d.explosion.entityDamage = 80.0;
        d.explosion.createFire = true;
        d.explosion.algorithm.fireChance = 1.0;
        d.explosion.algorithm.resistanceMultiplier = 0.02;
        d.explosion.algorithm.minimumRayPower = 0.001f;
        d.explosion.algorithm.rayStep = 0.25;
        d.explosion.algorithm.rayRandomness = 1.5;
        d.explosion.algorithm.edgePhysicsOnly = true;
        d.explosion.blockBudgetMultiplier = 1.35f;
        d.explosion.blockRules.add(blockRule("BEDROCK", "KEEP", 1.0));
        d.explosion.effects.removeWater = true;
        d.explosion.effects.removeLava = true;
        d.explosion.effects.liquidRadius = 96.0f;
        d.explosion.effects.destroyTntBlocks = true;
        d.explosion.effects.detonateOtherPrimed = true;
        d.hologram.offsetY = 1.5;
        applyHellScar(d, 52.0f, 14);
        d.explosion.effects.craterFill.hellFloorLavaRatio = 0.14;
        d.explosion.effects.craterFill.magmaShellWidth = 16.0f;
        d.explosion.effects.craterFill.magmaShellLayers = 12;
        d.explosion.effects.craterFill.floorMaterial = "MAGMA_BLOCK";
        d.explosion.effects.craterFill.coatMaterial = "LAVA";
        d.explosion.effects.craterFill.allowLavaCoat = true;
        d.explosion.effects.craterFill.lavaChance = 1.0;
        d.explosion.effects.craterFill.shellMaterial = "MAGMA_BLOCK";
        applyTsarEffects(d);
        applyApocalypseEffects(d);
        applyTsarWarheads(d);
        applyPurchase(d, PurchaseType.VANILLA_TNT, 16);
        d.purchase.useCooldownSeconds = 30;
        return d;
    }

    private static void applyApocalypseEffects(DynamiteDefinition d) {
        PostExplosionAction quake = new PostExplosionAction();
        quake.type = "SOUND";
        quake.param = "entity_wither_break_block";
        quake.delayTicks = 8;
        d.explosion.postActions.add(quake);
        PostExplosionAction rumble = new PostExplosionAction();
        rumble.type = "SOUND";
        rumble.param = "entity_ender_dragon_growl";
        rumble.delayTicks = 12;
        d.explosion.postActions.add(rumble);
        PostExplosionAction ash = new PostExplosionAction();
        ash.type = "PARTICLE";
        ash.param = "LAVA";
        ash.args = List.of("900", "72");
        ash.delayTicks = 2;
        d.explosion.postActions.add(ash);
        PostExplosionAction ring = new PostExplosionAction();
        ring.type = "PARTICLE";
        ring.param = "EXPLOSION_EMITTER";
        ring.args = List.of("48", "24");
        ring.delayTicks = 0;
        d.explosion.postActions.add(ring);
        PostExplosionAction ring2 = new PostExplosionAction();
        ring2.type = "PARTICLE";
        ring2.param = "EXPLOSION_EMITTER";
        ring2.args = List.of("32", "16");
        ring2.delayTicks = 15;
        d.explosion.postActions.add(ring2);
        applyFuseLightning(d, 10, Math.min(28.0, d.explosion.radius * 0.22));
        PostExplosionAction boom2 = new PostExplosionAction();
        boom2.type = "SOUND";
        boom2.param = "entity_generic_explode";
        boom2.delayTicks = 20;
        d.explosion.postActions.add(boom2);
    }

    private static void applyHellScar(DynamiteDefinition d, float craterRadius, int floorDepth) {
        CraterFillSettings fill = d.explosion.effects.craterFill;
        fill.enabled = true;
        fill.radius = craterRadius;
        fill.floorMaterial = "MAGMA_BLOCK";
        fill.coatMaterial = "LAVA";
        fill.allowLavaCoat = true;
        fill.floorDepth = floorDepth;
        fill.lavaChance = 1.0;
        fill.magmaShell = true;
        fill.magmaShellWidth = 5.0f;
        fill.magmaShellLayers = 5;
        fill.shellMaterial = "MAGMA_BLOCK";
        fill.hellFloorScatter = false;
    }

    private static void applyTsarWarheads(DynamiteDefinition d) {
        TsarWarheadSettings warheads = d.explosion.effects.warheads;
        warheads.enabled = true;
        warheads.launchTicksBeforeEnd = 35;
        warheads.launchSpeed = 1.15;
        warheads.upwardBoost = 0.42;
        warheads.warheadFuseTicks = 55;
        warheads.warheadIds.add("void_crescent");
        warheads.warheadIds.add("soul_supernova");
        warheads.warheadIds.add("crying_souls");
        warheads.warheadIds.add("eclipse_heart");
        warheads.warheadIds.add("abyss_collapse");
        warheads.warheadIds.add("soul_supernova");
    }

    private static void applyFuseLightning(DynamiteDefinition d, int boltCount, double spreadRadius) {
        FuseLightningSettings lightning = d.explosion.effects.fuseLightning;
        lightning.enabled = true;
        lightning.ticksBeforeEnd = 20;
        lightning.boltCount = boltCount;
        lightning.boltIntervalTicks = 2;
        lightning.spreadRadius = spreadRadius;
    }

    private static void applyTsarEffects(DynamiteDefinition d) {
        applyFuseLightning(d, 5, Math.min(14.0, d.explosion.radius * 0.15));
        List<PostExplosionAction> actions = new ArrayList<>();
        PostExplosionAction boom = new PostExplosionAction();
        boom.type = "SOUND";
        boom.param = "entity_generic_explode";
        boom.delayTicks = 0;
        actions.add(boom);
        PostExplosionAction lavaBurst = new PostExplosionAction();
        lavaBurst.type = "PARTICLE";
        lavaBurst.param = "LAVA";
        lavaBurst.args = List.of("320", "36");
        lavaBurst.delayTicks = 4;
        actions.add(lavaBurst);
        PostExplosionAction flame = new PostExplosionAction();
        flame.type = "PARTICLE";
        flame.param = "FLAME";
        flame.args = List.of("160", "22");
        flame.delayTicks = 6;
        actions.add(flame);
        PostExplosionAction smoke = new PostExplosionAction();
        smoke.type = "PARTICLE";
        smoke.param = "EXPLOSION_EMITTER";
        smoke.args = List.of("16", "8");
        smoke.delayTicks = 1;
        actions.add(smoke);
        d.explosion.postActions = actions;
    }

    private static void applyPurchase(DynamiteDefinition dynamite, PurchaseType type, double amount) {
        dynamite.purchase.type = type.name();
        dynamite.purchase.amount = amount;
        dynamite.purchase.money = 0;
        dynamite.purchase.experience = 0;
        dynamite.purchase.vanillaTnt = 0;
    }

    private static BlockExplosionRule blockRule(String target, String mode, double chance) {
        BlockExplosionRule rule = new BlockExplosionRule();
        rule.target = target;
        rule.mode = mode;
        rule.chance = chance;
        return rule;
    }

    private static PostExplosionAction soundAction(String sound, int delayTicks) {
        PostExplosionAction action = new PostExplosionAction();
        action.type = "SOUND";
        action.param = sound;
        action.delayTicks = delayTicks;
        return action;
    }

    private static DynamiteDefinition base(
            String id,
            String name,
            List<String> lore,
            String glowRgb,
            int fuseTicks,
            float radius,
            float power,
            String quality
    ) {
        DynamiteDefinition d = new DynamiteDefinition();
        d.id = id;
        d.item.displayName = name;
        d.item.lore = lore;
        d.glow.colorRgb = glowRgb;
        d.fuseTicks = fuseTicks;
        d.explosion.radius = radius;
        d.explosion.power = power;
        d.explosion.quality = quality;
        d.explosion.spreadAcrossTicks = true;
        d.explosion.algorithm.dropChanceMultiplier = 1.0;
        d.explosion.algorithm.resistanceMultiplier = 0.24;
        d.explosion.algorithm.minimumRayPower = 0.015f;
        d.explosion.algorithm.rayStep = 0.4;
        d.explosion.algorithm.rayRandomness = 1.15;
        return d;
    }

}
