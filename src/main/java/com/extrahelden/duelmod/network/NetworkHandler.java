package com.extrahelden.duelmod.network;

import com.extrahelden.duelmod.DuelMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkHandler {
    private static final String PROTOCOL = "1";

   public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
           new ResourceLocation(DuelMod.MOD_ID, "main"),
           () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals
   );





   /** Einmal in commonSetup -> enqueueWork aufrufen. */
  public static void register() {
      int id = 0;

       CHANNEL.registerMessage(
               id++,
               SyncLivesS2CPacket.class,
               SyncLivesS2CPacket::encode,
               SyncLivesS2CPacket::decode,
               SyncLivesS2CPacket::handle
       );
    }



    /** Öffentliche Helper-API fürs Senden. */
     public static void syncLives(ServerPlayer player, int lives, String ownerName, String ownerUuid) {
         boolean linkedActive = (lives == 0) && player.getPersistentData().getBoolean("LinkedHeartActive");
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                 new SyncLivesS2CPacket(lives, ownerName, ownerUuid, linkedActive)
         );
    };
}

