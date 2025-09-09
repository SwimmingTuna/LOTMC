package net.swimmingtuna.lotm.blocks.DimensionalSight;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DimensionalSightBlockTileEntity<T extends DimensionalTileEntity> extends BlockTileEntity<T> {
    public static final AABB boundingBox = new AABB(0.0, 0.0, 0.0, 1.0, 0.0625, 1.0);

    public DimensionalSightBlockTileEntity(BlockBehaviour.Properties material, String name) {
        super(material, name);
    }

    public DimensionalSightBlockTileEntity(String name) {
        this(BlockBehaviour.Properties.copy(Blocks.GLASS).noOcclusion(), name);
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DimensionalTileEntity(blockPos, blockState);
    }

    public VoxelShape getShape(BlockState bs, BlockGetter bg, BlockPos bp, CollisionContext ctx) {
        return Shapes.create(boundingBox);
    }

    public VoxelShape getOcclusionShape(BlockState bs, BlockGetter bg, BlockPos bp) {
        return Shapes.create(boundingBox);
    }

    public VoxelShape getCollisionShape(BlockState bs, BlockGetter bg, BlockPos bp, CollisionContext ctx) {
        return Shapes.create(boundingBox);
    }

    public VoxelShape getInteractionShape(BlockState bs, BlockGetter bg, BlockPos bp) {
        return Shapes.create(boundingBox);
    }

    public Class<T> getTileEntityClass() {
        return null;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return (a, b, c, tile) -> {
            if (tile instanceof BlockEntityTicker ticker) {
                ticker.tick(a, b, c, tile);
            }

        };
    }
}

