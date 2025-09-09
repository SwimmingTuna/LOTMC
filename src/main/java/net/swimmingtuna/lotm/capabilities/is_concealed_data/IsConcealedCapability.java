package net.swimmingtuna.lotm.capabilities.is_concealed_data;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

public class IsConcealedCapability implements IIsConcealedCapability, INBTSerializable<CompoundTag> {
    private boolean isConcealed = false;
    private UUID concealmentOwner = new UUID(0, 0);
    private int concealmentSequence = 9;

    @Override
    public boolean isConcealed() {
        return isConcealed;
    }

    @Override
    public UUID concealmentOwner() {
        return concealmentOwner;
    }

    @Override
    public int concealmentSequence() {
        return concealmentSequence;
    }

    @Override
    public void setConcealed(boolean isConcealed) {
        this.isConcealed = isConcealed;
    }

    @Override
    public void setConcealmentOwner(UUID concealmentOwner) {
        this.concealmentOwner = concealmentOwner;
    }

    @Override
    public void setConcealmentSequence(int sequence) {
        this.concealmentSequence = sequence;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putBoolean("isConcealed", isConcealed);
        tag.putUUID("concealmentOwner", concealmentOwner);
        tag.putInt("concealmentSequence", concealmentSequence);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        isConcealed = tag.getBoolean("isConcealed");
        concealmentOwner = tag.getUUID("concealmentOwner");
        concealmentSequence = tag.getInt("concealmentSequence");
    }

    public void copyFrom(IsConcealedCapability other){
        isConcealed = other.isConcealed;
        concealmentOwner = other.concealmentOwner;
        concealmentSequence = other.concealmentSequence;
    }
}
