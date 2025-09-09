package net.swimmingtuna.lotm.entity.Model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.SpaceFragmentationEntity;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class SpaceFragmentationModel extends GeoModel<SpaceFragmentationEntity> {

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getModelResource(SpaceFragmentationEntity spaceFragmentationEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "geo/space_fragmentation.geo.json");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getTextureResource(SpaceFragmentationEntity spaceFragmentationEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/space_fragmentation.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SpaceFragmentationEntity spaceFragmentationEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "animations/space_fragmentation.animation.json");
    }

    @Override
    public void setCustomAnimations(SpaceFragmentationEntity animatable, long instanceId, AnimationState<SpaceFragmentationEntity> animationState) {
        CoreGeoBone door = getAnimationProcessor().getBone("rift");

        if (door != null) {
            float yaw = animatable.getYRot();
            door.setRotY(yaw * Mth.DEG_TO_RAD);
        }
    }
}
