package bm.b0b0b0.SoulBlast.decay.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class DecayBlockTypeDefinition {

    public float resistance = 0.2f;

    public DecayRegenerationSettings regeneration = new DecayRegenerationSettings();

    public DecayBlockOptions options = new DecayBlockOptions();

    public Map<String, String> variables = new LinkedHashMap<>();

}
