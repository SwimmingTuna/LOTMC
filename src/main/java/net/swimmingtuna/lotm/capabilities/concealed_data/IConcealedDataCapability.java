package net.swimmingtuna.lotm.capabilities.concealed_data;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface IConcealedDataCapability {
    boolean ownsConcealedSpace();
    BlockPos centerConcealedSpace();
    BlockPos spawnConcealedSpace();
    BlockPos exitConcealedSpace();
    ResourceKey<Level> exitDimensionConcealedSpace();
    int sequenceConcealedSpace();

    void setOwnsConcealedSpace(boolean ownsConcealedSpace);
    void setCenterConcealedSpace(BlockPos centerConcealedSpace);
    void setSpawnConcealedSpace(BlockPos spawnConcealedSpace);
    void setExitConcealedSpace(BlockPos exitConcealedSpace);
    void setExitDimensionConcealedSpace(ResourceKey<Level> exitDimensionConcealedSpace);
    void setSequenceConcealedSpace(int sequenceConcealedSpace);
}
