package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.ClientData.ClientShouldntRenderInvisibilityData;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncShouldntRenderInvisibilityPacketS2C {
    private final boolean shouldntRender;
    private final UUID playerUUID;
    private final int invisibilityAmount;

    public SyncShouldntRenderInvisibilityPacketS2C(boolean shouldntRender, UUID playerUUID, int invisibilityAmount) {
        this.shouldntRender = shouldntRender;
        this.playerUUID = playerUUID;
        this.invisibilityAmount = invisibilityAmount;
    }

    public SyncShouldntRenderInvisibilityPacketS2C(FriendlyByteBuf buf) {
        this.shouldntRender = buf.readBoolean();
        this.playerUUID = buf.readUUID();
        this.invisibilityAmount = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.shouldntRender);
        buf.writeUUID(this.playerUUID);
        buf.writeInt(this.invisibilityAmount);
    }

    public static void handle(SyncShouldntRenderInvisibilityPacketS2C msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientShouldntRenderInvisibilityData.setShouldntRender(msg.shouldntRender, msg.playerUUID, msg.invisibilityAmount);
        });
        context.setPacketHandled(true);
    }
}
