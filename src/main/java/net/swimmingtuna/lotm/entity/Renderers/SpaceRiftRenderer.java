package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.Model.SpaceRiftModel;
import net.swimmingtuna.lotm.entity.SpaceRiftEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SpaceRiftRenderer extends GeoEntityRenderer<SpaceRiftEntity> {
    public SpaceRiftRenderer(EntityRendererProvider.Context context) {
        super(context, new SpaceRiftModel());
    }

    @Override
    public void render(SpaceRiftEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(SpaceRiftEntity rift) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/space_rift.png");
    }
}