package bm.b0b0b0.SoulBlast.config;

import java.nio.file.Path;

public final class MaterialGroupsConfigBootstrap {

    private MaterialGroupsConfigBootstrap() {
    }

    public static void writeDefaults(Path path) {
        new MaterialGroupsFileConfig().save(path);
    }

}
