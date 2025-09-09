package net.swimmingtuna.lotm.capabilities.scribed_abilities;

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

public class ScribedAbilitiesProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<IScribedAbilitiesCapability> SCRIBED_ABILITIES = CapabilityManager.get(new CapabilityToken<IScribedAbilitiesCapability>() {});

    private ScribedAbilitiesCapability scribedAbilities = null;
    private final LazyOptional<IScribedAbilitiesCapability> optional = LazyOptional.of(this::createScribedAbilities);

    private ScribedAbilitiesCapability createScribedAbilities(){
        if(this.scribedAbilities == null){
            this.scribedAbilities = new ScribedAbilitiesCapability();
        }
        return this.scribedAbilities;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == SCRIBED_ABILITIES){
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return createScribedAbilities().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createScribedAbilities().deserializeNBT(nbt);
    }
}