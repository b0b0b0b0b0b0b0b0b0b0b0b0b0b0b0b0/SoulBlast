package bm.b0b0b0.SoulBlast.config;

import java.util.*;

public final class MaterialGroupCatalog {

    private static final String[] OVERWORLD_WOODS = {
            "OAK", "SPRUCE", "BIRCH", "JUNGLE", "ACACIA", "DARK_OAK",
            "MANGROVE", "CHERRY", "PALE_OAK"
    };

    private static final String[] NETHER_WOODS = {"CRIMSON", "WARPED"};

    private static final String[] CONCRETE_COLORS = {
            "WHITE", "ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME", "PINK", "GRAY",
            "LIGHT_GRAY", "CYAN", "PURPLE", "BLUE", "BROWN", "GREEN", "RED", "BLACK"
    };

    private static final String[] TERRACOTTA_COLORS = CONCRETE_COLORS;

    private static final String[] STAINED_GLASS_COLORS = CONCRETE_COLORS;

    private MaterialGroupCatalog() {
    }

    public static MaterialGroupDefinition raidTimber() {
        MaterialGroupDefinition group = new MaterialGroupDefinition();
        group.blastResistance = 2.5f;
        Set<String> materials = new LinkedHashSet<>();
        for (String wood : OVERWORLD_WOODS) {
            addOverworldWood(materials, wood);
        }
        for (String wood : NETHER_WOODS) {
            addNetherWood(materials, wood);
        }
        addBambooSet(materials);
        materials.add("LADDER");
        materials.add("SCAFFOLDING");
        materials.add("COMPOSTER");
        materials.add("BOOKSHELF");
        materials.add("CHISELED_BOOKSHELF");
        group.materials.addAll(materials);
        return group;
    }

    public static MaterialGroupDefinition raidMasonry() {
        MaterialGroupDefinition group = new MaterialGroupDefinition();
        group.blastResistance = 5.0f;
        Set<String> materials = new LinkedHashSet<>();
        addStoneFamily(materials);
        addDeepslateFamily(materials);
        addBlackstoneFamily(materials);
        addBrickFamily(materials);
        for (String color : CONCRETE_COLORS) {
            materials.add(color + "_CONCRETE");
            materials.add(color + "_CONCRETE_POWDER");
        }
        for (String color : TERRACOTTA_COLORS) {
            materials.add(color + "_TERRACOTTA");
            materials.add(color + "_GLAZED_TERRACOTTA");
        }
        for (String color : STAINED_GLASS_COLORS) {
            materials.add(color + "_STAINED_GLASS");
            materials.add(color + "_STAINED_GLASS_PANE");
        }
        addQuartzFamily(materials);
        addCopperFamily(materials);
        addPrismarineFamily(materials);
        addEndFamily(materials);
        materials.add("GLASS");
        materials.add("TINTED_GLASS");
        materials.add("GLASS_PANE");
        materials.add("IRON_BLOCK");
        materials.add("IRON_BARS");
        materials.add("CHAIN");
        materials.add("RAW_IRON_BLOCK");
        materials.add("RAW_GOLD_BLOCK");
        materials.add("RAW_COPPER_BLOCK");
        materials.add("GOLD_BLOCK");
        materials.add("NETHER_BRICKS");
        materials.add("RED_NETHER_BRICKS");
        materials.add("CRACKED_NETHER_BRICKS");
        materials.add("CHISELED_NETHER_BRICKS");
        materials.add("NETHER_BRICK_FENCE");
        materials.add("NETHER_BRICK_STAIRS");
        materials.add("NETHER_BRICK_SLAB");
        materials.add("NETHER_BRICK_WALL");
        materials.add("GLOWSTONE");
        materials.add("SEA_LANTERN");
        materials.add("MAGMA_BLOCK");
        materials.add("HAY_BLOCK");
        materials.add("DRYING_RACK");
        group.materials.addAll(materials);
        return group;
    }

