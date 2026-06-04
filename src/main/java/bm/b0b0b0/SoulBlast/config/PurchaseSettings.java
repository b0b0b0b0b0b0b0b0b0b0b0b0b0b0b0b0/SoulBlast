package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class PurchaseSettings {

    @Comment(@CommentValue("Один канал: MONEY | EXPERIENCE | VANILLA_TNT | FREE"))
    public String type = "FREE";

    @Comment(@CommentValue("Сумма: монеты, опыт или штуки TNT"))
    public double amount = 0;

    @Comment(@CommentValue("Кулдаун покупки в секундах (0 = из config.yml, -1 = выкл)"))
    public int purchaseCooldownSeconds = 0;

    @Comment(@CommentValue("Кулдаун установки/взрыва в секундах (0 = из config.yml, -1 = выкл)"))
    public int useCooldownSeconds = 0;

    public double money = 0;

    public int experience = 0;

    public int vanillaTnt = 0;

    public boolean isFree() {
        return resolve().isFree();
    }

    public PurchaseResolution resolve() {
        PurchaseType explicit = PurchaseType.parse(type);
        if (explicit != PurchaseType.FREE && amount > 0) {
            return new PurchaseResolution(explicit, amount);
        }
        int channels = 0;
        PurchaseType legacy = PurchaseType.FREE;
        if (vanillaTnt > 0) {
            channels++;
            legacy = PurchaseType.VANILLA_TNT;
        }
        if (money > 0) {
            channels++;
            legacy = PurchaseType.MONEY;
        }
        if (experience > 0) {
            channels++;
            legacy = PurchaseType.EXPERIENCE;
        }
        if (channels == 1) {
            return legacyAmount(legacy);
        }
        if (channels > 1) {
            if (vanillaTnt > 0) {
                return new PurchaseResolution(PurchaseType.VANILLA_TNT, vanillaTnt);
            }
            if (money > 0) {
                return new PurchaseResolution(PurchaseType.MONEY, money);
            }
            return new PurchaseResolution(PurchaseType.EXPERIENCE, experience);
        }
        return PurchaseResolution.free();
    }

    public void consolidate() {
        PurchaseResolution resolution = resolve();
        type = resolution.type().name();
        amount = resolution.amount();
        money = 0;
        experience = 0;
        vanillaTnt = 0;
    }

    private PurchaseResolution legacyAmount(PurchaseType legacy) {
        return switch (legacy) {
            case MONEY -> new PurchaseResolution(PurchaseType.MONEY, money);
            case EXPERIENCE -> new PurchaseResolution(PurchaseType.EXPERIENCE, experience);
            case VANILLA_TNT -> new PurchaseResolution(PurchaseType.VANILLA_TNT, vanillaTnt);
            default -> PurchaseResolution.free();
        };
    }

}
