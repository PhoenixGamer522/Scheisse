package com.extrahelden.duelmod.events;

import com.extrahelden.duelmod.api.InventoryProvider;
import com.extrahelden.duelmod.api.InventoryProviderRegistry;
import com.extrahelden.duelmod.provider.CuriosCosmeticInventoryProvider;
import com.extrahelden.duelmod.provider.CuriosInventoryProvider;
import com.extrahelden.duelmod.provider.EnderChestProvider;
import com.extrahelden.duelmod.provider.PlayerInventoryProvider;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class InventoryProviderEvents {
    public static class RegisterInventoryProvidersEvent extends Event {
        private final List<Consumer<InventoryProvider>> listeners = new ArrayList<>();

        public void register(InventoryProvider provider) {
            InventoryProviderRegistry.register(provider);
        }

        public void addListener(Consumer<InventoryProvider> listener) {
            listeners.add(listener);
        }

        public void fire(InventoryProvider provider) {
            listeners.forEach(listener -> listener.accept(provider));
        }
    }

    public static void registerProviders(FMLCommonSetupEvent event) {
        RegisterInventoryProvidersEvent registerEvent = new RegisterInventoryProvidersEvent();
        registerEvent.register(new PlayerInventoryProvider());
        registerEvent.register(new EnderChestProvider());
        if (ModList.get().isLoaded("curios")) {
            registerEvent.register(new CuriosInventoryProvider());
            registerEvent.register(new CuriosCosmeticInventoryProvider());
        }
    }
}
