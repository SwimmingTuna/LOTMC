package net.swimmingtuna.lotm.blocks.DimensionalSight;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DimensionalSightPacketS2C {
    public BlockPos pos;
    public List<SynchedEntityData.DataValue<?>> dataManagerEntries;

    public DimensionalSightPacketS2C() {
    }

    public DimensionalSightPacketS2C(BlockPos pos, List<SynchedEntityData.DataValue<?>> dataManagerEntries) {
        this.pos = pos;
        this.dataManagerEntries = dataManagerEntries;
    }

    public void fromBytes(ByteBuf buf) {
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        FriendlyByteBuf buffer = new FriendlyByteBuf(buf);

        try {
            List<SynchedEntityData.DataValue<?>> list = new ArrayList();

            short i;
            while((i = buf.readUnsignedByte()) != 255) {
                list.add(DataValue.read(buffer, i));
            }

            this.dataManagerEntries = list;
        } catch (Exception var5) {
            Exception e = var5;
        }

    }

    public void toBytes(ByteBuf buf) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        buf.writeInt(this.pos.getX());
        buf.writeInt(this.pos.getY());
        buf.writeInt(this.pos.getZ());

        for (SynchedEntityData.DataValue<?> dataManagerEntry : this.dataManagerEntries) {
            dataManagerEntry.write(buffer);
        }

        buffer.writeByte(255);
    }

    public static void encode(DimensionalSightPacketS2C msg, FriendlyByteBuf buf) {
        msg.toBytes(buf);
    }

    public static DimensionalSightPacketS2C decode(FriendlyByteBuf buf) {
        DimensionalSightPacketS2C msg = new DimensionalSightPacketS2C();
        msg.fromBytes(buf);
        return msg;
    }

    public static void handle(DimensionalSightPacketS2C message, Supplier<NetworkEvent.Context> context) {
        DimensionalSightTileEntity.updateTE(message);
        ((NetworkEvent.Context)context.get()).setPacketHandled(true);
    }
}

