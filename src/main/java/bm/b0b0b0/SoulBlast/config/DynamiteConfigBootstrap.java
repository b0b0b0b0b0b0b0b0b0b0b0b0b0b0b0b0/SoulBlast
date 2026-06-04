package bm.b0b0b0.SoulBlast.config;

import java.nio.file.Path;

public final class DynamiteConfigBootstrap {

    private DynamiteConfigBootstrap() {
    }

    public static void writeDefaults(Path path) {
        new DynamitesFileConfig().save(path);
    }

}
