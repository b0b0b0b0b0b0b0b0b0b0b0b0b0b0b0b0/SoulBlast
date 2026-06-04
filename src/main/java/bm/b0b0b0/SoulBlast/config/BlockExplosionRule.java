package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class BlockExplosionRule {

    @Comment(@CommentValue("Bukkit Material или ключ группы material-groups"))
    public String target = "STONE";

    @Comment(@CommentValue("BREAK, KEEP, DROP, TRANSFORM"))
    public String mode = "BREAK";

    @Comment(@CommentValue("Material после TRANSFORM (если применимо)"))
    public String transformInto = "AIR";

    @Comment(@CommentValue("Шанс применить правило 0-1"))
    public double chance = 1.0;

    @Comment(@CommentValue("Переопределение дропа (пусто = ваниль)"))
    public String dropMaterial = "";

}
