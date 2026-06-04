package bm.b0b0b0.SoulBlast.config.menu;

import bm.b0b0b0.SoulBlast.config.SerializerConfigs;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.language.object.YamlSerializable;

import java.util.LinkedHashMap;
import java.util.Map;

public class MenuFileConfig extends YamlSerializable {

    public MenuFileConfig() {
        super(SerializerConfigs.YAML);
    }

    public MenuOptionsSettings options = new MenuOptionsSettings();

    public MenuCommandSettings command = new MenuCommandSettings();

    @Comment(@CommentValue("Символ схемы → иконка слота"))
    public Map<String, MenuIconDefinition> icons = defaultIcons();

    private static Map<String, MenuIconDefinition> defaultIcons() {
        Map<String, MenuIconDefinition> map = new LinkedHashMap<>();

        map.put(" ", filler("PURPLE_STAINED_GLASS_PANE", " "));
        map.put("@", dynamiteEntry());
        map.put("G", goalSlot());
        map.put("P", playerSettings());
        map.put("<", pageArrow("ARROW", "&#721ddb◀ &#C084FCПыль прошлого", "PREVIOUS_PAGE"));
        map.put(">", pageArrow("ARROW", "&#721ddbБуря впереди &#C084FC▶", "NEXT_PAGE"));
        map.put("S", sortIcon());

        return map;
    }

    private static MenuIconDefinition filler(String material, String name) {
        MenuIconDefinition icon = new MenuIconDefinition();
        icon.type = "FILLER";
        icon.display.material = material;
        icon.display.name = name;
        return icon;
    }

    private static MenuIconDefinition dynamiteEntry() {
        MenuIconDefinition icon = new MenuIconDefinition();
        icon.type = "DYNAMITE_ENTRY";
        icon.display.material = "TNT";
        icon.display.name = "%soul_name%";
        icon.display.lore = java.util.List.of(
                "",
                " %soul_desc_1% ",
                " %soul_desc_2% ",
                "%soul_desc_gap%",
                "&#721ddbСила души&#757575: &#E9D5FF%blast_power%",
                "&#721ddbИскра до вспышки&#757575: &#86EFAC%fuse_seconds%с",
                ""
        );
        return icon;
    }

    private static MenuIconDefinition pageArrow(String material, String name, String type) {
        MenuIconDefinition icon = new MenuIconDefinition();
        icon.type = type;
        icon.display.material = material;
        icon.display.name = name;
        icon.display.lore = java.util.List.of(
                "",
                "&#AAAAAAСтраница &#F3E8FF%current_page%&#757575/&#F3E8FF%max_pages%",
                "",
                "&#757575Нажмите, чтобы листать гримуар.",
                ""
        );
        return icon;
    }

    private static MenuIconDefinition goalSlot() {
        MenuIconDefinition icon = new MenuIconDefinition();
        icon.type = "GOAL_SLOT";
        icon.display.material = "END_CRYSTAL";
        icon.display.name = "&#C084FC✦ &#721ddbКопилка обмена";
        icon.display.lore = java.util.List.of(
                "",
                "&#AAAAAA&#F3E8FFПКМ &#AAAAAA— выбрать заряд для склада",
                "&#AAAAAA&#F3E8FFЛКМ &#AAAAAAпо копилке — TNT / забрать",
                ""
        );
        return icon;
    }

    private static MenuIconDefinition playerSettings() {
        MenuIconDefinition icon = new MenuIconDefinition();
        icon.type = "PLAYER_SETTINGS";
        icon.display.material = "BLAZE_POWDER";
        icon.display.name = "&#C084FC✦ &#E9D5FFИскра поджига";
        icon.display.lore = java.util.List.of(
                "",
                "&#AAAAAAСейчас&#757575: %auto_ignite_label%",
                "",
                "&#757575Нажмите, чтобы сменить.",
                "&#AAAAAAВлияет на &#F3E8FFвсе &#AAAAAAваши заряды",
                "&#AAAAAAпри установке.",
                ""
        );
        return icon;
    }

    private static MenuIconDefinition sortIcon() {
        MenuIconDefinition icon = new MenuIconDefinition();
        icon.type = "SORT_CYCLE";
        icon.display.material = "HOPPER";
        icon.display.name = "&#C084FC✦ &#721ddbСортировка душ";
        icon.display.lore = java.util.List.of(
                "",
                "&#AAAAAAПорядок&#757575: &#E9D5FF%sort_label%",
                "",
                "&#757575Нажмите, чтобы сменить круг силы.",
                ""
        );
        return icon;
    }

}
