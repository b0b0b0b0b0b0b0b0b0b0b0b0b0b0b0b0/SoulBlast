package bm.b0b0b0.SoulBlast.ps.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

import java.util.ArrayList;
import java.util.List;

public class PsHologramTypeSettings {

    @Comment(@CommentValue("Включить голограмму"))
    public boolean enabled = true;

    @Comment(@CommentValue("Высота над блоком"))
    public double offsetY = 2.0;

    @Comment(@CommentValue("Параметры TextDisplay (дистанция, сквозь блоки, тень, масштаб)"))
    public PsHologramDisplaySettings display = new PsHologramDisplaySettings();

    @Comment({
            @CommentValue("Строки (%owner_name%, %owner%, %durability%, %durability_maximum%,"),
            @CommentValue("%radius_x%, %radius_y%, %radius_z%, %owner_prefix%, %owner_suffix%)")
    })
    public List<String> lines = new ArrayList<>();

}
