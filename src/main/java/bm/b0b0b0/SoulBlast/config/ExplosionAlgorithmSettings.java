package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class ExplosionAlgorithmSettings {

    @Comment(@CommentValue("Множитель случайного разброса силы луча (1.0 = ванильно-подобно)"))
    public double rayRandomness = 1.0;

    @Comment(@CommentValue("Шаг луча в блоках (меньше = точнее, дороже)"))
    public double rayStep = 0.3;

    @Comment(@CommentValue("Множитель дропа (1.0 = лут с блоков, 0 = без дропа)"))
    public double dropChanceMultiplier = 1.0;

    @Comment(@CommentValue("Bukkit-физика (соседи, гравитация) только на оболочке объёма взрыва"))
    public boolean edgePhysicsOnly = true;

    @Comment(@CommentValue("Дополнительный шанс поджечь блок (0-1)"))
    public double fireChance = 0.0;

    @Comment(@CommentValue("Множитель к взрывоустойчивости (<1 = разрушительнее, гриф: 0.2–0.4)"))
    public double resistanceMultiplier = 0.28;

    @Comment(@CommentValue("Минимальная сила луча для разрушения блока"))
    public float minimumRayPower = 0.02f;

    @Comment(@CommentValue("WAVE: не бить блоки за стенами (луч от центра заряда)"))
    public boolean waveLineOfSight = true;

    @Comment(@CommentValue("WAVE: после сферы прогнать лучи — усиленный decay на их дорожках"))
    public boolean waveRayOverlay = false;

    @Comment(@CommentValue("Число лучей оверлея (0 = авто от радиуса)"))
    public int waveRayOverlayRays = 0;

    @Comment(@CommentValue("Множитель decay на блоках, попавших и в волну, и в луч"))
    public float waveRayOverlayDecayMultiplier = 1.85f;

    @Comment(@CommentValue("Мгновенный разлом обсидиана (без decay), сила от расстояния до центра"))
    public boolean obsidianInstantShatter = false;

    @Comment(@CommentValue("Порог близости (0–1) для мгновенного разлома обычного обсидиана"))
    public float obsidianShatterObsidianProximity = 0.28f;

    @Comment(@CommentValue("Порог близости (0–1) для плачущего обсидиана (выше = ближе к центру)"))
    public float obsidianShatterCryingProximity = 0.42f;

}
