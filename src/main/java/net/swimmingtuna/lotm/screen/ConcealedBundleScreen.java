package net.swimmingtuna.lotm.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.swimmingtuna.lotm.LOTM;

public class ConcealedBundleScreen extends AbstractContainerScreen<ConcealedBundleMenu> {
    private final int rows;

    public ConcealedBundleScreen(ConcealedBundleMenu container, Inventory inv, Component title) {
        super(container, inv, title);
        this.rows = container.getRows();

        this.imageWidth = 176;
        this.imageHeight = 114 + 18 * rows;
    }

    private ResourceLocation getScreenTexture(){
        return new ResourceLocation(LOTM.MOD_ID, "textures/gui/concealed_bundle_menu_" + rows + ".png");
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(getScreenTexture(), x, y, 0, 0, this.imageWidth, this.imageHeight, 512, 512);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 0x404040, false);
    }
}
