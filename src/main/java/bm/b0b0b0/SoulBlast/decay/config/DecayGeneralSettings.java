package bm.b0b0b0.SoulBlast.decay.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class DecayGeneralSettings {

    @Comment(@CommentValue("Постепенное разрушение вместо мгновенного ломания для блоков из decay/blocks.yml"))
    public boolean enabled = true;

    @Comment(@CommentValue("Максимум блоков в состоянии decay одновременно"))
    public int maxActiveBlocks = 4096;

    @Comment(@CommentValue("Сколько блоков обновлять трещинами за тик (пакеты игрокам)"))
    public int damagePacketsPerTick = 120;

    @Comment(@CommentValue("Радиус зрителей для sendBlockDamage"))
    public int viewerRadius = 48;

    @Comment(@CommentValue("Интервал тика decay при активных блоках"))
    public int tickInterval = 1;

    @Comment(@CommentValue("Как часто пересчитывать реген (тики)"))
    public int regenerationIntervalTicks = 20;

    @Comment(@CommentValue("Период повторной отправки трещин клиенту (тики)"))
    public int crackRefreshTicks = 20;

    @Comment(@CommentValue("Стабильный sourceId для overlay трещин"))
    public int crackSourceId = 917364;

    @Comment(@CommentValue("Максимум урона decay за один удар взрыва по блоку (0.05–0.25)"))
    public float maxDamagePerHit = 0.14f;

    @Comment(@CommentValue("Урон decay слабее к краю радиуса взрыва"))
    public boolean explosionDistanceFalloffEnabled = true;

    @Comment(@CommentValue("Множитель урона у границы радиуса (0 = нет урона, 1 = как в центре)"))
    public float explosionMinDamageMultiplierAtEdge = 0.15f;

    @Comment(@CommentValue("LINEAR или QUADRATIC — насколько быстро падает урон с расстоянием"))
    public String explosionDistanceFalloffCurve = "QUADRATIC";

}
