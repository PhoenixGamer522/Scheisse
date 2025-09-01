package com.extrahelden.duelmod;

import com.extrahelden.duelmod.command.ShowLivesCommand;
import com.extrahelden.duelmod.command.LiveCommand;
import com.extrahelden.duelmod.command.DuelCommand;
import com.extrahelden.duelmod.command.AcceptCommand;
import com.extrahelden.duelmod.command.DenyCommand;
import com.extrahelden.duelmod.command.VanishCommand;
import com.extrahelden.duelmod.effect.ModEffects;
import com.extrahelden.duelmod.handler.DeathHandler;
import com.extrahelden.duelmod.handler.ModEntities;
import com.extrahelden.duelmod.handler.MyForgeEventHandler;
import com.extrahelden.duelmod.network.NetworkHandler;
import com.extrahelden.duelmod.serializer.GameProfileSerializer;
import com.extrahelden.duelmod.util.DuelConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(DuelMod.MOD_ID)
public class DuelMod {

    public static final String MOD_ID = "forge_mod";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** Start-Leben für neue Spieler */
    public static final int ANZAHL_LEBEN = 3;

    public DuelMod() {

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // ----- Configs -----
        // Hauptconfig (wird als forge_mod-common.toml gespeichert)
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DuelConfig.COMMON_SPEC);
        // Linked-Heart-Overrides in eigener Datei, damit es keinen Konflikt mit COMMON gibt
        // ----- Lifecycle -----
        modBus.addListener(this::commonSetup);
        modBus.addListener(ClientMod::clientSetup);
        modBus.addListener(ModEntities::registerEntityAttributes);

        // ----- Registries -----
        ModEffects.register(modBus);
        ModEntities.register(modBus);

        // ----- Forge EventBus -----
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.register(this); // für ServerStarting/Stopping
        MinecraftForge.EVENT_BUS.register(MyForgeEventHandler.class);


        LOGGER.info("DuelMod init: {}", new ResourceLocation(MOD_ID, "main"));
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Netzwerkpakete
             NetworkHandler.register();
            // (falls benötigt) Serializer etc.
            GameProfileSerializer.register();
            LOGGER.info("Common setup complete: network + serializers registered.");
        });
    }

    public static int getAnzahlLeben() {
        return ANZAHL_LEBEN;}

    private void onRegisterCommands(RegisterCommandsEvent event) {
        ShowLivesCommand.register(event.getDispatcher());
        LiveCommand.register(event.getDispatcher());
        VanishCommand.register(event.getDispatcher());
        DuelCommand.register(event.getDispatcher());
        AcceptCommand.register(event.getDispatcher());
        DenyCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        DeathHandler.load(event.getServer());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        DeathHandler.save(event.getServer());
    }

    // ---------- CLIENT ----------
    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ClientMod {
        @SubscribeEvent
        public static void clientSetup(final FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                // Dein Herz-Overlay oben drüber legen
                LOGGER.info("Client setup complete: HeartBarOverlay registered.");
            });
        }
    }
}
