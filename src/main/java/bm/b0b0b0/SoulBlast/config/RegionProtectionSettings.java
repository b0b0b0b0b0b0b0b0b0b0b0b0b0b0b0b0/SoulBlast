package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

import java.util.ArrayList;
import java.util.List;

public class RegionProtectionSettings {

    @Comment(@CommentValue("Защита спавна и регионов WorldGuard (нужен WorldGuard)"))
    public boolean enabled = true;

    @Comment(@CommentValue("Без WorldGuard не давать ставить динамит, если защита включена"))
    public boolean requireWorldGuard = true;

    @Comment(@CommentValue("Отступ от границы региона = радиус взрыва × множитель (2.0 = x2 к мощности)"))
    public double marginRadiusMultiplier = 2.0;

    @Comment(@CommentValue("Имена регионов WG (spawn, hub и т.д.), без учёта регистра"))
    public List<String> regionNames = defaultRegionNames();

    @Comment(@CommentValue("region-names: полный запрет — soulblast.region.bypass и PS-рейд не помогают"))
    public boolean strictListedRegions = true;

    @Comment(@CommentValue("Регионы из списка не защищать (даже если совпало имя)"))
    public List<String> exemptRegionNames = new ArrayList<>();

    @Comment(@CommentValue("Защищать регионы, где у игрока BUILD = deny (типичный spawn)"))
    public boolean protectBuildDenyRegions = true;

    @Comment(@CommentValue("Защищать регионы, где флаг WG = deny (см. worldguard-flags)"))
    public boolean protectFlagDenyRegions = true;

    @Comment(@CommentValue("Флаги WG: deny в регионе = защита (soulblast-dynamite регистрируется плагином)"))
    public List<String> worldguardFlags = defaultFlags();

    @Comment(@CommentValue("Проверять, что сфера взрыва не задевает защищённый регион"))
    public boolean checkExplosionFootprint = true;

    @Comment(@CommentValue("Миры (пусто = все)"))
    public List<String> worlds = new ArrayList<>();

    @Comment(@CommentValue("Обход защиты"))
    public String bypassPermission = "soulblast.region.bypass";

    private static List<String> defaultRegionNames() {
        List<String> names = new ArrayList<>();
        names.add("spawn");
        names.add("__spawn__");
        return names;
    }

    private static List<String> defaultFlags() {
        List<String> flags = new ArrayList<>();
        flags.add("soulblast-dynamite");
        flags.add("tnt");
        flags.add("other-explosion");
        return flags;
    }

}
