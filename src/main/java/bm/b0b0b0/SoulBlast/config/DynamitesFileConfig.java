package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.LoadResult;
import net.elytrium.serializer.language.object.YamlSerializable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class DynamitesFileConfig extends YamlSerializable {

    public DynamiteDefinition void_crescent = preset("void_crescent");
    public DynamiteDefinition soul_supernova = preset("soul_supernova");
    public DynamiteDefinition crying_souls = preset("crying_souls");
    public DynamiteDefinition eclipse_heart = preset("eclipse_heart");
    public DynamiteDefinition abyss_collapse = preset("abyss_collapse");
    public DynamiteDefinition last_pyre = preset("last_pyre");

    public DynamitesFileConfig() {
        super(SerializerConfigs.YAML);
    }

    private static DynamiteDefinition preset(String id) {
        return DynamitePresets.createDefaults().get(id);
    }

    @Override
    public LoadResult reload(Path path) {
        if (usesLegacyWrapper(path)) {
            DynamitesLegacyFileConfig legacy = new DynamitesLegacyFileConfig();
            LoadResult result = legacy.reload(path);
            applyLegacy(legacy.dynamites);
            return result;
        }
        return super.reload(path);
    }

    public Map<String, DynamiteDefinition> toMap() {
        Map<String, DynamiteDefinition> map = new LinkedHashMap<>();
        map.put("void_crescent", void_crescent);
        map.put("soul_supernova", soul_supernova);
        map.put("crying_souls", crying_souls);
        map.put("eclipse_heart", eclipse_heart);
        map.put("abyss_collapse", abyss_collapse);
        map.put("last_pyre", last_pyre);
        return map;
    }

    private void applyLegacy(Map<String, DynamiteDefinition> legacy) {
        if (legacy == null) {
            return;
        }
        copyIfPresent(legacy, "void_crescent", void_crescent);
        copyIfPresent(legacy, "soul_supernova", soul_supernova);
        copyIfPresent(legacy, "crying_souls", crying_souls);
        copyIfPresent(legacy, "obsidian_rite", crying_souls);
        copyIfPresent(legacy, "eclipse_heart", eclipse_heart);
        copyIfPresent(legacy, "abyss_collapse", abyss_collapse);
        copyIfPresent(legacy, "last_pyre", last_pyre);
    }

    private static void copyIfPresent(
            Map<String, DynamiteDefinition> legacy,
            String id,
            DynamiteDefinition target
    ) {
        DynamiteDefinition source = legacy.get(id);
        if (source != null) {
            DynamiteDefinitionCloner.copyInto(source, target);
        }
    }

    private static boolean usesLegacyWrapper(Path path) {
        try {
            String content = Files.readString(path);
            return content.contains("\ndynamites:\n") || content.startsWith("dynamites:\n");
        } catch (IOException exception) {
            return false;
        }
    }

    private static final class DynamitesLegacyFileConfig extends YamlSerializable {

        public Map<String, DynamiteDefinition> dynamites = new LinkedHashMap<>();

        DynamitesLegacyFileConfig() {
            super(SerializerConfigs.YAML);
        }

    }

}
