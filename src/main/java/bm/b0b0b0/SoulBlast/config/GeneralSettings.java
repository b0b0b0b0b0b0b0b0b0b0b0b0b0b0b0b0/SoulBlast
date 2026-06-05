package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class GeneralSettings {

    @Comment(@CommentValue("SAFE — баланс TPS; GRIEF — крупные ямы (см. grief-*)"))
    public String destructionMode = "GRIEF";

    @Comment(@CommentValue("true — не ограничивать grief-* (риск вылета клиента при нескольких взрывах)"))
    public boolean griefUnlimitedBlocks = false;

    @Comment(@CommentValue("При destruction-mode: GRIEF — потолок блоков на один заряд (0 = 3200)"))
    public int griefMaxBlocksPerExplosion = 3200;

    @Comment(@CommentValue("При GRIEF — блоков за тик на все взрывы (0 = 450)"))
    public int griefMaxBlocksPerExplosionTick = 450;

    @Comment(@CommentValue("При GRIEF — лучей при расчёте (0 = 8192)"))
    public int griefMaxSamplingRays = 0;

    @Comment(@CommentValue("При GRIEF — шагов лучей за тик (0 = 16000)"))
    public int griefMaxSamplingStepsPerTick = 16_000;

    @Comment(@CommentValue("Потолок блоков заливки кратера (лава/магма); 0 = половина max-blocks-per-explosion"))
    public int maxCraterFillBlocksPerExplosion = 0;

    @Comment(@CommentValue("При GRIEF — заливка кратера (0 = 45000)"))
    public int griefMaxCraterFillBlocksPerExplosion = 45_000;

    @Comment(@CommentValue("last_pyre: REPLACE маски в тик детонации (0 = как grief-max-blocks-per-explosion-tick)"))
    public int griefLastPyreMaskBurstOnDetonate = 0;

    @Comment(@CommentValue("last_pyre: множитель блоков/тик при разгрузке очереди (1.0 = без буста)"))
    public float griefLastPyreQueueTickMultiplier = 1.0f;

    @Comment(@CommentValue("last_pyre: молний за тик во время адской маски (0 = выкл)"))
    public int griefLastPyreMaskLightningPerTick = 4;

    @Comment(@CommentValue("last_pyre: радиус молний при маске (0 = радиус взрыва)"))
    public float griefLastPyreMaskLightningRadius = 0f;

    @Comment(@CommentValue("last_pyre: мгновенное осушение ядра при детонации (блоков по горизонтали)"))
    public int griefLastPyreInnerDrainRadius = 18;

    @Comment(@CommentValue("last_pyre: минимум жидкости за тик при кольцевом осушении (0 = только доля бюджета)"))
    public int griefLastPyreDrainBlocksPerTick = 200;

    @Comment(@CommentValue("last_pyre: фейковых летящих TNT за тик во время осушения (0 = выкл)"))
    public int griefLastPyreDecoyBurstsPerTick = 1;

    @Comment(@CommentValue("last_pyre: молний за тик во время осушения (0 = выкл)"))
    public int griefLastPyreDrainLightningPerTick = 2;

    @Comment(@CommentValue("Сколько блоков одного взрыва ломать за тик сервера (1500–2500 — баланс TPS)"))
    public int maxBlocksPerExplosionTick = 1800;

    @Comment(@CommentValue("Потолок блоков на один взрыв (даже у царь-бомбы)"))
    public int maxBlocksPerExplosion = 5500;

    @Comment(@CommentValue("Максимум лучей при расчёте сферы (меньше = меньше лаг при подготовке)"))
    public int maxSamplingRays = 576;

    @Comment(@CommentValue("Шагов лучей за тик при подготовке (только главный поток)"))
    public int maxSamplingStepsPerTick = 2400;

    @Comment(@CommentValue("Не трогать непрогруженные чанки (false = полный снос, но нагрузка)"))
    public boolean sampleOnlyLoadedChunks = false;

    @Comment(@CommentValue("Лог взрывов last_pyre и маски в консоль + игроку"))
    public boolean explosionDebug = false;

    @Comment(@CommentValue("Максимум одновременных отложенных взрывов в очереди"))
    public int maxQueuedExplosions = 16;

    @Comment(@CommentValue("Тиков между проверкой очереди взрывов (1 = каждый тик)"))
    public int explosionQueueIntervalTicks = 1;

    @Comment(@CommentValue("Автоподжиг при установке для новых игроков"))
    public boolean defaultAutoIgnite = true;

}
