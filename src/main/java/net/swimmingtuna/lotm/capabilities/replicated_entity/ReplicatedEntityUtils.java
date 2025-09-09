package net.swimmingtuna.lotm.capabilities.replicated_entity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.util.Replicating.ReplicatedEntityDataHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReplicatedEntityUtils {

    public static Optional<IReplicatedEntityCapability> getReplicatedEntityData(Player player) {
        return player.getCapability(ReplicatedEntityProvider.REPLICATED_ENTITY).resolve();
    }

    public static List<ReplicatedEntityDataHolder> getEntities(Player player){
        return getReplicatedEntityData(player).map(IReplicatedEntityCapability::replicatedEntities).orElse(new ArrayList<>());
    }

    public static int getMaxEntities(Player player){
        return getReplicatedEntityData(player).map(IReplicatedEntityCapability::maxEntities).orElse(5);
    }

    public static int getMaxAbilitiesUse(Player player){
        return getReplicatedEntityData(player).map(IReplicatedEntityCapability::maxAbilitiesUse).orElse(1);
    }

    public static void addReplicatedEntities(Player player, Player target){
        player.getCapability(ReplicatedEntityProvider.REPLICATED_ENTITY).ifPresent(data -> {
            if (data.replicatedEntities().size() < data.maxEntities()) {
                data.addReplicatedEntities(ReplicatedEntityDataHolder.createDataHolder(target));
                player.displayClientMessage(Component.literal("Entity successfully Replicated"), true);
            }else{
                player.displayClientMessage(Component.literal("You can't Replicate anymore entities"), true);
            }
        });
    }

    public static void removeReplicatedEntities(Player player, ReplicatedEntityDataHolder dataHolder){
        player.getCapability(ReplicatedEntityProvider.REPLICATED_ENTITY).ifPresent(data -> {
            data.removeReplicatedEntities(dataHolder);
        });
    }

    public static void setMaxEntities(Player player, int max){
        player.getCapability(ReplicatedEntityProvider.REPLICATED_ENTITY).ifPresent(data -> {
            data.setMaxEntities(max);
        });
    }

    public static void setMaxAbilitiesUse(Player player, int max){
        player.getCapability(ReplicatedEntityProvider.REPLICATED_ENTITY).ifPresent(data -> {
            data.setMaxAbilitiesUse(max);
        });
    }

    public static PlayerMobEntity getEntity(Player user, ReplicatedEntityDataHolder dataHolder){
        PlayerMobEntity clone = new PlayerMobEntity(EntityInit.PLAYER_MOB_ENTITY.get(), user.level(), dataHolder.getPathway(), dataHolder.getSequence(), 0);
        clone.setSequence(dataHolder.getSequence());
        clone.setPathway(dataHolder.getPathway());
        clone.setIsClone(true);
        clone.setCreator(user.getUUID());
        clone.setProfile(dataHolder.getGameProfile());
        clone.setUsername(dataHolder.getGameProfile().getName());
        clone.setHasAbilityCap(true);
        clone.setMaxAbilitiesUse(getMaxAbilitiesUse(user));
        return clone;
    }
}