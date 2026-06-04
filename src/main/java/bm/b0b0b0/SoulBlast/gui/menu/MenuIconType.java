package bm.b0b0b0.SoulBlast.gui.menu;

import java.util.Locale;

public enum MenuIconType {

    DYNAMITE_ENTRY,
    GOAL_SLOT,
    PLAYER_SETTINGS,
    PREVIOUS_PAGE,
    NEXT_PAGE,
    SORT_CYCLE,
    INFO_PANEL,
    FILLER,
    UNKNOWN;

    public static MenuIconType fromConfig(String raw) {
        if (raw == null || raw.isBlank()) {
            return UNKNOWN;
        }
        try {
            return MenuIconType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return UNKNOWN;
        }
    }

}
