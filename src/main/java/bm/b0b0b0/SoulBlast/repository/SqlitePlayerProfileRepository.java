package bm.b0b0b0.SoulBlast.repository;

import bm.b0b0b0.SoulBlast.config.DatabaseSettings;
import bm.b0b0b0.SoulBlast.model.PlayerProfile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SqlitePlayerProfileRepository implements PlayerProfileRepository {

    private final JavaPlugin plugin;
    private final DatabaseSettings databaseSettings;
    private final ExecutorService executor;
    private volatile Connection connection;

    public SqlitePlayerProfileRepository(JavaPlugin plugin, DatabaseSettings databaseSettings) {
        this.plugin = plugin;
        this.databaseSettings = databaseSettings;
        this.executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "SoulBlast-SQLite");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void initialize() {
        executor.execute(() -> {
            try {
                migrateLegacyDatabaseFile();
                openConnection();
                migrate();
            } catch (SQLException exception) {
                plugin.getLogger().severe("SQLite init failed: " + exception.getMessage());
            }
        });
    }

    @Override
    public void shutdown() {
        executor.execute(this::closeConnection);
        executor.shutdown();
    }

    @Override
    public CompletableFuture<Optional<PlayerProfile>> loadAsync(UUID uuid, boolean defaultAutoIgnite) {
        return CompletableFuture.supplyAsync(() -> loadSync(uuid, defaultAutoIgnite), executor);
    }

    @Override
    public void saveAsync(PlayerProfile profile) {
        executor.execute(() -> saveSync(profile));
    }

    private Optional<PlayerProfile> loadSync(UUID uuid, boolean defaultAutoIgnite) {
        try {
            ensureConnection();
            boolean autoIgnite = defaultAutoIgnite;
            String goalId = null;
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT auto_ignite, goal_dynamite_id FROM player_profiles WHERE uuid = ?"
            )) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        autoIgnite = result.getInt("auto_ignite") == 1;
                        goalId = result.getString("goal_dynamite_id");
                    } else {
                        return Optional.empty();
                    }
                }
            }
            Map<String, Integer> deposits = loadDeposits(uuid);
            return Optional.of(new PlayerProfile(uuid, autoIgnite, goalId, deposits));
        } catch (SQLException exception) {
            plugin.getLogger().severe("SQLite load failed for " + uuid + ": " + exception.getMessage());
            return Optional.empty();
        }
    }

    private Map<String, Integer> loadDeposits(UUID uuid) throws SQLException {
        Map<String, Integer> deposits = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT dynamite_id, vanilla_tnt FROM player_deposits WHERE uuid = ?"
        )) {
            statement.setString(1, uuid.toString());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    deposits.put(result.getString("dynamite_id"), result.getInt("vanilla_tnt"));
                }
            }
        }
        return deposits;
    }

    private void saveSync(PlayerProfile profile) {
        try {
            ensureConnection();
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(
                    """
                            INSERT INTO player_profiles (uuid, auto_ignite, goal_dynamite_id)
                            VALUES (?, ?, ?)
                            ON CONFLICT(uuid) DO UPDATE SET
                                auto_ignite = excluded.auto_ignite,
                                goal_dynamite_id = excluded.goal_dynamite_id
                            """
            )) {
                statement.setString(1, profile.uuid().toString());
                statement.setInt(2, profile.autoIgnite() ? 1 : 0);
                statement.setString(3, profile.goalDynamiteId());
                statement.executeUpdate();
            }
            try (PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM player_deposits WHERE uuid = ?"
            )) {
                delete.setString(1, profile.uuid().toString());
                delete.executeUpdate();
            }
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO player_deposits (uuid, dynamite_id, vanilla_tnt) VALUES (?, ?, ?)"
            )) {
                for (Map.Entry<String, Integer> entry : profile.vanillaTntDepositsCopy().entrySet()) {
                    if (entry.getValue() <= 0) {
                        continue;
                    }
                    insert.setString(1, profile.uuid().toString());
                    insert.setString(2, entry.getKey());
                    insert.setInt(3, entry.getValue());
                    insert.addBatch();
                }
                insert.executeBatch();
            }
            connection.commit();
        } catch (SQLException exception) {
            rollbackQuietly();
            plugin.getLogger().severe("SQLite save failed for " + profile.uuid() + ": " + exception.getMessage());
        } finally {
            setAutoCommitQuietly(true);
        }
    }

    private void migrate() throws SQLException {
        ensureConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_profiles (
                        uuid TEXT PRIMARY KEY,
                        auto_ignite INTEGER NOT NULL DEFAULT 1,
                        goal_dynamite_id TEXT
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_deposits (
                        uuid TEXT NOT NULL,
                        dynamite_id TEXT NOT NULL,
                        vanilla_tnt INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY (uuid, dynamite_id)
                    )
                    """);
        }
    }

    private void migrateLegacyDatabaseFile() {
        File target = databaseFile();
        if (target.exists()) {
            return;
        }
        File legacy = new File(plugin.getDataFolder(), "player-data.db");
        if (!legacy.isFile()) {
            return;
        }
        File parent = target.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        if (legacy.renameTo(target)) {
            plugin.getLogger().info("Player database moved to " + databaseSettings.fileName);
        }
    }

    private File databaseFile() {
        return new File(plugin.getDataFolder(), databaseSettings.fileName);
    }

    private void openConnection() throws SQLException {
        File file = databaseFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute("PRAGMA synchronous=NORMAL");
        }
    }

    private void ensureConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            openConnection();
        }
    }

    private void closeConnection() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException exception) {
            plugin.getLogger().warning("SQLite close failed: " + exception.getMessage());
        } finally {
            connection = null;
        }
    }

    private void rollbackQuietly() {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException ignored) {
        }
    }

    private void setAutoCommitQuietly(boolean autoCommit) {
        try {
            if (connection != null) {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException ignored) {
        }
    }

}
