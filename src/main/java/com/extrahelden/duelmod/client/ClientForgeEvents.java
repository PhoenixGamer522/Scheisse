package com.extrahelden.duelmod.client;

import com.extrahelden.duelmod.DuelMod;
import com.extrahelden.duelmod.gui.CustomDeathScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DuelMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientForgeEvents {

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof DeathScreen old && !(event.getScreen() instanceof CustomDeathScreen)) {
            System.out.println("[DuelMod] Replacing DeathScreen with CustomDeathScreen");
            event.setNewScreen(new CustomDeathScreen(old.getTitle(), true));
        }
    }
}
