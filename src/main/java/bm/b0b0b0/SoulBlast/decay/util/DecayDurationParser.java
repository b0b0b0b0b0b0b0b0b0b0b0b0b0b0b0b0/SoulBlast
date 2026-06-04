package bm.b0b0b0.SoulBlast.decay.util;

public final class DecayDurationParser {

    private DecayDurationParser() {
    }

    public static long parseMillis(String input) {
        if (input == null || input.isBlank()) {
            return 60_000L;
        }
        String normalized = input.trim().toLowerCase().replace(',', '.');
        String[] parts = normalized.split("\\s+");
        if (parts.length == 0) {
            return 60_000L;
        }
        double value;
        try {
            value = Double.parseDouble(parts[0]);
        } catch (NumberFormatException exception) {
            return 60_000L;
        }
        String unit = parts.length > 1 ? parts[1] : "min";
        long multiplier = switch (unit) {
            case "s", "sec", "сек", "с" -> 1000L;
            case "h", "hour", "hours", "ч", "час", "часа", "часов" -> 3_600_000L;
            case "d", "day", "days", "д", "день", "дня", "дней" -> 86_400_000L;
            default -> 60_000L;
        };
        return Math.max(1000L, (long) (value * multiplier));
    }

}
