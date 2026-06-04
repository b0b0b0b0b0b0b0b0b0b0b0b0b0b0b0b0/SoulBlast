package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class PlayerCooldownDynamiteOverride {

    @Comment(@CommentValue("Покупка в сек (-1 = как у tier)"))
    public int purchaseSeconds = -1;

    @Comment(@CommentValue("Использование в сек (-1 = как у tier)"))
    public int useSeconds = -1;

}
