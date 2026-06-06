package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class CoreProtectIntegrationSettings {

    @Comment(@CommentValue("Логировать взрывы SoulBlast в CoreProtect (все заряды из dynamites.yml, не по id)"))
    public boolean enabled = true;

    @Comment(@CommentValue("Логировать сломанные блоки"))
    public boolean logBreaks = true;

    @Comment(@CommentValue("Логировать поставленные блоки (маска, медальон)"))
    public boolean logPlacements = true;

    @Comment(@CommentValue("Логировать осушение воды/лавы"))
    public boolean logLiquidClear = true;

    @Comment(@CommentValue("Имя в логе CoreProtect для всех взрывов SoulBlast"))
    public String fallbackUser = "#soulblast";

    @Comment(@CommentValue("Всегда писать взрывы как fallback-user (не как ник поджигателя)"))
    public boolean logAllAsFallbackUser = true;

}
