package bm.b0b0b0.SoulBlast.repository;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;

import java.util.*;

public final class DynamiteRegistry {

    private Map<String, DynamiteDefinition> byId = new LinkedHashMap<>();

    public void reload(Map<String, DynamiteDefinition> definitions) {
        Map<String, DynamiteDefinition> next = new LinkedHashMap<>();
        for (Map.Entry<String, DynamiteDefinition> entry : definitions.entrySet()) {
            DynamiteDefinition definition = entry.getValue();
            if (definition.id == null || definition.id.isBlank()) {
                definition.id = entry.getKey();
            }
            next.put(definition.id.toLowerCase(), definition);
        }
        byId = Collections.unmodifiableMap(next);
    }

    public Optional<DynamiteDefinition> find(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byId.get(id.toLowerCase()));
    }

    public Collection<DynamiteDefinition> all() {
        return byId.values();
    }

}
