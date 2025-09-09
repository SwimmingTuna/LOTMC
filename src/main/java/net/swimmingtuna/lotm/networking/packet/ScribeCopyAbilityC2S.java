package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;


import java.util.function.Supplier;

import static net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.ScribeAbilities.acceptCopiedAbilities;

public class ScribeCopyAbilityC2S implements LeftClickType {
    public ScribeCopyAbilityC2S() {
    }

    public ScribeCopyAbilityC2S(FriendlyByteBuf buf) {

    }

    public void toByte(FriendlyByteBuf buf) {

    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            acceptCopiedAbilities(player);

        });
        ctx.get().setPacketHandled(true);
        return true;
    }

}