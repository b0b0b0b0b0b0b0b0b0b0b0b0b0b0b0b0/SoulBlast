package bm.b0b0b0.SoulBlast.ps.integration;

import bm.b0b0b0.SoulBlast.integration.PluginIntegrationsReporter;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

public final class LuckPermsBridge {

    private final boolean available;
    private final Method getUserManager;
    private final Method getUser;
    private final Method getCachedData;
    private final Method getMetaData;
    private final Method getPrefix;
    private final Method getSuffix;
    private final Object api;

    private LuckPermsBridge(
            boolean available,
            Object api,
            Method getUserManager,
            Method getUser,
            Method getCachedData,
            Method getMetaData,
            Method getPrefix,
            Method getSuffix
    ) {
        this.available = available;
        this.api = api;
        this.getUserManager = getUserManager;
        this.getUser = getUser;
        this.getCachedData = getCachedData;
        this.getMetaData = getMetaData;
        this.getPrefix = getPrefix;
        this.getSuffix = getSuffix;
    }

    public static LuckPermsBridge tryCreate(JavaPlugin plugin) {
        if (!PluginIntegrationsReporter.isPluginActive(plugin, "LuckPerms")) {
            return disabled();
        }
        try {
            Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            Method get = providerClass.getMethod("get");
            Object api = get.invoke(null);
            Class<?> apiClass = Class.forName("net.luckperms.api.LuckPerms");
            Method userManagerMethod = apiClass.getMethod("getUserManager");
            Class<?> userManagerClass = Class.forName("net.luckperms.api.model.user.UserManager");
            Method getUserMethod = userManagerClass.getMethod("getUser", UUID.class);
            Class<?> userClass = Class.forName("net.luckperms.api.model.user.User");
            Method cachedDataMethod = userClass.getMethod("getCachedData");
            Class<?> cachedDataClass = Class.forName("net.luckperms.api.cacheddata.CachedDataManager");
            Method metaDataMethod = cachedDataClass.getMethod("getMetaData");
            Class<?> metaDataClass = Class.forName("net.luckperms.api.cacheddata.CachedMetaData");
            Method prefixMethod = metaDataClass.getMethod("getPrefix");
            Method suffixMethod = metaDataClass.getMethod("getSuffix");
            return new LuckPermsBridge(
                    true,
                    api,
                    userManagerMethod,
                    getUserMethod,
                    cachedDataMethod,
                    metaDataMethod,
                    prefixMethod,
                    suffixMethod
            );
        } catch (Throwable failure) {
            return disabled();
        }
    }

    public static LuckPermsBridge disabled() {
        return new LuckPermsBridge(false, null, null, null, null, null, null, null);
    }

    public boolean available() {
        return available;
    }

    public Optional<ChatMeta> resolve(UUID playerId) {
        if (!available || playerId == null) {
            return Optional.empty();
        }
        try {
            Object userManager = getUserManager.invoke(api);
            Object user = getUser.invoke(userManager, playerId);
            if (user == null) {
                return Optional.empty();
            }
            Object cached = getCachedData.invoke(user);
            Object meta = getMetaData.invoke(cached);
            String prefix = stringify(getPrefix.invoke(meta));
            String suffix = stringify(getSuffix.invoke(meta));
            return Optional.of(new ChatMeta(prefix, suffix));
        } catch (ReflectiveOperationException exception) {
            return Optional.empty();
        }
    }

    private static String stringify(Object value) {
        return value == null ? "" : value.toString();
    }

    public record ChatMeta(String prefix, String suffix) {
    }

}
