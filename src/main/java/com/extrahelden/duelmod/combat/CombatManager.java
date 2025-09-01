package com.extrahelden.duelmod.combat;

import com.extrahelden.duelmod.DuelMod;
import net.minecraft.server.level.ServerPlayer;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages combat timers for players.
 */
public final class CombatManager {
    private static final Map<UUID, CombatTimer> TIMERS = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> PARTNERS = new ConcurrentHashMap<>();
    public static final int EXTEND_TICKS = 20 * 30; // 30 seconds

    private CombatManager() {
    }

    private static CombatTimer extendTimer(ServerPlayer player) {
        return TIMERS.compute(player.getUUID(), (uuid, existing) -> {
            if (existing == null) {
                return new CombatTimer(EXTEND_TICKS);
            }
            existing.addTicks(EXTEND_TICKS);
            return existing;
        });
    }

    /**
     * Put both players into combat or extend their timers and link them as combat partners.
     */
    public static void engage(ServerPlayer a, ServerPlayer b) {
        CombatTimer ta = extendTimer(a);
        CombatTimer tb = extendTimer(b);
        PARTNERS.put(a.getUUID(), b.getUUID());
        PARTNERS.put(b.getUUID(), a.getUUID());
        DuelMod.LOGGER.debug("Player {} is in combat with {} ({} ticks remaining)",
                a.getGameProfile().getName(), b.getGameProfile().getName(), ta.getTicks());
        DuelMod.LOGGER.debug("Player {} is in combat with {} ({} ticks remaining)",
                b.getGameProfile().getName(), a.getGameProfile().getName(), tb.getTicks());
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
        Iterator<Map.Entry<UUID, CombatTimer>> it = TIMERS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, CombatTimer> entry = it.next();
            if (!entry.getValue().tick()) {
                UUID id = entry.getKey();
                it.remove();
                UUID partner = PARTNERS.remove(id);
                if (partner != null) {
                    UUID back = PARTNERS.get(partner);
                    if (back != null && back.equals(id)) {
                        PARTNERS.remove(partner);
                    }
                }
            }
        }
    }

    /**
     * Remove a player's combat timer.
     */
    public static void remove(ServerPlayer player) {
        UUID id = player.getUUID();
        TIMERS.remove(id);
        UUID partner = PARTNERS.remove(id);
        if (partner != null) {
            TIMERS.remove(partner);
            UUID back = PARTNERS.get(partner);
            if (back != null && back.equals(id)) {
                PARTNERS.remove(partner);
            }
        }
    }
}

