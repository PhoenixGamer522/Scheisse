package com.extrahelden.duelmod.provider;

import com.extrahelden.duelmod.api.InventoryProvider;
import com.extrahelden.duelmod.gui.PlayerInventoryScreenHandler;
import com.extrahelden.duelmod.util.InventoryLockManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class PlayerInventoryProvider implements InventoryProvider {
    @Override
    public String getId() {
        return "inv";
    }

    @Override
    public boolean isAvailable(ServerPlayer target) {
        return true; // El inventario siempre está disponible
    }

    @Override
    public InventoryLockManager.InventoryType getLockType() {
        return InventoryLockManager.InventoryType.PLAYER_INVENTORY;
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, ServerPlayer viewer, ServerPlayer target) {
        return new PlayerInventoryScreenHandler(syncId, viewer, target);
    }

    @Override
    public Component getDisplayName(ServerPlayer target) {
        return target.getDisplayName();
    }

    @Override
    public String getPermission() {
        return "inv_view.inv";
    }
}
