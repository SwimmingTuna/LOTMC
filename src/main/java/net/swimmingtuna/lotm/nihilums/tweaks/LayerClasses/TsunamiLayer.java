package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.LightningStorm;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.Tsunami;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class TsunamiLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        Tsunami.tsunami(event.getEntity());
    }

    @Override
    public String getID() {
        return "TsunamiEventID";
    }
}
