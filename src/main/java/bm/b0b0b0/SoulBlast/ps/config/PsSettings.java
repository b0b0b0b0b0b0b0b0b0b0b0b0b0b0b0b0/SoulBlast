package bm.b0b0b0.SoulBlast.ps.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class PsSettings {

    @Comment(@CommentValue("Подробные логи в консоль (создание привата, алиасы, голограмма)"))
    public boolean debug = false;

    @Comment(@CommentValue("Лог прочности привата: постановка динамита, ДО/ПОСЛЕ взрыва"))
    public boolean durabilityTrace = false;

    @Comment(@CommentValue("Без лишних строк в консоли при старте и /soulblast reload"))
    public boolean silentStartup = true;

    @Comment(@CommentValue("Запрет пересечения нового привата с существующими"))
    public boolean blockMergeWithOtherRegions = true;

    @Comment(@CommentValue("Прочность привата от кастомных динамитов SoulBlast"))
    public boolean supportSoulblast = true;

    public PsAllowInRegionSettings allowInRegion = new PsAllowInRegionSettings();

    public PsHologramHideSettings hologramHide = new PsHologramHideSettings();

}
