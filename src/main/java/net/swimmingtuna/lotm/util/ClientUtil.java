package net.swimmingtuna.lotm.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.block.Blocks;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalTileEntity;
import net.swimmingtuna.lotm.networking.packet.ForceLookPacketS2C;
import net.swimmingtuna.lotm.util.ClientData.ClientGrayscaleData;
import net.swimmingtuna.lotm.util.ClientData.ClientLookData;
import org.joml.Matrix4f;

public class ClientUtil {

    public static void handleForceLook(ForceLookPacketS2C packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            if (packet.isSmooth()) {
                ClientLookData.setTargetRotation(packet.getYaw(), packet.getPitch());
                ClientLookData.setSmoothLooking(true);
            } else {
                ForceLookPacketS2C.setPlayerRotation(mc.player, packet.getYaw(), packet.getPitch());
                ClientLookData.setSmoothLooking(false);
            }
        }
    }

    public static void removeDimensionalSight(DimensionalTileEntity dimensionalTileEntity) {
        if (dimensionalTileEntity.getLevel() != null) {
            dimensionalTileEntity.getLevel().setBlock(dimensionalTileEntity.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
        }
    }

    public static void renderGrayscaleUsingGUI(GuiGraphics guiGraphics) {
        if (!ClientGrayscaleData.isActive()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        float intensity = ClientGrayscaleData.getCurrentIntensity();
        if (intensity <= 0.0f) return;
        int alpha = (int)(intensity * 180);
        if (intensity > 0.7f) {
            guiGraphics.fill(0, 0, screenWidth, screenHeight, (alpha << 24) | 0x101010);
            guiGraphics.fill(0, 0, screenWidth, screenHeight, ((alpha/2) << 24) | 0x202020);
            guiGraphics.fill(0, 0, screenWidth, screenHeight, ((alpha/3) << 24) | 0x404040);
        } else if (intensity > 0.3f) {
            guiGraphics.fill(0, 0, screenWidth, screenHeight, (alpha << 24) | 0x202020);
            guiGraphics.fill(0, 0, screenWidth, screenHeight, ((alpha/2) << 24) | 0x404040);
        } else {
            guiGraphics.fill(0, 0, screenWidth, screenHeight, (alpha << 24) | 0x303030);
        }
    }

    public static void renderLayeredGrayscale() {
        if (!ClientGrayscaleData.isActive()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getWidth();
        int screenHeight = mc.getWindow().getHeight();

        float intensity = ClientGrayscaleData.getCurrentIntensity();
        if (intensity <= 0.0f) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        PoseStack poseStack = new PoseStack();
        Matrix4f matrix = poseStack.last().pose();

        // Multiple layers for more realistic grayscale effect
        float baseAlpha = intensity * 0.2f;

        // Layer 1: Dark gray
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrix, 0, screenHeight, -90).color(0.2f, 0.2f, 0.2f, baseAlpha).endVertex();
        buffer.vertex(matrix, screenWidth, screenHeight, -90).color(0.2f, 0.2f, 0.2f, baseAlpha).endVertex();
        buffer.vertex(matrix, screenWidth, 0, -90).color(0.2f, 0.2f, 0.2f, baseAlpha).endVertex();
        buffer.vertex(matrix, 0, 0, -90).color(0.2f, 0.2f, 0.2f, baseAlpha).endVertex();
        tesselator.end();

        // Layer 2: Medium gray
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrix, 0, screenHeight, -89).color(0.5f, 0.5f, 0.5f, baseAlpha).endVertex();
        buffer.vertex(matrix, screenWidth, screenHeight, -89).color(0.5f, 0.5f, 0.5f, baseAlpha).endVertex();
        buffer.vertex(matrix, screenWidth, 0, -89).color(0.5f, 0.5f, 0.5f, baseAlpha).endVertex();
        buffer.vertex(matrix, 0, 0, -89).color(0.5f, 0.5f, 0.5f, baseAlpha).endVertex();
        tesselator.end();

        // Layer 3: Light gray (if intensity is high)
        if (intensity > 0.5f) {
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buffer.vertex(matrix, 0, screenHeight, -88).color(0.8f, 0.8f, 0.8f, baseAlpha * 0.5f).endVertex();
            buffer.vertex(matrix, screenWidth, screenHeight, -88).color(0.8f, 0.8f, 0.8f, baseAlpha * 0.5f).endVertex();
            buffer.vertex(matrix, screenWidth, 0, -88).color(0.8f, 0.8f, 0.8f, baseAlpha * 0.5f).endVertex();
            buffer.vertex(matrix, 0, 0, -88).color(0.8f, 0.8f, 0.8f, baseAlpha * 0.5f).endVertex();
            tesselator.end();
        }

        RenderSystem.disableBlend();
    }
}
