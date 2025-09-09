package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.ClientData.ClientShouldntRenderHandData;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncShouldntRenderHandPacketS2C {
    private final boolean shouldntRender;
    private final UUID playerUUID;

    public SyncShouldntRenderHandPacketS2C(boolean shouldntRender, UUID playerUUID) {
        this.shouldntRender = shouldntRender;
        this.playerUUID = playerUUID;
    }

    public SyncShouldntRenderHandPacketS2C(FriendlyByteBuf buf) {
        this.shouldntRender = buf.readBoolean();
        this.playerUUID = buf.readUUID();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.shouldntRender);
        buf.writeUUID(this.playerUUID);
    }

    public static void handle(SyncShouldntRenderHandPacketS2C msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientShouldntRenderHandData.setShouldntRender(msg.shouldntRender, msg.playerUUID);
        });
        context.setPacketHandled(true);
    }
}
