package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.DawnArmory;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.TwilightAccelerate;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class TwilightAccelerateTickLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        TwilightAccelerate.twilightAccelerateTick(event);
    }

    @Override
    public String getID() {
        return "TwilightAccelerateTickEventID";
    }
}
