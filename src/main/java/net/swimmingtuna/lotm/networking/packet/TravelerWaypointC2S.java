package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.TravelersDoorWaypoint;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;

import java.util.function.Supplier;

public class TravelerWaypointC2S implements LeftClickType {
    public TravelerWaypointC2S() {

    }

    public TravelerWaypointC2S(FriendlyByteBuf buf) {

    }

    public void toByte(FriendlyByteBuf buf) {

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        context.enqueueWork(() -> {
            if (player == null) return;
            CompoundTag tag = player.getPersistentData();
            if(tag.getInt("waypointNetworkCooldown") > 0) return;
            if(!tag.contains("doorWaypoint")) tag.putInt("doorWaypoint", 1);
            if(player.isShiftKeyDown()){
                TravelersDoorWaypoint.toggleInstant(player);
                tag.putInt("waypointMessageCooldown", 30);
                player.displayClientMessage(Component.literal("Instant waypoint: " + tag.getBoolean("doorWaypointIsInstant")).withStyle(BeyonderUtil.getStyle(player)), true);
                return;
            }
            int currentWaypoint = tag.getInt("doorWaypoint");
            int maxWaypoints = 20 - (BeyonderUtil.getSequence(player) * 3);
            int newWaypoint = currentWaypoint + 1;
            if (newWaypoint > maxWaypoints) {
                newWaypoint = 1;
            }
            String name = "";
            if(tag.contains("waypointName" + newWaypoint)) name = ": " + tag.getString("waypointName" + newWaypoint);
            tag.putInt("doorWaypoint", newWaypoint);
            tag.putInt("waypointMessageCooldown", 30);
            tag.putInt("waypointNetworkCooldown", 2);
            player.displayClientMessage(Component.literal("Waypoint " + newWaypoint + name).withStyle(BeyonderUtil.getStyle(player)), true);
        });
        return true;
    }
}
