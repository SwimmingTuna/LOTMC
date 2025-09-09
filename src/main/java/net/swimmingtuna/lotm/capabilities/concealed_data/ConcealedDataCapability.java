package net.swimmingtuna.lotm.capabilities.concealed_data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

public class ConcealedDataCapability implements IConcealedDataCapability, INBTSerializable<CompoundTag> {
    private boolean ownsConcealedSpace = false;
    private BlockPos centerConcealedSpace = BlockPos.ZERO;
    private BlockPos spawnConcealedSpace = BlockPos.ZERO;
    private BlockPos exitConcealedSpace = BlockPos.ZERO;
    private ResourceKey<Level> exitDimensionConcealedSpace = Level.OVERWORLD;
    private int sequenceConcealedSpace = 9;

    @Override
    public boolean ownsConcealedSpace() {
        return ownsConcealedSpace;
    }

    @Override
    public BlockPos centerConcealedSpace() {
        return centerConcealedSpace;
    }

    @Override
    public BlockPos spawnConcealedSpace() {
        return spawnConcealedSpace;
    }

    @Override
    public BlockPos exitConcealedSpace() {
        return exitConcealedSpace;
    }

    @Override
    public ResourceKey<Level> exitDimensionConcealedSpace() {
        return exitDimensionConcealedSpace;
    }

    @Override
    public int sequenceConcealedSpace() {
        return sequenceConcealedSpace;
    }

    @Override
    public void setOwnsConcealedSpace(boolean ownsConcealedSpace) {
        this.ownsConcealedSpace = ownsConcealedSpace;
    }

    @Override
    public void setCenterConcealedSpace(BlockPos centerConcealedSpace) {
        this.centerConcealedSpace = centerConcealedSpace;
    }

    @Override
    public void setSpawnConcealedSpace(BlockPos spawnConcealedSpace) {
        this.spawnConcealedSpace = spawnConcealedSpace;
    }

    @Override
    public void setExitConcealedSpace(BlockPos exitConcealedSpace) {
        this.exitConcealedSpace = exitConcealedSpace;
    }

    @Override
    public void setExitDimensionConcealedSpace(ResourceKey<Level> exitDimensionConcealedSpace) {
        this.exitDimensionConcealedSpace = exitDimensionConcealedSpace;
    }

    @Override
    public void setSequenceConcealedSpace(int sequenceConcealedSpace) {
        this.sequenceConcealedSpace = sequenceConcealedSpace;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putBoolean("ownsConcealedSpace", ownsConcealedSpace);
        tag.putLong("centerConcealedSpace", centerConcealedSpace.asLong());
        tag.putLong("spawnConcealedSpace", spawnConcealedSpace.asLong());
        tag.putLong("exitConcealedSpace", exitConcealedSpace.asLong());
        tag.putString("exitDimensionConcealedSpace", exitDimensionConcealedSpace.location().toString());
        tag.putInt("sequenceConcealedSpace", sequenceConcealedSpace);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        ownsConcealedSpace = tag.getBoolean("ownsConcealedSpace");
        centerConcealedSpace = BlockPos.of(tag.getLong("centerConcealedSpace"));
        spawnConcealedSpace = BlockPos.of(tag.getLong("spawnConcealedSpace"));
        exitConcealedSpace = BlockPos.of(tag.getLong("exitConcealedSpace"));

        String dimString = tag.getString("exitDimensionConcealedSpace");
        exitDimensionConcealedSpace = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimString));

        sequenceConcealedSpace = tag.getInt("sequenceConcealedSpace");
    }

    public void copyFrom(ConcealedDataCapability other) {
        this.ownsConcealedSpace = other.ownsConcealedSpace;
        this.centerConcealedSpace = other.centerConcealedSpace;
        this.spawnConcealedSpace = other.spawnConcealedSpace;
        this.exitConcealedSpace = other.exitConcealedSpace;
        this.exitDimensionConcealedSpace = other.exitDimensionConcealedSpace;
        this.sequenceConcealedSpace = other.sequenceConcealedSpace;
    }
}
