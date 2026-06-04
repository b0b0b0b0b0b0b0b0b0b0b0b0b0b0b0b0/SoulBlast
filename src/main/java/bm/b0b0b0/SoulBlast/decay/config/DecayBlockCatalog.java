package bm.b0b0b0.SoulBlast.decay.config;

import java.util.LinkedHashMap;
import java.util.Map;

public final class DecayBlockCatalog {

    private DecayBlockCatalog() {
    }

    public static Map<String, DecayBlockTypeDefinition> defaults() {
        Map<String, DecayBlockTypeDefinition> map = new LinkedHashMap<>();
        map.put("BRICKS", brick("BRICKS", "&aКирпичи", "&aБыстрая", "&aСлабая", 0.2f, "1 min", "BRICK", "1"));
        map.put("OBSIDIAN", obsidianLike("OBSIDIAN", "&eОбсидиан", "&eСредняя", "&6Выше среднего", 0.5f, "3 min"));
        map.put("CRYING_OBSIDIAN", obsidianLike("CRYING_OBSIDIAN", "&cПлачущий обсидиан", "&cДолгая", "&cСильная", 0.9f, "5 min"));
        map.put("STONE_BRICKS", brick("STONE_BRICKS", "&aКаменные кирпичи", "&aМедленная", "&eСредняя", 0.35f, "8 min", "STONE", "1"));
        map.put("NETHER_BRICKS", brick("NETHER_BRICKS", "&aНезерские кирпичи", "&aБыстрая", "&aСлабая", 0.2f, "1 min", "NETHERRACK", "1"));
        map.put("END_STONE_BRICKS", brick("END_STONE_BRICKS", "&aЭндерняковые кирпичи", "&aБыстрая", "&aСлабая", 0.2f, "1 min", "END_STONE", "1"));
        map.put("PURPUR_BLOCK", brick("PURPUR_BLOCK", "&aПурпур", "&aБыстрая", "&eСредняя", 0.3f, "1 min", "POPPED_CHORUS_FRUIT", "1"));
        map.put("RED_NETHER_BRICKS", brick("RED_NETHER_BRICKS", "&aКрасные незерские кирпичи", "&aБыстрая", "&eСредняя", 0.3f, "1 min", "NETHER_WART", "1"));
        map.put("ENDER_CHEST", enderChest());
        return map;
    }

    private static DecayBlockTypeDefinition brick(
            String material,
            String display,
            String regenName,
            String resistName,
            float resistance,
            String every,
            String repairMaterial,
            String repairAmount
    ) {
        DecayBlockTypeDefinition type = base(display, regenName, resistName, resistance, every);
        type.regeneration.materials.put(repairMaterial, repairAmount);
        return type;
    }

    private static DecayBlockTypeDefinition obsidianLike(
            String material,
            String display,
            String regenName,
            String resistName,
            float resistance,
            String every
    ) {
        DecayBlockTypeDefinition type = base(display, regenName, resistName, resistance, every);
        type.options.canPistonMove = false;
        return type;
    }

    private static DecayBlockTypeDefinition enderChest() {
        DecayBlockTypeDefinition type = base("&eЭндер-сундук", "&eСредняя", "&6Выше среднего", 0.5f, "3 min");
        type.options.canPlayerInteract = true;
        return type;
    }

    private static DecayBlockTypeDefinition base(
            String display,
            String regenName,
            String resistName,
            float resistance,
            String every
    ) {
        DecayBlockTypeDefinition type = new DecayBlockTypeDefinition();
        type.resistance = resistance;
        type.regeneration.every = every;
        type.options.canPlayerBreak = true;
        type.options.canPistonMove = true;
        type.variables.put("display_name", display);
        type.variables.put("regeneration_name", regenName);
        type.variables.put("resistance_name", resistName);
        return type;
    }

}
