package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class EntityExplosionEntry {

    @Comment(@CommentValue("Bukkit EntityType"))
    public String entityType = "ENDER_CRYSTAL";

    @Comment(@CommentValue("Множитель урона от взрыва"))
    public double damageMultiplier = 2.0;

    @Comment(@CommentValue("Множитель отбрасывания"))
    public double knockbackMultiplier = 1.5;

    @Comment(@CommentValue("Уничтожать сущность при попадании в радиус"))
    public boolean destroyOnExplosion = false;

}
