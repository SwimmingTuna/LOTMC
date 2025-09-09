package net.swimmingtuna.lotm.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.swimmingtuna.lotm.init.BlockEntityInit;

public class RealVoidBlockEntity extends BlockEntity {
    public RealVoidBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.REAL_VOID_BLOCK_ENTITY.get(), pos, state);
    }
}