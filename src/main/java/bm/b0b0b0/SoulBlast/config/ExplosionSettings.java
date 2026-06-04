package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

import java.util.ArrayList;
import java.util.List;

public class ExplosionSettings {

    @Comment(@CommentValue("Радиус взрыва в блоках"))
    public float radius = 4.0f;

    @Comment(@CommentValue("Урон сущностям (0 = только отбрасывание)"))
    public double entityDamage = 8.0;

    @Comment(@CommentValue("Наносить урон игрокам (false = без урона, отброс может остаться)"))
    public boolean damagePlayers = true;

    @Comment(@CommentValue("Сила взрыва / power для отбрасывания"))
    public float power = 4.0f;

    @Comment(@CommentValue("LOW/MEDIUM/HIGH — лучи; WAVE — сфера как граната; EXTREME — царь, объём"))
    public String quality = "MEDIUM";

    @Comment(@CommentValue("Лучей при расчёте (0 = авто по quality)"))
    public int samplingRayOverride = 0;

    @Comment(@CommentValue("Доля лимита general на блоки (1.0 = весь потолок)"))
    public float blockBudgetMultiplier = 1.0f;

    @Comment(@CommentValue("Создавать огонь в мире"))
    public boolean createFire = false;

    @Comment(@CommentValue("Разрушать блоки (false = только урон и эффекты)"))
    public boolean breakBlocks = true;

    @Comment(@CommentValue("Использовать распределение по тикам из general"))
    public boolean spreadAcrossTicks = true;

    @Comment(@CommentValue("STANDARD — гриф-база; WHITELIST — только block-rules; OMNIVORE — царь, всё кроме KEEP"))
    public String blockPolicy = "STANDARD";

    public ExplosionAlgorithmSettings algorithm = new ExplosionAlgorithmSettings();

    public ExplosionEffectsSettings effects = new ExplosionEffectsSettings();

    @Comment(@CommentValue("Правила для конкретных блоков/групп"))
    public List<BlockExplosionRule> blockRules = new ArrayList<>();

    @Comment(@CommentValue("Действия после взрыва"))
    public List<PostExplosionAction> postActions = new ArrayList<>();

}
