package bm.b0b0b0.SoulBlast.ps.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class PsDurabilitySettings {

    @Comment(@CommentValue("Считать прочность привата (как шкаф в Rust)"))
    public boolean enabled = true;

    @Comment(@CommentValue("Сколько урона нужно суммарно, чтобы снести блок привата"))
    public int maximum = 3;

    @Comment(@CommentValue("Урон, если взрыв SoulBlast в радиусе от блока привата (не только прямой удар)"))
    public boolean proximityDamage = true;

    @Comment(@CommentValue("Радиус от центра взрыва до блока привата (-1 = радиус динамита)"))
    public double proximityRadius = -1.0;

}
