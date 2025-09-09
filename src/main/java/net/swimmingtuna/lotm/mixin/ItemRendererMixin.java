package net.swimmingtuna.lotm.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiGraphics.class)
public class ItemRendererMixin {

    @Redirect(
            method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I")
    )
    private int renderStackCountWithDynamicSize(GuiGraphics instance, Font font, String text, int x, int y, int color, boolean shadow) {
        if (text != null && text.length() > 2) {
            PoseStack poseStack = instance.pose();
            poseStack.pushPose();
            float scale = getScaleForText(text.length());
            float scaledWidth = font.width(text) * scale;
            float newX = x + (font.width(text) - scaledWidth);

            poseStack.scale(scale, scale, 1.0F);
            int result = instance.drawString(font, text, (int)(newX / scale), (int)(y / scale), color, shadow);
            poseStack.popPose();
            return result;
        }
        return instance.drawString(font, text, x, y, color, shadow);
    }

    private static float getScaleForText(int textLength) {
        if (textLength <= 2) return 1.0F;
        if (textLength == 3) return 0.8F;
        if (textLength == 4) return 0.65F;
        return 0.5F;
    }
}