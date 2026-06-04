package bm.b0b0b0.SoulBlast.repository;

import bm.b0b0b0.SoulBlast.model.PlayerProfile;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerProfileRepository {

    void initialize();

    void shutdown();

    CompletableFuture<Optional<PlayerProfile>> loadAsync(UUID uuid, boolean defaultAutoIgnite);

    void saveAsync(PlayerProfile profile);

}
