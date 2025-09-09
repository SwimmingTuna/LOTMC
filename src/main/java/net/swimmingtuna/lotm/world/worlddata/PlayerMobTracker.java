package net.swimmingtuna.lotm.world.worlddata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.SyncPlayerMobTrackerPacketS2C;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerMobTracker extends SavedData {
    private static final String DATA_NAME = "lotm_playermob_tracker";

    private final Map<UUID, PlayerMobData> trackedMobs = new ConcurrentHashMap<>();

    public PlayerMobTracker() {
        super();
    }

    public static PlayerMobTracker get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(PlayerMobTracker::load, PlayerMobTracker::new, DATA_NAME);
    }

    public static PlayerMobTracker load(CompoundTag tag) {
        PlayerMobTracker tracker = new PlayerMobTracker();

        ListTag mobList = tag.getList("TrackedMobs", Tag.TAG_COMPOUND);
        for (int i = 0; i < mobList.size(); i++) {
            CompoundTag mobTag = mobList.getCompound(i);
            PlayerMobData mobData = PlayerMobData.fromNBT(mobTag);
            tracker.trackedMobs.put(mobData.entityUUID, mobData);
        }

        return tracker;
    }

    public Map<String, List<PlayerMobEntity>> getAllPlayerMobsAcrossDimensions(ServerLevel level) {
        Map<String, List<PlayerMobEntity>> result = new HashMap<>();

        for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
            String dimensionKey = serverLevel.dimension().location().toString();
            List<PlayerMobEntity> mobsInDimension = new ArrayList<>();

            for (PlayerMobData mobData : getTrackedMobsInDimension(dimensionKey)) {
                Entity entity = serverLevel.getEntity(mobData.entityUUID);
                if (entity instanceof PlayerMobEntity playerMob && entity.isAlive()) {
                    mobsInDimension.add(playerMob);
                }
            }

            if (!mobsInDimension.isEmpty()) {
                result.put(dimensionKey, mobsInDimension);
            }
        }

        return result;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag mobList = new ListTag();

        for (PlayerMobData mobData : trackedMobs.values()) {
            mobList.add(mobData.toNBT());
        }

        tag.put("TrackedMobs", mobList);
        return tag;
    }

    /**
     * Add a PlayerMobEntity to the tracker
     */
    public void addPlayerMob(PlayerMobEntity entity) {
        if (entity == null || entity.level().isClientSide()) return;
        if (trackedMobs.containsKey(entity.getUUID())) {
            updatePlayerMobPosition(entity);
            return;
        }
        PlayerMobData mobData = new PlayerMobData(
                entity.getUUID(),
                entity.getName().getString(),
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                entity.level().dimension().location().toString(),
                entity.getCurrentSequence(),
                entity.getCurrentPathway() != null ? entity.getCurrentPathway().getClass().getSimpleName() : "None",
                System.currentTimeMillis()
        );

        trackedMobs.put(entity.getUUID(), mobData);
        setDirty();
        syncToAllPlayers((ServerLevel) entity.level());
    }

    public void removePlayerMob(UUID entityUUID, ServerLevel level) {
        PlayerMobData removed = trackedMobs.remove(entityUUID);
        if (removed != null) {
            setDirty();
            syncToAllPlayers(level);
        }
    }

    /**
     * Update position of a tracked mob
     */
    public void updatePlayerMobPosition(PlayerMobEntity entity) {
        if (entity == null || entity.level().isClientSide()) return;

        PlayerMobData mobData = trackedMobs.get(entity.getUUID());
        if (mobData != null) {
            mobData.x = entity.getX();
            mobData.y = entity.getY();
            mobData.z = entity.getZ();
            mobData.lastUpdated = System.currentTimeMillis();
            setDirty();
        }
    }

    /**
     * Get all tracked PlayerMobEntities
     */
    public Map<UUID, PlayerMobData> getTrackedMobs() {
        return new HashMap<>(trackedMobs);
    }

    /**
     * Get tracked mob by UUID
     */
    public PlayerMobData getTrackedMob(UUID entityUUID) {
        return trackedMobs.get(entityUUID);
    }

    /**
     * Check if a mob is being tracked
     */
    public boolean isTracked(UUID entityUUID) {
        return trackedMobs.containsKey(entityUUID);
    }

    /**
     * Get count of tracked mobs
     */
    public int getTrackedMobCount() {
        return trackedMobs.size();
    }

    /**
     * Get tracked mobs by dimension
     */
    public List<PlayerMobData> getTrackedMobsInDimension(String dimensionKey) {
        return trackedMobs.values().stream()
                .filter(data -> data.dimension.equals(dimensionKey))
                .toList();
    }

    /**
     * Clean up old entries (optional - for entities that might have been missed)
     */
    public void cleanupMissingEntities(ServerLevel level) {
        boolean removed = false;
        Iterator<Map.Entry<UUID, PlayerMobData>> iterator = trackedMobs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PlayerMobData> entry = iterator.next();
            UUID entityUUID = entry.getKey();
            Entity entity = level.getEntity(entityUUID);
            if (!(entity instanceof PlayerMobEntity) || !entity.isAlive()) {
                iterator.remove();
                removed = true;
            }
        }

        if (removed) {
            setDirty();
            syncToAllPlayers(level);
        }
    }

    private void syncToAllPlayers(ServerLevel level) {
        final int MAX_ENTRIES_PER_PACKET = 250;
        List<PlayerMobData> allMobs = new ArrayList<>(trackedMobs.values());
        for (int i = 0; i < allMobs.size(); i += MAX_ENTRIES_PER_PACKET) {
            List<PlayerMobData> sublist = allMobs.subList(i, Math.min(i + MAX_ENTRIES_PER_PACKET, allMobs.size()));
            Map<UUID, PlayerMobData> chunk = new HashMap<>();
            for (PlayerMobData mob : sublist) {
                chunk.put(mob.entityUUID, mob);
            }
            LOTMNetworkHandler.sendToAllPlayers(new SyncPlayerMobTrackerPacketS2C(chunk));
        }
    }


    public void handleDimensionChange(UUID entityUUID, ServerLevel oldLevel, ServerLevel newLevel) {
        PlayerMobData mobData = trackedMobs.get(entityUUID);
        if (mobData != null) {
            removePlayerMob(entityUUID, oldLevel);
        }
    }

    /**
     * Data class to store PlayerMobEntity information
     */
    public static class PlayerMobData {
        public final UUID entityUUID;
        public final String name;
        public double x, y, z;
        public final String dimension;
        public final int sequence;
        public final String pathway;
        public long lastUpdated;

        public PlayerMobData(UUID entityUUID, String name, double x, double y, double z,
                             String dimension, int sequence, String pathway, long lastUpdated) {
            this.entityUUID = entityUUID;
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.dimension = dimension;
            this.sequence = sequence;
            this.pathway = pathway;
            this.lastUpdated = lastUpdated;
        }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("UUID", entityUUID);
            tag.putString("Name", name);
            tag.putDouble("X", x);
            tag.putDouble("Y", y);
            tag.putDouble("Z", z);
            tag.putString("Dimension", dimension);
            tag.putInt("Sequence", sequence);
            tag.putString("Pathway", pathway);
            tag.putLong("LastUpdated", lastUpdated);
            return tag;
        }

        public static PlayerMobData fromNBT(CompoundTag tag) {
            return new PlayerMobData(
                    tag.getUUID("UUID"),
                    tag.getString("Name"),
                    tag.getDouble("X"),
                    tag.getDouble("Y"),
                    tag.getDouble("Z"),
                    tag.getString("Dimension"),
                    tag.getInt("Sequence"),
                    tag.getString("Pathway"),
                    tag.getLong("LastUpdated")
            );
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            PlayerMobData that = (PlayerMobData) obj;
            return Objects.equals(entityUUID, that.entityUUID);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entityUUID);
        }
    }
}