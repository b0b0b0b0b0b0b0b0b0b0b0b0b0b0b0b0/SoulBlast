package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class FuseLightningSettings {

    @Comment(@CommentValue("Молнии по земле перед взрывом (пока TNT ещё на месте)"))
    public boolean enabled = false;

    @Comment(@CommentValue("За сколько тиков до детонации начать град (20 = ~1 сек)"))
    public int ticksBeforeEnd = 20;

    @Comment(@CommentValue("Число ударов"))
    public int boltCount = 6;

    @Comment(@CommentValue("Интервал между ударами в тиках"))
    public int boltIntervalTicks = 2;

    @Comment(@CommentValue("Разброс по горизонтали от центра TNT"))
    public double spreadRadius = 10.0;

    @Comment(@CommentValue("Реальная молния (урон/поджог) или только эффект"))
    public boolean realLightning = false;

}
