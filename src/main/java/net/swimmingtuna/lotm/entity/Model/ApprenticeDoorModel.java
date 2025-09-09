package net.swimmingtuna.lotm.entity.Model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.ApprenticeDoorEntity;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class ApprenticeDoorModel extends GeoModel<ApprenticeDoorEntity> {
    private ResourceLocation getModel(ApprenticeDoorEntity door){
        ResourceLocation model;
        if(door.getSequence() > 7){
            model = new ResourceLocation(LOTM.MOD_ID, "geo/low_sequence_door.geo.json");
        }else if(door.getSequence() >3){
            model = new ResourceLocation(LOTM.MOD_ID, "geo/mid_sequence_door.geo.json");
        }else{
            model = new ResourceLocation(LOTM.MOD_ID, "geo/high_sequence_door.geo.json");
        }
        return model;
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

    private ResourceLocation getAnimation(ApprenticeDoorEntity door){
        ResourceLocation animation;
        if(door.getSequence() > 7){
            animation = new ResourceLocation(LOTM.MOD_ID, "animations/low_sequence_door.animation.json");
        }else if(door.getSequence() > 3){
            animation = new ResourceLocation(LOTM.MOD_ID, "animations/mid_sequence_door.animation.json");
        }else{
            animation = new ResourceLocation(LOTM.MOD_ID, "animations/high_sequence_door.animation.json");
        }
        return animation;
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getModelResource(ApprenticeDoorEntity door) {
        return getModel(door);
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getTextureResource(ApprenticeDoorEntity door) {
        return getTexture(door);
    }

    @Override
    public ResourceLocation getAnimationResource(ApprenticeDoorEntity door) {
        return getAnimation(door);
    }

    @Override
    public void setCustomAnimations(ApprenticeDoorEntity animatable, long instanceId, AnimationState<ApprenticeDoorEntity> animationState) {
        CoreGeoBone door = getAnimationProcessor().getBone("door");

        if (door != null) {
            float yaw = animatable.getYRot();
            door.setRotY(yaw * Mth.DEG_TO_RAD);
        }
    }
}