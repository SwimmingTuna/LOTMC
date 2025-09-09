package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.entity.DragonBreathEntity;

import java.util.function.Supplier;

public class UpdateDragonBreathS2C {
    private final double startX;
    private final double startY;
    private final double startZ;
    private final double endX;
    private final double endY;
    private final double endZ;
    private final int entityId;

    // Additional data needed by renderer
    private final float prevYaw;
    private final float renderYaw;
    private final float prevPitch;
    private final float renderPitch;
    private final int time;
    private final int charge;
    private final int duration;
    private final float animation;
    private final float size;
    private final boolean causesFire;
    private final double prevCollidePosX;
    private final double prevCollidePosY;
    private final double prevCollidePosZ;
    private final double collidePosX;
    private final double collidePosY;
    private final double collidePosZ;

    public UpdateDragonBreathS2C(double startX, double startY, double startZ, double endX, double endY, double endZ,
                                 int entityId, float prevYaw, float renderYaw, float prevPitch, float renderPitch,
                                 int time, int charge, int duration, float animation, float size,
                                 boolean causesFire, double prevCollidePosX, double prevCollidePosY, double prevCollidePosZ,
                                 double collidePosX, double collidePosY, double collidePosZ) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
        this.entityId = entityId;
        this.prevYaw = prevYaw;
        this.renderYaw = renderYaw;
        this.prevPitch = prevPitch;
        this.renderPitch = renderPitch;
        this.time = time;
        this.charge = charge;
        this.duration = duration;
        this.animation = animation;
        this.size = size;
        this.causesFire = causesFire;
        this.prevCollidePosX = prevCollidePosX;
        this.prevCollidePosY = prevCollidePosY;
        this.prevCollidePosZ = prevCollidePosZ;
        this.collidePosX = collidePosX;
        this.collidePosY = collidePosY;
        this.collidePosZ = collidePosZ;
    }

    // Serializer
    public static void encode(UpdateDragonBreathS2C msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.startX);
        buf.writeDouble(msg.startY);
        buf.writeDouble(msg.startZ);
        buf.writeDouble(msg.endX);
        buf.writeDouble(msg.endY);
        buf.writeDouble(msg.endZ);
        buf.writeInt(msg.entityId);
        buf.writeFloat(msg.prevYaw);
        buf.writeFloat(msg.renderYaw);
        buf.writeFloat(msg.prevPitch);
        buf.writeFloat(msg.renderPitch);
        buf.writeInt(msg.time);
        buf.writeInt(msg.charge);
        buf.writeInt(msg.duration);
        buf.writeFloat(msg.animation);
        buf.writeFloat(msg.size);
        buf.writeBoolean(msg.causesFire);
        buf.writeDouble(msg.prevCollidePosX);
        buf.writeDouble(msg.prevCollidePosY);
        buf.writeDouble(msg.prevCollidePosZ);
        buf.writeDouble(msg.collidePosX);
        buf.writeDouble(msg.collidePosY);
        buf.writeDouble(msg.collidePosZ);
    }

    // Deserializer
    public static UpdateDragonBreathS2C decode(FriendlyByteBuf buf) {
        return new UpdateDragonBreathS2C(
                buf.readDouble(), // startX
                buf.readDouble(), // startY
                buf.readDouble(), // startZ
                buf.readDouble(), // endX
                buf.readDouble(), // endY
                buf.readDouble(), // endZ
                buf.readInt(),    // entityId
                buf.readFloat(),  // prevYaw
                buf.readFloat(),  // renderYaw
                buf.readFloat(),  // prevPitch
                buf.readFloat(),  // renderPitch
                buf.readInt(),    // time
                buf.readInt(),    // charge
                buf.readInt(),    // duration
                buf.readFloat(),  // animation
                buf.readFloat(),  // size
                buf.readBoolean(), // causesFire
                buf.readDouble(), // prevCollidePosX
                buf.readDouble(), // prevCollidePosY
                buf.readDouble(), // prevCollidePosZ
                buf.readDouble(), // collidePosX
                buf.readDouble(), // collidePosY
                buf.readDouble()  // collidePosZ
        );
    }

    public static void handle(UpdateDragonBreathS2C msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                Entity entity = minecraft.level.getEntity(msg.entityId);
                if (entity instanceof DragonBreathEntity dragonBreathEntity) {
                    dragonBreathEntity.setPos(msg.startX, msg.startY, msg.startZ);
                    dragonBreathEntity.endPos = new Vec3(msg.endX, msg.endY, msg.endZ);
                    dragonBreathEntity.prevYaw = msg.prevYaw;
                    dragonBreathEntity.renderYaw = msg.renderYaw;
                    dragonBreathEntity.prevPitch = msg.prevPitch;
                    dragonBreathEntity.renderPitch = msg.renderPitch;
                    dragonBreathEntity.setTime(msg.time);
                    dragonBreathEntity.setCharge(msg.charge);
                    dragonBreathEntity.setDuration(msg.duration);
                    dragonBreathEntity.animation = (int) msg.animation;
                    dragonBreathEntity.setSize((int) msg.size);
                    dragonBreathEntity.setCausesFire(msg.causesFire);
                    dragonBreathEntity.prevCollidePosX = msg.prevCollidePosX;
                    dragonBreathEntity.prevCollidePosY = msg.prevCollidePosY;
                    dragonBreathEntity.prevCollidePosZ = msg.prevCollidePosZ;
                    dragonBreathEntity.collidePosX = msg.collidePosX;
                    dragonBreathEntity.collidePosY = msg.collidePosY;
                    dragonBreathEntity.collidePosZ = msg.collidePosZ;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}