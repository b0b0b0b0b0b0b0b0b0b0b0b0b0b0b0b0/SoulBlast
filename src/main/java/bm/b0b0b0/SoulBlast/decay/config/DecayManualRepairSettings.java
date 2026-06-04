package bm.b0b0b0.SoulBlast.decay.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class DecayManualRepairSettings {

    @Comment(@CommentValue("Ручная починка треснутых блоков (ресурс в инвентаре, клик по треснутой стене)"))
    public boolean enabled = true;

    @Comment(@CommentValue("ПКМ по блоку"))
    public boolean rightClick = true;

    @Comment(@CommentValue("ЛКМ по блоку (быстрее в рейде, осторожно с ломанием)"))
    public boolean leftClick = true;

    @Comment(@CommentValue("Право на починку (defender во время рейда)"))
    public String permission = "soulblast.decay.repair";

    @Comment(@CommentValue("Сколько «прочности» decay снимается за один клик (0.05–0.25)"))
    public float repairPerClick = 0.14f;

    @Comment(@CommentValue("Пауза между кликами одним игроком (мс), анти-спам"))
    public int cooldownMs = 200;

    @Comment(@CommentValue("Дистанция до блока"))
    public double reach = 5.5;

    @Comment(@CommentValue("Ручная починка только этим материалом (песок — дешёвый расходник, не камень/обсидиан)"))
    public boolean sandOnly = true;

    @Comment(@CommentValue("Если sand-only: false — брать regeneration.materials из blocks.yml"))
    public String fallbackMaterial = "SAND";

    @Comment(@CommentValue("Звук при успешной починке (пусто = без звука)"))
    public String sound = "BLOCK_STONE_PLACE";

    @Comment(@CommentValue("Частицы вокруг блока"))
    public boolean particles = true;

}
