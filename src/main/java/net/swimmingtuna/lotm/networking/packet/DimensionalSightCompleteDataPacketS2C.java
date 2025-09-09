package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class DimensionalSightCompleteDataPacketS2C {
    private final BlockPos pos;
    private final String viewTarget;

    // Entity rotation and animation data
    private final float yaw;
    private final float headYaw;
    private final float renderYaw;
    private final float pitch;
    private final float prevYaw;
    private final float prevHeadYaw;
    private final float prevRenderYaw;
    private final float prevPitch;

    // Entity velocity and position
    private final double velX;
    private final double velY;
    private final double velZ;
    private final Vec3 targetPos;

    // Animation data
    private final float prevSwingProgress;
    private final float swingProgress;
    private final float limbSwingAmount;
    private final float prevLimbSwingAmount;
    private final float limbSwing;

    // Entity identification
    private final UUID scryUniqueID;

    // Block and entity data
    private final List<DimensionalSightTileEntity.BlockPosInfo> blockList;
    private final List<SynchedEntityData.DataValue<?>> scryDataManager;
    private CompoundTag scryNBT;

    // Timing data
    private final int scryTimer;
    private final int scryMaxTimer;

    public DimensionalSightCompleteDataPacketS2C(DimensionalSightTileEntity tileEntity) {
        this.pos = tileEntity.getBlockPos();
        this.viewTarget = tileEntity.viewTarget != null ? tileEntity.viewTarget : "";

        // Entity rotation and animation data
        this.yaw = tileEntity.yaw;
        this.headYaw = tileEntity.headYaw;
        this.renderYaw = tileEntity.renderYaw;
        this.pitch = tileEntity.pitch;
        this.prevYaw = tileEntity.prevYaw;
        this.prevHeadYaw = tileEntity.prevHeadYaw;
        this.prevRenderYaw = tileEntity.prevRenderYaw;
        this.prevPitch = tileEntity.prevPitch;

        // Entity velocity and position
        this.velX = tileEntity.velX;
        this.velY = tileEntity.velY;
        this.velZ = tileEntity.velZ;
        this.targetPos = tileEntity.targetPos;

        // Animation data
        this.prevSwingProgress = tileEntity.prevSwingProgress;
        this.swingProgress = tileEntity.swingProgress;
        this.limbSwingAmount = tileEntity.limbSwingAmount;
        this.prevLimbSwingAmount = tileEntity.prevLimbSwingAmount;
        this.limbSwing = tileEntity.limbSwing;

        // Entity identification
        this.scryUniqueID = tileEntity.scryUniqueID;

        // Block and entity data
        this.blockList = tileEntity.blockList != null ? new ArrayList<>(tileEntity.blockList) : new ArrayList<>();
        this.scryDataManager = tileEntity.scryDataManager != null ? new ArrayList<>(tileEntity.scryDataManager) : new ArrayList<>();
        this.scryNBT = tileEntity.scryNBT != null ? tileEntity.scryNBT.copy() : new CompoundTag();

        // Timing data
        this.scryTimer = tileEntity.scryTimer;
        this.scryMaxTimer = tileEntity.scryMaxTimer;
    }

    public DimensionalSightCompleteDataPacketS2C(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.viewTarget = buf.readUtf();

        // Entity rotation and animation data
        this.yaw = buf.readFloat();
        this.headYaw = buf.readFloat();
        this.renderYaw = buf.readFloat();
        this.pitch = buf.readFloat();
        this.prevYaw = buf.readFloat();
        this.prevHeadYaw = buf.readFloat();
        this.prevRenderYaw = buf.readFloat();
        this.prevPitch = buf.readFloat();

        // Entity velocity and position
        this.velX = buf.readDouble();
        this.velY = buf.readDouble();
        this.velZ = buf.readDouble();

        // Read target position (nullable)
        if (buf.readBoolean()) {
            this.targetPos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        } else {
            this.targetPos = null;
        }

        // Animation data
        this.prevSwingProgress = buf.readFloat();
        this.swingProgress = buf.readFloat();
        this.limbSwingAmount = buf.readFloat();
        this.prevLimbSwingAmount = buf.readFloat();
        this.limbSwing = buf.readFloat();

        // Entity identification (nullable)
        if (buf.readBoolean()) {
            this.scryUniqueID = buf.readUUID();
        } else {
            this.scryUniqueID = null;
        }

        // Block list
        int blockListSize = buf.readInt();
        this.blockList = new ArrayList<>();
        for (int i = 0; i < blockListSize; i++) {
            CompoundTag blockData = buf.readNbt();
            if (blockData != null) {
                DimensionalSightTileEntity.BlockPosInfo info = new DimensionalSightTileEntity.BlockPosInfo();
                info.read(blockData, null); // Level will be set when applying the packet
                this.blockList.add(info);
            }
        }

        // Entity data manager
        int dataManagerSize = buf.readInt();
        this.scryDataManager = new ArrayList<>();
        for (int i = 0; i < dataManagerSize; i++) {
            // Note: SynchedEntityData.DataValue serialization is complex
            // This is a simplified approach - you may need to implement custom serialization
            // based on your specific data requirements
        }

        // Entity NBT data
        this.scryNBT = buf.readNbt();
        if (this.scryNBT == null) {
            this.scryNBT = new CompoundTag();
        }

        // Timing data
        this.scryTimer = buf.readInt();
        this.scryMaxTimer = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.viewTarget);

        // Entity rotation and animation data
        buf.writeFloat(this.yaw);
        buf.writeFloat(this.headYaw);
        buf.writeFloat(this.renderYaw);
        buf.writeFloat(this.pitch);
        buf.writeFloat(this.prevYaw);
        buf.writeFloat(this.prevHeadYaw);
        buf.writeFloat(this.prevRenderYaw);
        buf.writeFloat(this.prevPitch);

        // Entity velocity and position
        buf.writeDouble(this.velX);
        buf.writeDouble(this.velY);
        buf.writeDouble(this.velZ);

        // Write target position (nullable)
        if (this.targetPos != null) {
            buf.writeBoolean(true);
            buf.writeDouble(this.targetPos.x);
            buf.writeDouble(this.targetPos.y);
            buf.writeDouble(this.targetPos.z);
        } else {
            buf.writeBoolean(false);
        }

        // Animation data
        buf.writeFloat(this.prevSwingProgress);
        buf.writeFloat(this.swingProgress);
        buf.writeFloat(this.limbSwingAmount);
        buf.writeFloat(this.prevLimbSwingAmount);
        buf.writeFloat(this.limbSwing);

        // Entity identification (nullable)
        if (this.scryUniqueID != null) {
            buf.writeBoolean(true);
            buf.writeUUID(this.scryUniqueID);
        } else {
            buf.writeBoolean(false);
        }

        // Block list
        buf.writeInt(this.blockList.size());
        for (DimensionalSightTileEntity.BlockPosInfo info : this.blockList) {
            CompoundTag blockData = info.write();
            buf.writeNbt(blockData);
        }

        // Entity data manager
        buf.writeInt(this.scryDataManager.size());
        for (SynchedEntityData.DataValue<?> dataValue : this.scryDataManager) {
            // Note: You'll need to implement proper serialization for DataValue
            // This is a placeholder for the complex serialization logic
        }

        // Entity NBT data
        buf.writeNbt(this.scryNBT);

        // Timing data
        buf.writeInt(this.scryTimer);
        buf.writeInt(this.scryMaxTimer);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Apply the packet data on the client side
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level != null) {
                net.minecraft.world.level.block.entity.BlockEntity te = mc.level.getBlockEntity(this.pos);
                if (te instanceof DimensionalSightTileEntity) {
                    DimensionalSightTileEntity dimensionalSightTE = (DimensionalSightTileEntity) te;

                    // Apply all the data to the tile entity
                    dimensionalSightTE.viewTarget = this.viewTarget;

                    // Entity rotation and animation data
                    dimensionalSightTE.yaw = this.yaw;
                    dimensionalSightTE.headYaw = this.headYaw;
                    dimensionalSightTE.renderYaw = this.renderYaw;
                    dimensionalSightTE.pitch = this.pitch;
                    dimensionalSightTE.prevYaw = this.prevYaw;
                    dimensionalSightTE.prevHeadYaw = this.prevHeadYaw;
                    dimensionalSightTE.prevRenderYaw = this.prevRenderYaw;
                    dimensionalSightTE.prevPitch = this.prevPitch;

                    // Entity velocity and position
                    dimensionalSightTE.velX = this.velX;
                    dimensionalSightTE.velY = this.velY;
                    dimensionalSightTE.velZ = this.velZ;
                    dimensionalSightTE.targetPos = this.targetPos;

                    // Animation data
                    dimensionalSightTE.prevSwingProgress = this.prevSwingProgress;
                    dimensionalSightTE.swingProgress = this.swingProgress;
                    dimensionalSightTE.limbSwingAmount = this.limbSwingAmount;
                    dimensionalSightTE.prevLimbSwingAmount = this.prevLimbSwingAmount;
                    dimensionalSightTE.limbSwing = this.limbSwing;

                    // Entity identification
                    dimensionalSightTE.scryUniqueID = this.scryUniqueID;

                    // Block and entity data
                    dimensionalSightTE.blockList = (ArrayList<DimensionalSightTileEntity.BlockPosInfo>) this.blockList;
                    dimensionalSightTE.scryDataManager = this.scryDataManager;
                    dimensionalSightTE.scryNBT = this.scryNBT;

                    // Timing data
                    dimensionalSightTE.scryTimer = this.scryTimer;
                    dimensionalSightTE.scryMaxTimer = this.scryMaxTimer;

                    // Trigger client-side updates
                    dimensionalSightTE.doRead = true;
                }
            }
        });
        context.setPacketHandled(true);
    }

    // Getters for accessing packet data
    public BlockPos getPos() { return pos; }
    public String getViewTarget() { return viewTarget; }
    public float getYaw() { return yaw; }
    public float getHeadYaw() { return headYaw; }
    public float getRenderYaw() { return renderYaw; }
    public float getPitch() { return pitch; }
    public Vec3 getTargetPos() { return targetPos; }
    public UUID getScryUniqueID() { return scryUniqueID; }
    public List<DimensionalSightTileEntity.BlockPosInfo> getBlockList() { return blockList; }
    public CompoundTag getScryNBT() { return scryNBT; }
    public int getScryTimer() { return scryTimer; }
    public int getScryMaxTimer() { return scryMaxTimer; }
}