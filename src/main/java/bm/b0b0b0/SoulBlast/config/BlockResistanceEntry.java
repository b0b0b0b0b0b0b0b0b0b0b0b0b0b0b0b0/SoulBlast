package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class BlockResistanceEntry {

    @Comment(@CommentValue("Material или ключ группы"))
    public String target = "OBSIDIAN";

    public float blastResistance = 1200.0f;

}
