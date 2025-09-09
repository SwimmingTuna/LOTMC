package net.swimmingtuna.lotm.util.PlayerMobs;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerMobSequenceData extends SavedData {
    private static final String DATA_NAME = "lotm_server_map";
    private final Set<UUID> trackedUUIDs = new HashSet<>();
    private final Map<UUID, PathwaySequenceData> pathwaySequenceMap = new HashMap<>();
    public static class PathwaySequenceData {
        @Nullable
        private final BeyonderClass pathway;
        @Nullable
        private final Integer sequence;
        public PathwaySequenceData(@Nullable BeyonderClass pathway, @Nullable Integer sequence) {
            this.pathway = pathway;
            this.sequence = sequence;
        }

        @Nullable
        public BeyonderClass getPathway() {
            return pathway;
        }

        @Nullable
        public Integer getSequence() {
            return sequence;
        }

        public boolean hasPathway() {
            return pathway != null;
        }

        public boolean hasSequence() {
            return sequence != null;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            PathwaySequenceData that = (PathwaySequenceData) obj;
            return Objects.equals(pathway, that.pathway) && Objects.equals(sequence, that.sequence);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pathway, sequence);
        }

        @Override
        public String toString() {
            return "PathwaySequenceData{pathway=" +
                    (pathway != null ? BeyonderUtil.getPathwayName(pathway) : "null") +
                    ", sequence=" + sequence + "}";
        }
    }

    // Constructor
    public PlayerMobSequenceData() {
        super();
    }

    // Static method to get or create the data
    public static PlayerMobSequenceData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                PlayerMobSequenceData::load,
                PlayerMobSequenceData::new,
                DATA_NAME
        );
    }

    // Load from NBT
    public static PlayerMobSequenceData load(CompoundTag tag) {
        PlayerMobSequenceData data = new PlayerMobSequenceData();

        // Load UUIDs
        if (tag.contains("TrackedUUIDs", Tag.TAG_LIST)) {
            ListTag uuidList = tag.getList("TrackedUUIDs", Tag.TAG_STRING);
            for (int i = 0; i < uuidList.size(); i++) {
                try {
                    UUID uuid = UUID.fromString(uuidList.getString(i));
                    data.trackedUUIDs.add(uuid);
                } catch (IllegalArgumentException e) {
                    // Skip invalid UUIDs
                }
            }
        }

        // Load Pathway and Sequence data
        if (tag.contains("PathwaySequenceData", Tag.TAG_COMPOUND)) {
            CompoundTag pathwayData = tag.getCompound("PathwaySequenceData");
            for (String uuidString : pathwayData.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    CompoundTag entityData = pathwayData.getCompound(uuidString);

                    BeyonderClass pathway = null;
                    Integer sequence = null;

                    if (entityData.contains("Pathway")) {
                        String pathwayName = entityData.getString("Pathway");
                        if (!pathwayName.isEmpty()) {
                            pathway = BeyonderUtil.getPathwayByName(pathwayName);
                        }
                    }

                    if (entityData.contains("Sequence")) {
                        sequence = entityData.getInt("Sequence");
                        // Treat -1 as null (common pattern in your code)
                        if (sequence == -1) {
                            sequence = null;
                        }
                    }

                    data.pathwaySequenceMap.put(uuid, new PathwaySequenceData(pathway, sequence));
                } catch (IllegalArgumentException e) {
                    // Skip invalid UUIDs
                }
            }
        }

        return data;
    }

    // Save to NBT
    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag uuidList = new ListTag();
        for (UUID uuid : trackedUUIDs) {
            uuidList.add(StringTag.valueOf(uuid.toString()));
        }
        tag.put("TrackedUUIDs", uuidList);
        CompoundTag pathwayData = new CompoundTag();
        for (Map.Entry<UUID, PathwaySequenceData> entry : pathwaySequenceMap.entrySet()) {
            CompoundTag entityData = new CompoundTag();
            PathwaySequenceData data = entry.getValue();
            if (data.hasPathway()) {
                entityData.putString("Pathway", BeyonderUtil.getPathwayName(data.getPathway()));
            }
            if (data.hasSequence()) {
                entityData.putInt("Sequence", data.getSequence());
            }
            pathwayData.put(entry.getKey().toString(), entityData);
        }
        tag.put("PathwaySequenceData", pathwayData);

        return tag;
    }

    public void addUUID(UUID uuid) {
        trackedUUIDs.add(uuid);
        setDirty();
    }

    public void removeUUID(UUID uuid) {
        trackedUUIDs.remove(uuid);
        pathwaySequenceMap.remove(uuid);
        setDirty();
    }

    public boolean hasUUID(UUID uuid) {
        return trackedUUIDs.contains(uuid);
    }

    public Set<UUID> getAllUUIDs() {
        return new HashSet<>(trackedUUIDs);
    }

    public int getUUIDCount() {
        return trackedUUIDs.size();
    }

    public void clearUUIDs() {
        trackedUUIDs.clear();
        pathwaySequenceMap.clear();
        setDirty();
    }

    // Pathway and Sequence Section Methods
    public void setPathwaySequenceData(UUID uuid, @Nullable BeyonderClass pathway, @Nullable Integer sequence) {
        pathwaySequenceMap.put(uuid, new PathwaySequenceData(pathway, sequence));
        if (!trackedUUIDs.contains(uuid)) {
            trackedUUIDs.add(uuid);
        }
        setDirty();
    }

    public void setPathway(UUID uuid, @Nullable BeyonderClass pathway) {
        PathwaySequenceData existing = pathwaySequenceMap.get(uuid);
        Integer currentSequence = existing != null ? existing.getSequence() : null;
        setPathwaySequenceData(uuid, pathway, currentSequence);
    }

    public void setSequence(UUID uuid, @Nullable Integer sequence) {
        PathwaySequenceData existing = pathwaySequenceMap.get(uuid);
        BeyonderClass currentPathway = existing != null ? existing.getPathway() : null;
        setPathwaySequenceData(uuid, currentPathway, sequence);
    }

    @Nullable
    public PathwaySequenceData getPathwaySequenceData(UUID uuid) {
        return pathwaySequenceMap.get(uuid);
    }

    @Nullable
    public BeyonderClass getPathway(UUID uuid) {
        PathwaySequenceData data = pathwaySequenceMap.get(uuid);
        return data != null ? data.getPathway() : null;
    }

    @Nullable
    public Integer getSequence(UUID uuid) {
        PathwaySequenceData data = pathwaySequenceMap.get(uuid);
        return data != null ? data.getSequence() : null;
    }

    public boolean hasPathwayData(UUID uuid) {
        return pathwaySequenceMap.containsKey(uuid);
    }

    public void removePathwaySequenceData(UUID uuid) {
        pathwaySequenceMap.remove(uuid);
        setDirty();
    }

    public Map<UUID, PathwaySequenceData> getAllPathwaySequenceData() {
        return new HashMap<>(pathwaySequenceMap);
    }

    public int getPathwayDataCount() {
        return pathwaySequenceMap.size();
    }

    public void clearPathwaySequenceData() {
        pathwaySequenceMap.clear();
        setDirty();
    }

    // Utility Methods
    public List<UUID> getUUIDsWithPathway(BeyonderClass pathway) {
        List<UUID> result = new ArrayList<>();
        for (Map.Entry<UUID, PathwaySequenceData> entry : pathwaySequenceMap.entrySet()) {
            if (Objects.equals(entry.getValue().getPathway(), pathway)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public List<UUID> getUUIDsWithSequence(int sequence) {
        List<UUID> result = new ArrayList<>();
        for (Map.Entry<UUID, PathwaySequenceData> entry : pathwaySequenceMap.entrySet()) {
            if (Objects.equals(entry.getValue().getSequence(), sequence)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public List<UUID> getUUIDsWithPathwayAndSequence(BeyonderClass pathway, int sequence) {
        List<UUID> result = new ArrayList<>();
        for (Map.Entry<UUID, PathwaySequenceData> entry : pathwaySequenceMap.entrySet()) {
            PathwaySequenceData data = entry.getValue();
            if (Objects.equals(data.getPathway(), pathway) && Objects.equals(data.getSequence(), sequence)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public void cleanup() {
        pathwaySequenceMap.entrySet().removeIf(entry -> !trackedUUIDs.contains(entry.getKey()));
        setDirty();
    }

    protected boolean shouldTrackEntity(Entity entity) {
        if (entity instanceof PlayerMobEntity playerMobEntity && playerMobEntity.getIsClone()) {
            return false;
        } else {
            return true;
        }
    }

    public void onEntityJoinLevel(Entity entity) {
        if (shouldTrackEntity(entity)) {
            addUUID(entity.getUUID());
        }
    }

    public void onEntityLeaveLevel(Entity entity) {
        removeUUID(entity.getUUID());
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            PlayerMobSequenceData data = PlayerMobSequenceData.get(serverLevel);
            data.onEntityJoinLevel(event.getEntity());
        }
    }

    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            PlayerMobSequenceData data = PlayerMobSequenceData.get(serverLevel);
            data.onEntityLeaveLevel(event.getEntity());
        }
    }

    @Override
    public String toString() {
        return "PlayerMobSequenceData{" +
                "trackedUUIDs=" + trackedUUIDs.size() + " entries, " +
                "pathwaySequenceMap=" + pathwaySequenceMap.size() + " entries}";
    }
}
