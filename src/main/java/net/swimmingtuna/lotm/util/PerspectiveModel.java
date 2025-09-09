package net.swimmingtuna.lotm.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public interface PerspectiveModel extends BakedModel {

    @Nullable
    PerspectiveModelState getModelState();

    @Override
    default BakedModel applyTransform(ItemDisplayContext context, PoseStack pStack, boolean leftFlip) {
        PerspectiveModelState modelState = getModelState();
        if (modelState != null) {
            Transformation transform = getModelState().getTransform(context);

            Vector3f trans = transform.getTranslation();
            pStack.translate(trans.x(), trans.y(), trans.z());

            pStack.mulPose(transform.getLeftRotation());

            Vector3f scale = transform.getScale();
            pStack.scale(scale.x(), scale.y(), scale.z());

            pStack.mulPose(transform.getRightRotation());
            return this;
        }
        return BakedModel.super.applyTransform(context, pStack, leftFlip);
    }
}
