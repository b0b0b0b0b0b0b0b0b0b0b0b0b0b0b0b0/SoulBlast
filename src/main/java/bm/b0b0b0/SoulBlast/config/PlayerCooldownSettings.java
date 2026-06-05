package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlayerCooldownSettings {

    @Comment(@CommentValue("Персональный кулдаун на покупку и использование тяжёлых зарядов"))
    public boolean enabled = true;

    @Comment(@CommentValue("Обход кулдауна"))
    public String bypassPermission = "soulblast.cooldown.bypass";

    @Comment(@CommentValue("Кулдаун при покупке / выдаче из меню"))
    public boolean applyOnPurchase = false;

    @Comment(@CommentValue("Кулдаун при установке и поджоге"))
    public boolean applyOnUse = true;

    @Comment(@CommentValue("Один таймер на всю группу tier (не давать скупать разные тяжёлые подряд)"))
    public boolean sharedTierCooldown = true;

    public List<PlayerCooldownTier> tiers = defaultTiers();

    @Comment(@CommentValue("Переопределение по id динамита"))
    public Map<String, PlayerCooldownDynamiteOverride> dynamites = defaultDynamites();

    private static List<PlayerCooldownTier> defaultTiers() {
        List<PlayerCooldownTier> list = new ArrayList<>();
        PlayerCooldownTier extreme = new PlayerCooldownTier();
        extreme.id = "extreme";
        extreme.minRadius = 85.0f;
        extreme.matchExtremeQuality = true;
        extreme.purchaseSeconds = 0;
        extreme.useSeconds = 30;
        list.add(extreme);
        PlayerCooldownTier heavy = new PlayerCooldownTier();
        heavy.id = "heavy";
        heavy.minRadius = 38.0f;
        heavy.purchaseSeconds = 0;
        heavy.useSeconds = 25;
        list.add(heavy);
        PlayerCooldownTier medium = new PlayerCooldownTier();
        medium.id = "medium";
        medium.minRadius = 28.0f;
        medium.purchaseSeconds = 0;
        medium.useSeconds = 15;
        list.add(medium);
        return list;
    }

    private static Map<String, PlayerCooldownDynamiteOverride> defaultDynamites() {
        Map<String, PlayerCooldownDynamiteOverride> map = new LinkedHashMap<>();
        PlayerCooldownDynamiteOverride lastPyre = new PlayerCooldownDynamiteOverride();
        lastPyre.purchaseSeconds = 0;
        lastPyre.useSeconds = 30;
        lastPyre.noBypass = true;
        map.put("last_pyre", lastPyre);
        return map;
    }

}
