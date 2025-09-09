package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.ClientData.ClientShouldntMoveData;

import java.util.function.Supplier;

public class ClientShouldntMovePacketS2C {
    private final int shouldntMoveAmount;

    public ClientShouldntMovePacketS2C(int shouldntMoveTimer) {
        this.shouldntMoveAmount = shouldntMoveTimer;
    }

    public ClientShouldntMovePacketS2C(FriendlyByteBuf buf) {
        this.shouldntMoveAmount = buf.readInt();
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeInt(shouldntMoveAmount);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (ClientShouldntMoveData.getDontMoveTimer() < shouldntMoveAmount) {
                ClientShouldntMoveData.setDontMoveTimer(shouldntMoveAmount);
            }
        });
        return true;
    }
}
