package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;

import java.util.function.Supplier;

public class UpdateItemInHandC2S implements LeftClickType {
    private final int activeSlot;
    private final ItemStack newItem;

    public UpdateItemInHandC2S(int activeSlot, ItemStack newItem) {
        this.activeSlot = activeSlot;
        this.newItem = newItem;
    }

    public UpdateItemInHandC2S(FriendlyByteBuf buf) {
        this.activeSlot = buf.readInt();
        this.newItem = buf.readItem();
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeInt(activeSlot);
        buf.writeItem(newItem);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            player.getInventory().setItem(activeSlot, newItem);

        });
        ctx.get().setPacketHandled(true);
        return true;
    }
}