    public static MaterialGroupDefinition naturalTerrain() {
        MaterialGroupDefinition group = new MaterialGroupDefinition();
        group.blastResistance = 0.5f;
        group.materials.add("DIRT");
        group.materials.add("GRASS_BLOCK");
        group.materials.add("COARSE_DIRT");
        group.materials.add("ROOTED_DIRT");
        group.materials.add("PODZOL");
        group.materials.add("MYCELIUM");
        group.materials.add("SAND");
        group.materials.add("RED_SAND");
        group.materials.add("GRAVEL");
        group.materials.add("CLAY");
        group.materials.add("MUD");
        group.materials.add("MUDDY_MANGROVE_ROOTS");
        group.materials.add("MANGROVE_ROOTS");
        group.materials.add("SNOW");
        group.materials.add("SNOW_BLOCK");
        group.materials.add("SOUL_SAND");
        group.materials.add("SOUL_SOIL");
        group.materials.add("FARMLAND");
        group.materials.add("DIRT_PATH");
        return group;
    }

    public static List<String> raidTimberMaterialNames() {
        return new ArrayList<>(raidTimber().materials);
    }

    public static List<String> raidMasonryMaterialNames() {
        return new ArrayList<>(raidMasonry().materials);
    }

    public static Map<String, MaterialGroupDefinition> buildDefaultsMap() {
        Map<String, MaterialGroupDefinition> map = new LinkedHashMap<>();
        map.put("stone_like", stoneLike());
        map.put("obsidian_hard", obsidianHard());
        map.put("base_raid", baseRaid());
        map.put("wood_like", woodLike());
        map.put("trap_raid", trapRaid());
        map.put("fragile_trap", fragileTrap());
        map.put("raid_timber", raidTimber());
        map.put("raid_masonry", raidMasonry());
        map.put("natural_terrain", naturalTerrain());
        map.put("storage_shulker", storageShulker());
        map.put("liquid_sources", liquidSources());
        return map;
    }

    public static MaterialGroupDefinition liquidSources() {
        MaterialGroupDefinition group = new MaterialGroupDefinition();
        group.blastResistance = 0.1f;
        group.materials.add("WATER");
        group.materials.add("LAVA");
        group.materials.add("KELP");
        group.materials.add("KELP_PLANT");
        group.materials.add("SEAGRASS");
        group.materials.add("TALL_SEAGRASS");
        group.materials.add("BUBBLE_COLUMN");
        return group;
    }

    public static MaterialGroupDefinition storageShulker() {
        MaterialGroupDefinition group = new MaterialGroupDefinition();
        group.blastResistance = 2.0f;
        group.materials.add("SHULKER_BOX");
        for (String color : CONCRETE_COLORS) {
            if ("LIGHT_BLUE".equals(color)) {
                group.materials.add("LIGHT_BLUE_SHULKER_BOX");
            } else if ("LIGHT_GRAY".equals(color)) {
                group.materials.add("LIGHT_GRAY_SHULKER_BOX");
            } else {
                group.materials.add(color + "_SHULKER_BOX");
            }
        }
        return group;
    }

    public static MaterialGroupDefinition stoneLike() {
        MaterialGroupDefinition group = new MaterialGroupDefinition();
        group.blastResistance = 6.0f;
        group.materials.add("STONE");
        group.materials.add("COBBLESTONE");
        group.materials.add("DEEPSLATE");
        group.materials.add("STONE_BRICKS");
        return group;
    }

    public static MaterialGroupDefinition obsidianHard() {
        MaterialGroupDefinition group = new MaterialGroupDefinition();
        group.blastResistance = 8.0f;
        group.materials.add("OBSIDIAN");
        group.materials.add("CRYING_OBSIDIAN");
        return group;
    }

    public static MaterialGroupDefinition baseRaid() {
        MaterialGroupDefinition group = new MaterialGroupDefinition();
        group.blastResistance = 4.0f;
        group.materials.add("OAK_PLANKS");
        group.materials.add("SPRUCE_PLANKS");
        group.materials.add("BIRCH_PLANKS");
        group.materials.add("JUNGLE_PLANKS");
        group.materials.add("DARK_OAK_PLANKS");
        return group;
    }

