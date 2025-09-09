package net.swimmingtuna.lotm.nihilums.tweaks.EventManager;

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

public class EventsProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<IEventsCapabilityData> EVENTS_DATA
            = CapabilityManager.get(new CapabilityToken<IEventsCapabilityData>() {});

    private EventsCapabilityData eventsData = null;
    private final LazyOptional<IEventsCapabilityData> optional = LazyOptional.of(this::create);

    private EventsCapabilityData create() {
        if (this.eventsData == null) {
            this.eventsData  = new EventsCapabilityData();
        }
        return this.eventsData;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        if (capability == EVENTS_DATA) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return create().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        create().deserializeNBT(compoundTag);
    }
}
