package net.swimmingtuna.lotm.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VoidGlass extends Block {
    public VoidGlass() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .strength(100.0F, 3600.0F)
                .noLootTable()
                .noOcclusion()
                .requiresCorrectToolForDrops()
                .lightLevel(state -> 15));
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return adjacentBlockState.is(this); // Don't render inner sides between same blocks
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty(); // This hides the outline when targeting with cursor
    }
}