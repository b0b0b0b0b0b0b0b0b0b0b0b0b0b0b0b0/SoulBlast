package bm.b0b0b0.SoulBlast.decay.repository;

import bm.b0b0b0.SoulBlast.decay.model.DecayingBlockKey;
import bm.b0b0b0.SoulBlast.decay.model.DecayingBlockState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class DecayingBlockStore {

    private final ConcurrentHashMap<DecayingBlockKey, DecayingBlockState> active = new ConcurrentHashMap<>();

    public Optional<DecayingBlockState> find(DecayingBlockKey key) {
        return Optional.ofNullable(active.get(key));
    }

    public DecayingBlockState put(DecayingBlockState state) {
        return active.put(state.key(), state);
    }

    public DecayingBlockState remove(DecayingBlockKey key) {
        return active.remove(key);
    }

    public int size() {
        return active.size();
    }

    public boolean isEmpty() {
        return active.isEmpty();
    }

    public Collection<DecayingBlockState> values() {
        return active.values();
    }

    public ArrayList<DecayingBlockState> snapshot() {
        return new ArrayList<>(active.values());
    }

    public void clear() {
        active.clear();
    }

}
