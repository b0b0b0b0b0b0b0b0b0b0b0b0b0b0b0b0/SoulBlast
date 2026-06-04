package bm.b0b0b0.SoulBlast.config.menu;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

import java.util.ArrayList;
import java.util.List;

public class MenuCommandSettings {

    @Comment(@CommentValue("Включить отдельную команду открытия меню (смена alias — перезапуск)"))
    public boolean enabled = true;

    @Comment(@CommentValue("Право на открытие; пусто — только soulblast.menu из plugin.yml"))
    public String permission = "soulblast.menu";

    @Comment(@CommentValue("Должны совпадать с alias в plugin.yml у команды soulgrimoire"))
    public List<String> alias = new ArrayList<>(List.of(
            "soulgrimoire",
            "soulfire"
    ));

}
