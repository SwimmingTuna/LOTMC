package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.BlinkState;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.LightningRedirection;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class LightningRedirectionLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        LightningRedirection.lightningRedirectionTick(event);
    }

    @Override
    public String getID() {
        return "LightningRedirectionEventID";
    }
}
