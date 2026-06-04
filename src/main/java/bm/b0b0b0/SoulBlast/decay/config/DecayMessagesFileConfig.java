package bm.b0b0b0.SoulBlast.decay.config;

import bm.b0b0b0.SoulBlast.config.SerializerConfigs;
import net.elytrium.serializer.language.object.YamlSerializable;

public class DecayMessagesFileConfig extends YamlSerializable {

    public String menuTitle = "&8Прочные блоки";

    public String menuInfoName = "&eИнформация";

    public String menuSortName = "&bСортировка";

    public DecayMessagesFileConfig() {
        super(SerializerConfigs.YAML);
    }

}
