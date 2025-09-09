package net.swimmingtuna.lotm.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Matrix4f;

public class RealVoidBlockRenderer implements BlockEntityRenderer<RealVoidBlockEntity> {
    public RealVoidBlockRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(RealVoidBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        poseStack.pushPose();

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.endPortal());
        Matrix4f matrix = poseStack.last().pose();

        renderSimpleEndPortalCube(buffer, matrix);

        poseStack.popPose();
    }

    private void renderSimpleEndPortalCube(VertexConsumer buffer, Matrix4f matrix) {

        addPortalQuad(buffer, matrix, 0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1);

        addPortalQuad(buffer, matrix, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0);

        addPortalQuad(buffer, matrix, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0);

        addPortalQuad(buffer, matrix, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 1);

        addPortalQuad(buffer, matrix, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 1, 1);

        addPortalQuad(buffer, matrix, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0);
    }

    private void addPortalQuad(VertexConsumer buffer, Matrix4f matrix,
                               float x1, float y1, float z1,
                               float x2, float y2, float z2,
                               float x3, float y3, float z3,
                               float x4, float y4, float z4) {

        buffer.vertex(matrix, x1, y1, z1).color(1f, 1f, 1f, 1f).uv(0, 0).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(1f, 1f, 1f, 1f).uv(1, 0).endVertex();
        buffer.vertex(matrix, x3, y3, z3).color(1f, 1f, 1f, 1f).uv(1, 1).endVertex();
        buffer.vertex(matrix, x4, y4, z4).color(1f, 1f, 1f, 1f).uv(0, 1).endVertex();
    }
}