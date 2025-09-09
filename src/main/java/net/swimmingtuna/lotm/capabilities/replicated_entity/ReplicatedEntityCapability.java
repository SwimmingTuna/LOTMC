package net.swimmingtuna.lotm.capabilities.replicated_entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.swimmingtuna.lotm.util.Replicating.ReplicatedEntityDataHolder;

import java.util.ArrayList;
import java.util.List;

public class ReplicatedEntityCapability implements IReplicatedEntityCapability, INBTSerializable<CompoundTag> {
    private List<ReplicatedEntityDataHolder> replicatedEntities = new ArrayList<>();
    private int maxEntities = 5;
    private int maxAbilitiesUse = 1;

    @Override
    public List<ReplicatedEntityDataHolder> replicatedEntities() {
        return this.replicatedEntities;
    }

    @Override
    public int maxEntities() {
        return this.maxEntities;
    }

    @Override
    public int maxAbilitiesUse() {
        return this.maxAbilitiesUse;
    }

    @Override
    public void addReplicatedEntities(ReplicatedEntityDataHolder dataHolder) {
        if (replicatedEntities.size() < maxEntities) {
            replicatedEntities.add(dataHolder);
        }
    }

    @Override
    public void removeReplicatedEntities(ReplicatedEntityDataHolder dataHolder) {
        replicatedEntities.removeIf(existing -> existing.isSameEntity(dataHolder));
    }

    @Override
    public void setMaxEntities(int maxEntities) {
        this.maxEntities = maxEntities;
    }

    @Override
    public void setMaxAbilitiesUse(int maxAbilitiesUse) {
        this.maxAbilitiesUse = maxAbilitiesUse;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        ListTag entityList = new net.minecraft.nbt.ListTag();
        for (ReplicatedEntityDataHolder holder : replicatedEntities) {
            entityList.add(holder.serialize());
        }
        tag.put("ReplicatedEntities", entityList);

        tag.putInt("maxEntities", maxEntities);
        tag.putInt("maxAbilitiesUse", maxAbilitiesUse);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {

        this.replicatedEntities = new ArrayList<>();
        if (tag.contains("ReplicatedEntities", net.minecraft.nbt.Tag.TAG_LIST)) {
            ListTag entityList = tag.getList("ReplicatedEntities", net.minecraft.nbt.Tag.TAG_COMPOUND);
            for (int i = 0; i < entityList.size(); i++) {
                CompoundTag holderTag = entityList.getCompound(i);
                ReplicatedEntityDataHolder holder = ReplicatedEntityDataHolder.deserialize(holderTag);
                this.replicatedEntities.add(holder);
            }
        }

        this.maxEntities = tag.getInt("maxEntities");
        this.maxAbilitiesUse = tag.getInt("maxAbilitiesUse");
    }


    public void copyFrom(ReplicatedEntityCapability other){
        this.replicatedEntities = new ArrayList<>(other.replicatedEntities);
        this.maxEntities = other.maxEntities;
        this.maxAbilitiesUse = other.maxAbilitiesUse;
    }
}