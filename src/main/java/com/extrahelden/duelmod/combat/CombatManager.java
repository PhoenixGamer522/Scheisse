package com.extrahelden.duelmod.combat;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages combat timers for players.
 */
public final class CombatManager {
    private static final Map<UUID, CombatTimer> TIMERS = new ConcurrentHashMap<>();

    public static final int EXTEND_TICKS = 20 * 30; // 30 seconds

    private CombatManager() {
    }


            if (existing == null) {
                return new CombatTimer(EXTEND_TICKS);
            }
            existing.addTicks(EXTEND_TICKS);
            return existing;
        });

    }

    /**
     * Check if the player currently has an active combat timer.
     */
    public static boolean isInCombat(ServerPlayer player) {
        CombatTimer timer = TIMERS.get(player.getUUID());
        return timer != null && timer.isActive();
    }

    /**
     * Get remaining ticks of combat for the given player.
     *
     * @param player player to check
     * @return remaining ticks or {@code 0} if not in combat
     */
    public static int getRemainingTicks(ServerPlayer player) {
        CombatTimer timer = TIMERS.get(player.getUUID());
        return timer != null ? timer.getTicks() : 0;
    }

    /**
     * Tick all combat timers and remove expired ones.
     */
    public static void tick() {

    }

    /**
     * Remove a player's combat timer.
     */
    public static void remove(ServerPlayer player) {
    }
}

