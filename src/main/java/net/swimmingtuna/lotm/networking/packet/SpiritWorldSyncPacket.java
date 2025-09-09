package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.SpiritWorld.SpiritWorldHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class SpiritWorldSyncPacket {
    private final Map<UUID, Boolean> spiritWorldData;

    public SpiritWorldSyncPacket(Map<UUID, Boolean> spiritWorldData) {
        this.spiritWorldData = spiritWorldData;
    }

    public SpiritWorldSyncPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.spiritWorldData = new HashMap<>();

        for (int i = 0; i < size; i++) {
            UUID uuid = buf.readUUID();
            boolean inSpiritWorld = buf.readBoolean();
            this.spiritWorldData.put(uuid, inSpiritWorld);
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(spiritWorldData.size());

        for (Map.Entry<UUID, Boolean> entry : spiritWorldData.entrySet()) {
            buf.writeUUID(entry.getKey());
            buf.writeBoolean(entry.getValue());
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            SpiritWorldHandler.updateSpiritWorldData(spiritWorldData);
        });
        return true;
    }
}