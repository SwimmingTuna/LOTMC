package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.PsychologicalInvisibility;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class PsychologicalInvisibilityLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        PsychologicalInvisibility.psychologicalInvisibilityTick(event);
    }

    @Override
    public String getID() {
        return "PsychologicalInvisibilityEventID";
    }
}
