package net.swimmingtuna.lotm.capabilities.concealed_data;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ConcealedDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<IConcealedDataCapability> CONCEALED_DATA = CapabilityManager.get(new CapabilityToken<IConcealedDataCapability>() {});

    private ConcealedDataCapability concealedData = null;
    private final LazyOptional<IConcealedDataCapability> optional = LazyOptional.of(this::createConcealedData);

    private ConcealedDataCapability createConcealedData() {
        if (this.concealedData == null) {
            this.concealedData = new ConcealedDataCapability();
        }
        return this.concealedData;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CONCEALED_DATA) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return createConcealedData().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createConcealedData().deserializeNBT(nbt);
    }
}
