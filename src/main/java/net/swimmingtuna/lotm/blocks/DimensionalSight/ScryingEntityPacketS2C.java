package net.swimmingtuna.lotm.blocks.DimensionalSight;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.function.Supplier;

public class ScryingEntityPacketS2C {
    public double posX;
    public double posY;
    public double posZ;
    public CompoundTag entityTag;

    public ScryingEntityPacketS2C() {
    }

    public ScryingEntityPacketS2C(double posX, double posY, double posZ, CompoundTag tag) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.entityTag = tag;
    }

    public void fromBytes(FriendlyByteBuf buf) {
        this.posX = buf.readDouble();
        this.posY = buf.readDouble();
        this.posZ = buf.readDouble();
        if (buf.readBoolean()) {
            this.entityTag = buf.readAnySizeNbt();
        } else {
            this.entityTag = null;
        }

    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(this.posX);
        buf.writeDouble(this.posY);
        buf.writeDouble(this.posZ);
        if (this.entityTag == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeNbt(this.entityTag);
        }

    }

    public static void encode(ScryingEntityPacketS2C msg, FriendlyByteBuf buf) {
        msg.toBytes(buf);
    }

    public static ScryingEntityPacketS2C decode(FriendlyByteBuf buf) {
        ScryingEntityPacketS2C msg = new ScryingEntityPacketS2C();
        msg.fromBytes(buf);
        return msg;
    }

    public static void handle(ScryingEntityPacketS2C message, Supplier<NetworkEvent.Context> context) {
        scryingHandler(message);
        ((NetworkEvent.Context) context.get()).setPacketHandled(true);
    }

    public static BlockPos toBlockPos(double x, double y, double z) {
        return new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
    }


    public static void scryingHandler(ScryingEntityPacketS2C message) {
        BlockEntity be = Minecraft.getInstance().level.getBlockEntity(toBlockPos(message.posX, message.posY, message.posZ));
        if (be instanceof DimensionalSightTileEntity) {
            if (message.entityTag == null) {
                ((DimensionalSightTileEntity) be).scryUniqueID = null;
                ((DimensionalSightTileEntity) be).lst = new ArrayList();
                ((DimensionalSightTileEntity) be).scryNBT = null;
            }

            ((DimensionalSightTileEntity) be).scryNBT = message.entityTag;
            ((DimensionalSightTileEntity) be).doRead = true;
        }
    }
}

