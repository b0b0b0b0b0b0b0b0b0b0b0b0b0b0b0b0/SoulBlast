package bm.b0b0b0.SoulBlast.ps.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PsHologramHideSession {

    public enum Mode {
        HIDE,
        SHOW,
        TOGGLE
    }

    private final Map<UUID, ArmRequest> armed = new ConcurrentHashMap<>();

    public void arm(UUID playerId, Mode mode, int timeoutSeconds) {
        if (playerId == null || mode == null) {
            return;
        }
        int seconds = Math.max(5, timeoutSeconds);
        armed.put(playerId, new ArmRequest(mode, System.currentTimeMillis() + seconds * 1000L));
    }

    public void disarm(UUID playerId) {
        if (playerId != null) {
            armed.remove(playerId);
        }
    }

    public boolean isArmed(UUID playerId) {
        ArmRequest request = armed.get(playerId);
        if (request == null) {
            return false;
        }
        if (System.currentTimeMillis() > request.expiresAtMillis()) {
            armed.remove(playerId);
            return false;
        }
        return true;
    }

    public void clearAll() {
        armed.clear();
    }

    public Optional<Mode> consume(UUID playerId) {
        ArmRequest request = armed.get(playerId);
        if (request == null) {
            return Optional.empty();
        }
        armed.remove(playerId);
        if (System.currentTimeMillis() > request.expiresAtMillis()) {
            return Optional.empty();
        }
        return Optional.of(request.mode());
    }

    private record ArmRequest(Mode mode, long expiresAtMillis) {
    }

}
