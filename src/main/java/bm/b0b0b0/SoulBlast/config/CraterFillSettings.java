package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class CraterFillSettings {

    @Comment(@CommentValue("Залить дно кратера после разрушения блоков"))
    public boolean enabled = false;

    public float radius = 5.0f;

    public String floorMaterial = "OBSIDIAN";

    @Comment(@CommentValue("Пусто = без заливки; LAVA только при allow-lava-coat (царь-бомба)"))
    public String coatMaterial = "";

    public int floorDepth = 2;

    @Comment(@CommentValue("Шанс лавы на дне 0-1; для грифа оставь 0"))
    public double lavaChance = 0.0;

    @Comment(@CommentValue("Заливать лавой/жидким coat (только last_pyre)"))
    public boolean allowLavaCoat = false;

    @Comment(@CommentValue("Кольцо магмы вокруг кратера (маска)"))
    public boolean magmaShell = false;

    @Comment(@CommentValue("Ширина магматического кольца за пределами radius кратера"))
    public float magmaShellWidth = 4.0f;

    @Comment(@CommentValue("Высота магматической маски в блоках"))
    public int magmaShellLayers = 4;

    public String shellMaterial = "MAGMA";

    public boolean spreadAcrossTicks = true;

    @Comment(@CommentValue("EXTREME: россыпь по полу вместо сплошных слоёв"))
    public boolean hellFloorScatter = true;

    @Comment(@CommentValue("Шанс блока на клетку пола (0.35–0.45 — проходимо)"))
    public double hellFloorDensity = 0.4;

    @Comment(@CommentValue("Доля адского камня/песка душ среди россыпи (магма — основа, проходимо)"))
    public double hellFloorLavaRatio = 0.18;

}
