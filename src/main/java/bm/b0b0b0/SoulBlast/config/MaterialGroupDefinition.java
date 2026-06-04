package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

import java.util.ArrayList;
import java.util.List;

public class MaterialGroupDefinition {

    @Comment(@CommentValue("Единая взрывоустойчивость для всех материалов группы"))
    public float blastResistance = 6.0f;

    @Comment(@CommentValue("Список Material (Bukkit)"))
    public List<String> materials = new ArrayList<>();

}
