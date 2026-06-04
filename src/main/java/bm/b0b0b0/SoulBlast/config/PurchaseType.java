package bm.b0b0b0.SoulBlast.config;

public enum PurchaseType {

    FREE,
    MONEY,
    EXPERIENCE,
    VANILLA_TNT;

    public static PurchaseType parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return FREE;
        }
        return switch (raw.trim().toUpperCase()) {
            case "MONEY", "VAULT", "CURRENCY" -> MONEY;
            case "EXPERIENCE", "EXP", "XP" -> EXPERIENCE;
            case "VANILLA_TNT", "TNT", "VANILLA-TNT" -> VANILLA_TNT;
            case "FREE", "NONE" -> FREE;
            default -> FREE;
        };
    }

}
