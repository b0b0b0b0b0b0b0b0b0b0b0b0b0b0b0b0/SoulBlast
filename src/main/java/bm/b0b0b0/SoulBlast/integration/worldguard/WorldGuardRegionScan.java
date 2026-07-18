package bm.b0b0b0.SoulBlast.integration.worldguard;

import bm.b0b0b0.SoulBlast.integration.PluginIntegrationsReporter;
import bm.b0b0b0.SoulBlast.ps.integration.ProtectionStonesBridge;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class WorldGuardRegionScan {

    private static volatile boolean scanFailureLogged;

    private WorldGuardRegionScan() {
    }

    public static int countProtectionStoneRegions(JavaPlugin plugin, ProtectionStonesBridge bridge) {
        if (!bridge.available() || !PluginIntegrationsReporter.isPluginActive(plugin, "WorldGuard")) {
            return 0;
        }
        int total = 0;
        for (World world : plugin.getServer().getWorlds()) {
            try {
                total += countInWorld(world, bridge);
            } catch (Throwable failure) {
                if (!scanFailureLogged) {
                    scanFailureLogged = true;
                    plugin.getLogger().warning(
                            "Подсчёт PS-регионов не удался (дальше без спама): " + failure.getMessage()
                    );
                }
                return total;
            }
        }
        return total;
    }

    public static List<Block> listProtectionStoneBlocks(
            JavaPlugin plugin,
            ProtectionStonesBridge bridge,
            World world
    ) {
        if (!bridge.available() || !PluginIntegrationsReporter.isPluginActive(plugin, "WorldGuard")) {
            return List.of();
        }
        try {
            return scan(world, bridge);
        } catch (Throwable failure) {
            if (!scanFailureLogged) {
                scanFailureLogged = true;
                plugin.getLogger().warning(
                        "Сканирование PS-регионов не удалось (дальше без спама): " + failure.getMessage()
                );
            }
            return List.of();
        }
    }

    private static int countInWorld(World world, ProtectionStonesBridge bridge) throws Throwable {
        MethodHandle fromWgRegion = fromWgRegionHandle();
        Map<?, ?> regions = worldRegions(world);
        if (regions == null) {
            return 0;
        }
        int count = 0;
        for (Object wgRegion : regions.values()) {
            Object psRegion = fromWgRegion.invoke(world, wgRegion);
            if (psRegion != null) {
                count++;
            }
        }
        return count;
    }

    private static List<Block> scan(World world, ProtectionStonesBridge bridge) throws Throwable {
        MethodHandle fromWgRegion = fromWgRegionHandle();
        Map<?, ?> regions = worldRegions(world);
        if (regions == null) {
            return List.of();
        }
        List<Block> blocks = new ArrayList<>();
        for (Object wgRegion : regions.values()) {
            Object psRegion = fromWgRegion.invoke(world, wgRegion);
            if (psRegion == null) {
                continue;
            }
            Block protectBlock = bridge.protectBlockFromRegion(psRegion);
            if (protectBlock != null && bridge.isProtectBlock(protectBlock)) {
                blocks.add(protectBlock);
            }
        }
        return blocks;
    }

    private static MethodHandle fromWgRegionHandle() throws ReflectiveOperationException {
        Class<?> regionClass = Class.forName("dev.espi.protectionstones.PSRegion");
        Class<?> wgRegionClass = Class.forName("com.sk89q.worldguard.protection.regions.ProtectedRegion");
        return MethodHandles.publicLookup().findStatic(
                regionClass,
                "fromWGRegion",
                MethodType.methodType(regionClass, World.class, wgRegionClass)
        );
    }

    private static Map<?, ?> worldRegions(World world) throws ReflectiveOperationException {
        Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
        Object worldGuard = worldGuardClass.getMethod("getInstance").invoke(null);
        Object platform = worldGuard.getClass().getMethod("getPlatform").invoke(worldGuard);
        Object container = platform.getClass().getMethod("getRegionContainer").invoke(platform);
        Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
        Object adaptedWorld = bukkitAdapterClass.getMethod("adapt", World.class).invoke(null, world);
        Object manager = resolveRegionManager(container, adaptedWorld, world, bukkitAdapterClass);
        if (manager == null) {
            return null;
        }
        Method getRegions = manager.getClass().getMethod("getRegions");
        return (Map<?, ?>) getRegions.invoke(manager);
    }

    private static Object resolveRegionManager(
            Object container,
            Object adaptedWorld,
            World bukkitWorld,
            Class<?> bukkitAdapterClass
    ) throws ReflectiveOperationException {
        ReflectiveOperationException lastFailure = null;
        for (String parameterType : List.of(
                "com.sk89q.worldedit.world.World",
                "com.sk89q.worldedit.bukkit.BukkitWorld"
        )) {
            try {
                Class<?> paramClass = Class.forName(parameterType);
                Method get = container.getClass().getMethod("get", paramClass);
                return get.invoke(container, adaptedWorld);
            } catch (ReflectiveOperationException exception) {
                lastFailure = exception;
            }
        }
        try {
            Method asBukkitWorld = bukkitAdapterClass.getMethod("asBukkitWorld", World.class);
            Object bukkitWeWorld = asBukkitWorld.invoke(null, bukkitWorld);
            Class<?> bukkitWorldClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitWorld");
            Method get = container.getClass().getMethod("get", bukkitWorldClass);
            return get.invoke(container, bukkitWeWorld);
        } catch (ReflectiveOperationException exception) {
            if (lastFailure != null) {
                throw lastFailure;
            }
            throw exception;
        }
    }

}
