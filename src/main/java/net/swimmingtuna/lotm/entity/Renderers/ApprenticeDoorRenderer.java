package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.ApprenticeDoorEntity;
import net.swimmingtuna.lotm.entity.Model.ApprenticeDoorModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ApprenticeDoorRenderer extends GeoEntityRenderer<ApprenticeDoorEntity> {
    public ApprenticeDoorRenderer(EntityRendererProvider.Context context) {
        super(context, new ApprenticeDoorModel());
    }

    private ResourceLocation getTexture(ApprenticeDoorEntity door){
        ResourceLocation texture;
        if(door.getSequence() > 7){
            texture = new ResourceLocation(LOTM.MOD_ID, "textures/entity/low_sequence_door.png");
        }else if(door.getSequence() > 3){
            texture = new ResourceLocation(LOTM.MOD_ID, "textures/entity/mid_sequence_door.png");
        }else{
            texture = new ResourceLocation(LOTM.MOD_ID, "textures/entity/high_sequence_door.png");
        }
        return texture;
    }

    @Override
    public ResourceLocation getTextureLocation(ApprenticeDoorEntity door) {
        return getTexture(door);
    }

    @Override
    protected void applyRotations(ApprenticeDoorEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.applyRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);

        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYaw()));
    }
}