package bm.b0b0b0.SoulBlast.ps.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class PsMinecartAllowSettings {

    @Comment(@CommentValue("Удочка для перемещения вагонетки"))
    public boolean hookUp = true;

    @Comment(@CommentValue("Открыть вагонетку с воронкой"))
    public boolean open = true;

}
