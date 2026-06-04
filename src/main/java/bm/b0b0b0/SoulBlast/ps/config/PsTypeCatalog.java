package bm.b0b0b0.SoulBlast.ps.config;

import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PsTypeCatalog {

    private PsTypeCatalog() {
    }

    public static Map<String, PsProtectionTypeDefinition> defaults() {
        return new LinkedHashMap<>();
    }

    public static PsProtectionTypeDefinition template(String alias, Material material) {
        String materialLabel = material == null ? "?" : material.name();
        PsProtectionTypeDefinition type = new PsProtectionTypeDefinition();
        type.hologram.enabled = true;
        type.hologram.offsetY = 2.0;
        type.hologram.lines = List.of(
                "&bБлок привата &7(&f" + alias + "&7)",
                "&fВладелец: &b%owner_name%",
                "&fПрочность: &b%durability%&7/&b%durability_maximum%",
                "&fРадиус&8: &b%radius_x%&7x&b%radius_y%&7x&b%radius_z%",
                "&8" + materialLabel
        );
        type.lightningStrike.create = true;
        type.lightningStrike.remove = true;
        type.sound.create = "BLOCK_BEACON_ACTIVATE";
        type.sound.remove = "BLOCK_BEACON_DEACTIVATE";
        type.durability.enabled = true;
        type.durability.maximum = 3;
        type.breakProtectionBlock.withExplosion.enabled = true;
        type.breakProtectionBlock.withExplosion.onlyDynamiteTypes.put("crying_souls", 1);
        type.breakProtectionBlock.withExplosion.onlyDynamiteTypes.put("void_crescent", 1);
        type.breakProtectionBlock.withExplosion.onlyDynamiteTypes.put("soul_supernova", 1);
        type.breakProtectionBlock.withWither.enabled = true;
        type.itemGlow.enabled = true;
        type.allowSoulblastDynamiteInForeignClaims = true;
        return type;
    }

}