    public static MaterialGroupDefinition woodLike() {
        MaterialGroupDefinition group = new MaterialGroupDefinition();
        group.blastResistance = 3.0f;
        group.materials.add("OAK_PLANKS");
        group.materials.add("SPRUCE_PLANKS");
        group.materials.add("BIRCH_PLANKS");
        return group;
    }

    public static MaterialGroupDefinition trapRaid() {
        MaterialGroupDefinition group = new MaterialGroupDefinition();
        group.blastResistance = 2.5f;
        group.materials.add("REDSTONE_WIRE");
        group.materials.add("REDSTONE_TORCH");
        group.materials.add("REPEATER");
        group.materials.add("COMPARATOR");
        group.materials.add("OBSERVER");
        group.materials.add("PISTON");
        group.materials.add("STICKY_PISTON");
        group.materials.add("DISPENSER");
        group.materials.add("DROPPER");
        group.materials.add("HOPPER");
        group.materials.add("TARGET");
        group.materials.add("NOTE_BLOCK");
        group.materials.add("LEVER");
        group.materials.add("OAK_BUTTON");
        group.materials.add("STONE_BUTTON");
        group.materials.add("OAK_PRESSURE_PLATE");
        group.materials.add("STONE_PRESSURE_PLATE");
        group.materials.add("HEAVY_WEIGHTED_PRESSURE_PLATE");
        group.materials.add("LIGHT_WEIGHTED_PRESSURE_PLATE");
        group.materials.add("TRIPWIRE");
        group.materials.add("TRIPWIRE_HOOK");
        group.materials.add("REDSTONE_BLOCK");
        group.materials.add("REDSTONE_LAMP");
        group.materials.add("DETECTOR_RAIL");
        group.materials.add("POWERED_RAIL");
        group.materials.add("ACTIVATOR_RAIL");
        group.materials.add("RAIL");
        group.materials.add("DAYLIGHT_DETECTOR");
        group.materials.add("SCULK_SENSOR");
        group.materials.add("CALIBRATED_SCULK_SENSOR");
        group.materials.add("RESPAWN_ANCHOR");
        return group;
    }

    public static MaterialGroupDefinition fragileTrap() {
        MaterialGroupDefinition group = new MaterialGroupDefinition();
        group.blastResistance = 1.5f;
        group.materials.add("GLASS");
        group.materials.add("TINTED_GLASS");
        group.materials.add("GLASS_PANE");
        group.materials.add("TORCH");
        group.materials.add("SOUL_TORCH");
        group.materials.add("LANTERN");
        group.materials.add("SOUL_LANTERN");
        group.materials.add("LADDER");
        group.materials.add("SCAFFOLDING");
        return group;
    }

    private static void addOverworldWood(Set<String> materials, String wood) {
        materials.add(wood + "_LOG");
        materials.add("STRIPPED_" + wood + "_LOG");
        materials.add(wood + "_WOOD");
        materials.add("STRIPPED_" + wood + "_WOOD");
        addWoodBuildingShapes(materials, wood);
    }

    private static void addNetherWood(Set<String> materials, String wood) {
        materials.add(wood + "_STEM");
        materials.add("STRIPPED_" + wood + "_STEM");
        materials.add(wood + "_HYPHAE");
        materials.add("STRIPPED_" + wood + "_HYPHAE");
        addWoodBuildingShapes(materials, wood);
    }

    private static void addBambooSet(Set<String> materials) {
        materials.add("BAMBOO_BLOCK");
        materials.add("STRIPPED_BAMBOO_BLOCK");
        materials.add("BAMBOO_PLANKS");
        materials.add("BAMBOO_MOSAIC");
        materials.add("BAMBOO_STAIRS");
        materials.add("BAMBOO_MOSAIC_STAIRS");
        materials.add("BAMBOO_SLAB");
        materials.add("BAMBOO_MOSAIC_SLAB");
        materials.add("BAMBOO_FENCE");
        materials.add("BAMBOO_FENCE_GATE");
        materials.add("BAMBOO_DOOR");
        materials.add("BAMBOO_TRAPDOOR");
        materials.add("BAMBOO_BUTTON");
        materials.add("BAMBOO_PRESSURE_PLATE");
        materials.add("BAMBOO_SIGN");
        materials.add("BAMBOO_HANGING_SIGN");
        materials.add("BAMBOO_SHELF");
        materials.add("BAMBOO");
    }

