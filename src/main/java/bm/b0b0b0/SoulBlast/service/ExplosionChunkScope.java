package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.CraterFillSettings;
import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public final class ExplosionChunkScope {

    public void pin(Location center, DynamiteDefinition dynamite) {
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        float radius = resolveRadius(dynamite);
        int centerChunkX = center.getBlockX() >> 4;
        int centerChunkZ = center.getBlockZ() >> 4;
        int chunkRadius = (int) Math.ceil(radius / 16.0) + 1;
        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                Chunk chunk = world.getChunkAt(centerChunkX + dx, centerChunkZ + dz);
                chunk.load(true);
            }
        }
    }

    private static float resolveRadius(DynamiteDefinition dynamite) {
        float radius = dynamite.explosion.radius;
        if (dynamite.explosion.effects.liquidRadius > 0.0f) {
            radius = Math.max(radius, dynamite.explosion.effects.liquidRadius);
        }
        CraterFillSettings fill = dynamite.explosion.effects.craterFill;
        if (fill.enabled) {
            radius = Math.max(radius, fill.radius + fill.magmaShellWidth);
        }
        if (TsarBombRules.isTsar(dynamite)) {
            radius *= 1.08f;
        }
        return radius;
    }

}
