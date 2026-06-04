package bm.b0b0b0.SoulBlast.model;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.List;

public final class ExplosionRaySamplingProgress {

    public boolean active;
    public int totalRays;
    public int nextRay;
    public List<ExplosionWaveCell> waveCells = List.of();
    public int waveIndex;
    public boolean rayOverlayActive;
    public final LongOpenHashSet rayBoosted = new LongOpenHashSet();
    public double ox;
    public double oy;
    public double oz;
    public double x;
    public double y;
    public double z;
    public float power;
    public int steps;
    public int maxSteps;
    public final LongOpenHashSet seen = new LongOpenHashSet();

    public void reset() {
        active = false;
        totalRays = 0;
        nextRay = 0;
        ox = 0;
        oy = 0;
        oz = 0;
        x = 0;
        y = 0;
        z = 0;
        power = 0;
        steps = 0;
        maxSteps = 0;
        waveCells = List.of();
        waveIndex = 0;
        rayOverlayActive = false;
        rayBoosted.clear();
        seen.clear();
    }

    public void setWaveCells(List<ExplosionWaveCell> cells) {
        waveCells = cells == null ? List.of() : List.copyOf(cells);
        waveIndex = 0;
    }

}
