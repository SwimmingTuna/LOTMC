package net.swimmingtuna.lotm.capabilities.replicated_entity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReplicatedEntityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<IReplicatedEntityCapability> REPLICATED_ENTITY = CapabilityManager.get(new CapabilityToken<IReplicatedEntityCapability>() {});

    private ReplicatedEntityCapability replicatedEntity = null;
    private final LazyOptional<IReplicatedEntityCapability> optional = LazyOptional.of(this::createReplicatedEntity);


    private ReplicatedEntityCapability createReplicatedEntity(){
        if(this.replicatedEntity == null){
            this.replicatedEntity = new ReplicatedEntityCapability();
        }
        return this.replicatedEntity;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction direction) {
        if(cap == REPLICATED_ENTITY){
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return createReplicatedEntity().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        createReplicatedEntity().deserializeNBT(tag);
    }
}