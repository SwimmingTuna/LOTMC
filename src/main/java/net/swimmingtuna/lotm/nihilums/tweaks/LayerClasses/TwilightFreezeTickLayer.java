package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.BlinkState;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.TwilightFreeze;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class TwilightFreezeTickLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        TwilightFreeze.twilightFreezeTick(event);
    }

    @Override
    public String getID() {
        return "TwilightFreezeTickEventID";
    }
}
