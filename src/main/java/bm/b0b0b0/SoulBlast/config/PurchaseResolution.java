package bm.b0b0b0.SoulBlast.config;

public record PurchaseResolution(PurchaseType type, double amount) {

    public static PurchaseResolution free() {
        return new PurchaseResolution(PurchaseType.FREE, 0);
    }

    public boolean isFree() {
        return type == PurchaseType.FREE || amount <= 0;
    }

    public double moneyCost() {
        return type == PurchaseType.MONEY ? amount : 0;
    }

    public int experienceCost() {
        return type == PurchaseType.EXPERIENCE ? (int) Math.round(amount) : 0;
    }

    public int vanillaTntCost() {
        return type == PurchaseType.VANILLA_TNT ? (int) Math.round(amount) : 0;
    }

}
