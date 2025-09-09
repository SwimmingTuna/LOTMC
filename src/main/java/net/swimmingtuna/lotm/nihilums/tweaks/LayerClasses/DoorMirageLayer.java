package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.DoorMirage;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class DoorMirageLayer implements IFunction {

    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        DoorMirage.mirageTick(event.getEntity());
    }

    @Override
    public String getID() {
        return "DoorMirageEventID";
    }
}
