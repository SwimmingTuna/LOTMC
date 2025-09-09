package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.util.ClientUtil;

import java.util.function.Supplier;


public class CleanupDimensionalSightPacketS2C {
    private final BlockPos tileEntityPos;
    private final int entityId;

    public CleanupDimensionalSightPacketS2C(BlockPos pos, int entityId) {
        this.tileEntityPos = pos;
        this.entityId = entityId;
    }

    public CleanupDimensionalSightPacketS2C(FriendlyByteBuf buffer) {
        this.tileEntityPos = buffer.readBlockPos();
        this.entityId = buffer.readInt();
    }

    public static CleanupDimensionalSightPacketS2C decode(FriendlyByteBuf buffer) {
        return new CleanupDimensionalSightPacketS2C(buffer);
    }

    public static void encode(CleanupDimensionalSightPacketS2C packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.tileEntityPos);
        buffer.writeInt(packet.entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                handleClientSide();
            }
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClientSide() {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            BlockEntity blockEntity = level.getBlockEntity(tileEntityPos);
            if (blockEntity instanceof DimensionalSightTileEntity dimensionalSight) {
                ClientUtil.removeDimensionalSight(dimensionalSight);
            }
            Entity entity = level.getEntity(entityId);
            if (entity != null) {
                try {
                    if (level instanceof ClientLevel clientLevel) {
                        clientLevel.removeEntity(entityId, Entity.RemovalReason.DISCARDED);
                    }
                    entity.remove(Entity.RemovalReason.DISCARDED);
                } catch (Exception ignored) {
                    LOTM.LOGGER.info("PROBLEM WITH DIMENSIONAL SIGHT CLEANUP PACKET!");
                }
            }
        }
    }
}
