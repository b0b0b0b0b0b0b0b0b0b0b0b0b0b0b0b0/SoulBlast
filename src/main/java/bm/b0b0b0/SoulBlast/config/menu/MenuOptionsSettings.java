package bm.b0b0b0.SoulBlast.config.menu;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

import java.util.ArrayList;
import java.util.List;

public class MenuOptionsSettings {

    @Comment(@CommentValue("Заголовок меню (&#RRGGBB hex и &-коды)"))
    public String title = "        &#C084FC✦ &#721ddbГримуар Между Мирами &#C084FC✦";

    @Comment(@CommentValue("Схема слотов: 1–6 строк по 9 символов, каждый символ — ключ из icons"))
    public List<String> layout = defaultLayout();

    @Comment(@CommentValue("HIGHEST_POWER, LOWEST_POWER, SHORTEST_FUSE, LONGEST_FUSE, NAME_AZ, NAME_ZA"))
    public String defaultSortingType = "HIGHEST_POWER";

    @Comment(@CommentValue("Убрать стрелку «назад/вперёд», если страницы нет"))
    public boolean removeDirectionIconIfNoneExists = true;

    @Comment(@CommentValue("ID динамитов, скрытых из списка меню"))
    public List<String> exclude = new ArrayList<>();

    private static List<String> defaultLayout() {
        return new ArrayList<>(List.of(
                "P   G   S",
                " @@@@@@@ ",
                " <     > "
        ));
    }

}
