package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.VolcanicEruption;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class VolcanicEruptionLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        VolcanicEruption.volcanicEruptionTick(event);
    }

    @Override
    public String getID() {
        return "VolcanicEruptionEventID";
    }
}
