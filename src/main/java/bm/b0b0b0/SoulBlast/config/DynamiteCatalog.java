package bm.b0b0b0.SoulBlast.config;

import java.util.LinkedHashMap;
import java.util.Map;

public final class DynamiteCatalog {

    private DynamiteCatalog() {
    }

    public static Map<String, DynamiteDefinition> build(DynamitesFileConfig fileConfig) {
        Map<String, DynamiteDefinition> dynamites = new LinkedHashMap<>(fileConfig.toMap());
        for (Map.Entry<String, DynamiteDefinition> presetEntry : DynamitePresets.createDefaults().entrySet()) {
            if (!dynamites.containsKey(presetEntry.getKey())) {
                dynamites.put(presetEntry.getKey(), presetEntry.getValue());
            }
        }
        new DynamiteDefaultsMerger().merge(dynamites);
        for (DynamiteDefinition definition : dynamites.values()) {
            definition.purchase.consolidate();
        }
        return dynamites;
    }

}
