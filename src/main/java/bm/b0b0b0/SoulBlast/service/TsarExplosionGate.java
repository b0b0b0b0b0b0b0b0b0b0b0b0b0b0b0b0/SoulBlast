package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayDeque;
import java.util.function.Consumer;

public final class TsarExplosionGate {

    public static final int COOLDOWN_SECONDS = 30;
    public static final int MAX_PENDING = 8;

    private final JavaPlugin plugin;
    private final ArrayDeque<ExplosionJob> pending = new ArrayDeque<>();
    private final Object lock = new Object();
    private Consumer<ExplosionJob> launcher;
    private boolean active;
    private long cooldownUntilMs;

    public TsarExplosionGate(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void bindLauncher(Consumer<ExplosionJob> launcher) {
        this.launcher = launcher;
    }

    public boolean isActive() {
        synchronized (lock) {
            return active;
        }
    }

    public boolean hasPending() {
        synchronized (lock) {
            return !pending.isEmpty();
        }
    }

    public long remainingSeconds() {
        long remainingMs = cooldownUntilMs - System.currentTimeMillis();
        if (remainingMs <= 0) {
            return 0L;
        }
        return (remainingMs + 999L) / 1000L;
    }

    public boolean isServerBlocked() {
        return remainingSeconds() > 0 || isActive();
    }

    public void armCooldown() {
        synchronized (lock) {
            cooldownUntilMs = System.currentTimeMillis() + COOLDOWN_SECONDS * 1000L;
        }
    }

    public enum AdmitOutcome {
        STARTED,
        QUEUED,
        REJECTED_FULL,
        NO_LAUNCHER
    }

    public AdmitOutcome admit(ExplosionJob job) {
        if (launcher == null) {
            return AdmitOutcome.NO_LAUNCHER;
        }
        synchronized (lock) {
            if (active || !pending.isEmpty() || remainingSeconds() > 0) {
                if (pending.size() >= MAX_PENDING) {
                    return AdmitOutcome.REJECTED_FULL;
                }
                pending.offerLast(job);
                return AdmitOutcome.QUEUED;
            }
            startLocked(job);
            return AdmitOutcome.STARTED;
        }
    }

    public void onExplosionFinished() {
        synchronized (lock) {
            active = false;
            armCooldownLocked();
            scheduleDrainLocked();
        }
    }

    private void startLocked(ExplosionJob job) {
        active = true;
        launcher.accept(job);
    }

    private void armCooldownLocked() {
        cooldownUntilMs = System.currentTimeMillis() + COOLDOWN_SECONDS * 1000L;
    }

    private void scheduleDrainLocked() {
        long waitTicks = Math.max(1L, (remainingSeconds() * 20L));
        plugin.getServer().getScheduler().runTaskLater(plugin, this::drain, waitTicks);
    }

    private void drain() {
        synchronized (lock) {
            if (active) {
                return;
            }
            if (remainingSeconds() > 0) {
                scheduleDrainLocked();
                return;
            }
            ExplosionJob next = pending.pollFirst();
            if (next == null) {
                return;
            }
            startLocked(next);
        }
    }

    public void shutdown() {
        synchronized (lock) {
            pending.clear();
            active = false;
        }
    }

}
