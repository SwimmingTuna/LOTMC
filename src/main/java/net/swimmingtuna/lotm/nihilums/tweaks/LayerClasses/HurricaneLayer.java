package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.Earthquake;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.Hurricane;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class HurricaneLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        Hurricane.hurricane(event.getEntity());
    }

    @Override
    public String getID() {
        return "HurricaneEventID";
    }
}
