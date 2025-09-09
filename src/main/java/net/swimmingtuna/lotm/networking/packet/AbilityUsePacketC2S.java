package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.BeyonderUtil;

import java.util.function.Supplier;

public class AbilityUsePacketC2S {
    private final int abilityNumber;

    public AbilityUsePacketC2S(int abilityNumber) {
        this.abilityNumber = abilityNumber;
    }

    public AbilityUsePacketC2S(FriendlyByteBuf buf) {
        this.abilityNumber = buf.readInt();
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeInt(abilityNumber);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        context.enqueueWork(() -> {
            if (player == null) return;
            BeyonderUtil.useAbilityByNumber(player, abilityNumber, InteractionHand.MAIN_HAND);
        });
        return true;
    }
}
