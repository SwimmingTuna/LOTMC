package net.swimmingtuna.lotm.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.swimmingtuna.lotm.util.effect.ModEffects;

public class FlashOverlay implements IGuiOverlay {
    public static final FlashOverlay INSTANCE = new FlashOverlay();
    private static final int FADE_DURATION = 100;
    private int fadeTicker = 0;

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        MobEffectInstance flashEffect = player.getEffect(ModEffects.FLASH.get());
        float opacity = 0.0F;

        if (flashEffect != null) {
            opacity = 0.90F;
            fadeTicker = FADE_DURATION;
        } else if (fadeTicker > 0) {
            opacity = (float) fadeTicker / FADE_DURATION * 0.95F;
            fadeTicker--;
        }

        if (opacity <= 0.01F) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
        guiGraphics.fill(0, 0, screenWidth, screenHeight, 0xFFFFFFFF);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}