package com.extrahelden.duelmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

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
        var infoPacket = new ClientboundPlayerInfoRemovePacket(List.of(player.getUUID()));
        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            if (other != player) {
                other.connection.send(infoPacket);
            }
        }
        hideEquipmentForOthers(player);
    }

    public static void removeVanish(ServerPlayer player) {
        player.getPersistentData().putBoolean("Vanished", false);
        player.setInvisible(false);
        var server = player.getServer();
        if (server == null) return;
        var infoPacket = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(player));
        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            if (other != player) {
                other.connection.send(infoPacket);
            }
        }
        showEquipment(player);
    }

    public static void hideEquipmentForOthers(ServerPlayer player) {
        broadcastEquipment(player, buildEmptyEquipment());
    }

    public static void showEquipment(ServerPlayer player) {
        broadcastEquipment(player, buildActualEquipment(player));
    }

    public static void sendEmptyEquipment(ServerPlayer vanished, ServerPlayer viewer) {
        viewer.connection.send(new ClientboundSetEquipmentPacket(vanished.getId(), buildEmptyEquipment()));
    }

    private static void broadcastEquipment(ServerPlayer player, List<Pair<EquipmentSlot, ItemStack>> equipment) {
        var server = player.getServer();
        if (server == null) return;
        var packet = new ClientboundSetEquipmentPacket(player.getId(), equipment);
        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            if (other != player) {
                other.connection.send(packet);
            }
        }
    }

    private static List<Pair<EquipmentSlot, ItemStack>> buildEmptyEquipment() {
        List<Pair<EquipmentSlot, ItemStack>> list = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            list.add(Pair.of(slot, ItemStack.EMPTY));
        }
        return list;
    }

    private static List<Pair<EquipmentSlot, ItemStack>> buildActualEquipment(ServerPlayer player) {
        List<Pair<EquipmentSlot, ItemStack>> list = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            list.add(Pair.of(slot, player.getItemBySlot(slot)));
        }
        return list;
    }
}
