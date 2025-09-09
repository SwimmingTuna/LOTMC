package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.DomainOfDecay;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.DomainOfProvidence;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;

import java.util.function.Supplier;

public class MonsterDomainLeftClickC2S implements LeftClickType {
    public MonsterDomainLeftClickC2S() {

    }

    public MonsterDomainLeftClickC2S(FriendlyByteBuf buf) {

    }

    public void toByte(FriendlyByteBuf buf) {

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        context.enqueueWork(() -> {
            if (player == null) return;
            ItemStack heldItem = player.getMainHandItem();
            int heldItemSlot = player.getInventory().selected;
            if (heldItem.getItem() instanceof DomainOfDecay) {
                player.getInventory().setItem(heldItemSlot, ItemInit.PROVIDENCEDOMAIN.get().getDefaultInstance());
            } else if (heldItem.getItem() instanceof DomainOfProvidence) {
                player.getInventory().setItem(heldItemSlot, ItemInit.DECAYDOMAIN.get().getDefaultInstance());
            }
        });
        return true;
    }
}
