package bm.b0b0b0.SoulBlast.ps.integration;

import bm.b0b0b0.SoulBlast.integration.PluginIntegrationsReporter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ProtectionStonesBridge {

    private final boolean available;
    private final Method regionFromLocation;
    private final Method regionFromLocationGroup;
    private final Method regionGetType;
    private final Method regionGetTypeOptions;
    private final Method regionGetOwners;
    private final Method regionGetProtectBlock;
    private final Method regionDelete;
    private final Method psGetInstance;
    private final Method psGetConfiguredBlocks;
    private final Method psIsProtectBlock;
    private final Method blockUtilGetProtectBlockType;
    private final Class<?> protectBlockClass;

    private ProtectionStonesBridge(
            boolean available,
            Method regionFromLocation,
            Method regionFromLocationGroup,
            Method regionGetType,
            Method regionGetTypeOptions,
            Method regionGetOwners,
            Method regionGetProtectBlock,
            Method regionDelete,
            Method psGetInstance,
            Method psGetConfiguredBlocks,
            Method psIsProtectBlock,
            Method blockUtilGetProtectBlockType,
            Class<?> protectBlockClass
    ) {
        this.available = available;
        this.regionFromLocation = regionFromLocation;
        this.regionFromLocationGroup = regionFromLocationGroup;
        this.regionGetType = regionGetType;
        this.regionGetTypeOptions = regionGetTypeOptions;
        this.regionGetOwners = regionGetOwners;
        this.regionGetProtectBlock = regionGetProtectBlock;
        this.regionDelete = regionDelete;
        this.psGetInstance = psGetInstance;
        this.psGetConfiguredBlocks = psGetConfiguredBlocks;
        this.psIsProtectBlock = psIsProtectBlock;
        this.blockUtilGetProtectBlockType = blockUtilGetProtectBlockType;
        this.protectBlockClass = protectBlockClass;
    }

    public static ProtectionStonesBridge tryCreate(JavaPlugin plugin) {
        if (!PluginIntegrationsReporter.isPluginActive(plugin, "ProtectionStones")) {
            return disabled();
        }
        try {
            Class<?> regionClass = Class.forName("dev.espi.protectionstones.PSRegion");
            Class<?> psClass = Class.forName("dev.espi.protectionstones.ProtectionStones");
            Class<?> protectBlockClass = Class.forName("dev.espi.protectionstones.PSProtectBlock");
            Class<?> blockUtilClass = Class.forName("dev.espi.protectionstones.utils.BlockUtil");
            return new ProtectionStonesBridge(
                    true,
                    regionClass.getMethod("fromLocation", Location.class),
                    regionClass.getMethod("fromLocationGroup", Location.class),
                    regionGetTypeMethod(regionClass),
                    regionClass.getMethod("getTypeOptions"),
                    regionClass.getMethod("getOwners"),
                    regionClass.getMethod("getProtectBlock"),
                    regionClass.getMethod("deleteRegion", boolean.class),
                    psClass.getMethod("getInstance"),
                    psClass.getMethod("getConfiguredBlocks"),
                    psClass.getMethod("isProtectBlock", Block.class),
                    blockUtilClass.getMethod("getProtectBlockType", Block.class),
                    protectBlockClass
            );
        } catch (Throwable failure) {
            return disabled();
        }
    }

    private static Method regionGetTypeMethod(Class<?> regionClass) throws NoSuchMethodException {
        try {
            return regionClass.getMethod("getType");
        } catch (NoSuchMethodException exception) {
            return regionClass.getMethod("getTypeOptions");
        }
    }

    public static ProtectionStonesBridge disabled() {
        return new ProtectionStonesBridge(
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public boolean available() {
        return available;
    }

    public List<PsConfiguredBlockInfo> listConfiguredBlocks() {
        if (!available) {
            return Collections.emptyList();
        }
        try {
            Object instance = psGetInstance.invoke(null);
            if (instance == null) {
                return Collections.emptyList();
            }
            Object configured = psGetConfiguredBlocks.invoke(instance);
            if (!(configured instanceof Collection<?> blocks)) {
                return Collections.emptyList();
            }
            List<PsConfiguredBlockInfo> result = new ArrayList<>();
            for (Object blockType : blocks) {
                if (!protectBlockClass.isInstance(blockType)) {
                    continue;
                }
                String alias = readStringField(blockType, "alias");
                if (alias == null || alias.isBlank()) {
                    continue;
                }
                Material material = readMaterialField(blockType, "type");
                result.add(new PsConfiguredBlockInfo(alias, material));
            }
            return List.copyOf(result);
        } catch (ReflectiveOperationException exception) {
            return Collections.emptyList();
        }
    }

    public Optional<String> aliasForMaterial(Material material) {
        if (!available || material == null) {
            return Optional.empty();
        }
        try {
            Object instance = psGetInstance.invoke(null);
            if (instance == null) {
                return Optional.empty();
            }
            Object configured = psGetConfiguredBlocks.invoke(instance);
            if (!(configured instanceof Collection<?> blocks)) {
                return Optional.empty();
            }
            for (Object blockType : blocks) {
                if (!protectBlockClass.isInstance(blockType)) {
                    continue;
                }
                Material configuredMaterial = readMaterialField(blockType, "type");
                if (configuredMaterial == material) {
                    return Optional.ofNullable(readStringField(blockType, "alias"));
                }
            }
        } catch (ReflectiveOperationException exception) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    public Optional<PsRegionSnapshot> snapshotAt(Location location) {
        if (!available || location == null || location.getWorld() == null) {
            return Optional.empty();
        }
        try {
            Object region = regionFromLocation.invoke(null, location);
            if (region == null && regionFromLocationGroup != null) {
                region = regionFromLocationGroup.invoke(null, location);
            }
            return snapshotFromRegion(region);
        } catch (ReflectiveOperationException exception) {
            return Optional.empty();
        }
    }

    public boolean isProtectBlock(Block block) {
        if (!available || block == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(psIsProtectBlock.invoke(null, block));
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }

    public Optional<Block> protectBlockAt(Location location) {
        if (!available || location == null || location.getWorld() == null) {
            return Optional.empty();
        }
        try {
            Object region = regionFromLocation.invoke(null, location);
            if (region == null && regionFromLocationGroup != null) {
                region = regionFromLocationGroup.invoke(null, location);
            }
            if (region == null) {
                return Optional.empty();
            }
            Block protectBlock = protectBlockFromRegionInternal(region);
            if (protectBlock == null || !isProtectBlock(protectBlock)) {
                return Optional.empty();
            }
            return Optional.of(protectBlock);
        } catch (ReflectiveOperationException exception) {
            return Optional.empty();
        }
    }

    public boolean explosionCenterAffectsProtectBlock(Location center, Block protectBlock) {
        if (!available || center == null || center.getWorld() == null || protectBlock == null) {
            return false;
        }
        if (!protectBlock.getWorld().equals(center.getWorld())) {
            return false;
        }
        try {
            Object region = regionFromLocation.invoke(null, center);
            if (region == null && regionFromLocationGroup != null) {
                region = regionFromLocationGroup.invoke(null, center);
            }
            if (region == null) {
                return false;
            }
            Block linked = protectBlockFromRegionInternal(region);
            return linked != null
                    && linked.getX() == protectBlock.getX()
                    && linked.getY() == protectBlock.getY()
                    && linked.getZ() == protectBlock.getZ();
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }

    public Block protectBlockFromRegion(Object region) {
        return protectBlockFromRegionInternal(region);
    }

    public boolean overlapsForeignRegion(Location location, UUID placerId) {
        if (!available || location == null) {
            return false;
        }
        try {
            Object region = regionFromLocation.invoke(null, location);
            if (region == null) {
                return false;
            }
            UUID ownerId = resolveOwner(region);
            if (ownerId == null) {
                return true;
            }
            return placerId == null || !ownerId.equals(placerId);
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }

    public boolean deleteRegion(Block block) {
        if (!available || block == null) {
            return false;
        }
        try {
            Object region = regionFromLocation.invoke(null, block.getLocation());
            if (region == null) {
                return false;
            }
            regionDelete.invoke(region, false);
            return true;
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }

    public Object regionFromEvent(Object event) {
        if (!available || event == null) {
            return null;
        }
        try {
            Method getRegion = event.getClass().getMethod("getRegion");
            return getRegion.invoke(event);
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }

    public Optional<PsRegionSnapshot> snapshotFromRegion(Object region) {
        if (!available || region == null) {
            return Optional.empty();
        }
        try {
            String alias = resolveAlias(region);
            Block protectBlock = protectBlockFromRegionInternal(region);
            if (protectBlock != null) {
                alias = aliasForProtectionBlock(protectBlock).orElse(alias);
            }
            alias = canonicalAlias(alias);
            if (alias == null || alias.isBlank()) {
                return Optional.empty();
            }
            Object typeOptions = regionGetTypeOptions.invoke(region);
            UUID ownerId = resolveOwner(region);
            int radiusX = readRadius(typeOptions, "xRadius");
            int radiusY = readRadius(typeOptions, "yRadius");
            int radiusZ = readRadius(typeOptions, "zRadius");
            return Optional.of(new PsRegionSnapshot(alias, ownerId, "?", radiusX, radiusY, radiusZ));
        } catch (ReflectiveOperationException exception) {
            return Optional.empty();
        }
    }

    public Optional<String> aliasForProtectionBlock(Block block) {
        if (!available || block == null) {
            return Optional.empty();
        }
        try {
            if (!(Boolean) psIsProtectBlock.invoke(null, block)) {
                return Optional.empty();
            }
            String alias = (String) blockUtilGetProtectBlockType.invoke(null, block);
            if (alias != null && !alias.isBlank()) {
                return Optional.of(alias);
            }
        } catch (ReflectiveOperationException exception) {
            return Optional.empty();
        }
        return aliasForMaterial(block.getType());
    }

    public String canonicalAlias(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        for (PsConfiguredBlockInfo block : listConfiguredBlocks()) {
            if (raw.equals(block.alias())) {
                return raw;
            }
        }
        Material material = materialFromName(raw);
        if (material != null) {
            for (PsConfiguredBlockInfo block : listConfiguredBlocks()) {
                if (material.equals(block.material())) {
                    return block.alias();
                }
            }
        }
        return raw;
    }

    private String resolveAlias(Object region) throws ReflectiveOperationException {
        Object typeResult = regionGetType.invoke(region);
        if (typeResult instanceof String alias) {
            return alias;
        }
        if (typeResult instanceof Material material) {
            return material.name();
        }
        if (protectBlockClass.isInstance(typeResult)) {
            String alias = readStringField(typeResult, "alias");
            if (alias != null && !alias.isBlank()) {
                return alias;
            }
            Material material = readMaterialField(typeResult, "type");
            if (material != null) {
                return material.name();
            }
        }
        Object typeOptions = regionGetTypeOptions.invoke(region);
        if (protectBlockClass.isInstance(typeOptions)) {
            String alias = readStringField(typeOptions, "alias");
            if (alias != null && !alias.isBlank()) {
                return alias;
            }
        }
        return null;
    }

    private Block protectBlockFromRegionInternal(Object region) {
        if (!available || region == null) {
            return null;
        }
        try {
            Object block = regionGetProtectBlock.invoke(region);
            if (block instanceof Block resolved) {
                return resolved;
            }
        } catch (ReflectiveOperationException exception) {
            return null;
        }
        return null;
    }

    private static Material materialFromName(String name) {
        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private UUID resolveOwner(Object region) throws ReflectiveOperationException {
        Object owners = regionGetOwners.invoke(region);
        if (!(owners instanceof Collection<?> collection) || collection.isEmpty()) {
            return null;
        }
        Object first = collection.iterator().next();
        if (first instanceof UUID uuid) {
            return uuid;
        }
        return null;
    }

    private int readRadius(Object typeOptions, String fieldName) {
        if (typeOptions == null) {
            return 0;
        }
        return toInt(readField(typeOptions, fieldName));
    }

    private static String readStringField(Object target, String fieldName) {
        Object value = readField(target, fieldName);
        return value == null ? null : value.toString();
    }

    private static Material readMaterialField(Object target, String fieldName) {
        Object value = readField(target, fieldName);
        if (value instanceof Material material) {
            return material;
        }
        if (value instanceof String name) {
            try {
                return Material.valueOf(name);
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }
        return null;
    }

    private static Object readField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getField(fieldName);
            return field.get(target);
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }

    private static int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

}
