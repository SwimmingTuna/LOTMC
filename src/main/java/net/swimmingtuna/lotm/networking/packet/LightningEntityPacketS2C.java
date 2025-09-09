package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.entity.LightningEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class LightningEntityPacketS2C {
    private final int entityId;
    private final UUID targetEntityUUID;
    private final Vec3 targetPos;
    private final List<Vec3> positions;
    private final boolean synchedMovement;

    /**
     * Constructor for sending packet from server to client
     */
    public LightningEntityPacketS2C(LightningEntity entity) {
        this.entityId = entity.getId();

        // Target entity (may be null)
        this.targetEntityUUID = entity.getTargetEntity() != null ? entity.getTargetEntity().getUUID() : null;

        // Target position (may be null)
        this.targetPos = entity.getTargetPos();

        // Clone positions list to avoid concurrency issues
        this.positions = new ArrayList<>(entity.getPositions());

        this.synchedMovement = entity.getSynchedMovement();
    }

    /**
     * Constructor for receiving packet on client side
     */
    public LightningEntityPacketS2C(FriendlyByteBuf buffer) {
        // Read entity ID
        this.entityId = buffer.readInt();

        // Read target entity UUID (may be null)
        this.targetEntityUUID = buffer.readBoolean() ? buffer.readUUID() : null;

        // Read target position (may be null)
        this.targetPos = buffer.readBoolean() ? new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()) : null;

        // Read positions list
        int posCount = buffer.readInt();
        this.positions = new ArrayList<>(posCount);
        for (int i = 0; i < posCount; i++) {
            this.positions.add(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
        }
        this.synchedMovement = buffer.readBoolean();
    }

    /**
     * Writes packet data to buffer for sending
     */
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeBoolean(this.targetEntityUUID != null);
        if (this.targetEntityUUID != null) {
            buffer.writeUUID(this.targetEntityUUID);
        }

        // Write target position (may be null)
        buffer.writeBoolean(this.targetPos != null);
        if (this.targetPos != null) {
            buffer.writeDouble(this.targetPos.x);
            buffer.writeDouble(this.targetPos.y);
            buffer.writeDouble(this.targetPos.z);
        }

        // Write positions list
        buffer.writeInt(this.positions.size());
        for (Vec3 pos : this.positions) {
            buffer.writeDouble(pos.x);
            buffer.writeDouble(pos.y);
            buffer.writeDouble(pos.z);
        }
        buffer.writeBoolean(this.synchedMovement);
    }

    /**
     * Handles the packet on the client side
     */
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Ensure we're on client side
            if (Minecraft.getInstance().level != null) {
                Entity entity = Minecraft.getInstance().level.getEntity(this.entityId);

                if (entity instanceof LightningEntity lightningEntity) {
                    if (lightningEntity.getTargetEntity() != null) {
                        lightningEntity.setTargetEntity(lightningEntity.getTargetEntity());
                    }

                    // Update target position
                    if (this.targetPos != null) {
                        lightningEntity.setTargetPos(this.targetPos);
                    }

                    // Clear and update all positions
                    lightningEntity.getPositions().clear();
                    lightningEntity.getPositions().addAll(this.positions);

                    // If positions list is not empty, update last position
                    if (!this.positions.isEmpty()) {
                        lightningEntity.setLastPos(this.positions.get(this.positions.size() - 1));
                    }
                    lightningEntity.setSynchedMovement(this.synchedMovement);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
