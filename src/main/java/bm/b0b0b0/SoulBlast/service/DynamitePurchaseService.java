package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.config.EconomySettings;
import bm.b0b0b0.SoulBlast.config.PurchaseResolution;
import bm.b0b0b0.SoulBlast.config.PurchaseType;
import bm.b0b0b0.SoulBlast.model.PlayerProfile;
import bm.b0b0b0.SoulBlast.service.economy.ExperienceCostService;
import bm.b0b0b0.SoulBlast.service.economy.VaultEconomyBridge;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class DynamitePurchaseService {

    public enum PurchaseFailure {
        NONE,
        UNKNOWN_DYNAMITE,
        INVENTORY_FULL,
        INSUFFICIENT_MONEY,
        INSUFFICIENT_EXPERIENCE,
        INSUFFICIENT_TNT_DEPOSIT
    }

    public record PurchasePreview(
            double moneyCost,
            int experienceCost,
            int vanillaTntRequired,
            int vanillaTntDeposited,
            boolean moneyRequired,
            boolean experienceRequired,
            boolean vanillaTntRequiredFlag
    ) {
        public boolean tntDepositComplete() {
            return !vanillaTntRequiredFlag || vanillaTntDeposited >= vanillaTntRequired;
        }

        public int tntRemaining() {
            if (!vanillaTntRequiredFlag) {
                return 0;
            }
            return Math.max(0, vanillaTntRequired - vanillaTntDeposited);
        }

        public boolean catalogPaymentRequired() {
            return moneyRequired || experienceRequired;
        }

        public boolean copilkaPaymentRequired() {
            return vanillaTntRequiredFlag;
        }

        public boolean isFree() {
            return !catalogPaymentRequired() && !copilkaPaymentRequired();
        }
    }

    public record CatalogEvaluation(
            PurchasePreview preview,
            int experienceAvailable,
            double moneyAvailable,
            boolean inventoryFull
    ) {
        public boolean moneyBlocking() {
            return preview.moneyRequired() && moneyAvailable + 1.0E-6 < preview.moneyCost();
        }

        public boolean experienceBlocking() {
            return preview.experienceRequired() && experienceAvailable < preview.experienceCost();
        }

        public boolean canPurchase() {
            if (inventoryFull) {
                return false;
            }
            if (!preview.catalogPaymentRequired()) {
                return true;
            }
            return !moneyBlocking() && !experienceBlocking();
        }

        public PurchaseFailure firstFailure() {
            if (inventoryFull) {
                return PurchaseFailure.INVENTORY_FULL;
            }
            if (moneyBlocking()) {
                return PurchaseFailure.INSUFFICIENT_MONEY;
            }
            if (experienceBlocking()) {
                return PurchaseFailure.INSUFFICIENT_EXPERIENCE;
            }
            return PurchaseFailure.NONE;
        }
    }

    public record CopilkaEvaluation(
            PurchasePreview preview,
            boolean inventoryFull
    ) {
        public boolean tntBlocking() {
            return preview.tntRemaining() > 0;
        }

        public boolean canClaim() {
            if (inventoryFull) {
                return false;
            }
            if (!preview.copilkaPaymentRequired()) {
                return true;
            }
            return preview.tntDepositComplete();
        }

        public PurchaseFailure firstFailure() {
            if (inventoryFull) {
                return PurchaseFailure.INVENTORY_FULL;
            }
            if (tntBlocking()) {
                return PurchaseFailure.INSUFFICIENT_TNT_DEPOSIT;
            }
            return PurchaseFailure.NONE;
        }
    }

    private final VaultEconomyBridge economyBridge;
    private final ExperienceCostService experienceCostService;
    private EconomySettings economySettings;

    public DynamitePurchaseService(
            VaultEconomyBridge economyBridge,
            ExperienceCostService experienceCostService,
            EconomySettings economySettings
    ) {
        this.economyBridge = economyBridge;
        this.experienceCostService = experienceCostService;
        this.economySettings = economySettings;
    }

    public void reload(EconomySettings economySettings) {
        this.economySettings = economySettings;
    }

    public CatalogEvaluation evaluateCatalog(Player player, DynamiteDefinition definition, PlayerProfile profile) {
        PurchasePreview preview = preview(definition, profile);
        return new CatalogEvaluation(
                preview,
                experienceCostService.totalExperience(player),
                economyBridge.balance(player),
                player.getInventory().firstEmpty() == -1
        );
    }

    public CopilkaEvaluation evaluateCopilka(DynamiteDefinition definition, PlayerProfile profile, Player player) {
        PurchasePreview preview = preview(definition, profile);
        return new CopilkaEvaluation(
                preview,
                player.getInventory().firstEmpty() == -1
        );
    }

    public PurchasePreview preview(DynamiteDefinition definition, PlayerProfile profile) {
        PurchaseResolution payment = definition.purchase.resolve();
        double moneyCost = economyBridge.effectiveCost(payment.moneyCost());
        int experienceCost = payment.experienceCost();
        int tntRequired = payment.vanillaTntCost();
        int deposited = profile.depositedVanillaTnt(definition.id);
        return new PurchasePreview(
                moneyCost,
                experienceCost,
                tntRequired,
                deposited,
                payment.type() == PurchaseType.MONEY,
                payment.type() == PurchaseType.EXPERIENCE,
                payment.type() == PurchaseType.VANILLA_TNT
        );
    }

    public String formatCatalogMissing(CatalogEvaluation evaluation) {
        List<String> parts = new ArrayList<>();
        PurchasePreview preview = evaluation.preview();
        if (evaluation.moneyBlocking()) {
            parts.add("монет &#F3E8FF" + economyBridge.format(preview.moneyCost()));
        }
        if (evaluation.experienceBlocking()) {
            parts.add("опыта &#60A5FA" + preview.experienceCost() + " &#AAAAAA(есть &#60A5FA" + evaluation.experienceAvailable() + "&#AAAAAA)");
        }
        if (parts.isEmpty()) {
            return "";
        }
        return String.join("&#757575, ", parts);
    }

    public List<String> purchaseLoreLines(DynamiteDefinition definition, PlayerProfile profile) {
        PurchasePreview preview = preview(definition, profile);
        if (preview.isFree()) {
            return List.of(" &#86EFACДар гримуара — без дани ");
        }
        List<String> lines = new ArrayList<>();
        lines.add("");
        if (preview.catalogPaymentRequired()) {
            lines.add(" &#721ddbОплата:");
            if (preview.moneyRequired()) {
                lines.add(" &#757575• &#F3E8FF" + economyBridge.format(preview.moneyCost()) + " " + economySettings.currencySymbol);
            } else if (preview.experienceRequired()) {
                lines.add(" &#757575• &#60A5FA" + preview.experienceCost() + " &#AAAAAAопыта");
            }
            lines.add(" &#757575ЛКМ &#AAAAAA— обменять");
        }
        if (preview.copilkaPaymentRequired()) {
            if (preview.catalogPaymentRequired()) {
                lines.add("");
            }
            lines.add(" &#721ddbСклад TNT &#757575(&#F3E8FFG&#757575):");
            lines.add(" &#757575• &#EF4444" + preview.vanillaTntDeposited() + "&#757575/&#EF4444" + preview.vanillaTntRequired() + " &#AAAAAAобычного TNT");
            lines.add(" &#757575ПКМ &#AAAAAA— выбрать, &#757575ЛКМ &#AAAAAAпо копилке — внести / забрать");
        } else if (!preview.catalogPaymentRequired()) {
            lines.add(" &#757575ПКМ &#AAAAAA— в копилку");
        }
        if (!preview.catalogPaymentRequired() && preview.copilkaPaymentRequired()) {
            lines.add("");
        }
        return lines;
    }

    public List<String> goalLoreLines(DynamiteDefinition definition, PlayerProfile profile) {
        PurchasePreview preview = preview(definition, profile);
        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.add(" &#721ddbСклад TNT");
        if (!preview.copilkaPaymentRequired()) {
            lines.add(" &#AAAAAAДля этого заряда TNT не нужен");
            lines.add("");
            lines.add(" &#757575ПКМ &#AAAAAA— снять выбор");
            return lines;
        }
        lines.add(" &#AAAAAATNT&#757575: &#EF4444" + preview.vanillaTntDeposited() + "&#757575/&#EF4444" + preview.vanillaTntRequired());
        if (preview.tntDepositComplete()) {
            lines.add(" &#86EFACСклад полный — &#757575ЛКМ &#AAAAAAзабрать заряд");
        } else {
            lines.add(" &#757575ЛКМ &#AAAAAA— положить TNT из инвентаря");
        }
        lines.add(" &#757575ПКМ &#AAAAAA— снять выбор");
        return lines;
    }

    public PurchaseFailure tryCatalogPurchase(Player player, DynamiteDefinition definition, PlayerProfile profile) {
        CatalogEvaluation evaluation = evaluateCatalog(player, definition, profile);
        PurchaseFailure failure = evaluation.firstFailure();
        if (failure != PurchaseFailure.NONE) {
            return failure;
        }
        PurchasePreview preview = evaluation.preview();
        if (!preview.catalogPaymentRequired()) {
            return PurchaseFailure.NONE;
        }
        if (preview.moneyRequired() && !economyBridge.withdraw(player, preview.moneyCost())) {
            return PurchaseFailure.INSUFFICIENT_MONEY;
        }
        if (preview.experienceRequired() && !experienceCostService.withdraw(player, preview.experienceCost())) {
            return PurchaseFailure.INSUFFICIENT_EXPERIENCE;
        }
        return PurchaseFailure.NONE;
    }

    public PurchaseFailure tryCopilkaClaim(Player player, DynamiteDefinition definition, PlayerProfile profile) {
        CopilkaEvaluation evaluation = evaluateCopilka(definition, profile, player);
        PurchaseFailure failure = evaluation.firstFailure();
        if (failure != PurchaseFailure.NONE) {
            return failure;
        }
        PurchasePreview preview = evaluation.preview();
        if (preview.copilkaPaymentRequired()) {
            profile.resetVanillaTntDeposit(definition.id);
        }
        return PurchaseFailure.NONE;
    }

}
