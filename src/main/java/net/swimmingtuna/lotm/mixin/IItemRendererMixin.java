package net.swimmingtuna.lotm.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.swimmingtuna.lotm.util.IItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class IItemRendererMixin {

    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    public void onRenderItem(ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack mStack, MultiBufferSource buffers, int packedLight, int packedOverlay, BakedModel modelIn, CallbackInfo ci) {
        if (modelIn instanceof IItemRenderer) {
            ci.cancel();
            mStack.pushPose();
            IItemRenderer renderer = (IItemRenderer) ForgeHooksClient.handleCameraTransforms(mStack, modelIn, context, leftHand);
            mStack.translate(-0.5D, -0.5D, -0.5D);
            renderer.renderItem(stack, context, mStack, buffers, packedLight, packedOverlay);
            mStack.popPose();
        }
    }
}