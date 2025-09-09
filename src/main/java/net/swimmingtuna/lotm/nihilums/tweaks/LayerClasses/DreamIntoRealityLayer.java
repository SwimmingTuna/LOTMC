package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.beyonder.SpectatorClass;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.DreamIntoReality;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;
import net.swimmingtuna.lotm.util.BeyonderUtil;

public class DreamIntoRealityLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        DreamIntoReality.dreamIntoReality(event.getEntity());
    }

    @Override
    public String getID() {
        return "DreamIntoRealityEventID";
    }
}
