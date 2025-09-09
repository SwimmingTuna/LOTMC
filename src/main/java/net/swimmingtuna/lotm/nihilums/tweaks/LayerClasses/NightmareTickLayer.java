package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.DomainOfDecay;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.Nightmare;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class NightmareTickLayer implements IFunction {

    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        Nightmare.nightmareTick(event.getEntity());
    }

    @Override
    public String getID() {
        return "NightmareTickEventID";
    }
}
