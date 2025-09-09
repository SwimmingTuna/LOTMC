package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.RagingBlows;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.WindManipulationSense;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class WindManipulationSenseLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        WindManipulationSense.windManipulationSense(event.getEntity());
    }

    @Override
    public String getID() {
        return "RagingComboEventID";
    }
}
