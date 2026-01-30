package com.extrahelden.duelmod.events;

import com.extrahelden.duelmod.DuelMod;
import com.extrahelden.duelmod.command.InvViewCommands;
import com.extrahelden.duelmod.util.ITargetPlayerContainer;
import com.extrahelden.duelmod.util.InventoryLockManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = DuelMod.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        new InvViewCommands(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer joiningPlayer) {
            UUID joiningPlayerUUID = joiningPlayer.getUUID();
            InventoryLockManager.unlockAll(joiningPlayerUUID);

            event.getEntity().getServer().getPlayerList().getPlayers().forEach(player -> {
                if (player.containerMenu instanceof ITargetPlayerContainer container &&
                        container.getTargetPlayer().getUUID().equals(joiningPlayerUUID)) {
                    // Guardar datos del jugador objetivo antes de cerrar
                    DuelMod.savePlayerData(container.getTargetPlayer());
                    player.closeContainer();
                    player.displayClientMessage(Component.translatable("inv_view_neoforge.player_connected", joiningPlayer.getName()), false);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer leavingPlayer) {
            UUID leavingPlayerUUID = leavingPlayer.getUUID();
            InventoryLockManager.unlockAll(leavingPlayerUUID);

            event.getEntity().getServer().getPlayerList().getPlayers().forEach(player -> {
                if (player.containerMenu instanceof ITargetPlayerContainer container &&
                        container.getTargetPlayer().getUUID().equals(leavingPlayerUUID)) {
                    // Guardar datos del jugador objetivo antes de cerrar
                    DuelMod.savePlayerData(container.getTargetPlayer());
                    player.closeContainer();
                    player.displayClientMessage(Component.translatable("inv_view_neoforge.player_disconnected", leavingPlayer.getName()), false);
                }
            });
        }
    }
}
