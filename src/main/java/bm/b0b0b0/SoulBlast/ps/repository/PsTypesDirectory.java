package bm.b0b0b0.SoulBlast.ps.repository;

import bm.b0b0b0.SoulBlast.ps.config.PsProtectionTypeDefinition;
import bm.b0b0b0.SoulBlast.ps.config.PsTypesFileConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class PsTypesDirectory {

    private final JavaPlugin plugin;
    private final Path typesFolder;
    private final Path legacyTypesFile;

    public PsTypesDirectory(JavaPlugin plugin, Path psFolder) {
        this.plugin = plugin;
        this.typesFolder = psFolder.resolve("types");
        this.legacyTypesFile = psFolder.resolve("types.yml");
    }

    public Path typesFolder() {
        return typesFolder;
    }

    public Map<String, PsProtectionTypeDefinition> loadAll() {
        migrateLegacyIfNeeded();
        Map<String, PsProtectionTypeDefinition> loaded = new LinkedHashMap<>();
        if (!Files.isDirectory(typesFolder)) {
            return loaded;
        }
        try (Stream<Path> files = Files.list(typesFolder)) {
            files.filter(path -> path.getFileName().toString().endsWith(".yml"))
                    .forEach(path -> loadOne(path, loaded));
        } catch (IOException exception) {
            plugin.getLogger().warning("Не удалось прочитать ps/types/: " + exception.getMessage());
        }
        return loaded;
    }

    public void saveType(String alias, PsProtectionTypeDefinition definition) {
        if (alias == null || alias.isBlank() || definition == null) {
            return;
        }
        try {
            Files.createDirectories(typesFolder);
            String safeName = sanitizeFileName(alias) + ".yml";
            definition.save(typesFolder.resolve(safeName));
        } catch (IOException exception) {
            plugin.getLogger().warning("Не удалось сохранить тип " + alias + ": " + exception.getMessage());
        }
    }

    public void saveAll(Map<String, PsProtectionTypeDefinition> types) {
        if (types == null || types.isEmpty()) {
            return;
        }
        for (Map.Entry<String, PsProtectionTypeDefinition> entry : types.entrySet()) {
            saveType(entry.getKey(), entry.getValue());
        }
    }

    private void loadOne(Path path, Map<String, PsProtectionTypeDefinition> loaded) {
        String fileName = path.getFileName().toString();
        String alias = fileName.substring(0, fileName.length() - 4);
        if (alias.isBlank()) {
            return;
        }
        PsProtectionTypeDefinition definition = new PsProtectionTypeDefinition();
        definition.reload(path);
        loaded.put(alias, definition);
    }

    private void migrateLegacyIfNeeded() {
        if (!Files.isRegularFile(legacyTypesFile)) {
            return;
        }
        try {
            Files.createDirectories(typesFolder);
            try (Stream<Path> existing = Files.list(typesFolder)) {
                if (existing.findAny().isPresent()) {
                    return;
                }
            }
            PsTypesFileConfig legacy = new PsTypesFileConfig();
            legacy.reload(legacyTypesFile);
            if (legacy.types == null || legacy.types.isEmpty()) {
                return;
            }
            for (Map.Entry<String, PsProtectionTypeDefinition> entry : legacy.types.entrySet()) {
                if (entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null) {
                    continue;
                }
                saveType(entry.getKey().trim(), entry.getValue());
            }
            Path backup = legacyTypesFile.resolveSibling("types.yml.migrated");
            Files.move(legacyTypesFile, backup);
            plugin.getLogger().info(
                    "[ProtectionStones+] types.yml разнесён в ps/types/*.yml (резервная копия types.yml.migrated)"
            );
        } catch (IOException exception) {
            plugin.getLogger().warning("Миграция types.yml не удалась: " + exception.getMessage());
        }
    }

    private static String sanitizeFileName(String alias) {
        return alias.replaceAll("[^a-zA-Z0-9._+-]", "_");
    }

}
