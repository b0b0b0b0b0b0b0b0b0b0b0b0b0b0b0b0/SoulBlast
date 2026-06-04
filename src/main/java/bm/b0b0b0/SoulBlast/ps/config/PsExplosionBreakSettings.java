package bm.b0b0b0.SoulBlast.ps.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

import java.util.LinkedHashMap;
import java.util.Map;

public class PsExplosionBreakSettings {

    @Comment(@CommentValue("Снос блока привата от кастомных динамитов SoulBlast"))
    public boolean enabled = true;

    @Comment(@CommentValue("id динамита -> урон за один взрыв (1 = один взрыв снимает 1 прочности)"))
    public Map<String, Object> onlyDynamiteTypes = new LinkedHashMap<>();

}
