package net.swimmingtuna.lotm.capabilities.doll_data;

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

public class DollDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<IDollDataCapability> DOLL_DATA = CapabilityManager.get(new CapabilityToken<IDollDataCapability>() {});

    private DollDataCapability dollData = null;
    private final LazyOptional<IDollDataCapability> optional = LazyOptional.of(this::createDollData);

    private DollDataCapability createDollData(){
        if (this.dollData == null){
            this.dollData = new DollDataCapability();
        }
        return dollData;
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction direction) {
        if(cap == DOLL_DATA){
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return createDollData().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        createDollData().deserializeNBT(tag);
    }
}