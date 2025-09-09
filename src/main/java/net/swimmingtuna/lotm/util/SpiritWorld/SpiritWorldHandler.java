package net.swimmingtuna.lotm.util.SpiritWorld;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.SpiritWorldSyncPacket;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpiritWorldHandler {
    private static final Map<UUID, Boolean> entityVisibilityMap = new ConcurrentHashMap<>();
    private static int tickCounter = 0;
    private static final int SYNC_INTERVAL = 20; // Sync every second (20 ticks)

    public static void setInSpiritWorld(boolean value, UUID uuid) {
        entityVisibilityMap.put(uuid, value);
    }

    public static boolean getInSpiritWorldMap(UUID uuid) {
        return entityVisibilityMap.getOrDefault(uuid, false);
    }

    public static void removeEntity(UUID uuid) {
        entityVisibilityMap.remove(uuid);
    }

    public static void clear() {
        entityVisibilityMap.clear();
    }

    public static boolean bothInSameWorld(Entity entity1, Entity entity2) {
        boolean livingEntityInSpiritWorld = getInSpiritWorldMap(entity1.getUUID());
        boolean livingInSpiritWorld = getInSpiritWorldMap(entity2.getUUID());
        return livingEntityInSpiritWorld == livingInSpiritWorld;
    }

    public static boolean getInSpiritWorld(Entity entity) {
        if (entity instanceof Projectile projectile) {
            if (projectile.getOwner() != null) {
                return getInSpiritWorldMap(projectile.getOwner().getUUID());
            }
            return false;
        }
        return getInSpiritWorldMap(entity.getUUID());
    }

    // Client-side method to update data from server
    public static void updateSpiritWorldData(Map<UUID, Boolean> newData) {
        entityVisibilityMap.clear();
        entityVisibilityMap.putAll(newData);
    }

    public static void spiritWorldTrackEvent(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        tickCounter++;
        if (tickCounter >= SYNC_INTERVAL) {
            tickCounter = 0;
            // Send sync packet to all players
            LOTMNetworkHandler.sendToAllPlayers(new SpiritWorldSyncPacket(new ConcurrentHashMap<>(entityVisibilityMap)));
        }
    }

    public static Map<UUID, Boolean> getEntityVisibilityMap() {
        return new ConcurrentHashMap<>(entityVisibilityMap);
    }
}