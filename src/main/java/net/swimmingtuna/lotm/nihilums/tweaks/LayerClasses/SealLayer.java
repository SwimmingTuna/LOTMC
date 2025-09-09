package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.capabilities.sealed_data.SealedUtils;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class SealLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        SealedUtils.timerTick(event.getEntity());
    }

    @Override
    public String getID() {
        return "SealEventID";
    }
}
