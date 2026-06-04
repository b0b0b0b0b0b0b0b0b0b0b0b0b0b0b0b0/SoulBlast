package bm.b0b0b0.SoulBlast.ps.config;

import bm.b0b0b0.SoulBlast.config.SerializerConfigs;
import net.elytrium.serializer.language.object.YamlSerializable;

public class PsSettingsFileConfig extends YamlSerializable {

    public PsSettings settings = new PsSettings();

    public PsSettingsFileConfig() {
        super(SerializerConfigs.YAML);
    }

    public void applyLegacyRootFields() {
    }

}
