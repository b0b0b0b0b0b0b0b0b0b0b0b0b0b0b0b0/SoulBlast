package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class FuseMisfireSettings {

    @Comment(@CommentValue("Осечка: таймер кончился, взрыва нет — нужно подойти и ПКМ"))
    public boolean enabled = false;

    @Comment(@CommentValue("Шанс осечки при старте таймера (0–1)"))
    public double endChance = 0.0;

    @Comment(@CommentValue("За сколько тиков до конца показать предупреждение, если уже осечка"))
    public int warningTicks = 40;

    @Comment(@CommentValue("ПКМ на осечке: шанс снова запустить таймер (0–1)"))
    public double activateRelightChance = 0.5;

    @Comment(@CommentValue("ПКМ на осечке: шанс мгновенного взрыва (0–1)"))
    public double activateDetonateChance = 0.35;

    @Comment(@CommentValue("Звук осечки (entity.creeper.primed или ENTITY_CREEPER_PRIMED)"))
    public String dudSound = "entity.creeper.primed";

    public float dudSoundVolume = 1.0f;

    public float dudSoundPitch = 1.25f;

    @Comment(@CommentValue("Лёгкие частицы пока заряд в осечке"))
    public boolean dudAmbientParticles = true;

}
