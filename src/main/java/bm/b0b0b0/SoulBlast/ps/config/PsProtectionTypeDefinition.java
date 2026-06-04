package bm.b0b0b0.SoulBlast.ps.config;

import bm.b0b0b0.SoulBlast.config.SerializerConfigs;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.language.object.YamlSerializable;

public class PsProtectionTypeDefinition extends YamlSerializable {

    public PsProtectionTypeDefinition() {
        super(SerializerConfigs.YAML);
    }

    @Comment(@CommentValue("Голограмма над блоком привата (TextDisplay)"))
    public PsHologramTypeSettings hologram = new PsHologramTypeSettings();

    @Comment(@CommentValue("Молния при установке и снятии"))
    public PsLightningStrikeSettings lightningStrike = new PsLightningStrikeSettings();

    @Comment(@CommentValue("Звуки при установке и снятии"))
    public PsSoundPairSettings sound = new PsSoundPairSettings();

    @Comment(@CommentValue("Прочность от динамитов SoulBlast"))
    public PsDurabilitySettings durability = new PsDurabilitySettings();

    @Comment(@CommentValue("Разрушение блока привата"))
    public PsBreakProtectionSettings breakProtectionBlock = new PsBreakProtectionSettings();

    @Comment(@CommentValue("Свечение предмета привата в инвентаре (/ps get)"))
    public PsItemGlowSettings itemGlow = new PsItemGlowSettings();

    @Comment(@CommentValue("Ставить и взрывать SoulBlast-динамит в чужом регионе этого типа (не spawn из config.yml)"))
    public boolean allowSoulblastDynamiteInForeignClaims = true;

}
