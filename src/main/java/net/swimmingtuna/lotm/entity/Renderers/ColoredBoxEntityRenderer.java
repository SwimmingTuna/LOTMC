package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.ColoredBoxEntity;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class ColoredBoxEntityRenderer extends EntityRenderer<ColoredBoxEntity> {

    public enum ColorMode {
        GRAY(0.5f, 0.5f, 0.5f),
        BLACK(0.1f, 0.1f, 0.1f),
        RED(1.0f, 0.2f, 0.2f),
        BLUE(0.2f, 0.4f, 1.0f),
        YELLOW(1.0f, 1.0f, 0.2f),
        PURPLE(0.8f, 0.2f, 1.0f),
        GREEN(0.2f, 1.0f, 0.2f);

        public final float red, green, blue;

        ColorMode(float red, float green, float blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }
    }

    private static final ResourceLocation TEXTURE = new ResourceLocation(LOTM.MOD_ID, "textures/entity/colored_box.png");
    private static final Map<ColorMode, RenderType> RENDER_TYPES = new HashMap<>();

    static {
        for (ColorMode mode : ColorMode.values()) {
            RENDER_TYPES.put(mode, createRenderType(mode.name().toLowerCase()));
        }
    }

    public ColoredBoxEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ColoredBoxEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        entity.noCulling = true;

        float baseSize = 10f;
        float scaleFactor = Math.max(1f, entity.getMaxSize() / baseSize);
        float size = (float) entity.getMaxSize() / scaleFactor;
        float halfHeight = size / 2.0F;
        float halfWidth = halfHeight * 0.75f;

        // Get the color mode from the entity
        ColorMode colorMode = getColorModeFromEntity(entity);
        RenderType renderType = RENDER_TYPES.get(colorMode);

        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
        Matrix4f matrix = poseStack.last().pose();

        // Render all faces with the selected color
        renderFace(vertexConsumer, matrix, colorMode,
                -halfWidth, -halfHeight, halfWidth,
                halfWidth, -halfHeight, halfWidth,
                halfWidth, halfHeight, halfWidth,
                -halfWidth, halfHeight, halfWidth, packedLight);

        renderFace(vertexConsumer, matrix, colorMode,
                -halfWidth, -halfHeight, -halfWidth,
                -halfWidth, halfHeight, -halfWidth,
                halfWidth, halfHeight, -halfWidth,
                halfWidth, -halfHeight, -halfWidth, packedLight);

        renderFace(vertexConsumer, matrix, colorMode,
                -halfWidth, -halfHeight, -halfWidth,
                -halfWidth, -halfHeight, halfWidth,
                -halfWidth, halfHeight, halfWidth,
                -halfWidth, halfHeight, -halfWidth, packedLight);

        renderFace(vertexConsumer, matrix, colorMode,
                halfWidth, -halfHeight, -halfWidth,
                halfWidth, halfHeight, -halfWidth,
                halfWidth, halfHeight, halfWidth,
                halfWidth, -halfHeight, halfWidth, packedLight);

        renderFace(vertexConsumer, matrix, colorMode,
                -halfWidth, halfHeight, -halfWidth,
                -halfWidth, halfHeight, halfWidth,
                halfWidth, halfHeight, halfWidth,
                halfWidth, halfHeight, -halfWidth, packedLight);

        renderFace(vertexConsumer, matrix, colorMode,
                -halfWidth, -halfHeight, -halfWidth,
                halfWidth, -halfHeight, -halfWidth,
                halfWidth, -halfHeight, halfWidth,
                -halfWidth, -halfHeight, halfWidth, packedLight);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderFace(VertexConsumer vertexConsumer, Matrix4f matrix, ColorMode colorMode,
                            float x1, float y1, float z1, float x2, float y2, float z2,
                            float x3, float y3, float z3, float x4, float y4, float z4, int packedLight) {

        float alpha = 0.5F; // Semi-transparent

        vertexConsumer.vertex(matrix, x1, y1, z1)
                .color(colorMode.red, colorMode.green, colorMode.blue, alpha)
                .uv(0.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();

        vertexConsumer.vertex(matrix, x2, y2, z2)
                .color(colorMode.red, colorMode.green, colorMode.blue, alpha)
                .uv(1.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();

        vertexConsumer.vertex(matrix, x3, y3, z3)
                .color(colorMode.red, colorMode.green, colorMode.blue, alpha)
                .uv(1.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();

        vertexConsumer.vertex(matrix, x4, y4, z4)
                .color(colorMode.red, colorMode.green, colorMode.blue, alpha)
                .uv(0.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    /**
     * Gets the color mode from the entity. You'll need to implement the getColorMode() method in your entity class.
     */
    private ColorMode getColorModeFromEntity(ColoredBoxEntity entity) {
        // This assumes you'll add a getColorMode() method to your entity
        // For now, defaulting to YELLOW if the method doesn't exist
        try {
            return ColorMode.valueOf(entity.getColorMode().toUpperCase());
        } catch (Exception e) {
            return ColorMode.YELLOW; // Default fallback
        }
    }

    @Override
    public ResourceLocation getTextureLocation(ColoredBoxEntity entity) {
        return TEXTURE;
    }

    private static RenderType createRenderType(String name) {
        return RenderType.create(
                "colored_box" + name,
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                true,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(TEXTURE, false, false))
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                        .createCompositeState(true)
        );
    }
}