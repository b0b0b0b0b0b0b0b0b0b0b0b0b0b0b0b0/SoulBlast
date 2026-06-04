package bm.b0b0b0.SoulBlast.integration.worldguard;

import bm.b0b0b0.SoulBlast.integration.PluginIntegrationsReporter;
import bm.b0b0b0.SoulBlast.ps.integration.ProtectionStonesBridge;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class WorldGuardRegionScan {

    private static volatile boolean scanFailureLogged;

    private WorldGuardRegionScan() {
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
        } catch (ReflectiveOperationException exception) {
            if (!scanFailureLogged) {
                scanFailureLogged = true;
                plugin.getLogger().warning(
                        "Сканирование PS-регионов не удалось (дальше без спама): " + exception.getMessage()
                );
            }
            return List.of();
        }
    }

    private static List<Block> scan(World world, ProtectionStonesBridge bridge) throws ReflectiveOperationException {
        Class<?> regionClass = Class.forName("dev.espi.protectionstones.PSRegion");
        Method fromWgRegion = regionClass.getMethod(
                "fromWGRegion",
                World.class,
                Class.forName("com.sk89q.worldguard.protection.regions.ProtectedRegion")
        );
        Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
        Object worldGuard = worldGuardClass.getMethod("getInstance").invoke(null);
        Object platform = worldGuard.getClass().getMethod("getPlatform").invoke(worldGuard);
        Object container = platform.getClass().getMethod("getRegionContainer").invoke(platform);
        Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
        Object adaptedWorld = bukkitAdapterClass.getMethod("adapt", World.class).invoke(null, world);
        Object manager = resolveRegionManager(container, adaptedWorld, world, bukkitAdapterClass);
        if (manager == null) {
            return List.of();
        }
        Method getRegions = manager.getClass().getMethod("getRegions");
        Map<?, ?> regions = (Map<?, ?>) getRegions.invoke(manager);
        List<Block> blocks = new ArrayList<>();
        for (Object wgRegion : regions.values()) {
            Object psRegion = fromWgRegion.invoke(null, world, wgRegion);
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
