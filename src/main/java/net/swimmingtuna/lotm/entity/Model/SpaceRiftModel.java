package net.swimmingtuna.lotm.entity.Model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.SpaceRiftEntity;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class SpaceRiftModel extends GeoModel<SpaceRiftEntity> {
    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getModelResource(SpaceRiftEntity entity) {
        return new ResourceLocation(LOTM.MOD_ID, "geo/space_rift.geo.json");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getTextureResource(SpaceRiftEntity entity) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/space_rift.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SpaceRiftEntity entity) {
        return new ResourceLocation(LOTM.MOD_ID, "animations/space_rift.animation.json");
    }

    @Override
    public void setCustomAnimations(SpaceRiftEntity animatable, long instanceId, AnimationState<SpaceRiftEntity> animationState) {
        CoreGeoBone door = getAnimationProcessor().getBone("rift");

        if (door != null) {
            float yaw = animatable.getYRot();
            door.setRotY(yaw * Mth.DEG_TO_RAD);
        }
    }
}