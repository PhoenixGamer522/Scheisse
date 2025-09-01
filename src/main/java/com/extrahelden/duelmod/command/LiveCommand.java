package com.extrahelden.duelmod.command;


import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public class LiveCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("live")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    CompoundTag data = player.getPersistentData();
                    Scoreboard board = player.getScoreboard();
                    String teamName = "live_" + player.getScoreboardName();
                    PlayerTeam team = board.getPlayerTeam(teamName);
                    if (data.getBoolean("LivePrefix")) {
                        if (team != null) {
                            board.removePlayerFromTeam(player.getScoreboardName(), team);
                            board.removePlayerTeam(team);
                        }
                        data.putBoolean("LivePrefix", false);
                    } else {
                        if (team == null) {
                            team = board.addPlayerTeam(teamName);
                        }

                    }
                    return 1;
                }));
    }

