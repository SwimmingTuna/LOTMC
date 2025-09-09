package net.swimmingtuna.lotm.capabilities.is_concealed_data;

import java.util.UUID;

public interface IIsConcealedCapability {
    boolean isConcealed();
    UUID concealmentOwner();
    int concealmentSequence();

    void setConcealed(boolean isConcealed);
    void setConcealmentOwner(UUID concealmentOwner);
    void setConcealmentSequence(int sequence);
}
