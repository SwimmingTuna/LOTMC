package net.swimmingtuna.lotm.blocks.DimensionalSight;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TickingTileEntity extends UpdatingTileEntity implements BlockEntityTicker<TickingTileEntity> {
    public TickingTileEntity(BlockEntityType<?> type, BlockPos p, BlockState bs) {
        super(type, p, bs);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState, TickingTileEntity blockEntity) {
        this.tick();
    }

    public void tick() {
    }
}

