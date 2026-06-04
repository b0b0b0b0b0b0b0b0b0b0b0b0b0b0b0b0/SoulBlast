package bm.b0b0b0.SoulBlast.decay.gui;

public enum DecayBlocksSortMode {

    HIGHEST_RESISTANCE("Сильнейшие"),
    LOWEST_RESISTANCE("Слабейшие"),
    NAME("По имени");

    private final String displayName;

    DecayBlocksSortMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public DecayBlocksSortMode next() {
        DecayBlocksSortMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

}
