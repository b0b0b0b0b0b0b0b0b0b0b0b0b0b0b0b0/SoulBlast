package bm.b0b0b0.SoulBlast.decay.config;

import bm.b0b0b0.SoulBlast.config.SerializerConfigs;
import net.elytrium.serializer.LoadResult;
import net.elytrium.serializer.language.object.YamlSerializable;

import java.nio.file.Path;
import java.util.Map;

public class DecayBlocksFileConfig extends YamlSerializable {

    public Map<String, DecayBlockTypeDefinition> types = DecayBlockCatalog.defaults();

    public DecayBlocksFileConfig() {
        super(SerializerConfigs.YAML);
    }

    @Override
    public LoadResult reload(Path path) {
        LoadResult result = super.reload(path);
        if (types == null || types.isEmpty()) {
            types = DecayBlockCatalog.defaults();
        }
        return result;
    }

}
