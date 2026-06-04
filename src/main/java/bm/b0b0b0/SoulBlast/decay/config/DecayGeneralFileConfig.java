package bm.b0b0b0.SoulBlast.decay.config;

import bm.b0b0b0.SoulBlast.config.SerializerConfigs;
import net.elytrium.serializer.language.object.YamlSerializable;

public class DecayGeneralFileConfig extends YamlSerializable {

    public DecayGeneralSettings general = new DecayGeneralSettings();

    public DecayGeneralFileConfig() {
        super(SerializerConfigs.YAML);
    }

}
