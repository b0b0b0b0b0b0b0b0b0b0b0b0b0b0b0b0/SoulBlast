package bm.b0b0b0.SoulBlast.integration.worldguard;

import bm.b0b0b0.SoulBlast.config.RegionProtectionSettings;
import bm.b0b0b0.SoulBlast.service.region.AdaptedRegionVolume;
import bm.b0b0b0.SoulBlast.service.region.RegionBackend;
import bm.b0b0b0.SoulBlast.service.region.RegionPointCheck;
import bm.b0b0b0.SoulBlast.service.region.RegionVolume;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public final class WorldGuardRegionBackend implements RegionBackend {

    private static final String SOULBLAST_FLAG = "soulblast-dynamite";

    private final JavaPlugin plugin;
    private final Object flagRegistry;
    private final Method flagRegistryRegister;
    private final Method worldGuardGetInstance;
    private final Method platformGetRegionContainer;
    private final Method containerGet;
    private final Method containerCreateQuery;
    private final Method queryGetApplicableRegions;
    private final Method applicableSetTestState;
    private final Method regionManagerGetRegions;
    private final Method regionGetId;
    private final Method regionGetMinimumPoint;
    private final Method regionGetMaximumPoint;
    private final Method regionContains;
    private final Method regionGetFlag;
    private final Method blockVectorAt;
    private final Method blockVectorX;
    private final Method blockVectorY;
    private final Method blockVectorZ;
    private final Method bukkitAdapterAdaptLocation;
    private final Method bukkitAdapterAdaptWorld;
    private final Method worldGuardInst;
    private final Method wrapPlayer;
    private final Object buildFlag;
    private final Object stateDeny;
    private final Set<String> configuredFlagNames;
    private volatile boolean regionScanFailureLogged;
    private volatile boolean buildTestFailureLogged;

    private WorldGuardRegionBackend(
            JavaPlugin plugin,
            Object flagRegistry,
            Method flagRegistryRegister,
            Method worldGuardGetInstance,
            Method platformGetRegionContainer,
            Method containerGet,
            Method containerCreateQuery,
            Method queryGetApplicableRegions,
            Method applicableSetTestState,
            Method regionManagerGetRegions,
            Method regionGetId,
            Method regionGetMinimumPoint,
            Method regionGetMaximumPoint,
            Method regionContains,
            Method regionGetFlag,
            Method blockVectorAt,
            Method blockVectorX,
            Method blockVectorY,
            Method blockVectorZ,
            Method bukkitAdapterAdaptLocation,
            Method bukkitAdapterAdaptWorld,
            Method worldGuardInst,
            Method wrapPlayer,
            Object buildFlag,
            Object stateDeny,
            Set<String> configuredFlagNames
    ) {
        this.plugin = plugin;
        this.flagRegistry = flagRegistry;
        this.flagRegistryRegister = flagRegistryRegister;
        this.worldGuardGetInstance = worldGuardGetInstance;
        this.platformGetRegionContainer = platformGetRegionContainer;
        this.containerGet = containerGet;
        this.containerCreateQuery = containerCreateQuery;
        this.queryGetApplicableRegions = queryGetApplicableRegions;
        this.applicableSetTestState = applicableSetTestState;
        this.regionManagerGetRegions = regionManagerGetRegions;
        this.regionGetId = regionGetId;
        this.regionGetMinimumPoint = regionGetMinimumPoint;
        this.regionGetMaximumPoint = regionGetMaximumPoint;
        this.regionContains = regionContains;
        this.regionGetFlag = regionGetFlag;
        this.blockVectorAt = blockVectorAt;
        this.blockVectorX = blockVectorX;
        this.blockVectorY = blockVectorY;
        this.blockVectorZ = blockVectorZ;
        this.bukkitAdapterAdaptLocation = bukkitAdapterAdaptLocation;
        this.bukkitAdapterAdaptWorld = bukkitAdapterAdaptWorld;
        this.worldGuardInst = worldGuardInst;
        this.wrapPlayer = wrapPlayer;
        this.buildFlag = buildFlag;
        this.stateDeny = stateDeny;
        this.configuredFlagNames = configuredFlagNames;
    }

    public static WorldGuardRegionBackend tryCreate(JavaPlugin plugin, RegionProtectionSettings settings) {
        org.bukkit.plugin.Plugin worldGuard = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuard == null || !worldGuard.isEnabled()) {
            return null;
        }
        try {
            return build(plugin, settings);
        } catch (Throwable failure) {
            plugin.getLogger().warning("WorldGuard API unavailable: " + failure.getMessage());
            return null;
        }
    }

    public static boolean isWorldGuardPresent(JavaPlugin plugin) {
        org.bukkit.plugin.Plugin worldGuard = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        return worldGuard != null && worldGuard.isEnabled();
    }

    private static WorldGuardRegionBackend build(JavaPlugin plugin, RegionProtectionSettings settings) throws ReflectiveOperationException {
        Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
        Class<?> worldGuardPluginClass = Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin");
        Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
        Class<?> blockVectorClass = Class.forName("com.sk89q.worldedit.math.BlockVector3");
        Class<?> stateFlagClass = Class.forName("com.sk89q.worldguard.protection.flags.StateFlag");
        Class<?> stateEnumClass = Class.forName("com.sk89q.worldguard.protection.flags.StateFlag$State");
        Class<?> flagsClass = Class.forName("com.sk89q.worldguard.protection.flags.Flags");

        Method worldGuardGetInstance = worldGuardClass.getMethod("getInstance");
        Object worldGuard = worldGuardGetInstance.invoke(null);
        Object platform = worldGuard.getClass().getMethod("getPlatform").invoke(worldGuard);
        Method platformGetRegionContainer = platform.getClass().getMethod("getRegionContainer");
        Object container = platformGetRegionContainer.invoke(platform);
        Method containerGet = container.getClass().getMethod("get", Class.forName("com.sk89q.worldedit.world.World"));
        Method containerCreateQuery = container.getClass().getMethod("createQuery");
        Object query = containerCreateQuery.invoke(container);
        Method queryGetApplicableRegions = query.getClass().getMethod(
                "getApplicableRegions",
                Class.forName("com.sk89q.worldedit.util.Location")
        );

        Method applicableSetTestState = findApplicableSetTestState(stateFlagClass);

        Method regionManagerGetRegions = Class.forName("com.sk89q.worldguard.protection.managers.RegionManager").getMethod("getRegions");
        Method regionGetId = Class.forName("com.sk89q.worldguard.protection.regions.ProtectedRegion").getMethod("getId");
        Method regionGetMinimumPoint = Class.forName("com.sk89q.worldguard.protection.regions.ProtectedRegion").getMethod("getMinimumPoint");
        Method regionGetMaximumPoint = Class.forName("com.sk89q.worldguard.protection.regions.ProtectedRegion").getMethod("getMaximumPoint");
        Method regionContains = Class.forName("com.sk89q.worldguard.protection.regions.ProtectedRegion").getMethod("contains", blockVectorClass);
        Method regionGetFlag = Class.forName("com.sk89q.worldguard.protection.regions.ProtectedRegion").getMethod("getFlag", Class.forName("com.sk89q.worldguard.protection.flags.Flag"));

        Method blockVectorAt = blockVectorClass.getMethod("at", int.class, int.class, int.class);
        Method blockVectorX = blockVectorClass.getMethod("x");
        Method blockVectorY = blockVectorClass.getMethod("y");
        Method blockVectorZ = blockVectorClass.getMethod("z");
        Method bukkitAdapterAdaptLocation = bukkitAdapterClass.getMethod("adapt", Location.class);
        Method bukkitAdapterAdaptWorld = bukkitAdapterClass.getMethod("adapt", World.class);
        Method worldGuardInst = worldGuardPluginClass.getMethod("inst");
        Method wrapPlayer = worldGuardPluginClass.getMethod("wrapPlayer", Player.class);

        Object flagRegistry = worldGuard.getClass().getMethod("getFlagRegistry").invoke(worldGuard);
        Method flagRegistryRegister = flagRegistry.getClass().getMethod("register", Class.forName("com.sk89q.worldguard.protection.flags.Flag"));
        Object buildFlag = flagsClass.getField("BUILD").get(null);
        Object stateDeny = stateEnumClass.getMethod("valueOf", String.class).invoke(null, "DENY");

        Set<String> configuredFlagNames = new HashSet<>();
        for (String configured : settings.worldguardFlags) {
            if (configured != null && !configured.isBlank()) {
                configuredFlagNames.add(configured.trim().toLowerCase(Locale.ROOT));
            }
        }

        return new WorldGuardRegionBackend(
                plugin,
                flagRegistry,
                flagRegistryRegister,
                worldGuardGetInstance,
                platformGetRegionContainer,
                containerGet,
                containerCreateQuery,
                queryGetApplicableRegions,
                applicableSetTestState,
                regionManagerGetRegions,
                regionGetId,
                regionGetMinimumPoint,
                regionGetMaximumPoint,
                regionContains,
                regionGetFlag,
                blockVectorAt,
                blockVectorX,
                blockVectorY,
                blockVectorZ,
                bukkitAdapterAdaptLocation,
                bukkitAdapterAdaptWorld,
                worldGuardInst,
                wrapPlayer,
                buildFlag,
                stateDeny,
                configuredFlagNames
        );
    }

    @Override
    public boolean available() {
        return true;
    }

    @Override
    public void registerIntegration() {
        try {
            Class<?> stateFlagClass = Class.forName("com.sk89q.worldguard.protection.flags.StateFlag");
            Constructor<?> constructor = stateFlagClass.getConstructor(String.class, boolean.class);
            Object flag = constructor.newInstance(SOULBLAST_FLAG, true);
            flagRegistryRegister.invoke(flagRegistry, flag);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public List<RegionVolume> protectedVolumes(
            World world,
            Location sample,
            Player player,
            RegionProtectionSettings settings
    ) {
        return collectVolumes(world, sample, player, settings, true);
    }

    @Override
    public List<RegionVolume> dynamitePlacementVolumes(
            World world,
            Location sample,
            Player player,
            RegionProtectionSettings settings
    ) {
        return collectVolumes(world, sample, player, settings, false);
    }

    private List<RegionVolume> collectVolumes(
            World world,
            Location sample,
            Player player,
            RegionProtectionSettings settings,
            boolean includeBuildDenyZones
    ) {
        try {
            List<RegionVolume> volumes = collectProtectedVolumes(world, sample, player, settings, includeBuildDenyZones);
            regionScanFailureLogged = false;
            return volumes;
        } catch (ReflectiveOperationException exception) {
            if (!regionScanFailureLogged) {
                regionScanFailureLogged = true;
                plugin.getLogger().warning(
                        "WorldGuard region scan failed (further errors suppressed): " + exception.getMessage()
                );
            }
            return List.of();
        }
    }

    private List<RegionVolume> collectProtectedVolumes(
            World world,
            Location sample,
            Player player,
            RegionProtectionSettings settings,
            boolean includeBuildDenyZones
    ) throws ReflectiveOperationException {
        Object worldGuard = worldGuardGetInstance.invoke(null);
        Object platform = worldGuard.getClass().getMethod("getPlatform").invoke(worldGuard);
        Object container = platformGetRegionContainer.invoke(platform);
        Object adaptedWorld = bukkitAdapterAdaptWorld.invoke(null, world);
        Object manager = containerGet.invoke(container, adaptedWorld);
        if (manager == null) {
            return List.of();
        }
        Set<String> exempt = lowered(settings.exemptRegionNames);
        Set<String> named = lowered(settings.regionNames);
        Set<Object> matched = new HashSet<>();
        Map<?, ?> regions = (Map<?, ?>) regionManagerGetRegions.invoke(manager);
        for (Object region : copyRegionValues(regions)) {
            String id = (String) regionGetId.invoke(region);
            if (id == null || "__global__".equals(id) || isExempt(id, exempt)) {
                continue;
            }
            if (named.contains(id.toLowerCase(Locale.ROOT))) {
                matched.add(region);
                continue;
            }
            if (includeBuildDenyZones && settings.protectFlagDenyRegions && hasDenyFlag(region)) {
                matched.add(region);
            }
        }
        if (sample != null) {
            Object query = containerCreateQuery.invoke(container);
            Object adaptedLocation = bukkitAdapterAdaptLocation.invoke(null, sample);
            Object applicable = queryGetApplicableRegions.invoke(query, adaptedLocation);
            List<Object> applicableRegions = extractRegions(applicable);
            for (Object region : applicableRegions) {
                String id = (String) regionGetId.invoke(region);
                if (id == null || isExempt(id, exempt)) {
                    continue;
                }
                if (includeBuildDenyZones && settings.protectFlagDenyRegions && hasDenyFlag(region)) {
                    matched.add(region);
                    continue;
                }
                if (!includeBuildDenyZones && named.contains(id.toLowerCase(Locale.ROOT))) {
                    matched.add(region);
                }
            }
            if (includeBuildDenyZones && settings.protectBuildDenyRegions) {
                Object wgPlugin = worldGuardInst.invoke(null);
                Object localPlayer = player == null ? null : wrapPlayer.invoke(wgPlugin, player);
                boolean canBuild = true;
                if (localPlayer != null) {
                    canBuild = safeTestBuildOnSet(applicable, localPlayer, buildFlag);
                }
                if (localPlayer != null && !canBuild) {
                    matched.addAll(applicableRegions);
                } else if (localPlayer == null) {
                    for (Object region : applicableRegions) {
                        if (stateDeny.equals(regionGetFlag.invoke(region, buildFlag))) {
                            matched.add(region);
                        }
                    }
                }
            }
        }
        List<RegionVolume> volumes = new ArrayList<>(matched.size());
        for (Object region : matched) {
            volumes.add(adapt(region));
        }
        return volumes;
    }

    private AdaptedRegionVolume adapt(Object region) throws ReflectiveOperationException {
        String id = (String) regionGetId.invoke(region);
        Object min = regionGetMinimumPoint.invoke(region);
        Object max = regionGetMaximumPoint.invoke(region);
        int minX = (int) blockVectorX.invoke(min);
        int minY = (int) blockVectorY.invoke(min);
        int minZ = (int) blockVectorZ.invoke(min);
        int maxX = (int) blockVectorX.invoke(max);
        int maxY = (int) blockVectorY.invoke(max);
        int maxZ = (int) blockVectorZ.invoke(max);
        RegionPointCheck containsCheck = (x, y, z) -> {
            try {
                Object vector = blockVectorAt.invoke(null, x, y, z);
                return (boolean) regionContains.invoke(region, vector);
            } catch (ReflectiveOperationException exception) {
                return false;
            }
        };
        return new AdaptedRegionVolume(id, minX, minY, minZ, maxX, maxY, maxZ, containsCheck);
    }

    private boolean hasDenyFlag(Object region) throws ReflectiveOperationException {
        Class<?> flagsClass = Class.forName("com.sk89q.worldguard.protection.flags.Flags");
        for (String configured : configuredFlagNames) {
            Object flag = resolveFlag(flagsClass, configured);
            if (flag == null) {
                continue;
            }
            if (stateDeny.equals(regionGetFlag.invoke(region, flag))) {
                return true;
            }
        }
        return false;
    }

    private Object resolveFlag(Class<?> flagsClass, String configured) throws ReflectiveOperationException {
        if (SOULBLAST_FLAG.equals(configured)) {
            try {
                return flagRegistry.getClass().getMethod("get", String.class).invoke(flagRegistry, SOULBLAST_FLAG);
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
        String fieldName = configured.toUpperCase(Locale.ROOT).replace('-', '_');
        try {
            return flagsClass.getField(fieldName).get(null);
        } catch (NoSuchFieldException ignored) {
            return null;
        }
    }

    private static List<Object> extractRegions(Object applicableSet) throws ReflectiveOperationException {
        List<Object> regions = new ArrayList<>();
        if (applicableSet == null) {
            return regions;
        }
        Object regionCollection = invokeNoArg(applicableSet, "getRegions");
        if (regionCollection == null) {
            regionCollection = invokeNoArg(applicableSet, "getApplicableRegions");
        }
        if (regionCollection != null) {
            appendRegions(regions, regionCollection);
            return regions;
        }
        if (applicableSet instanceof Iterable<?> iterable) {
            for (Object region : iterable) {
                regions.add(region);
            }
        }
        return regions;
    }

    private static List<Object> copyRegionValues(Map<?, ?> regions) {
        List<Object> values = new ArrayList<>();
        if (regions == null) {
            return values;
        }
        appendRegions(values, regions.values());
        return values;
    }

    private static void appendRegions(List<Object> target, Object source) {
        if (source instanceof Collection<?> collection) {
            for (Object region : collection) {
                target.add(region);
            }
            return;
        }
        if (source instanceof Iterable<?> iterable) {
            for (Object region : iterable) {
                target.add(region);
            }
        }
    }

    private static Object invokeNoArg(Object target, String methodName) throws ReflectiveOperationException {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }

    private static Set<String> lowered(List<String> values) {
        Set<String> set = new HashSet<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                set.add(value.toLowerCase(Locale.ROOT));
            }
        }
        return set;
    }

    private static boolean isExempt(String regionId, Set<String> exempt) {
        return exempt.contains(regionId.toLowerCase(Locale.ROOT));
    }

    private static Method findApplicableSetTestState(Class<?> stateFlagClass) throws ReflectiveOperationException {
        Class<?> applicableSetClass = Class.forName("com.sk89q.worldguard.protection.ApplicableRegionSet");
        Class<?> associableClass = Class.forName("com.sk89q.worldguard.protection.association.RegionAssociable");
        return applicableSetClass.getMethod("testState", associableClass, stateFlagClass.arrayType());
    }

    private boolean safeTestBuildOnSet(Object applicable, Object localPlayer, Object buildFlag) {
        try {
            return invokeApplicableTestState(applicable, localPlayer, buildFlag);
        } catch (ReflectiveOperationException | IllegalArgumentException exception) {
            if (!buildTestFailureLogged) {
                buildTestFailureLogged = true;
                plugin.getLogger().warning(
                        "WorldGuard BUILD test failed (treating as allowed): " + exception.getMessage()
                );
            }
            return true;
        }
    }

    private boolean invokeApplicableTestState(Object applicable, Object localPlayer, Object buildFlag)
            throws ReflectiveOperationException {
        try {
            return (boolean) applicableSetTestState.invoke(applicable, localPlayer, buildFlag);
        } catch (IllegalArgumentException exception) {
            Class<?> stateFlagClass = Class.forName("com.sk89q.worldguard.protection.flags.StateFlag");
            Object flags = java.lang.reflect.Array.newInstance(stateFlagClass, 1);
            java.lang.reflect.Array.set(flags, 0, buildFlag);
            return (boolean) applicableSetTestState.invoke(applicable, localPlayer, flags);
        }
    }

}
