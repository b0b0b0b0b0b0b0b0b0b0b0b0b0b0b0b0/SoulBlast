package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class FuseMisfireGlobalSettings {

    @Comment(@CommentValue("Система осечек (пер-заряд в dynamites.yml → fuse-misfire)"))
    public boolean enabled = true;

    @Comment(@CommentValue("Секунд на действие с осечкой (ПКМ/ЛКМ); 0 = без лимита"))
    public int dudDisposalSeconds = 30;

}
