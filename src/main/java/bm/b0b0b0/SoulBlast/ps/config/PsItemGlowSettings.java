package bm.b0b0b0.SoulBlast.ps.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class PsItemGlowSettings {

    @Comment(@CommentValue("Свечение предмета привата в инвентаре (/ps get, выдача)"))
    public boolean enabled = true;

}
