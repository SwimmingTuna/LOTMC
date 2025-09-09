package net.swimmingtuna.lotm.blocks.DimensionalSight;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class DimensionalSightBlock extends DimensionalSightBlockTileEntity<DimensionalSightTileEntity> {
    public DimensionalSightBlock() {
        super("dimensional_sight");
    }

    public Class<DimensionalSightTileEntity> getTileEntityClass() {
        return DimensionalSightTileEntity.class;
    }

    @Nullable
    public DimensionalSightTileEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DimensionalSightTileEntity(blockPos, blockState);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof DimensionalSightTileEntity dimensionalSightTileEntity) {

            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return (a, b, c, tile) -> {
            if (tile instanceof DimensionalSightTileEntity) {
                ((DimensionalSightTileEntity)tile).tick(a, b, c, (DimensionalSightTileEntity)tile);
            }

        };
    }
}

