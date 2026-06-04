package bm.b0b0b0.SoulBlast.config;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public final class YamlConfigLoader {

    private final JavaPlugin plugin;

    public YamlConfigLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public <T extends net.elytrium.serializer.language.object.YamlSerializable> T load(
            String resourceName,
            String fileName,
            T defaults
    ) {
        return load(resourceName, fileName, defaults, new String[0]);
    }

    public <T extends net.elytrium.serializer.language.object.YamlSerializable> T load(
            String resourceName,
            String fileName,
            T defaults,
            String... legacyFileNames
    ) {
        migrateLegacyFiles(fileName, legacyFileNames);
        Path path = plugin.getDataFolder().toPath().resolve(fileName);
        ensureParent(path);
        if (!Files.exists(path)) {
            if (defaults instanceof DynamitesFileConfig) {
                DynamiteConfigBootstrap.writeDefaults(path);
            } else if (defaults instanceof MaterialGroupsFileConfig) {
                MaterialGroupsConfigBootstrap.writeDefaults(path);
            } else {
                copyResource(resourceName, path);
            }
        }
        stripUtf8BomIfPresent(path);
        defaults.reload(path);
        return defaults;
    }

    private void stripUtf8BomIfPresent(Path path) {
        if (!Files.isRegularFile(path)) {
            return;
        }
        try {
            byte[] bytes = Files.readAllBytes(path);
            if (bytes.length < 3 || bytes[0] != (byte) 0xEF || bytes[1] != (byte) 0xBB || bytes[2] != (byte) 0xBF) {
                return;
            }
            Files.write(path, Arrays.copyOfRange(bytes, 3, bytes.length));
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot strip UTF-8 BOM from " + path, exception);
        }
    }

    private void migrateLegacyFiles(String targetFileName, String... legacyFileNames) {
        if (legacyFileNames == null || legacyFileNames.length == 0) {
            return;
        }
        Path target = plugin.getDataFolder().toPath().resolve(targetFileName);
        if (Files.exists(target)) {
            return;
        }
        for (String legacyFileName : legacyFileNames) {
            if (legacyFileName == null || legacyFileName.isBlank()) {
                continue;
            }
            Path legacy = plugin.getDataFolder().toPath().resolve(legacyFileName);
            if (!Files.isRegularFile(legacy)) {
                continue;
            }
            try {
                ensureParent(target);
                Files.move(legacy, target, StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("Config moved: " + legacyFileName + " -> " + targetFileName);
            } catch (IOException exception) {
                plugin.getLogger().warning("Could not move " + legacyFileName + ": " + exception.getMessage());
            }
            return;
        }
    }

    private void ensureParent(Path path) {
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot create data folder for " + path, exception);
        }
    }

    private void copyResource(String resourceName, Path target) {
        try (InputStream input = plugin.getResource(resourceName)) {
            if (input == null) {
                throw new IllegalStateException("Missing resource: " + resourceName);
            }
            byte[] bytes = input.readAllBytes();
            if (bytes.length >= 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                bytes = Arrays.copyOfRange(bytes, 3, bytes.length);
            }
            Files.write(target, bytes);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot copy " + resourceName, exception);
        }
    }

}
