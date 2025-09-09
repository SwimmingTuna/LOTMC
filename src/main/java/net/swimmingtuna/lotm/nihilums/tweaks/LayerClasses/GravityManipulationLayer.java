package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.GravityManipulation;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class GravityManipulationLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        GravityManipulation.gravityManipulationTickEvent(event);
    }

    @Override
    public String getID() {
        return "GravityManipulationEventID";
    }
}
