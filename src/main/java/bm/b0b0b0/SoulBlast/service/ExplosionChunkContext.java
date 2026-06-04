package bm.b0b0b0.SoulBlast.service;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class ExplosionChunkContext {

    private Chunk chunk;
    private int chunkX = Integer.MIN_VALUE;
    private int chunkZ = Integer.MIN_VALUE;

    public Block blockAt(World world, int x, int y, int z) {
        int cx = x >> 4;
        int cz = z >> 4;
        if (chunk == null || chunkX != cx || chunkZ != cz) {
            chunk = world.getChunkAt(cx, cz);
            chunkX = cx;
            chunkZ = cz;
        }
        return chunk.getBlock(x & 15, y, z & 15);
    }

    public void reset() {
        chunk = null;
        chunkX = Integer.MIN_VALUE;
        chunkZ = Integer.MIN_VALUE;
    }

}
