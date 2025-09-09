package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.ClientUtil;

import java.util.function.Supplier;

public class ForceLookPacketS2C {
    private final float yaw;
    private final float pitch;
    private final boolean smooth;

    public ForceLookPacketS2C(float yaw, float pitch, boolean smooth) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.smooth = smooth;
    }

    public ForceLookPacketS2C(Vec3 playerPos, Vec3 targetPos, boolean smooth) {
        Vec3 lookDirection = targetPos.subtract(playerPos).normalize();
        this.yaw = (float) (Math.atan2(-lookDirection.x, lookDirection.z) * (180.0 / Math.PI));
        this.pitch = (float) (Math.asin(-lookDirection.y) * (180.0 / Math.PI));
        this.smooth = smooth;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);
        buffer.writeBoolean(smooth);
    }

    public static ForceLookPacketS2C decode(FriendlyByteBuf buffer) {
        float yaw = buffer.readFloat();
        float pitch = buffer.readFloat();
        boolean smooth = buffer.readBoolean();
        return new ForceLookPacketS2C(yaw, pitch, smooth);
    }

    public static void handle(ForceLookPacketS2C packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                handleClient(packet);
            });
        });
        context.setPacketHandled(true);
    }

    // Move client-side handling to a separate class to avoid loading client classes on server
    private static void handleClient(ForceLookPacketS2C packet) {
        ClientUtil.handleForceLook(packet);
    }

    public static void setPlayerRotation(Player player, float yaw, float pitch) {
        pitch = Mth.clamp(pitch, -90.0f, 90.0f);
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.yRotO = yaw;
        player.xRotO = pitch;
        player.yHeadRot = yaw;
        player.yHeadRotO = yaw;
    }

    // Getters for the client handler
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public boolean isSmooth() { return smooth; }
}