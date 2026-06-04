package bm.b0b0b0.SoulBlast.ps.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class PsHologramDisplaySettings {

    @Comment({
            @CommentValue("Дистанция видимости (Display.viewRange). Типично 16–64; меньше — ближе «пропадает»"),
            @CommentValue("Ваниль по умолчанию ~1.0; для приватов удобно 24–48")
    })
    public float viewRange = 32.0f;

    @Comment(@CommentValue("Текст сквозь блоки и стены (seeThrough)"))
    public boolean seeThrough = true;

    @Comment(@CommentValue("Тень у символов текста"))
    public boolean shadowed = true;

    @Comment(@CommentValue("Поворот к игроку: center | fixed | horizontal | vertical"))
    public String billboard = "center";

    @Comment(@CommentValue("Выравнивание строк: center | left | right"))
    public String alignment = "center";

    @Comment(@CommentValue("Масштаб текста (1.0 = стандарт)"))
    public float scale = 1.0f;

    @Comment(@CommentValue("Ширина строки в пикселях (перенос длинных строк), 0 = не менять"))
    public int lineWidth = 200;

    @Comment({
            @CommentValue("Прозрачность текста 0–255; -1 = не трогать (ваниль)"),
    })
    public int textOpacity = -1;

    @Comment({
            @CommentValue("Фон под текстом ARGB (0 = прозрачный). Пример тёмной плашки: -2147483648"),
            @CommentValue("Или default-background: true для ванильной подложки Minecraft")
    })
    public int backgroundColor = 0;

    @Comment(@CommentValue("Ванильная серая подложка за текстом"))
    public boolean defaultBackground = false;

    @Comment(@CommentValue("Радиус мягкой тени вокруг текста (0 = выкл)"))
    public float shadowRadius = 0.0f;

    @Comment(@CommentValue("Сила мягкой тени (0 = выкл)"))
    public float shadowStrength = 0.0f;

}
