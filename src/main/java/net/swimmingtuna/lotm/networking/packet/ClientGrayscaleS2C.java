package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.ClientData.ClientGrayscaleData;

import java.util.function.Supplier;

public class ClientGrayscaleS2C {
    private final int duration;
    private final float intensity;

    public ClientGrayscaleS2C(int duration, float intensity) {
        this.duration = duration;
        this.intensity = intensity;
    }

    public ClientGrayscaleS2C(int duration) {
        this(duration, 1.0f);
    }

    public ClientGrayscaleS2C(FriendlyByteBuf buf) {
        this.duration = buf.readInt();
        this.intensity = buf.readFloat();
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeInt(duration);
        buf.writeFloat(intensity);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientGrayscaleData.setGrayscaleEffect(duration, intensity);
        });
        return true;
    }
}