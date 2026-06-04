package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.FuseLightningSettings;
import bm.b0b0b0.SoulBlast.model.PrimedDynamiteSession;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class FuseLightningService {

    private final JavaPlugin plugin;

    public FuseLightningService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void tickPrimed(PrimedDynamiteSession session, TNTPrimed primed, FuseLightningSettings settings) {
        if (!settings.enabled || session.isFuseLightningTriggered()) {
            return;
        }
        if (primed.getFuseTicks() > settings.ticksBeforeEnd) {
            return;
        }
        session.setFuseLightningTriggered(true);
        session.setFuseLightningTasks(scheduleBarrage(session.getEntityId(), primed.getLocation().clone(), settings));
    }

    public void cancelSession(PrimedDynamiteSession session) {
        for (BukkitTask task : session.getFuseLightningTasks()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        session.getFuseLightningTasks().clear();
    }

    private List<BukkitTask> scheduleBarrage(UUID entityId, Location origin, FuseLightningSettings settings) {
        int bolts = Math.max(1, settings.boltCount);
        int interval = Math.max(1, settings.boltIntervalTicks);
        List<BukkitTask> tasks = new ArrayList<>(bolts);
        for (int index = 0; index < bolts; index++) {
            int delay = index * interval;
            BukkitTask task = plugin.getServer().getScheduler().runTaskLater(
                    plugin,
                    () -> strikeIfPrimedAlive(entityId, origin, settings),
                    delay
            );
            tasks.add(task);
        }
        return tasks;
    }

    private void strikeIfPrimedAlive(UUID entityId, Location origin, FuseLightningSettings settings) {
        TNTPrimed primed = resolvePrimed(entityId);
        Location center = primed != null ? primed.getLocation() : origin;
        strikeAt(center, settings);
    }

    private TNTPrimed resolvePrimed(UUID entityId) {
        var entity = plugin.getServer().getEntity(entityId);
        if (entity instanceof TNTPrimed primed && !primed.isDead()) {
            return primed;
        }
        return null;
    }

    private void strikeAt(Location center, FuseLightningSettings settings) {
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double spread = Math.max(0.5, settings.spreadRadius);
        double offsetX = (random.nextDouble() * 2.0 - 1.0) * spread;
        double offsetZ = (random.nextDouble() * 2.0 - 1.0) * spread;
        int blockX = center.getBlockX() + (int) Math.round(offsetX);
        int blockZ = center.getBlockZ() + (int) Math.round(offsetZ);
        int surfaceY = world.getHighestBlockYAt(blockX, blockZ, HeightMap.MOTION_BLOCKING);
        Location strike = new Location(world, blockX + 0.5, surfaceY + 1.0, blockZ + 0.5);
        if (settings.realLightning) {
            world.strikeLightning(strike);
        } else {
            world.strikeLightningEffect(strike);
        }
    }

}
