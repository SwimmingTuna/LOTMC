package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.beyonder.SailorClass;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class RainEyesLayer implements IFunction {


    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        SailorClass.rainEyesTickEvent(event);
    }

    @Override
    public String getID() {
        return "RainEyesEventID";
    }
}
