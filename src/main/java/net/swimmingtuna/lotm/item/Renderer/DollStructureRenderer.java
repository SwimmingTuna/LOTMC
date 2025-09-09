package net.swimmingtuna.lotm.item.Renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.util.IItemRenderer;
import net.swimmingtuna.lotm.util.PerspectiveModelState;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class DollStructureRenderer implements IItemRenderer {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final BlockPos.MutableBlockPos MUTABLE_POS = new BlockPos.MutableBlockPos();


    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        ListTag structureList = stack.getOrCreateTag().getList("StructureBlocks", Tag.TAG_COMPOUND);
        if (structureList.isEmpty()) return;

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        Set<BlockPos> structurePositions = new HashSet<>();

        for (Tag t : structureList) {
            CompoundTag tag = (CompoundTag) t;
            BlockPos pos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
            structurePositions.add(pos);

            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;
        int maxSize = Math.max(sizeX, Math.max(sizeY, sizeZ));

        float targetCubeSize;
        if (ctx == ItemDisplayContext.GUI || ctx == ItemDisplayContext.FIXED) {
            targetCubeSize = 1.0f;
        } else {
            targetCubeSize = 0.25f;
        }

        float scale = targetCubeSize / maxSize;
        float centerX = (minX + maxX + 1) / 2f;
        float centerY = minY;
        float centerZ = (minZ + maxZ + 1) / 2f;

        poseStack.pushPose();

        switch (ctx) {
            case GUI, FIXED, GROUND -> poseStack.translate(0.5f, 0f, 0.5);
            default -> poseStack.translate(0.5f, 0.5f, 0.6f);
        }

        poseStack.scale(scale, scale, scale);
        poseStack.translate(-centerX, -centerY, -centerZ);

        var dispatcher = Minecraft.getInstance().getBlockRenderer();

        for (Tag t : structureList) {
            CompoundTag tag = (CompoundTag) t;
            int x = tag.getInt("x");
            int y = tag.getInt("y");
            int z = tag.getInt("z");
            BlockPos pos = new BlockPos(x, y, z);

            if (!isExposed(pos, structurePositions)) continue;

            ResourceLocation id = new ResourceLocation(tag.getString("block"));
            Block block = ForgeRegistries.BLOCKS.getValue(id);
            if (block == null) continue;

            BlockState state = block.defaultBlockState();

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            dispatcher.renderSingleBlock(state, poseStack, buffer, 0xF000F0, overlay);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private boolean isExposed(BlockPos pos, Set<BlockPos> positions) {
        for (Direction dir : DIRECTIONS) {
            MUTABLE_POS.setWithOffset(pos, dir);
            if (!positions.contains(MUTABLE_POS)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable PerspectiveModelState getModelState() {
        return null;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }
}