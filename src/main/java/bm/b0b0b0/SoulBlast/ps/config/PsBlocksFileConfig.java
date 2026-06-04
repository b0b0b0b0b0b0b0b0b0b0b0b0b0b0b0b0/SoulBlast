package bm.b0b0b0.SoulBlast.ps.config;

import bm.b0b0b0.SoulBlast.config.SerializerConfigs;
import net.elytrium.serializer.LoadResult;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.language.object.YamlSerializable;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class PsBlocksFileConfig extends YamlSerializable {

    @Comment(@CommentValue("Сохранённые блоки привата (ключ: uuid мира:x:y:z)"))
    public Map<String, PsBlockRecord> blocks = new LinkedHashMap<>();

    public PsBlocksFileConfig() {
        super(SerializerConfigs.YAML);
    }

    @Override
    public LoadResult reload(Path path) {
        LoadResult result = super.reload(path);
        if (blocks == null) {
            blocks = new LinkedHashMap<>();
        }
        return result;
    }

}
