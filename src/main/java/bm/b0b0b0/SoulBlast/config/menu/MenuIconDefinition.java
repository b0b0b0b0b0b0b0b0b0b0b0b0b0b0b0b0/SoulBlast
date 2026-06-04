package bm.b0b0b0.SoulBlast.config.menu;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class MenuIconDefinition {

    @Comment(@CommentValue("DYNAMITE_ENTRY, PREVIOUS_PAGE, NEXT_PAGE, SORT_CYCLE, INFO_PANEL, FILLER"))
    public String type = "FILLER";

    public MenuIconDisplaySettings display = new MenuIconDisplaySettings();

}
