package net.swimmingtuna.lotm.util.Replicating;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;

import javax.annotation.Nullable;
import java.util.UUID;

public class ReplicatedEntityDataHolder {
    private final @Nullable BeyonderClass pathway;
    private final int sequence;
    private final UUID originalUUID;
    private final GameProfile gameProfile;

    public ReplicatedEntityDataHolder(Player player) {
        this.pathway = BeyonderUtil.getPathway(player);
        this.sequence = BeyonderUtil.getSequence(player);
        this.originalUUID = player.getUUID();
        this.gameProfile = player.getGameProfile();
    }

    public ReplicatedEntityDataHolder(@Nullable BeyonderClass pathway, int sequence, UUID originalUUID, @Nullable GameProfile gameProfile) {
        this.pathway = pathway;
        this.sequence = sequence;
        this.originalUUID = originalUUID;
        this.gameProfile = gameProfile;
    }

    public static ReplicatedEntityDataHolder createDataHolder(Player player){
        return new ReplicatedEntityDataHolder(player);
    }

    public BeyonderClass getPathway(){
        return this.pathway != null ? this.pathway : BeyonderClassInit.APPRENTICE.get();
    }

    public int getSequence(){
        return this.sequence;
    }

    public UUID getOriginalUUID(){
        return this.originalUUID;
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();

        if (pathway != null) {
            ResourceLocation id = BeyonderClassInit.getRegistry().getKey(pathway);
            if (id != null) {
                tag.putString("PathwayId", id.toString());
            }
        }

        tag.putInt("Sequence", sequence);

        if (gameProfile != null) {
            CompoundTag profileTag = new CompoundTag();
            profileTag.putString("Name", gameProfile.getName());
            if (gameProfile.getId() != null) {
                profileTag.putUUID("UUID", gameProfile.getId());
            }
            tag.put("GameProfile", profileTag);
        }

        tag.putUUID("OriginalUUID", originalUUID);

        return tag;
    }

    public boolean isSameEntity(ReplicatedEntityDataHolder other){
        return this.originalUUID.equals(other.originalUUID);
    }

    public static ReplicatedEntityDataHolder deserialize(CompoundTag tag) {
        GameProfile profile = null;
        if (tag.contains("GameProfile")) {
            CompoundTag profileTag = tag.getCompound("GameProfile");
            String name = profileTag.getString("Name");
            UUID uuid = profileTag.contains("UUID") ? profileTag.getUUID("UUID") : UUID.randomUUID();
            profile = new GameProfile(uuid, name);
        }

        BeyonderClass pathway = null;
        if (tag.contains("PathwayId")) {
            pathway = BeyonderClassInit.getRegistry().getValue(new ResourceLocation(tag.getString("PathwayId")));
        }

        int sequence = tag.getInt("Sequence");
        UUID originalUUID = tag.contains("OriginalUUID") ? tag.getUUID("OriginalUUID") : UUID.randomUUID();

        return new ReplicatedEntityDataHolder(pathway, sequence, originalUUID, profile);
    }
}
