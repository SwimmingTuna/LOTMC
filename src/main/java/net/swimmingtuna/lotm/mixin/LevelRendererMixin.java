package net.swimmingtuna.lotm.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LevelRenderer.class})
public class LevelRendererMixin {
    @Unique
    private static final ResourceLocation CRIMSON_MOON_TEXTURE = new ResourceLocation(LOTM.MOD_ID, "textures/entity/crimson_moon.png");

    @Unique
    private int lordOfTheMysteries$moonVertexCounter = 0;

    public LevelRendererMixin() {
    }

    @Redirect(
            method = {"renderSky"},
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V",
                    ordinal = 1
            )
    )
    private void switchMoonTexture(int slot, ResourceLocation original) {
        // Always use crimson moon texture
        RenderSystem.setShaderTexture(slot, CRIMSON_MOON_TEXTURE);
    }

    @ModifyConstant(
            method = {"renderSky"},
            constant = @Constant(floatValue = 20.0F)
    )
    private float scaleMoonSize(float original) {
        // Scale moon size by 3x
        return original * 2.5F;
    }

    @Inject(
            method = {"renderSky"},
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V",
                    ordinal = 1
            )
    )
    private void resetMoonVertexCounter(PoseStack poseStack, Matrix4f matrix4f, float partialTicks, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
        this.lordOfTheMysteries$moonVertexCounter = 0;
    }

    @Redirect(
            method = {"renderSky"},
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;uv(FF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
            )
    )
    private VertexConsumer overrideMoonUV(VertexConsumer consumer, float u, float v) {
        // Custom UV mapping for full texture display
        switch (this.lordOfTheMysteries$moonVertexCounter % 4) {
            case 0 -> consumer.uv(0.0F, 1.0F); // Bottom-left
            case 1 -> consumer.uv(1.0F, 1.0F); // Bottom-right
            case 2 -> consumer.uv(1.0F, 0.0F); // Top-right
            case 3 -> consumer.uv(0.0F, 0.0F); // Top-left
        }

        ++this.lordOfTheMysteries$moonVertexCounter;
        return consumer;
    }
}