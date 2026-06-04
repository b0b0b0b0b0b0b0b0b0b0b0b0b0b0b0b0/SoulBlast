package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

import java.util.ArrayList;
import java.util.List;

public class DynamiteItemSettings {

    @Comment(@CommentValue("Bukkit Material предмета"))
    public String material = "TNT";

    @Comment(@CommentValue("Отображаемое имя (мини-сообщение &, без § в файле — используйте &"))
    public String displayName = "&cКастомный динамит";

    @Comment(@CommentValue("Строки описания (lore)"))
    public List<String> lore = new ArrayList<>(List.of(
            "&7Заряд душ, заключённый в пламя"
    ));

    @Comment(@CommentValue("Custom Model Data (0 = выключено)"))
    public int customModelData = 0;

    @Comment(@CommentValue("Свечение предмета в инвентаре"))
    public boolean glow = true;

}
