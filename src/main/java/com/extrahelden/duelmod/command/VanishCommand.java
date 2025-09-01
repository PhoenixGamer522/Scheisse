package com.extrahelden.duelmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class VanishCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vanish")
                .requires(s -> s.hasPermission(3))
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    CompoundTag data = player.getPersistentData();
                    if (data.getBoolean("Vanished")) {
                        removeVanish(player);
                        player.sendSystemMessage(Component.literal("Vanish deaktiviert"));
                    } else {
                        applyVanish(player);
                        player.sendSystemMessage(Component.literal("Vanish aktiviert"));
                    }
                    return 1;
                }));
    }

    public static void applyVanish(ServerPlayer player) {
        player.getPersistentData().putBoolean("Vanished", true);
        player.setInvisible(true);
        var server = player.getServer();
        if (server == null) return;
        var packet = new ClientboundPlayerInfoRemovePacket(List.of(player.getUUID()));
        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            if (other != player) {
                other.connection.send(packet);
            }
        }
    }

    public static void removeVanish(ServerPlayer player) {
        player.getPersistentData().putBoolean("Vanished", false);
        player.setInvisible(false);
        var server = player.getServer();
        if (server == null) return;
        var packet = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(player));
        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            if (other != player) {
                other.connection.send(packet);
            }
        }
    }
}
