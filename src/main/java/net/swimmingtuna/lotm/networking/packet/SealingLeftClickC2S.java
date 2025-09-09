package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;

import java.util.function.Supplier;

public class SealingLeftClickC2S implements LeftClickType {
    public SealingLeftClickC2S() {

    }

    public SealingLeftClickC2S(FriendlyByteBuf buf) {

    }

    public void toByte(FriendlyByteBuf buf) {

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        context.enqueueWork(() -> {
            if (player == null) return;
            int sequence = BeyonderUtil.getSequence(player);
            CompoundTag tag = player.getPersistentData();
            int x = tag.getInt("planeswalkerSealingChoice");
                if (x <= sequence) {
                    tag.putInt("planeswalkerSealingChoice", 9);
                } else {
                    tag.putInt("planeswalkerSealingChoice", x - 1);
                }
            });
        return true;
    }
}
