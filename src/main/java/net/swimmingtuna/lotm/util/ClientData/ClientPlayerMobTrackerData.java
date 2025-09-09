package net.swimmingtuna.lotm.util.ClientData;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.swimmingtuna.lotm.world.worlddata.PlayerMobTracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientPlayerMobTrackerData {
    private static final Map<UUID, PlayerMobTracker.PlayerMobData> clientTrackedMobs = new HashMap<>();

    /**
     * Update the client-side tracked mobs map
     */
    public static void updateTrackedMobs(Map<UUID, PlayerMobTracker.PlayerMobData> trackedMobs) {
        clientTrackedMobs.clear();
        clientTrackedMobs.putAll(trackedMobs);
    }

    /**
     * Get all tracked mobs on client side
     */
    public static Map<UUID, PlayerMobTracker.PlayerMobData> getTrackedMobs() {
        return new HashMap<>(clientTrackedMobs);
    }

    /**
     * Get tracked mob by UUID
     */
    public static PlayerMobTracker.PlayerMobData getTrackedMob(UUID entityUUID) {
        return clientTrackedMobs.get(entityUUID);
    }

    /**
     * Check if a mob is being tracked
     */
    public static boolean isTracked(UUID entityUUID) {
        return clientTrackedMobs.containsKey(entityUUID);
    }

    /**
     * Get count of tracked mobs
     */
    public static int getTrackedMobCount() {
        return clientTrackedMobs.size();
    }

    /**
     * Get tracked mobs by dimension
     */
    public static List<PlayerMobTracker.PlayerMobData> getTrackedMobsInDimension(String dimensionKey) {
        return clientTrackedMobs.values().stream()
                .filter(data -> data.dimension.equals(dimensionKey))
                .toList();
    }

    /**
     * Clear all tracked mobs (called when disconnecting from server)
     */
    public static void clearTrackedMobs() {
        clientTrackedMobs.clear();
    }
}