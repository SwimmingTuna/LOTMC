package net.swimmingtuna.lotm.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.util.ClientData.ClientWormOfStarData;

public class WormOfStarOverlay implements IGuiOverlay {
    public static final WormOfStarOverlay INSTANCE = new WormOfStarOverlay();
    public static final ResourceLocation WORM_TEXTURE = new ResourceLocation(LOTM.MOD_ID, "textures/item/wormofstar.png");

    static final int ICON_SIZE = 12;
    static final int TEXT_OFFSET = 16;
    static final int MARGIN_FROM_SPIRITUALITY_BAR = 0;
    static final int TEXT_COLOR = ChatFormatting.BLUE.getColor();

    public static boolean shouldShowWormCounter(Player player) {
        return !player.isSpectator() && ClientWormOfStarData.getWormCount() > 0;
    }

    private static int getWormBarX(SpiritualityBarOverlay.Anchor anchor, int screenWidth) {
        int spiritualityBarX = getSpiritualityBarX(anchor, screenWidth);
        int spiritualityBarWidth = anchor == SpiritualityBarOverlay.Anchor.XP ?
                SpiritualityBarOverlay.XP_IMAGE_WIDTH : SpiritualityBarOverlay.DEFAULT_IMAGE_WIDTH;
        return spiritualityBarX + spiritualityBarWidth + MARGIN_FROM_SPIRITUALITY_BAR + 1; // Move 1 pixel right
    }

    private static int getSpiritualityBarX(SpiritualityBarOverlay.Anchor anchor, int screenWidth) {
        int configOffsetX = ClientConfigs.SPIRITUALITY_BAR_X_OFFSET.get();
        return switch (anchor) {
            case XP -> screenWidth / 2 - 91 - 3 + configOffsetX;
            case HUNGER, CENTER -> screenWidth / 2 - SpiritualityBarOverlay.DEFAULT_IMAGE_WIDTH / 2 +
                    (anchor == SpiritualityBarOverlay.Anchor.CENTER ? 0 : 50) + configOffsetX;
            case TOP_LEFT, BOTTOM_LEFT -> 20 + configOffsetX;
            default -> screenWidth - 20 - SpiritualityBarOverlay.DEFAULT_IMAGE_WIDTH + configOffsetX;
        };
    }

    private static int getWormBarY(SpiritualityBarOverlay.Anchor anchor, int screenHeight, ForgeGui gui) {
        int configOffsetY = ClientConfigs.SPIRITUALITY_BAR_Y_OFFSET.get();
        return switch (anchor) {
            case XP -> screenHeight - 32 + 3 - 8 + configOffsetY + 4; // Move 4 pixels down
            case HUNGER -> screenHeight - (gui.rightHeight - 2) - SpiritualityBarOverlay.IMAGE_HEIGHT / 2 + configOffsetY + 4; // Move 4 pixels down
            case CENTER -> screenHeight - 25 - (int) (-5 * 2.5f) - SpiritualityBarOverlay.IMAGE_HEIGHT / 2 + configOffsetY + 4; // Move 4 pixels down
            case TOP_LEFT, TOP_RIGHT -> 20 + configOffsetY + 4; // Move 4 pixels down
            default -> screenHeight - 20 - SpiritualityBarOverlay.IMAGE_HEIGHT + configOffsetY + 4; // Move 4 pixels down
        };
    }

    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        var player = Minecraft.getInstance().player;

        if (!shouldShowWormCounter(player)) return;

        // Only show if spirituality bar is also showing
        if (!SpiritualityBarOverlay.shouldShowSpiritualityBar(player)) return;

        int wormCount = ClientWormOfStarData.getWormCount();
        SpiritualityBarOverlay.Anchor anchor = ClientConfigs.SPIRITUALITY_BAR_ANCHOR.get();

        // Skip if spirituality bar would be hidden due to jump riding
        if (anchor == SpiritualityBarOverlay.Anchor.XP && player.getJumpRidingScale() > 0) return;

        int wormBarX = getWormBarX(anchor, screenWidth);
        int wormBarY = getWormBarY(anchor, screenHeight, gui);

        // Center the icon vertically with the spirituality bar and adjust position
        int iconY = wormBarY + (SpiritualityBarOverlay.IMAGE_HEIGHT - ICON_SIZE) / 2 + 5;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, WORM_TEXTURE);

        // Render the worm icon
        guiGraphics.blit(WORM_TEXTURE, wormBarX, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);

        // Render the count text
        String countText = String.valueOf(wormCount);
        int textX = wormBarX + TEXT_OFFSET;
        int textY = iconY + (ICON_SIZE - gui.getFont().lineHeight) / 2;

        guiGraphics.drawString(gui.getFont(), countText, textX, textY, TEXT_COLOR);
    }
}