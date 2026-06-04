package bm.b0b0b0.SoulBlast.decay.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

import java.util.LinkedHashMap;
import java.util.Map;

public class DecayRegenerationSettings {

    @Comment(@CommentValue("Пассивная реген трещин без игрока"))
    public String every = "1 min";

    @Comment({
            @CommentValue("При manual-repair.sand-only: true в general.yml игнорируется — всегда песок"),
            @CommentValue("Если sand-only: false — материал -> штук за клик (например SAND: \"1\")")
    })
    public Map<String, String> materials = new LinkedHashMap<>();

}
