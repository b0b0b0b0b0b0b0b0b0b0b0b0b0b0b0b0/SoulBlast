package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class ProtectionStonesIntegrationSettings {

    @Comment({
            @CommentValue("Интеграция с ProtectionStones (папка ps/ в данных SoulBlast)"),
            @CommentValue("По умолчанию включена. Если плагина нет — в консоль предупреждение,"),
            @CommentValue("модуль ждёт установки PS (config не меняется). false — только если PS не нужен."),
            @CommentValue("Нужны WorldGuard и ProtectionStones 2.10+")
    })
    public boolean enabled = true;

}
