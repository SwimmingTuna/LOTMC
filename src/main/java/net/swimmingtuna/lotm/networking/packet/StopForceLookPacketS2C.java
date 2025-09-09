package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.ClientData.ClientLookData;

import java.util.function.Supplier;

public class StopForceLookPacketS2C {

    public StopForceLookPacketS2C() {}

    public void encode(FriendlyByteBuf buffer) {
    }

    public static StopForceLookPacketS2C decode(FriendlyByteBuf buffer) {
        return new StopForceLookPacketS2C();
    }

    public static void handle(StopForceLookPacketS2C packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientLookData.setSmoothLooking(false);
            });
        });
        context.setPacketHandled(true);
    }
}