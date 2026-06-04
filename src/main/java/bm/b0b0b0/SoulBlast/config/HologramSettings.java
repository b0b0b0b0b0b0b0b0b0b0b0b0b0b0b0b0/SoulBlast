package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class HologramSettings {

    public boolean enabled = true;

    @Comment(@CommentValue("Строка 1: пусто = из messages fuse-hologram-name ({display})"))
    public String lineName = "";

    @Comment(@CommentValue("Строка 2: пусто = из messages fuse-hologram-timer ({fuse})"))
    public String lineTimer = "";

    @Comment(@CommentValue("Смещение Y от центра сущности"))
    public double offsetY = 1.2;

}
