package bm.b0b0b0.SoulBlast.ps.config;

import bm.b0b0b0.SoulBlast.config.SerializerConfigs;
import net.elytrium.serializer.language.object.YamlSerializable;

public class PsBlockRecord extends YamlSerializable {

    public PsBlockRecord() {
        super(SerializerConfigs.YAML);
    }

    public String worldId = "";
    public int x;
    public int y;
    public int z;
    public String typeAlias = "";
    public int durability;
    public int maximum;
    public String ownerName = "";
    public String ownerPrefix = "";
    public String ownerSuffix = "";
    public int radiusX;
    public int radiusY;
    public int radiusZ;
    public String ownerId = "";
    public boolean hologramHidden = false;

}