    private static void addWoodBuildingShapes(Set<String> materials, String wood) {
        materials.add(wood + "_PLANKS");
        materials.add(wood + "_STAIRS");
        materials.add(wood + "_SLAB");
        materials.add(wood + "_FENCE");
        materials.add(wood + "_FENCE_GATE");
        materials.add(wood + "_DOOR");
        materials.add(wood + "_TRAPDOOR");
        materials.add(wood + "_BUTTON");
        materials.add(wood + "_PRESSURE_PLATE");
        materials.add(wood + "_SIGN");
        materials.add(wood + "_HANGING_SIGN");
        materials.add(wood + "_SHELF");
    }

    private static void addStoneFamily(Set<String> materials) {
        materials.add("STONE");
        materials.add("COBBLESTONE");
        materials.add("MOSSY_COBBLESTONE");
        materials.add("SMOOTH_STONE");
        materials.add("STONE_BRICKS");
        materials.add("MOSSY_STONE_BRICKS");
        materials.add("CRACKED_STONE_BRICKS");
        materials.add("CHISELED_STONE_BRICKS");
        materials.add("ANDESITE");
        materials.add("POLISHED_ANDESITE");
        materials.add("DIORITE");
        materials.add("POLISHED_DIORITE");
        materials.add("GRANITE");
        materials.add("POLISHED_GRANITE");
        materials.add("CALCITE");
        materials.add("TUFF");
        materials.add("DRIPSTONE_BLOCK");
        addShapeVariants(materials, "COBBLESTONE");
        addShapeVariants(materials, "MOSSY_COBBLESTONE");
        addShapeVariants(materials, "STONE_BRICK");
        addShapeVariants(materials, "MOSSY_STONE_BRICK");
        addShapeVariants(materials, "ANDESITE");
        addShapeVariants(materials, "POLISHED_ANDESITE");
        addShapeVariants(materials, "DIORITE");
        addShapeVariants(materials, "POLISHED_DIORITE");
        addShapeVariants(materials, "GRANITE");
        addShapeVariants(materials, "POLISHED_GRANITE");
        addShapeVariants(materials, "SMOOTH_STONE");
        addShapeVariants(materials, "TUFF");
        addShapeVariants(materials, "TUFF_BRICK");
        materials.add("TUFF_BRICKS");
        materials.add("CHISELED_TUFF");
        materials.add("CHISELED_TUFF_BRICKS");
        materials.add("POLISHED_TUFF");
    }

    private static void addDeepslateFamily(Set<String> materials) {
        materials.add("DEEPSLATE");
        materials.add("COBBLED_DEEPSLATE");
        materials.add("POLISHED_DEEPSLATE");
        materials.add("DEEPSLATE_BRICKS");
        materials.add("DEEPSLATE_TILES");
        materials.add("CRACKED_DEEPSLATE_BRICKS");
        materials.add("CRACKED_DEEPSLATE_TILES");
        materials.add("CHISELED_DEEPSLATE");
        materials.add("REINFORCED_DEEPSLATE");
        addShapeVariants(materials, "COBBLED_DEEPSLATE");
        addShapeVariants(materials, "POLISHED_DEEPSLATE");
        addShapeVariants(materials, "DEEPSLATE_BRICK");
        addShapeVariants(materials, "DEEPSLATE_TILE");
    }

