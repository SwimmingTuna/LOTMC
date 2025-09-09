package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.Symbolization;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class SymbolizationLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        Symbolization.symbolizationTick(event);
    }

    @Override
    public String getID() {
        return "SymbolizationEventID";
    }
}
