package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class GlowSettings {

    @Comment(@CommentValue("Подсветка сущности динамита"))
    public boolean enabled = true;

    @Comment(@CommentValue("RGB цвет (0-255), формат r,g,b"))
    public String colorRgb = "255,80,40";

    @Comment(@CommentValue("Анимация: NONE, PULSE, RAINBOW"))
    public String animation = "PULSE";

    @Comment(@CommentValue("Интервал тиков между шагами анимации"))
    public int animationIntervalTicks = 4;

    @Comment(@CommentValue("Цветная обводка через scoreboard team (по color-rgb)"))
    public boolean useTeamColor = true;

    @Comment(@CommentValue("Частицы пыли цвета души вокруг подожжённого динамита"))
    public boolean spawnParticles = true;

}
