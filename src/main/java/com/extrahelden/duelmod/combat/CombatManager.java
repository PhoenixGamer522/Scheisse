package com.extrahelden.duelmod.combat;

import net.minecraft.server.level.ServerPlayer;

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

    /**
     * Put the player into combat or extend their timer.
     */
    public static void enterCombat(ServerPlayer player) {
        TIMERS.compute(player.getUUID(), (uuid, timer) -> {
            if (timer == null) {
                return new CombatTimer(EXTEND_TICKS);
            }
            timer.addTicks(EXTEND_TICKS);
            return timer;
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
     * Tick all combat timers and remove expired ones.
     */
    public static void tick() {
        TIMERS.entrySet().removeIf(entry -> !entry.getValue().tick());
    }

    /**
     * Remove a player's combat timer.
     */
    public static void remove(ServerPlayer player) {
        TIMERS.remove(player.getUUID());
    }
}

