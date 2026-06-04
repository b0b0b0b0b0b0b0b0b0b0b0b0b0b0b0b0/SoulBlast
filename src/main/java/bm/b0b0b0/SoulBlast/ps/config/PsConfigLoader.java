package bm.b0b0b0.SoulBlast.ps.config;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class PsConfigLoader {

    public static final String FOLDER = "ps";

    private final JavaPlugin plugin;

    public PsConfigLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public Path folder() {
        return plugin.getDataFolder().toPath().resolve(FOLDER);
    }

    public Path typesPath() {
        return folder().resolve("types.yml");
    }

    public Path typesFolder() {
        return folder().resolve("types");
    }

    public Path regionsFolder() {
        return folder().resolve("regions");
    }

    public <T extends net.elytrium.serializer.language.object.YamlSerializable> T load(
            String resourceName,
            String fileName,
            T defaults
    ) {
        Path path = folder().resolve(fileName);
        ensureParent(path);
        if (!Files.exists(path)) {
            copyResource(resourceName, path);
        }
        stripUtf8BomIfPresent(path);
        defaults.reload(path);
        if (defaults instanceof PsSettingsFileConfig settingsFile) {
            settingsFile.applyLegacyRootFields();
        }
        return defaults;
    }

    private void ensureParent(Path path) {
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot create ps folder for " + path, exception);
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

}
