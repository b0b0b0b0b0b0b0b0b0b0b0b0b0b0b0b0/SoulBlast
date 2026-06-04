package bm.b0b0b0.SoulBlast.ps.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class PsAllowInRegionSettings {

    public PsMinecartAllowSettings mineCart = new PsMinecartAllowSettings();

    @Comment(@CommentValue("Песок, гравий, сухой бетон и т.д. в чужом регионе"))
    public boolean fallingBlock = true;

    public boolean breakBlockWithWither = true;

    public boolean useFireArrowToIgniteTnt = true;

    public boolean usePiston = true;

    public boolean useSpawnEggs = true;

}
