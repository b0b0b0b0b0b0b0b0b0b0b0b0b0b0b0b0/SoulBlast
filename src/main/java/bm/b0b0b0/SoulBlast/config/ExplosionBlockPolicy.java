package bm.b0b0b0.SoulBlast.config;

public enum ExplosionBlockPolicy {

    STANDARD,
    WHITELIST,
    OMNIVORE;

    public static ExplosionBlockPolicy parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return STANDARD;
        }
        try {
            return ExplosionBlockPolicy.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return STANDARD;
        }
    }

}
