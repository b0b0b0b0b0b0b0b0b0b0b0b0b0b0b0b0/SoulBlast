package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class IgnitionSettings {

    @Comment(@CommentValue("Огниво / огненный заряд"))
    public boolean allowFlintAndSteel = true;

    @Comment(@CommentValue("Огонь / лава рядом"))
    public boolean allowFire = true;

    @Comment(@CommentValue("Лук с Flame"))
    public boolean allowFlameBow = true;

    @Comment(@CommentValue("Красный камень / кнопки / рычаги"))
    public boolean allowRedstone = true;

    @Comment(@CommentValue("Цепная реакция от другого взрыва"))
    public boolean allowExplosion = true;

    @Comment(@CommentValue("Поджигание другим динамитом"))
    public boolean allowOtherPrimed = true;

}
