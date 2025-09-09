package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.Exile;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class ExileLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        Exile.exileTickEvent(event);
    }

    @Override
    public String getID() {
        return "ExileEventID";
    }
}
