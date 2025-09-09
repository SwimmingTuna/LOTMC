package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.ClientData.ClientIgnoreShouldntRenderData;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientShouldntRenderS2C {
    private final UUID uuid;
    private final int ignoreShouldntRender;

    public ClientShouldntRenderS2C(UUID livingUUID, int ignoreShouldntRender) {
        this.uuid = livingUUID;
        this.ignoreShouldntRender = ignoreShouldntRender;
    }

    public ClientShouldntRenderS2C(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.ignoreShouldntRender = buf.readInt();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.uuid);
        buf.writeInt(this.ignoreShouldntRender);
    }

    public static void encode(ClientShouldntRenderS2C packet, FriendlyByteBuf buf) {
        packet.write(buf);
    }

    public static ClientShouldntRenderS2C decode(FriendlyByteBuf buf) {
        return new ClientShouldntRenderS2C(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientIgnoreShouldntRenderData.setIgnoreData(ignoreShouldntRender, uuid);
        });
        context.setPacketHandled(true);
    }
}
