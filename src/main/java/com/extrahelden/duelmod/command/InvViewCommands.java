package com.extrahelden.duelmod.command;

import com.extrahelden.duelmod.api.InventoryProvider;
import com.extrahelden.duelmod.api.InventoryProviderRegistry;
import com.extrahelden.duelmod.provider.PlayerInventoryProvider;
import com.extrahelden.duelmod.util.InventoryLockManager;
import com.extrahelden.duelmod.util.PermissionHandler;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

public class InvViewCommands {

    public InvViewCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        var viewCommand = Commands.literal("view")
                .requires(source -> source.hasPermission(2));

        for (InventoryProvider provider : InventoryProviderRegistry.getAllProviders()) {
            viewCommand.then(Commands.literal(provider.getId())
                    .requires(source -> PermissionHandler.hasPermission(source, provider.getPermission(), 2))
                    .then(Commands.argument("target", GameProfileArgument.gameProfile())
                            .executes(context -> executeViewCommand(context, provider))));
        }

        dispatcher.register(viewCommand);
    }

    private int executeViewCommand(CommandContext<CommandSourceStack> context, InventoryProvider provider) throws CommandSyntaxException {
        ServerPlayer viewer = context.getSource().getPlayerOrException();
        ServerPlayer target = getRequestedPlayer(context);

        if (viewer == target && provider instanceof PlayerInventoryProvider) {
            context.getSource().sendFailure(Component.translatable("inv_view_neoforge.command.error.invalid_inventory"));
            return 0;
        }

        if (!provider.isAvailable(target)) {
            context.getSource().sendFailure(Component.translatable("inv_view_neoforge.command.error.inventory_not_available"));
            return 0;
        }

        if (InventoryLockManager.isLocked(target.getUUID(), provider.getLockType())) {
            context.getSource().sendFailure(Component.translatable("inv_view_neoforge.command.error.inventory_in_use"));
            return 0;
        }

        openScreen(viewer, target, provider);
        return 1;
    }

    private void openScreen(ServerPlayer viewer, ServerPlayer target, InventoryProvider provider) {
        viewer.openMenu(new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return provider.getDisplayName(target);
            }

            @Override
            public @NotNull AbstractContainerMenu createMenu(int syncId, @NotNull Inventory inventory, @NotNull Player player) {
                return provider.createMenu(syncId, viewer, target);
            }
        });
    }

    /**
     * Forge 1.20.1: nur Online-Spieler.
     */
    private ServerPlayer getRequestedPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        GameProfile profile = GameProfileArgument.getGameProfiles(context, "target").iterator().next();
        ServerPlayer player = server.getPlayerList().getPlayer(profile.getId());

        if (player == null) {
            throw new CommandSyntaxException(
                    CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(),
                    Component.literal("Player " + profile.getName() + " not found.")
            );
        }

        return player;
    }
}
