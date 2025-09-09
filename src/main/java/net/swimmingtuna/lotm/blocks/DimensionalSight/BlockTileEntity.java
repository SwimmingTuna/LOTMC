package net.swimmingtuna.lotm.blocks.DimensionalSight;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class BlockTileEntity<TE extends BlockEntity> extends BlockBase implements EntityBlock {
    public BlockTileEntity(BlockBehaviour.Properties material, String name) {
        super(material, name);
    }

    public abstract Class<TE> getTileEntityClass();

    @Nullable
    public TE getTileEntity(Level world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null && getTileEntityClass().isInstance(blockEntity)) {
            return getTileEntityClass().cast(blockEntity);
        }
        return null;
    }

    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    public TE createTileEntity(BlockPos pos, BlockState bs, Level level) {
        BlockEntity blockEntity = this.newBlockEntity(pos, bs);
        if (blockEntity != null && getTileEntityClass().isInstance(blockEntity)) {
            return getTileEntityClass().cast(blockEntity);
        }
        return null;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> m_142354_(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return (a, b, c, tile) -> {
            if (tile instanceof TickingTileEntity) {
                ((TickingTileEntity)tile).tick();
            }
        };
    }
}