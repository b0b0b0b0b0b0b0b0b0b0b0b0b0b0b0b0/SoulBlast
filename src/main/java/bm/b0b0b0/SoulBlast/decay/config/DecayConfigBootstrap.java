package bm.b0b0b0.SoulBlast.decay.config;

import java.nio.file.Path;

public final class DecayConfigBootstrap {

    private DecayConfigBootstrap() {
    }

    public static void writeBlocks(Path path) {
        new DecayBlocksFileConfig().save(path);
    }

}
