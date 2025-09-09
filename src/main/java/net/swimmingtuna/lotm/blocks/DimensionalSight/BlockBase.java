package net.swimmingtuna.lotm.blocks.DimensionalSight;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockBase extends Block {
    protected String name;

    public BlockBase(BlockBehaviour.Properties p, String name) {
        super(p);
        this.name = name;
    }

    public BlockItem createItemBlock() {
        return new BlockItem(this, (new Item.Properties()).stacksTo(64));
    }
}

