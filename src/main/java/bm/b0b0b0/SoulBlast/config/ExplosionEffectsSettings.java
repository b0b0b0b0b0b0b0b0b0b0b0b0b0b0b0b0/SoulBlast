package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class ExplosionEffectsSettings {

    @Comment(@CommentValue("Убирать воду в радиусе"))
    public boolean removeWater = false;

    @Comment(@CommentValue("Убирать лаву в радиусе"))
    public boolean removeLava = false;

    @Comment(@CommentValue("0 = радиус взрыва"))
    public float liquidRadius = 0.0f;

    @Comment(@CommentValue("Лимит снятия жидкости за детонацию; 0 = авто от liquidRadius"))
    public int liquidDrainMaxBlocks = 0;

    @Comment(@CommentValue("Анимация засасывающей воронки при осушении"))
    public boolean drainVortex = false;

    @Comment(@CommentValue("Длительность воронки в тиках"))
    public int drainVortexTicks = 36;

    @Comment(@CommentValue("Множитель частиц и звуков воронки"))
    public float drainVortexIntensity = 1.0f;

    @Comment(@CommentValue("Разрушать блоки TNT"))
    public boolean destroyTntBlocks = false;

    @Comment(@CommentValue("Поджигать другие primed TNT в радиусе"))
    public boolean detonateOtherPrimed = false;

    public CraterFillSettings craterFill = new CraterFillSettings();

    public FuseLightningSettings fuseLightning = new FuseLightningSettings();

    public TsarWarheadSettings warheads = new TsarWarheadSettings();

    public ExplosionPresentationSettings presentation = new ExplosionPresentationSettings();

}
