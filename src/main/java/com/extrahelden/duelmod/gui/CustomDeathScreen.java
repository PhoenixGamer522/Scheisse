package com.extrahelden.duelmod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class CustomDeathScreen extends DeathScreen {

    private static final int FRAME_ANIM_COUNT = 149;
    private static final ResourceLocation[] FRAMES_ANIM = new ResourceLocation[FRAME_ANIM_COUNT];

    private int currentFrame = 0;
    private long lastFrameTimeNs = 0L;

    // 20 ms
    private static final int FRAME_INTERVAL_NS = 20_000_000;

    private boolean initialAnimationDone = false;

    public CustomDeathScreen(Component title, boolean causeReported) {
        super(title, causeReported);
    }

    @Override
    protected void init() {
        super.init();
    }

    /** Falls du später noch eigenes Overlay brauchst */
    protected void renderOverlay(GuiGraphics g) {}

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        long now = System.nanoTime();

        if (!initialAnimationDone) {
            if (currentFrame >= 129) {
                super.render(g, mouseX, mouseY, partialTick);
            }
            renderInitialAnimation(g, now);
        } else {
            renderOverlay(g);
            super.render(g, mouseX, mouseY, partialTick);
        }
    }

    private void renderInitialAnimation(GuiGraphics g, long now) {
        if (now - lastFrameTimeNs >= FRAME_INTERVAL_NS) {
            currentFrame++;
            lastFrameTimeNs = now;
        }

        if (currentFrame >= FRAME_ANIM_COUNT) {
            initialAnimationDone = true;
            currentFrame = FRAME_ANIM_COUNT - 1;
        }

        ResourceLocation tex = FRAMES_ANIM[currentFrame];
        RenderSystem.setShaderTexture(0, tex);

        if (currentFrame >= 129) {
            int fadeFrame = currentFrame - 129;
            float alpha = 1.0F - (float) fadeFrame / 20.0F;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
        } else {
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        }

        g.blit(tex, 0, 0, 0, 0, this.width, this.height, this.width, this.height);

        RenderSystem.disableBlend();
    }

    static {
        for (int i = 0; i < FRAME_ANIM_COUNT; i++) {
            FRAMES_ANIM[i] = new ResourceLocation(
                    "forge_mod", // <-- KEIN Leerzeichen, dein finaler modid!
                    String.format("textures/gui/frames_anim/minecrafthelden_%d.jpg", i)
            );
        }
    }
}
