package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

import java.util.ArrayList;
import java.util.List;

public class PostExplosionAction {

    @Comment(@CommentValue("PARTICLE, SOUND, COMMAND (LIGHTNING — в effects.fuse-lightning)"))
    public String type = "PARTICLE";

    @Comment(@CommentValue("Параметры: particle, sound, команда и т.д."))
    public String param = "EXPLOSION_LARGE";

    @Comment(@CommentValue("Доп. аргументы"))
    public List<String> args = new ArrayList<>();

    @Comment(@CommentValue("Задержка в тиках после завершения разрушения блоков"))
    public int delayTicks = 0;

}
