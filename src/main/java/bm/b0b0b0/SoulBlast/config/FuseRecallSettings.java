package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class FuseRecallSettings {

    @Comment(@CommentValue("ЛКМ по активированному заряду — остановить таймер и вернуть предмет поставившему"))
    public boolean enabled = true;

}
