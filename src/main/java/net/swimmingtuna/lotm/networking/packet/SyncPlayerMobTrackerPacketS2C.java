package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.ClientData.ClientPlayerMobTrackerData;
import net.swimmingtuna.lotm.world.worlddata.PlayerMobTracker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class SyncPlayerMobTrackerPacketS2C {
    private final Map<UUID, PlayerMobTracker.PlayerMobData> trackedMobs;

    public SyncPlayerMobTrackerPacketS2C(Map<UUID, PlayerMobTracker.PlayerMobData> trackedMobs) {
        this.trackedMobs = trackedMobs;
    }

    public SyncPlayerMobTrackerPacketS2C(FriendlyByteBuf buf) {
        this.trackedMobs = new HashMap<>();

        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            UUID uuid = buf.readUUID();
            String name = buf.readUtf();
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            String dimension = buf.readUtf();
            int sequence = buf.readInt();
            String pathway = buf.readUtf();
            long lastUpdated = buf.readLong();

            PlayerMobTracker.PlayerMobData data = new PlayerMobTracker.PlayerMobData(
                    uuid, name, x, y, z, dimension, sequence, pathway, lastUpdated
            );
            trackedMobs.put(uuid, data);
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(trackedMobs.size());

        for (PlayerMobTracker.PlayerMobData data : trackedMobs.values()) {
            buf.writeUUID(data.entityUUID);
            buf.writeUtf(data.name);
            buf.writeDouble(data.x);
            buf.writeDouble(data.y);
            buf.writeDouble(data.z);
            buf.writeUtf(data.dimension);
            buf.writeInt(data.sequence);
            buf.writeUtf(data.pathway);
            buf.writeLong(data.lastUpdated);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Update client-side tracker
            ClientPlayerMobTrackerData.updateTrackedMobs(trackedMobs);
        });
        return true;
    }
}
