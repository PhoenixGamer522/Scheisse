package com.extrahelden.duelmod.combat;

/**
 * Simple tick-based combat timer.
 */
public class CombatTimer {
    private int ticks;

    public CombatTimer(int ticks) {
        this.ticks = ticks;
    }

    /**
     * Add ticks to this timer.
     *
     * @param extraTicks ticks to add
     */
    public void addTicks(int extraTicks) {
        this.ticks += extraTicks;
    }

    /**
     * Decrement the timer by one tick.
     *
     * @return {@code true} if timer is still active after ticking
     */
    public boolean tick() {
        if (ticks > 0) {
            ticks--;
        }
        return ticks > 0;
    }

    public boolean isActive() {
        return ticks > 0;
    }
}

