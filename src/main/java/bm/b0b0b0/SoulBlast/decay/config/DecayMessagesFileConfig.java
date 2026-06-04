package bm.b0b0b0.SoulBlast.decay.config;

import bm.b0b0b0.SoulBlast.config.SerializerConfigs;
import net.elytrium.serializer.language.object.YamlSerializable;

public class DecayMessagesFileConfig extends YamlSerializable {

    public String menuTitle = "&8Прочные блоки";

    public String menuInfoName = "&eИнформация";

    public String menuSortName = "&bСортировка";

    public String repairSuccess = "&aСтена &7{percent}%&a трещин";

    public String repairAlreadyHealthy = "&7Блок уже без трещин";

    public String repairNoMaterial = "&cНужен &fпесок &cв инвентаре";

    public String repairNotDecaying = "&7Этот блок не повреждён взрывом";

    public String repairBlocked = "&cЗдесь нельзя чинить";

    public String repairTooFar = "&cСлишком далеко";

    public DecayMessagesFileConfig() {
        super(SerializerConfigs.YAML);
    }

}
