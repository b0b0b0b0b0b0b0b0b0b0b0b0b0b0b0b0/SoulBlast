package bm.b0b0b0.SoulBlast.ps.repository;

import bm.b0b0b0.SoulBlast.ps.model.PsBlockKey;
import bm.b0b0b0.SoulBlast.ps.model.PsBlockState;
import org.bukkit.World;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class PsBlockStore {

    private final ConcurrentHashMap<PsBlockKey, PsBlockState> states = new ConcurrentHashMap<>();

    public Optional<PsBlockState> find(PsBlockKey key) {
        return Optional.ofNullable(states.get(key));
    }

    public Optional<PsBlockState> findAtCoordinates(int x, int y, int z) {
        for (PsBlockState state : states.values()) {
            if (state.key().x() == x && state.key().y() == y && state.key().z() == z) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }

    public PsBlockState alignWorld(PsBlockState state, World world) {
        if (world == null || state.key().worldId().equals(world.getUID())) {
            return state;
        }
        states.remove(state.key());
        PsBlockState aligned = new PsBlockState(
                PsBlockKey.of(world, state.key().x(), state.key().y(), state.key().z()),
                state.typeAlias(),
                state.durability(),
                state.maximum(),
                state.ownerName(),
                state.ownerPrefix(),
                state.ownerSuffix(),
                state.radiusX(),
                state.radiusY(),
                state.radiusZ()
        );
        states.put(aligned.key(), aligned);
        return aligned;
    }

    public void put(PsBlockState state) {
        states.put(state.key(), state);
    }

    public PsBlockState remove(PsBlockKey key) {
        return states.remove(key);
    }

    public Collection<PsBlockState> all() {
        return states.values();
    }

    public void clear() {
        states.clear();
    }

    public int size() {
        return states.size();
    }

}
