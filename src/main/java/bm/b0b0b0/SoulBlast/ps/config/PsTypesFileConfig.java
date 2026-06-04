package bm.b0b0b0.SoulBlast.ps.config;

import bm.b0b0b0.SoulBlast.config.SerializerConfigs;
import net.elytrium.serializer.LoadResult;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.language.object.YamlSerializable;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class PsTypesFileConfig extends YamlSerializable {

    @Comment({
            @CommentValue("Типы привата ProtectionStones — ключ = alias из blocks/*.toml"),
            @CommentValue("Пустой файл заполнится автоматически при старте; дальше правь здесь")
    })
    public Map<String, PsProtectionTypeDefinition> types = new LinkedHashMap<>();

    public PsTypesFileConfig() {
        super(SerializerConfigs.YAML);
    }

    @Override
    public LoadResult reload(Path path) {
        LoadResult result = super.reload(path);
        if (types == null) {
            types = new LinkedHashMap<>();
        }
        return result;
    }

}
