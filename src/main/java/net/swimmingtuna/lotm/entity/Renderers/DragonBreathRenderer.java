package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.DragonBreathEntity;
import net.swimmingtuna.lotm.entity.Model.DragonBreathModel;
import net.swimmingtuna.lotm.util.LOTMRenderTypes;
import net.swimmingtuna.lotm.util.ParticleColors;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class DragonBreathRenderer extends EntityRenderer<DragonBreathEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(LOTM.MOD_ID, "textures/models/dragon_breath.png");
    private static final ResourceLocation CHARGE = new ResourceLocation(LOTM.MOD_ID, "textures/models/dragon_breath_charge.png");
    private static final int TEXTURE_WIDTH = 16;
    private static final int TEXTURE_HEIGHT = 512;
    private static final float BEAM_RADIUS = 0.5F;
    private boolean clearerView = true;

    private final DragonBreathModel model;

    public DragonBreathRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);

        this.model = new DragonBreathModel(pContext.bakeLayer(DragonBreathModel.LAYER));
    }

    @Override
    public void render(DragonBreathEntity pEntity, float pEntityYaw, float pPartialTick, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight) {
        this.clearerView = Minecraft.getInstance().player == pEntity.getOwner() &&
                Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;

        float yaw = (pEntity.prevYaw + (pEntity.renderYaw - pEntity.prevYaw) * pPartialTick) * Mth.RAD_TO_DEG;
        float pitch = (pEntity.prevPitch + (pEntity.renderPitch - pEntity.prevPitch) * pPartialTick) * Mth.RAD_TO_DEG;
        Vector3f color = null;
        if (pEntity.causesFire()) {
            color = ParticleColors.FIRE_YELLOW;
        } else {
            color = ParticleColors.FIRE_ORANGE;
        }
        float age = pEntity.getTime() + pPartialTick;
        float entitySize = pEntity.getSize();

        pPoseStack.pushPose();
        pPoseStack.scale(entitySize, entitySize, entitySize);

        // Render charge effect
        pPoseStack.pushPose();
        pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        pPoseStack.mulPose(Axis.ZN.rotationDegrees(pitch));
        VertexConsumer charge = pBuffer.getBuffer(LOTMRenderTypes.glow(CHARGE));
        this.model.setupAnim(pEntity, 0.0F, 0.0F, age, 0.0F, 0.0F);
        this.model.renderToBuffer(pPoseStack, charge, pPackedLight, OverlayTexture.NO_OVERLAY, color.x, color.y, color.z, 1.0F);
        pPoseStack.popPose();

        // Render beam (after charge time)
        if (pEntity.getTime() >= pEntity.getCharge()) {
            double collidePosX = pEntity.prevCollidePosX + (pEntity.collidePosX - pEntity.prevCollidePosX) * pPartialTick;
            double collidePosY = pEntity.prevCollidePosY + (pEntity.collidePosY - pEntity.prevCollidePosY) * pPartialTick;
            double collidePosZ = pEntity.prevCollidePosZ + (pEntity.collidePosZ - pEntity.prevCollidePosZ) * pPartialTick;
            double posX = pEntity.xo + (pEntity.getX() - pEntity.xo) * pPartialTick;
            double posY = pEntity.yo + (pEntity.getY() - pEntity.yo) * pPartialTick;
            double posZ = pEntity.zo + (pEntity.getZ() - pEntity.zo) * pPartialTick;
            float length = (float) Math.sqrt(Math.pow(collidePosX - posX, 2) + Math.pow(collidePosY - posY, 2) + Math.pow(collidePosZ - posZ, 2));
            int frame = Mth.floor((pEntity.animation - 1 + pPartialTick) * 2);
            if (frame < 0) {
                frame = pEntity.getFrames() * 2;
            }

            // Important: Don't create a new scale context - use the existing scaled context
            pPoseStack.pushPose();
            pPoseStack.translate(0.0F, (pEntity.getBbHeight() / 2.0F) - 0.5F, 0.0F);
            VertexConsumer beam = pBuffer.getBuffer(LOTMRenderTypes.glow(TEXTURE));
            float brightness = 1.0F - ((float) pEntity.getTime() / (pEntity.getCharge() + pEntity.getDuration() + pEntity.getFrames()));
            this.renderBeam(length, yaw, pitch, frame, pPoseStack, beam, brightness, pPackedLight, entitySize);
            pPoseStack.popPose();
        }

        pPoseStack.popPose(); // Pop the main scale
    }

    private void drawCube(float length, int frame, PoseStack poseStack, VertexConsumer consumer, float brightness, int packedLight, float scale) {
        float minU = 0.0F;
        float minV = 16.0F / TEXTURE_HEIGHT * frame;
        float maxU = minU + 16.0F / TEXTURE_WIDTH;
        float maxV = minV + 16.0F / TEXTURE_HEIGHT;
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        float offset = this.clearerView ? -1.0F : 0.0F;

        // Scale the beam radius based on entity size
        float scaledRadius = BEAM_RADIUS * scale;

        this.drawVertex(matrix4f, matrix3f, consumer, -scaledRadius, offset, scaledRadius, minU, minV, brightness, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, -scaledRadius, length, scaledRadius, minU, maxV, brightness, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, scaledRadius, length, scaledRadius, maxU, maxV, brightness, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, scaledRadius, offset, scaledRadius, maxU, minV, brightness, packedLight);

        this.drawVertex(matrix4f, matrix3f, consumer, scaledRadius, offset, -scaledRadius, minU, minV, brightness, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, scaledRadius, length, -scaledRadius, minU, maxV, brightness, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, -scaledRadius, length, -scaledRadius, maxU, maxV, brightness, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, -scaledRadius, offset, -scaledRadius, maxU, minV, brightness, packedLight);

        this.drawVertex(matrix4f, matrix3f, consumer, -scaledRadius, length, -scaledRadius, minU, minV, brightness, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, -scaledRadius, length, scaledRadius, minU, maxV, brightness, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, scaledRadius, length, scaledRadius, maxU, maxV, brightness, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, scaledRadius, length, -scaledRadius, maxU, minV, brightness, packedLight);

        this.drawVertex(matrix4f, matrix3f, consumer, -scaledRadius, offset, -scaledRadius, minU, minV, brightness, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, -scaledRadius, offset, scaledRadius, minU, maxV, brightness, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, scaledRadius, offset, scaledRadius, maxU, maxV, brightness, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, scaledRadius, offset, -scaledRadius, maxU, minV, brightness, packedLight);

        this.drawVertex(matrix4f, matrix3f, consumer, -scaledRadius, length, -scaledRadius, minU, minV, brightness, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, -scaledRadius, length, scaledRadius, minU, maxV, brightness, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, -scaledRadius, offset, scaledRadius, maxU, maxV, brightness, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, -scaledRadius, offset, -scaledRadius, maxU, minV, brightness, packedLight);

        this.drawVertex(matrix4f, matrix3f, consumer, scaledRadius, length, -scaledRadius, minU, minV, brightness, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, scaledRadius, length, scaledRadius, minU, maxV, brightness, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, scaledRadius, offset, scaledRadius, maxU, maxV, brightness, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, scaledRadius, offset, -scaledRadius, maxU, minV, brightness, packedLight);
    }

    private void renderBeam(float length, float yaw, float pitch, int frame, PoseStack poseStack, VertexConsumer consumer, float brightness, int packedLight, float entitySize) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XN.rotationDegrees(pitch));

        this.drawCube(length, frame, poseStack, consumer, brightness, packedLight, entitySize);

        poseStack.popPose();
    }

    public void drawVertex(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer consumer, float x, float y, float z, float u, float v, float brightness, int packedLight) {
        consumer.vertex(matrix4f, x, y, z)
                .color(brightness, brightness, brightness, 1.0F)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(matrix3f, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull DragonBreathEntity pEntity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    protected int getBlockLightLevel(@NotNull DragonBreathEntity pEntity, @NotNull BlockPos pPos) {
        return 15;
    }

    @Override
    public boolean shouldRender(DragonBreathEntity pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }
}