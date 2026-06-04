package bm.b0b0b0.SoulBlast.ps.repository;

import bm.b0b0b0.SoulBlast.ps.config.PsBlockRecord;
import bm.b0b0b0.SoulBlast.ps.config.PsBlocksFileConfig;
import bm.b0b0b0.SoulBlast.ps.config.PsConfigLoader;
import bm.b0b0b0.SoulBlast.ps.model.PsBlockKey;
import bm.b0b0b0.SoulBlast.ps.model.PsBlockState;
import bm.b0b0b0.SoulBlast.ps.model.PsBlockState;
import bm.b0b0b0.SoulBlast.ps.service.PsRegionId;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public final class PsBlockPersistence {

    private final JavaPlugin plugin;
    private final Path regionsFolder;
    private final Path legacyBlocksFile;

    public PsBlockPersistence(JavaPlugin plugin, PsConfigLoader configLoader) {
        this.plugin = plugin;
        this.regionsFolder = configLoader.regionsFolder();
        this.legacyBlocksFile = configLoader.folder().resolve("blocks.yml");
    }

    public Path regionsFolder() {
        return regionsFolder;
    }

    public void loadInto(PsBlockStore store) {
        store.clear();
        migrateLegacyIfNeeded();
        if (!Files.isDirectory(regionsFolder)) {
            return;
        }
        try (Stream<Path> files = Files.list(regionsFolder)) {
            files.filter(path -> path.getFileName().toString().endsWith(".yml"))
                    .forEach(path -> loadOne(path, store));
        } catch (IOException exception) {
            plugin.getLogger().warning("Не удалось прочитать ps/regions/: " + exception.getMessage());
        }
    }

    public void saveFrom(PsBlockStore store) {
        try {
            Files.createDirectories(regionsFolder);
            for (PsBlockState state : store.all()) {
                writeState(state);
            }
        } catch (IOException exception) {
            plugin.getLogger().warning("Не удалось сохранить ps/regions/: " + exception.getMessage());
        }
    }

    public void saveState(PsBlockState state) {
        if (state == null) {
            return;
        }
        try {
            Files.createDirectories(regionsFolder);
            writeState(state);
        } catch (IOException exception) {
            plugin.getLogger().warning("Не удалось сохранить регион: " + exception.getMessage());
        }
    }

    public void removeState(PsBlockKey key) {
        if (key == null) {
            return;
        }
        Path path = regionPath(key);
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            plugin.getLogger().warning("Не удалось удалить " + path.getFileName() + ": " + exception.getMessage());
        }
    }

    public static String encodeKey(PsBlockKey key) {
        return key.worldId().toString() + ":" + key.x() + ":" + key.y() + ":" + key.z();
    }

    private Path regionPath(PsBlockKey key) {
        return regionsFolder.resolve(PsRegionId.fileName(key));
    }

    private void writeState(PsBlockState state) throws IOException {
        PsBlockRecord record = toRecord(state);
        record.save(regionPath(state.key()));
    }

    private void loadOne(Path path, PsBlockStore store) {
        PsBlockRecord record = new PsBlockRecord();
        record.reload(path);
        PsBlockState state = fromRecord(record);
        if (state != null) {
            state = alignWorldOnLoad(state);
            if (state != null) {
                store.put(state);
            }
        }
    }

    private PsBlockState alignWorldOnLoad(PsBlockState state) {
        World world = plugin.getServer().getWorld(state.key().worldId());
        if (world == null) {
            world = findWorldForBlock(state.key().x(), state.key().y(), state.key().z());
        }
        if (world == null) {
            return state;
        }
        if (state.key().worldId().equals(world.getUID())) {
            return state;
        }
        return new PsBlockState(
                PsBlockKey.of(
                        world,
                        state.key().x(),
                        state.key().y(),
                        state.key().z()
                ),
                state.typeAlias(),
                state.durability(),
                state.maximum(),
                state.ownerName(),
                state.ownerPrefix(),
                state.ownerSuffix(),
                state.radiusX(),
                state.radiusY(),
                state.radiusZ()
        );
    }

    private World findWorldForBlock(int x, int y, int z) {
        for (World world : plugin.getServer().getWorlds()) {
            Block block = world.getBlockAt(x, y, z);
            if (!block.getType().isAir()) {
                return world;
            }
        }
        return null;
    }

    private void migrateLegacyIfNeeded() {
        if (!Files.isRegularFile(legacyBlocksFile)) {
            return;
        }
        try {
            Files.createDirectories(regionsFolder);
            try (Stream<Path> existing = Files.list(regionsFolder)) {
                if (existing.findAny().isPresent()) {
                    return;
                }
            }
            PsBlocksFileConfig legacy = new PsBlocksFileConfig();
            legacy.reload(legacyBlocksFile);
            if (legacy.blocks == null || legacy.blocks.isEmpty()) {
                return;
            }
            for (Map.Entry<String, PsBlockRecord> entry : legacy.blocks.entrySet()) {
                PsBlockState state = fromRecord(entry.getValue());
                if (state != null) {
                    writeState(state);
                }
            }
            Path backup = legacyBlocksFile.resolveSibling("blocks.yml.migrated");
            Files.move(legacyBlocksFile, backup);
            plugin.getLogger().info(
                    "[ProtectionStones+] blocks.yml разнесён в ps/regions/*.yml (резервная копия blocks.yml.migrated)"
            );
        } catch (IOException exception) {
            plugin.getLogger().warning("Миграция blocks.yml не удалась: " + exception.getMessage());
        }
    }

    private static PsBlockRecord toRecord(PsBlockState state) {
        PsBlockRecord record = new PsBlockRecord();
        record.worldId = state.key().worldId().toString();
        record.x = state.key().x();
        record.y = state.key().y();
        record.z = state.key().z();
        record.typeAlias = state.typeAlias();
        record.durability = state.durability();
        record.maximum = state.maximum();
        record.ownerName = state.ownerName();
        record.ownerPrefix = state.ownerPrefix();
        record.ownerSuffix = state.ownerSuffix();
        record.radiusX = state.radiusX();
        record.radiusY = state.radiusY();
        record.radiusZ = state.radiusZ();
        return record;
    }

    private static PsBlockState fromRecord(PsBlockRecord record) {
        if (record == null || record.worldId == null || record.worldId.isBlank()) {
            return null;
        }
        try {
            UUID worldId = UUID.fromString(record.worldId);
            return new PsBlockState(
                    new PsBlockKey(worldId, record.x, record.y, record.z),
                    record.typeAlias == null ? "" : record.typeAlias,
                    Math.max(0, record.durability),
                    Math.max(0, record.maximum),
                    record.ownerName == null ? "?" : record.ownerName,
                    record.ownerPrefix == null ? "" : record.ownerPrefix,
                    record.ownerSuffix == null ? "" : record.ownerSuffix,
                    record.radiusX,
                    record.radiusY,
                    record.radiusZ
            );
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

}
