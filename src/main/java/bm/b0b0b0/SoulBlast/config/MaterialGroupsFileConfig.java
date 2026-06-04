package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.language.object.YamlSerializable;

import java.util.Map;

public class MaterialGroupsFileConfig extends YamlSerializable {

    public Map<String, MaterialGroupDefinition> materialGroups = MaterialGroupCatalog.buildDefaultsMap();

    public MaterialGroupsFileConfig() {
        super(SerializerConfigs.YAML);
    }

}
