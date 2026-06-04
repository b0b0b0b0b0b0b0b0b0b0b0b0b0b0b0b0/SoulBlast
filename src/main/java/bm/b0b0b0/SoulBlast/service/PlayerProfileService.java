package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.model.PlayerProfile;
import bm.b0b0b0.SoulBlast.repository.PlayerProfileRepository;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerProfileService {

    private final PlayerProfileRepository repository;
    private final boolean defaultAutoIgnite;
    private final ConcurrentHashMap<UUID, PlayerProfile> cache = new ConcurrentHashMap<>();

    public PlayerProfileService(PlayerProfileRepository repository, boolean defaultAutoIgnite) {
        this.repository = repository;
        this.defaultAutoIgnite = defaultAutoIgnite;
    }

    public CompletableFuture<PlayerProfile> preload(Player player) {
        UUID uuid = player.getUniqueId();
        if (cache.containsKey(uuid)) {
            return CompletableFuture.completedFuture(cache.get(uuid));
        }
        return repository.loadAsync(uuid, defaultAutoIgnite).thenApply(optional -> {
            PlayerProfile profile = optional.orElseGet(() -> PlayerProfile.empty(uuid, defaultAutoIgnite));
            cache.put(uuid, profile);
            return profile;
        });
    }

    public PlayerProfile profile(Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), uuid -> PlayerProfile.empty(uuid, defaultAutoIgnite));
    }

    public PlayerProfile profile(UUID uuid) {
        return cache.computeIfAbsent(uuid, id -> PlayerProfile.empty(id, defaultAutoIgnite));
    }

    public void persist(PlayerProfile profile) {
        cache.put(profile.uuid(), profile);
        repository.saveAsync(profile);
    }

    public void unload(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerProfile profile = cache.remove(uuid);
        if (profile != null) {
            repository.saveAsync(profile);
        }
    }

    public void shutdown() {
        for (Map.Entry<UUID, PlayerProfile> entry : cache.entrySet()) {
            repository.saveAsync(entry.getValue());
        }
        cache.clear();
    }

    public boolean resolvesAutoIgnite(Player player, boolean dynamiteDefault) {
        return profile(player).autoIgnite();
    }

}
