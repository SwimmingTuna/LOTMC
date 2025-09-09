package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.Model.SpaceFragmentationModel;
import net.swimmingtuna.lotm.entity.SpaceFragmentationEntity;
import net.swimmingtuna.lotm.util.LOTMRenderTypes;
import org.joml.Matrix4f;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SpaceFragmentationRenderer extends GeoEntityRenderer<SpaceFragmentationEntity> {
    public SpaceFragmentationRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SpaceFragmentationModel());
    }

    @Override
    public void render(SpaceFragmentationEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer buffer = bufferSource.getBuffer(LOTMRenderTypes.END_PORTAL_NO_CULL);

        renderCross(buffer, matrix);

        poseStack.popPose();


        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(90));

        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

        Matrix4f verticalMatrix = poseStack.last().pose();
        VertexConsumer verticalBuffer = bufferSource.getBuffer(LOTMRenderTypes.END_PORTAL_NO_CULL);

        renderCross(verticalBuffer, verticalMatrix);

        poseStack.popPose();


        poseStack.pushPose();

        poseStack.translate(0, 5.050146875, -5.050146875);

        poseStack.mulPose(Axis.XP.rotationDegrees(90));

        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

        Matrix4f horizontalMatrix = poseStack.last().pose();
        VertexConsumer horizontalBuffer = bufferSource.getBuffer(LOTMRenderTypes.END_PORTAL_NO_CULL);

        renderCross(horizontalBuffer, horizontalMatrix);

        poseStack.popPose();
    }

    private void renderCross(VertexConsumer buffer, Matrix4f matrix){
        //center
        addPortalQuad(buffer, matrix, -1, 4.050514375f, 1, 4.050514375f, 1, 6.050514375f, -1, 6.050514375f);

        //down
        addPortalQuad(buffer, matrix, -0.19576875f, 4.050514375f, 0.19576875f, 4.050514375f, 0.19576875f, 0.850514375f, -0.19576875f, 0.850514375f);
        addPortalTriangle(buffer, matrix, -0.861653125f, 4.050514375f, -0.19576875f, 4.050514375f, -0.19576875f, 1.353614375f);
        addPortalTriangle(buffer, matrix, 0.861653125f, 4.050514375f, 0.19576875f, 4.050514375f, 0.19576875f, 1.353614375f);

        //left
        addPortalQuad(buffer, matrix, -1, 4.854745625f, -1, 5.246283125f, -4.2f, 5.246283125f, -4.2f, 4.854745625f);
        addPortalTriangle(buffer, matrix, -1, 4.18886125f, -1, 4.854745625f, -3.2629f, 4.854745625f);
        addPortalTriangle(buffer, matrix, -1, 5.9121675f, -1, 5.246283125f, -3.2629f, 5.246283125f);

        //up
        addPortalQuad(buffer, matrix, -0.19576875f, 6.050514375f, 0.19576875f, 6.050514375f, 0.19576875f, 9.250514375f, -0.19576875f, 9.250514375f);
        addPortalTriangle(buffer, matrix, -0.861653125f, 6.050514375f, -0.19576875f, 6.050514375f, -0.19576875f, 8.747414365f);
        addPortalTriangle(buffer, matrix, 0.861653125f, 6.050514375f, 0.19576875f, 6.050514375f, 0.19576875f, 8.747414365f);

        //right
        addPortalQuad(buffer, matrix, 1, 4.854745625f, 1, 5.246283125f, 4.2f, 5.246283125f, 4.2f, 4.854745625f);
        addPortalTriangle(buffer, matrix, 1, 4.18886125f, 1, 4.854745625f, 3.2629f, 4.854745625f);
        addPortalTriangle(buffer, matrix, 1, 5.9121675f, 1, 5.246283125f, 3.2629f, 5.246283125f);
    }

    private void addPortalQuad(VertexConsumer buffer, Matrix4f matrix,
                               float x1, float y1,
                               float x2, float y2,
                               float x3, float y3,
                               float x4, float y4) {

        buffer.vertex(matrix, x1, y1, 0).color(1f, 1f, 1f, 1f).uv(0, 0).endVertex();
        buffer.vertex(matrix, x2, y2, 0).color(1f, 1f, 1f, 1f).uv(1, 0).endVertex();
        buffer.vertex(matrix, x3, y3, 0).color(1f, 1f, 1f, 1f).uv(1, 1).endVertex();
        buffer.vertex(matrix, x4, y4, 0).color(1f, 1f, 1f, 1f).uv(0, 1).endVertex();
    }

    private void addPortalTriangle(VertexConsumer buffer, Matrix4f matrix,
                                   float x1, float y1,
                                   float x2, float y2,
                                   float x3, float y3) {

        buffer.vertex(matrix, x1, y1, 0).color(1f, 1f, 1f, 1f).uv(0, 0).endVertex();
        buffer.vertex(matrix, x2, y2, 0).color(1f, 1f, 1f, 1f).uv(1, 0).endVertex();
        buffer.vertex(matrix, x3, y3, 0).color(1f, 1f, 1f, 1f).uv(0.5f, 1).endVertex();

        buffer.vertex(matrix, x3, y3, 0).color(1f, 1f, 1f, 1f).uv(0.5f, 1).endVertex();
        buffer.vertex(matrix, x2, y2, 0).color(1f, 1f, 1f, 1f).uv(1, 0).endVertex();
        buffer.vertex(matrix, x1, y1, 0).color(1f, 1f, 1f, 1f).uv(0, 0).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(SpaceFragmentationEntity entity) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/space_fragmentation.png");
    }

    @Override
    protected void applyRotations(SpaceFragmentationEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.applyRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);

        poseStack.mulPose(Axis.YP.rotationDegrees(0));
    }
}
