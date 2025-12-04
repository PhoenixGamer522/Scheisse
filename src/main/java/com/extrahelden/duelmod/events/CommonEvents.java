package com.extrahelden.duelmod.events;

import com.extrahelden.duelmod.DuelMod;
import com.extrahelden.duelmod.handler.DummyManager;
import com.extrahelden.duelmod.combat.CombatTimer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


import java.nio.charset.StandardCharsets;
import java.util.*;

@Mod.EventBusSubscriber(modid = DuelMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents {

    private static final Map<LivingEntity, CombatTimer> timers = new HashMap<>();
    private static short elapsedTicks = 20;
    private static final Random RANDOM = new Random();
    private static final Map<BlockPos, Long> graves = new HashMap<>();
    private static final Map<BlockPos, Integer> experienceMap = new HashMap<>();
    private static final Map<UUID, LinkedList<BlockPos>> deathPositions = new HashMap<>();
    private static final DummyManager dummyManager = new DummyManager();

    // imports zusätzlich:

// ----------------- CommonEvents -----------------

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer newP)) return;
        if (!event.isWasDeath()) return;

        var old = event.getOriginal().getPersistentData();
        var data = newP.getPersistentData();

        int lives = old.getInt("MyLives");

        String ownerName = old.getString("LinkedHeartOwner");
        if (ownerName == null || ownerName.isBlank()) {
            ownerName = newP.getGameProfile().getName();
        }

        String ownerUuid = old.getString("LinkedHeartOwnerUUID");
        if (ownerUuid == null || ownerUuid.isBlank()) {
            ownerUuid = resolveOwnerUuid(newP, ownerName);
        }

        data.putInt("MyLives", lives);
        data.putString("LinkedHeartOwner", ownerName);
        data.putString("LinkedHeartOwnerUUID", ownerUuid);

        // ✅ neue 4-Parameter-Variante
        ///NetworkHandler.syncLives(newP, lives, ownerName, ownerUuid);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p)) return;

        var data = p.getPersistentData();
        int lives = data.getInt("MyLives");

        String ownerName = data.getString("LinkedHeartOwner");
        if (ownerName == null || ownerName.isBlank()) ownerName = p.getGameProfile().getName();

        String ownerUuid = data.getString("LinkedHeartOwnerUUID");
        if (ownerUuid == null || ownerUuid.isBlank()) {
            ownerUuid = resolveOwnerUuid(p, ownerName);
            data.putString("LinkedHeartOwnerUUID", ownerUuid);
        }

        // einen Tick später syncen
        final int lf = lives;
        final String on = ownerName;
        final String ou = ownerUuid;
        ///  p.server.execute(() -> NetworkHandler.syncLives(p, lf, on, ou));
    }

    public static void onDeath(LivingDeathEvent e){
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        boolean byPlayer = e.getSource().getEntity() instanceof ServerPlayer;
        ///NetworkHandler.sendKilledByPlayer(sp, byPlayer);
    }

    /**
     * UUID zu Owner-Name ermitteln (Server-Cache → Fallback Offline-UUID)
     */
    private static String resolveOwnerUuid(ServerPlayer context, String ownerName) {
        if (ownerName == null || ownerName.isBlank()) return "";
        var srv = context.getServer();
        if (srv != null) {
            var opt = srv.getProfileCache().get(ownerName); // Optional<GameProfile>
            if (opt.isPresent() && opt.get().getId() != null) {
                return opt.get().getId().toString();
            }
        }
        UUID off = UUID.nameUUIDFromBytes(("OfflinePlayer:" + ownerName)
                .getBytes(StandardCharsets.UTF_8));
        return off.toString();
    }
}

