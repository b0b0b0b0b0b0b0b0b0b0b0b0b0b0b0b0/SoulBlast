package bm.b0b0b0.SoulBlast.decay.service;

public final class DecayCrackProgress {

    private DecayCrackProgress() {
    }

    public static float toClientProgress(float damage) {
        return Math.clamp(damage, 0.0f, 1.0f);
    }

}
