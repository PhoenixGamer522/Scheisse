package com.extrahelden.duelmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;

public class InvseeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("invsee")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer viewer = ctx.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                            openInventory(viewer, target);
                            return 1;
                        }))));
    }

    private static void openInventory(ServerPlayer viewer, ServerPlayer target) {
        // 5 rows (45 slots) container to fit main inventory + armor + offhand
        SimpleContainer container = new SimpleContainer(45);

        // copy main inventory and hotbar (0-35)
        for (int i = 0; i < target.getInventory().items.size(); i++) {
            ItemStack stack = target.getInventory().items.get(i).copy();
            container.setItem(i, stack);
        }

        // armor slots 36-39
        for (int i = 0; i < target.getInventory().armor.size(); i++) {
            ItemStack stack = target.getInventory().armor.get(i).copy();
            container.setItem(36 + i, stack);
        }

        // offhand slot 40
        container.setItem(40, target.getInventory().offhand.get(0).copy());

        viewer.openMenu(new net.minecraft.world.MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal(target.getName().getString() + " Inventar");
            }

            @Override
            public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inventory, net.minecraft.world.entity.player.Player player) {
                return ChestMenu.fiveRows(id, inventory, container);
            }
        });
    }
}
