package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class ExplosionPresentationSettings {

    @Comment(@CommentValue("Доп. частицы и звуки при детонации (поверх ванильной вспышки)"))
    public boolean enabled = true;

    @Comment(@CommentValue("Множитель количества частиц и громкости (1.0 = база)"))
    public float intensity = 1.0f;

}
