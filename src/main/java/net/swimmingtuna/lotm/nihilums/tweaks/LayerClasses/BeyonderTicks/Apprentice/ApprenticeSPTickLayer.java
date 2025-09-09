package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses.BeyonderTicks.Apprentice;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.beyonder.ApprenticeClass;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class ApprenticeSPTickLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        ApprenticeClass.apprenticeTick(event);
    }

    @Override
    public String getID() {
        return "ApprenticeSPTickEventID";
    }
}
