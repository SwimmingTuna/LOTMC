package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.CalamityIncarnationTsunami;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.ExtremeColdness;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class CalamityIncarnationTsunamiTickLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        CalamityIncarnationTsunami.calamityIncarnationTsunamiTick(event.getEntity());
    }

    @Override
    public String getID() {
        return "ExtremeColdnessEventID";
    }
}
