package bm.b0b0b0.SoulBlast.integration.coreprotect;

import bm.b0b0b0.SoulBlast.config.CoreProtectIntegrationSettings;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public final class CoreProtectBridge {

    private final JavaPlugin plugin;
    private CoreProtectIntegrationSettings settings = new CoreProtectIntegrationSettings();
    private CoreProtectAPI api;
    private int boundApiVersion;
    private String bindIssue = "";
    private final AtomicLong logFailures = new AtomicLong();

    public CoreProtectBridge(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload(CoreProtectIntegrationSettings settings) {
        this.settings = settings == null ? new CoreProtectIntegrationSettings() : settings;
        bindApi(resolveApi());
    }

    public boolean active() {
        return settings.enabled && api != null;
    }

    public int boundApiVersion() {
        return boundApiVersion;
    }

    public String bindIssue() {
        return bindIssue;
    }

    public String logUser() {
        return settings.fallbackUser == null || settings.fallbackUser.isBlank()
                ? "#soulblast"
                : settings.fallbackUser;
    }

    public void logBreak(ExplosionJob job, Block block) {
        logBreak(job == null ? null : job.getSource(), block);
    }

    public void logBreak(Entity source, Block block) {
        if (!active() || !settings.logBreaks || block == null) {
            return;
        }
        Material type = block.getType();
        if (type.isAir() || type == Material.CAVE_AIR) {
            return;
        }
        invokeLogRemoval(resolveUser(source), block.getState());
    }

    public void logLiquidClear(ExplosionJob job, Block block) {
        logLiquidClear(job == null ? null : job.getSource(), block);
    }

    public void logLiquidClear(Entity source, Block block) {
        if (!active() || !settings.logLiquidClear || block == null) {
            return;
        }
        Material type = block.getType();
        if (type.isAir() || type == Material.CAVE_AIR) {
            return;
        }
        invokeLogRemoval(resolveUser(source), block.getState());
    }

    public void logPlace(ExplosionJob job, Block block) {
        logPlace(job == null ? null : job.getSource(), block);
    }

    public void logPlace(Entity source, Block block) {
        if (!active() || !settings.logPlacements || block == null) {
            return;
        }
        Material type = block.getType();
        if (type.isAir() || type == Material.CAVE_AIR) {
            return;
        }
        invokeLogPlacement(resolveUser(source), block.getState());
    }

    public RollbackOutcome rollbackAround(String user, Location center, int radius, int timeSeconds) {
        if (!active()) {
            return RollbackOutcome.unavailable();
        }
        if (center == null || center.getWorld() == null) {
            return RollbackOutcome.invalidCenter();
        }
        if (user == null || user.isBlank()) {
            return RollbackOutcome.invalidUser();
        }
        if (radius <= 0 || timeSeconds <= 0) {
            return RollbackOutcome.invalidArguments();
        }
        try {
            List<String[]> rows = api.performRollback(
                    timeSeconds,
                    List.of(user),
                    null,
                    null,
                    null,
                    null,
                    radius,
                    center
            );
            int changes = rows == null ? 0 : rows.size();
            return RollbackOutcome.success(changes);
        } catch (Exception exception) {
            plugin.getLogger().warning("CoreProtect rollback failed: " + exception.getMessage());
            return RollbackOutcome.failed(exception.getMessage());
        }
    }

    private String resolveUser(ExplosionJob job) {
        return resolveUser(job == null ? null : job.getSource());
    }

    private String resolveUser(Entity source) {
        if (settings.logAllAsFallbackUser) {
            return logUser();
        }
        if (source instanceof Player player) {
            return player.getName();
        }
        return logUser();
    }

    private void invokeLogPlacement(String user, BlockState state) {
        if (api == null || state == null) {
            return;
        }
        try {
            if (!api.logPlacement(user, state)) {
                noteLogFailure("logPlacement");
            }
        } catch (Exception exception) {
            noteLogFailure("logPlacement: " + exception.getMessage());
        }
    }

    private void invokeLogRemoval(String user, BlockState state) {
        if (api == null || state == null) {
            return;
        }
        try {
            if (!api.logRemoval(user, state)) {
                noteLogFailure("logRemoval");
            }
        } catch (Exception exception) {
            noteLogFailure("logRemoval: " + exception.getMessage());
        }
    }

    private void noteLogFailure(String detail) {
        long count = logFailures.incrementAndGet();
        if (count == 1 || count % 5000 == 0) {
            plugin.getLogger().warning("CoreProtect " + detail + " (failures: " + count + ")");
        }
    }

    private CoreProtectAPI resolveApi() {
        Plugin coreProtect = plugin.getServer().getPluginManager().getPlugin("CoreProtect");
        if (!(coreProtect instanceof CoreProtect coreProtectPlugin)) {
            return null;
        }
        try {
            CoreProtectAPI apiObject = coreProtectPlugin.getAPI();
            if (apiObject == null || !apiObject.isEnabled()) {
                return null;
            }
            if (apiObject.APIVersion() < 10) {
                plugin.getLogger().warning("CoreProtect API v" + apiObject.APIVersion() + " — нужен 10+");
                return null;
            }
            return apiObject;
        } catch (Exception exception) {
            plugin.getLogger().warning("CoreProtect API unavailable: " + exception.getMessage());
            return null;
        }
    }

    private void bindApi(CoreProtectAPI apiObject) {
        this.api = null;
        this.boundApiVersion = 0;
        this.bindIssue = "";
        logFailures.set(0);
        if (!settings.enabled) {
            this.bindIssue = "disabled";
            return;
        }
        if (apiObject == null) {
            this.bindIssue = "unavailable";
            return;
        }
        this.api = apiObject;
        this.boundApiVersion = apiObject.APIVersion();
    }

    public enum RollbackOutcomeType {
        SUCCESS,
        UNAVAILABLE,
        INVALID_USER,
        INVALID_CENTER,
        INVALID_ARGUMENTS,
        FAILED
    }

    public record RollbackOutcome(RollbackOutcomeType type, int changes, String detail) {

        public static RollbackOutcome success(int changes) {
            return new RollbackOutcome(RollbackOutcomeType.SUCCESS, changes, "");
        }

        public static RollbackOutcome unavailable() {
            return new RollbackOutcome(RollbackOutcomeType.UNAVAILABLE, 0, "");
        }

        public static RollbackOutcome invalidUser() {
            return new RollbackOutcome(RollbackOutcomeType.INVALID_USER, 0, "");
        }

        public static RollbackOutcome invalidCenter() {
            return new RollbackOutcome(RollbackOutcomeType.INVALID_CENTER, 0, "");
        }

        public static RollbackOutcome invalidArguments() {
            return new RollbackOutcome(RollbackOutcomeType.INVALID_ARGUMENTS, 0, "");
        }

        public static RollbackOutcome failed(String detail) {
            return new RollbackOutcome(RollbackOutcomeType.FAILED, 0, detail == null ? "" : detail);
        }
    }

}
