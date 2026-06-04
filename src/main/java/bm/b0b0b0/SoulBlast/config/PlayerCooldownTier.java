package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class PlayerCooldownTier {

    @Comment(@CommentValue("Ключ группы (для shared-tier-cooldown)"))
    public String id = "heavy";

    @Comment(@CommentValue("Минимальный радиус взрыва динамита"))
    public float minRadius = 38.0f;

    @Comment(@CommentValue("Считать tier, если quality = EXTREME"))
    public boolean matchExtremeQuality = false;

    @Comment(@CommentValue("Кулдаун покупки в секундах (0 = не применять этот tier)"))
    public int purchaseSeconds = 0;

    @Comment(@CommentValue("Кулдаун установки/поджога в секундах"))
    public int useSeconds = 0;

}