    private static void addBlackstoneFamily(Set<String> materials) {
        materials.add("BLACKSTONE");
        materials.add("GILDED_BLACKSTONE");
        materials.add("POLISHED_BLACKSTONE");
        materials.add("CHISELED_POLISHED_BLACKSTONE");
        materials.add("BLACKSTONE_SLAB");
        materials.add("BLACKSTONE_STAIRS");
        materials.add("BLACKSTONE_WALL");
        materials.add("POLISHED_BLACKSTONE_SLAB");
        materials.add("POLISHED_BLACKSTONE_STAIRS");
        materials.add("POLISHED_BLACKSTONE_WALL");
        materials.add("POLISHED_BLACKSTONE_BRICKS");
        materials.add("CRACKED_POLISHED_BLACKSTONE_BRICKS");
        materials.add("POLISHED_BLACKSTONE_BRICK_SLAB");
        materials.add("POLISHED_BLACKSTONE_BRICK_STAIRS");
        materials.add("POLISHED_BLACKSTONE_BRICK_WALL");
    }

    private static void addBrickFamily(Set<String> materials) {
        materials.add("BRICKS");
        materials.add("MUD_BRICKS");
        materials.add("PACKED_MUD");
        addShapeVariants(materials, "BRICK");
        addShapeVariants(materials, "MUD_BRICK");
    }

    private static void addQuartzFamily(Set<String> materials) {
        materials.add("QUARTZ_BLOCK");
        materials.add("SMOOTH_QUARTZ");
        materials.add("QUARTZ_BRICKS");
        materials.add("CHISELED_QUARTZ_BLOCK");
        materials.add("QUARTZ_PILLAR");
        addShapeVariants(materials, "QUARTZ");
        addShapeVariants(materials, "SMOOTH_QUARTZ");
    }

    private static void addCopperFamily(Set<String> materials) {
        String[] stages = {"COPPER", "EXPOSED_COPPER", "WEATHERED_COPPER", "OXIDIZED_COPPER"};
        for (String stage : stages) {
            materials.add(stage + "_BLOCK");
            materials.add("CUT_" + stage);
            materials.add("CUT_" + stage + "_STAIRS");
            materials.add("CUT_" + stage + "_SLAB");
            materials.add("CHISELED_" + stage);
        }
        materials.add("WAXED_COPPER_BLOCK");
        materials.add("WAXED_CUT_COPPER");
        materials.add("WAXED_CUT_COPPER_STAIRS");
        materials.add("WAXED_CUT_COPPER_SLAB");
        materials.add("WAXED_EXPOSED_COPPER");
        materials.add("WAXED_WEATHERED_COPPER");
        materials.add("WAXED_OXIDIZED_COPPER");
        materials.add("COPPER_GRATE");
        materials.add("EXPOSED_COPPER_GRATE");
        materials.add("WEATHERED_COPPER_GRATE");
        materials.add("OXIDIZED_COPPER_GRATE");
        materials.add("COPPER_BULB");
        materials.add("EXPOSED_COPPER_BULB");
        materials.add("WEATHERED_COPPER_BULB");
        materials.add("OXIDIZED_COPPER_BULB");
        materials.add("COPPER_DOOR");
        materials.add("EXPOSED_COPPER_DOOR");
        materials.add("WEATHERED_COPPER_DOOR");
        materials.add("OXIDIZED_COPPER_DOOR");
        materials.add("COPPER_TRAPDOOR");
        materials.add("EXPOSED_COPPER_TRAPDOOR");
        materials.add("WEATHERED_COPPER_TRAPDOOR");
        materials.add("OXIDIZED_COPPER_TRAPDOOR");
    }

    private static void addPrismarineFamily(Set<String> materials) {
        materials.add("PRISMARINE");
        materials.add("PRISMARINE_BRICKS");
        materials.add("DARK_PRISMARINE");
        addShapeVariants(materials, "PRISMARINE");
        addShapeVariants(materials, "PRISMARINE_BRICK");
        addShapeVariants(materials, "DARK_PRISMARINE");
    }

    private static void addEndFamily(Set<String> materials) {
        materials.add("END_STONE");
        materials.add("END_STONE_BRICKS");
        materials.add("PURPUR_BLOCK");
        materials.add("PURPUR_PILLAR");
        addShapeVariants(materials, "END_STONE_BRICK");
        addShapeVariants(materials, "PURPUR");
    }

    private static void addShapeVariants(Set<String> materials, String base) {
        materials.add(base + "_STAIRS");
        materials.add(base + "_SLAB");
        materials.add(base + "_WALL");
    }

}
