package bm.b0b0b0.SoulBlast.decay.config;

import bm.b0b0b0.SoulBlast.config.SerializerConfigs;
import net.elytrium.serializer.language.object.YamlSerializable;

import java.util.LinkedHashMap;
import java.util.Map;

public class DecayDamageSourcesFileConfig extends YamlSerializable {

    public Map<String, String> types = defaultSources();

    public DecayDamageSourcesFileConfig() {
        super(SerializerConfigs.YAML);
    }

    private static Map<String, String> defaultSources() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("DEFAULT", "1");
        map.put("TNT", "1-2");
        map.put("last_pyre", "2-3");
        map.put("void_crescent", "1.5-2.2");
        map.put("crying_souls", "3.2-4.5");
        map.put("soul_supernova", "1-2");
        return map;
    }

}
