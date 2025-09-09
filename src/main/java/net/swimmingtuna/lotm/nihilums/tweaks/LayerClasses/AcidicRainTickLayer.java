package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.AcidicRain;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.DreamIntoReality;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class AcidicRainTickLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        AcidicRain.acidicRainTick(event.getEntity());
    }

    @Override
    public String getID() {
        return "AcidicRainEventID";
    }
}
