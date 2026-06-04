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

    @Comment(@CommentValue("Максимум одновременных отложенных взрывов в очереди"))
    public int maxQueuedExplosions = 16;

    @Comment(@CommentValue("Тиков между проверкой очереди взрывов (1 = каждый тик)"))
    public int explosionQueueIntervalTicks = 1;

    @Comment(@CommentValue("Автоподжиг при установке для новых игроков"))
    public boolean defaultAutoIgnite = true;

}
