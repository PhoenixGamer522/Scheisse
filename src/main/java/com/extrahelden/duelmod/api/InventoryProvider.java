package com.extrahelden.duelmod.api;

import com.extrahelden.duelmod.util.InventoryLockManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public interface InventoryProvider {
    String getId();
    boolean isAvailable(ServerPlayer target);
    InventoryLockManager.InventoryType getLockType();
    AbstractContainerMenu createMenu(int syncId, ServerPlayer viewer, ServerPlayer target);
    Component getDisplayName(ServerPlayer target);
    String getPermission();
}
