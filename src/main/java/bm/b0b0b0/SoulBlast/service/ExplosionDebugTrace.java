package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.SoulBlast;
import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.model.ExplosionBlockAction;
import bm.b0b0b0.SoulBlast.model.ExplosionJob;
import bm.b0b0b0.SoulBlast.model.ExplosionJobDiagnostics;
import bm.b0b0b0.SoulBlast.model.ExplosionJobPhase;
import org.bukkit.Location;

public final class ExplosionDebugTrace {

    private final SoulBlast plugin;
    private boolean enabled;

    public ExplosionDebugTrace(SoulBlast plugin) {
        this.plugin = plugin;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean enabled() {
        return enabled;
    }

    public void warn(String message) {
        if (!enabled) {
            return;
        }
        plugin.getLogger().warning("[SoulBlast:explosion] " + message);
    }

    public void enqueue(Location center, DynamiteDefinition dynamite, TsarExplosionGate.AdmitOutcome outcome) {
        if (!enabled) {
            return;
        }
        plugin.getLogger().info("[SoulBlast:explosion] enqueue id=" + dynamite.id
                + " gate=" + outcome
                + " center=" + format(center));
        if (outcome == TsarExplosionGate.AdmitOutcome.QUEUED) {
            warn("last_pyre queued by gate");
        }
        if (outcome == TsarExplosionGate.AdmitOutcome.REJECTED_FULL) {
            warn("last_pyre rejected, gate queue full");
        }
        if (outcome == TsarExplosionGate.AdmitOutcome.NO_LAUNCHER) {
            warn("last_pyre no launcher");
        }
    }

    public void phase(ExplosionJob job, ExplosionJobPhase from, ExplosionJobPhase to, int pending) {
        if (!enabled) {
            return;
        }
        plugin.getLogger().info("[SoulBlast:explosion] phase " + from + "->" + to
                + " id=" + job.getDynamite().id
                + " pending=" + pending
                + " center=" + format(job.getCenter()));
    }

    public void maskPlanned(ExplosionJob job, int planned, int reach, int maxOps, int columns) {
        if (!enabled) {
            return;
        }
        ExplosionJobDiagnostics diagnostics = job.getDiagnostics();
        String line = "mask-planned id=" + job.getDynamite().id
                + " planned=" + diagnostics.plannedMask()
                + " reach=" + reach
                + " cap=" + maxOps
                + " columns=" + columns
                + " center=" + format(job.getCenter());
        plugin.getLogger().info("[SoulBlast:explosion] " + line);
        if (diagnostics.plannedMask() <= 0) {
            warn("mask empty " + line);
        }
    }

    public void complete(ExplosionJob job) {
        if (!enabled) {
            return;
        }
        ExplosionJobDiagnostics diagnostics = job.getDiagnostics();
        DynamiteDefinition dynamite = job.getDynamite();
        String summary = "complete id=" + dynamite.id
                + " phase=" + job.getPhase()
                + " plannedBreak=" + diagnostics.plannedBreak()
                + " plannedMask=" + diagnostics.plannedMask()
                + " applied=" + diagnostics.applied()
                + " replace=" + diagnostics.applied(ExplosionBlockAction.REPLACE)
                + " break=" + diagnostics.applied(ExplosionBlockAction.BREAK)
                + " regionBlocked=" + diagnostics.regionBlocked()
                + " skipped=" + diagnostics.skippedOther()
                + " materials=[" + diagnostics.maskMaterialSummary() + "]"
                + " center=" + format(job.getCenter());
        if (TsarBombRules.isTsar(dynamite) && diagnostics.applied(ExplosionBlockAction.REPLACE) <= 0) {
            warn("mask not applied " + summary);
            return;
        }
        plugin.getLogger().info("[SoulBlast:explosion] " + summary);
    }

    private static String format(Location center) {
        if (center == null || center.getWorld() == null) {
            return "null";
        }
        return center.getWorld().getName() + '@'
                + center.getBlockX() + ',' + center.getBlockY() + ',' + center.getBlockZ();
    }

}
