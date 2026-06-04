package bm.b0b0b0.SoulBlast.gui.menu;

import java.util.Locale;

public enum MenuSortingType {

    HIGHEST_POWER,
    LOWEST_POWER,
    SHORTEST_FUSE,
    LONGEST_FUSE,
    NAME_AZ,
    NAME_ZA;

    public static MenuSortingType fromConfig(String raw) {
        if (raw == null || raw.isBlank()) {
            return HIGHEST_POWER;
        }
        try {
            return MenuSortingType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return HIGHEST_POWER;
        }
    }

    public MenuSortingType next() {
        MenuSortingType[] values = MenuSortingType.values();
        return values[(ordinal() + 1) % values.length];
    }

    public String label() {
        return switch (this) {
            case HIGHEST_POWER -> "Сильнейшая душа";
            case LOWEST_POWER -> "Тихая искра";
            case SHORTEST_FUSE -> "Быстрый всполох";
            case LONGEST_FUSE -> "Медленное пламя";
            case NAME_AZ -> "По имени (А–Я)";
            case NAME_ZA -> "По имени (Я–А)";
        };
    }

}
