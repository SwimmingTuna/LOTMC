package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.ClientData.ClientWormOfStarData;

import java.util.function.Supplier;

public class ClientWormOfStarDataS2C {
    private final int wormAmount;

    public ClientWormOfStarDataS2C(int abilityNumber) {
        this.wormAmount = abilityNumber;
    }

    public ClientWormOfStarDataS2C(FriendlyByteBuf buf) {
        this.wormAmount = buf.readInt();
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeInt(wormAmount);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientWormOfStarData.setWormCount(wormAmount);
        });
        return true;
    }
}
