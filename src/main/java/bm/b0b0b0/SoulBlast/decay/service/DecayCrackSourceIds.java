package bm.b0b0b0.SoulBlast.decay.service;

import java.util.concurrent.atomic.AtomicInteger;

public final class DecayCrackSourceIds {

    private static final int MIN_ID = 8_000_000;
    private static final int MAX_ID = 90_000_000;
    private static final AtomicInteger NEXT = new AtomicInteger(MIN_ID);

    private DecayCrackSourceIds() {
    }

    public static int allocate() {
        int value = NEXT.incrementAndGet();
        if (value > MAX_ID) {
            NEXT.set(MIN_ID);
            return MIN_ID;
        }
        return value;
    }

}
