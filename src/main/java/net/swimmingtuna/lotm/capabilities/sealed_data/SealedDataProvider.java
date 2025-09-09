package net.swimmingtuna.lotm.capabilities.sealed_data;

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

public class SealedDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<ISealedDataCapability> SEALED_DATA = CapabilityManager.get(new CapabilityToken<ISealedDataCapability>() {});

    private SealedDataCapability sealedData = null;
    private final LazyOptional<ISealedDataCapability> optional = LazyOptional.of(this::createSealedData);

    private SealedDataCapability createSealedData() {
        if (this.sealedData == null) {
            this.sealedData = new SealedDataCapability();
        }
        return this.sealedData;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction direction) {
        if(cap == SEALED_DATA){
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return createSealedData().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        createSealedData().deserializeNBT(tag);
    }
}