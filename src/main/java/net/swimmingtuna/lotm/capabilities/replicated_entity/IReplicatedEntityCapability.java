package net.swimmingtuna.lotm.capabilities.replicated_entity;


import net.swimmingtuna.lotm.util.Replicating.ReplicatedEntityDataHolder;

import java.util.List;

public interface IReplicatedEntityCapability {
    List<ReplicatedEntityDataHolder> replicatedEntities();

    int maxEntities();

    int maxAbilitiesUse();

    void addReplicatedEntities(ReplicatedEntityDataHolder dataHolder);

    void removeReplicatedEntities(ReplicatedEntityDataHolder dataHolder);

    void setMaxEntities(int maxEntities);

    void setMaxAbilitiesUse(int maxAbilitiesUse);
}