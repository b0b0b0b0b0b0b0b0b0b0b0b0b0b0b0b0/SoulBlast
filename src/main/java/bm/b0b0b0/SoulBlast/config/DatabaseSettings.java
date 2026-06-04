package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class DatabaseSettings {

    @Comment(@CommentValue("Путь к SQLite относительно папки плагина"))
    public String fileName = "users/player-data.db";

}
