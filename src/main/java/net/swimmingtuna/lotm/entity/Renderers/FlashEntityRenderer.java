package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.FlashEntity;
import net.swimmingtuna.lotm.entity.Model.FlashEntityModel;

public class FlashEntityRenderer extends EntityRenderer<FlashEntity> {
    public static final ResourceLocation FLASH_LOCATION = new ResourceLocation(LOTM.MOD_ID, "textures/models/flash.png");
    public final Model model;

    public FlashEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new FlashEntityModel<>(context.bakeLayer(FlashEntityModel.FLASH_LOCATION));
    }

    @Override
    public void render(FlashEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        VertexConsumer vertexConsumer = buffers.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FlashEntity entity) {
        return FLASH_LOCATION;
    }

}
