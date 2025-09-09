package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.entity.DimensionalSightSealEntity;

public class DimensionalSightSealRenderer extends EntityRenderer<DimensionalSightSealEntity> {

    public DimensionalSightSealRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DimensionalSightSealEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        poseStack.pushPose();
        poseStack.scale(5.0f, 5.0f, 5.0f);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());
        renderCube(poseStack, vertexConsumer, packedLight);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    private void renderCube(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight) {
        int red = 0;
        int green = 0;
        int blue = 0;
        int alpha = 255;
        addVertex(poseStack, vertexConsumer, 0, 0, 1, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 1, 0, 1, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 1, 1, 1, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 0, 1, 1, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 1, 0, 0, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 0, 0, 0, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 0, 1, 0, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 1, 1, 0, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 1, 0, 1, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 1, 0, 0, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 1, 1, 0, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 1, 1, 1, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 0, 0, 0, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 0, 0, 1, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 0, 1, 1, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 0, 1, 0, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 0, 1, 1, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 1, 1, 1, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 1, 1, 0, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 0, 1, 0, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 0, 0, 0, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 1, 0, 0, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 1, 0, 1, red, green, blue, alpha, packedLight);
        addVertex(poseStack, vertexConsumer, 0, 0, 1, red, green, blue, alpha, packedLight);
    }

    private void addVertex(PoseStack poseStack, VertexConsumer vertexConsumer,
                           float x, float y, float z, int red, int green, int blue, int alpha, int packedLight) {
        vertexConsumer.vertex(poseStack.last().pose(), x, y, z)
                .color(red, green, blue, alpha)
                .uv(0, 0) // UV coordinates (not needed for solid color but required)
                .overlayCoords(0) // No overlay
                .uv2(packedLight) // Light level
                .normal(poseStack.last().normal(), 0, 1, 0) // Normal vector
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(DimensionalSightSealEntity entity) {
        return null;
    }
}
