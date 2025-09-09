package net.swimmingtuna.lotm.blocks.DimensionalSight;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DimensionalSightBE extends DimensionalSightBlockTileEntity<DimensionalTileEntity> {
    public DimensionalSightBE() {
        super("mahoujin");
    }


    public Class<DimensionalTileEntity> getTileEntityClass() {
        return DimensionalTileEntity.class;
    }

    @Nullable
    public DimensionalTileEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DimensionalTileEntity(blockPos, blockState);
    }
}

