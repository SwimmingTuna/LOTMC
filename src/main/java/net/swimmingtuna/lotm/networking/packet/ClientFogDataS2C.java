package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.ClientData.ClientFogData;

import java.util.function.Supplier;

public class ClientFogDataS2C {
    private final int fogAmount;

    public ClientFogDataS2C(int abilityNumber) {
        this.fogAmount = abilityNumber;
    }

    public ClientFogDataS2C(FriendlyByteBuf buf) {
        this.fogAmount = buf.readInt();
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeInt(fogAmount);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientFogData.setFogTimer(fogAmount);
        });
        return true;
    }
}
