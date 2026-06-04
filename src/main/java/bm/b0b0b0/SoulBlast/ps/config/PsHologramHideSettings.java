package bm.b0b0b0.SoulBlast.ps.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class PsHologramHideSettings {

    @Comment(@CommentValue("Команда /psholo — скрыть/показать свою голограмму ударом по блоку привата"))
    public boolean enabled = true;

    @Comment(@CommentValue("Сколько секунд ждать удар по блоку после команды"))
    public int armTimeoutSeconds = 60;

}
