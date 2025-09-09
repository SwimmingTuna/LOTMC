package net.swimmingtuna.lotm.capabilities.is_concealed_data;

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

public class IsConcealedProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<IIsConcealedCapability> IS_CONCEALED = CapabilityManager.get(new CapabilityToken<IIsConcealedCapability>() {});

    private IsConcealedCapability isConcealed = null;
    private final LazyOptional<IIsConcealedCapability> optional = LazyOptional.of(this::createIsConcealed);

    private IsConcealedCapability createIsConcealed() {
        if (this.isConcealed == null) {
            this.isConcealed = new IsConcealedCapability();
        }
        return this.isConcealed;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction direction) {
        if (cap == IS_CONCEALED) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return createIsConcealed().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createIsConcealed().deserializeNBT(nbt);
    }
}
