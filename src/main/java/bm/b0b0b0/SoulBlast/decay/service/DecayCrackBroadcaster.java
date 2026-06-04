package bm.b0b0b0.SoulBlast.decay.service;

import bm.b0b0b0.SoulBlast.decay.config.DecayGeneralSettings;
import bm.b0b0b0.SoulBlast.decay.model.DecayingBlockState;
import bm.b0b0b0.SoulBlast.decay.repository.DecayingBlockStore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class DecayCrackBroadcaster {

    private final DecayingBlockStore store;
    private DecayGeneralSettings settings;

    public DecayCrackBroadcaster(DecayingBlockStore store) {
        this.store = store;
    }

    public void reload(DecayGeneralSettings settings) {
        this.settings = settings;
    }

    public void broadcastState(DecayingBlockState state) {
        broadcastState(state, false);
    }

    public void broadcastState(DecayingBlockState state, boolean allowRegression) {
        if (settings == null || state.damage() <= 0.0f) {
            return;
        }
        World world = worldOf(state);
        if (world == null) {
            return;
        }
        Block block = blockOf(world, state);
        if (block == null) {
            return;
        }
        sendToViewers(world, blockCenter(block), state, allowRegression);
        state.clearCrackDirty();
    }

    public void clearBlock(DecayingBlockState state) {
        if (settings == null) {
            return;
        }
        World world = worldOf(state);
        if (world == null) {
            return;
        }
        Block block = blockOf(world, state);
        if (block == null) {
            return;
        }
        sendProgress(world, blockCenter(block), state, 0.0f, true);
        state.resetClientProgress();
        state.clearCrackDirty();
    }

    public int broadcastTick() {
        if (settings == null || store.isEmpty()) {
            return 0;
        }
        int budget = settings.damagePacketsPerTick;
        int sent = 0;
        for (DecayingBlockState state : store.snapshot()) {
            if (budget <= 0) {
                break;
            }
            if (state.damage() <= 0.0f || !state.isCrackDirty()) {
                continue;
            }
            World world = worldOf(state);
            if (world == null) {
                store.remove(state.key());
                continue;
            }
            if (!world.isChunkLoaded(state.key().x() >> 4, state.key().z() >> 4)) {
                continue;
            }
            Block block = blockOf(world, state);
            if (block == null) {
                store.remove(state.key());
                continue;
            }
            int viewers = sendToViewers(world, blockCenter(block), state, false);
            if (viewers > 0) {
                sent++;
                budget--;
            }
            state.clearCrackDirty();
        }
        return sent;
    }

    public void refreshAll() {
        for (DecayingBlockState state : store.values()) {
            if (state.damage() > 0.0f) {
                state.markCrackDirty();
            }
        }
    }

    public void clearAllViewers() {
        if (settings == null) {
            return;
        }
        for (DecayingBlockState state : store.snapshot()) {
            clearBlock(state);
        }
    }

    private World worldOf(DecayingBlockState state) {
        return Bukkit.getWorld(state.key().worldId());
    }

    private Block blockOf(World world, DecayingBlockState state) {
        Block block = world.getBlockAt(state.key().x(), state.key().y(), state.key().z());
        if (block.getType() != state.material()) {
            return null;
        }
        return block;
    }

    private int sendToViewers(World world, Location center, DecayingBlockState state, boolean allowRegression) {
        float progress = DecayCrackProgress.toClientProgress(state.damage());
        return sendProgress(world, center, state, progress, allowRegression);
    }

    private int sendProgress(
            World world,
            Location center,
            DecayingBlockState state,
            float progress,
            boolean allowRegression
    ) {
        if (progress <= 0.0f) {
            return clearProgressForViewers(world, center, state);
        }
        if (!allowRegression && progress + 0.001f < state.lastSentClientProgress()) {
            return 0;
        }
        int sourceId = state.crackSourceId();
        double radiusSq = (double) settings.viewerRadius * settings.viewerRadius;
        int sent = 0;
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distanceSquared(center) > radiusSq) {
                continue;
            }
            player.sendBlockDamage(center, progress, sourceId);
            sent++;
        }
        state.recordSentClientProgress(progress);
        return sent;
    }

    private int clearProgressForViewers(World world, Location center, DecayingBlockState state) {
        int sourceId = state.crackSourceId();
        double radiusSq = (double) settings.viewerRadius * settings.viewerRadius;
        int sent = 0;
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distanceSquared(center) > radiusSq) {
                continue;
            }
            player.sendBlockDamage(center, 0.0f, sourceId);
            sent++;
        }
        return sent;
    }

    private static Location blockCenter(Block block) {
        return block.getLocation().add(0.5, 0.5, 0.5);
    }

}
