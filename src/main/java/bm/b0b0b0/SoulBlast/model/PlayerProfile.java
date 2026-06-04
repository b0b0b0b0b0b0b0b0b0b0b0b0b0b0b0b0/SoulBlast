package bm.b0b0b0.SoulBlast.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerProfile {

    private final UUID uuid;
    private boolean autoIgnite;
    private String goalDynamiteId;
    private final Map<String, Integer> vanillaTntDeposits;

    public PlayerProfile(UUID uuid, boolean autoIgnite, String goalDynamiteId, Map<String, Integer> vanillaTntDeposits) {
        this.uuid = uuid;
        this.autoIgnite = autoIgnite;
        this.goalDynamiteId = goalDynamiteId;
        this.vanillaTntDeposits = new HashMap<>(vanillaTntDeposits);
    }

    public static PlayerProfile empty(UUID uuid, boolean defaultAutoIgnite) {
        return new PlayerProfile(uuid, defaultAutoIgnite, null, Map.of());
    }

    public UUID uuid() {
        return uuid;
    }

    public boolean autoIgnite() {
        return autoIgnite;
    }

    public void setAutoIgnite(boolean autoIgnite) {
        this.autoIgnite = autoIgnite;
    }

    public String goalDynamiteId() {
        return goalDynamiteId;
    }

    public void setGoalDynamiteId(String goalDynamiteId) {
        this.goalDynamiteId = goalDynamiteId;
    }

    public int depositedVanillaTnt(String dynamiteId) {
        if (dynamiteId == null) {
            return 0;
        }
        return vanillaTntDeposits.getOrDefault(dynamiteId.toLowerCase(), 0);
    }

    public void addVanillaTntDeposit(String dynamiteId, int amount) {
        if (dynamiteId == null || amount <= 0) {
            return;
        }
        String key = dynamiteId.toLowerCase();
        vanillaTntDeposits.put(key, depositedVanillaTnt(key) + amount);
    }

    public void resetVanillaTntDeposit(String dynamiteId) {
        if (dynamiteId == null) {
            return;
        }
        vanillaTntDeposits.remove(dynamiteId.toLowerCase());
    }

    public Map<String, Integer> vanillaTntDepositsCopy() {
        return Map.copyOf(vanillaTntDeposits);
    }

}
