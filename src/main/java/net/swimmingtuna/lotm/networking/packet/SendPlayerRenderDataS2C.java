package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

// SendPlayerRenderDataS2C.java
public class SendPlayerRenderDataS2C {
    private final UUID entityUUID;
    private final float yaw;
    private final float pitch;
    private final float headYaw;
    private final float bodyYaw;
    private final double velX;
    private final double velY;
    private final double velZ;
    private final float swingProgress;
    private final double posX;
    private final double posY;
    private final double posZ;
    private final boolean onGround;
    private final float fallDistance;
    private final Vec3 displayCenter;
    private final boolean onFire;

    public SendPlayerRenderDataS2C(UUID entityUUID, float yaw, float pitch, float headYaw, float bodyYaw, double velX, double velY, double velZ, float swingProgress, double posX, double posY, double posZ, boolean onGround, float fallDistance, Vec3 displayCenter, boolean onFire) {
        this.entityUUID = entityUUID;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onFire = onFire;
        this.headYaw = headYaw;
        this.bodyYaw = bodyYaw;
        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;
        this.swingProgress = swingProgress;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.onGround = onGround;
        this.fallDistance = fallDistance;
        this.displayCenter = displayCenter;
    }

    public SendPlayerRenderDataS2C(FriendlyByteBuf buf) {
        this.entityUUID = buf.readUUID();
        this.yaw = buf.readFloat();
        this.pitch = buf.readFloat();
        this.headYaw = buf.readFloat();
        this.bodyYaw = buf.readFloat();
        this.velX = buf.readDouble();
        this.velY = buf.readDouble();
        this.velZ = buf.readDouble();
        this.swingProgress = buf.readFloat();
        this.posX = buf.readDouble();
        this.posY = buf.readDouble();
        this.posZ = buf.readDouble();
        this.onGround = buf.readBoolean();
        this.fallDistance = buf.readFloat();
        this.displayCenter = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.onFire = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.entityUUID);
        buf.writeFloat(this.yaw);
        buf.writeFloat(this.pitch);
        buf.writeFloat(this.headYaw);
        buf.writeFloat(this.bodyYaw);
        buf.writeDouble(this.velX);
        buf.writeDouble(this.velY);
        buf.writeDouble(this.velZ);
        buf.writeFloat(this.swingProgress);
        buf.writeDouble(this.posX);
        buf.writeDouble(this.posY);
        buf.writeDouble(this.posZ);
        buf.writeBoolean(this.onGround);
        buf.writeFloat(this.fallDistance);
        buf.writeDouble(this.displayCenter.x);
        buf.writeDouble(this.displayCenter.y);
        buf.writeDouble(this.displayCenter.z);
        buf.writeBoolean(this.onFire);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            LocalPlayer clientPlayer = Minecraft.getInstance().player;
            if (clientPlayer != null && clientPlayer.level() instanceof ClientLevel clientLevel) {
                Entity observedEntity = null;
                for (Entity entity : clientLevel.entitiesForRendering()) {
                    if (entity.getUUID().equals(this.entityUUID)) {
                        observedEntity = entity;
                        break;
                    }
                }
                if (observedEntity != null) {
                    CompoundTag renderData = new CompoundTag();
                    renderData.putUUID("displayEntityUUID", this.entityUUID);
                    renderData.putFloat("displayYaw", this.yaw);
                    renderData.putFloat("displayPitch", this.pitch);
                    renderData.putFloat("displayHeadYaw", this.headYaw);
                    renderData.putFloat("displayRenderYaw", this.bodyYaw);
                    renderData.putDouble("displayVelX", this.velX);
                    renderData.putDouble("displayVelY", this.velY);
                    renderData.putDouble("displayVelZ", this.velZ);
                    renderData.putFloat("displaySwingProgress", this.swingProgress);
                    renderData.putDouble("displayEntityDisplayPosX", this.posX);
                    renderData.putDouble("displayEntityDisplayPosY", this.posY);
                    renderData.putDouble("displayEntityDisplayPosZ", this.posZ);
                    renderData.putDouble("displayCenterX", this.displayCenter.x);
                    renderData.putDouble("displayCenterY", this.displayCenter.y);
                    renderData.putDouble("displayCenterZ", this.displayCenter.z);
                    renderData.putBoolean("displayOnGround", this.onGround);
                    renderData.putFloat("displayFallDistance", this.fallDistance);
                    renderData.putBoolean("displayOnFire", this.onFire);
                    observedEntity.getPersistentData().put("dimensionalSightRenderData", renderData);
                }
            }
        });
        return true;
    }

    // Getters
    public UUID getEntityUUID() {
        return entityUUID;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getHeadYaw() {
        return headYaw;
    }

    public float getBodyYaw() {
        return bodyYaw;
    }

    public double getVelX() {
        return velX;
    }

    public double getVelY() {
        return velY;
    }

    public double getVelZ() {
        return velZ;
    }

    public float getSwingProgress() {
        return swingProgress;
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public double getPosZ() {
        return posZ;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public float getFallDistance() {
        return fallDistance;
    }

    public Vec3 getDisplayCenter() {
        return displayCenter;
    }
}