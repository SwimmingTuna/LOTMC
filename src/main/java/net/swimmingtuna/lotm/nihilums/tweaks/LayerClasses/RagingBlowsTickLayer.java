package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.RagingBlows;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.Tsunami;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class RagingBlowsTickLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        RagingBlows.ragingBlowsTick(event.getEntity());
    }

    @Override
    public String getID() {
        return "RagingBlowsEventID";
    }
}
