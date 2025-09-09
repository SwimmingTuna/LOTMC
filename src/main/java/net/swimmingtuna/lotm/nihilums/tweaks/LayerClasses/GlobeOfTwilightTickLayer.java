package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.MonsterDangerSense;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.GlobeOfTwilight;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class GlobeOfTwilightTickLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        GlobeOfTwilight.globeOfTwilightTick(event);
    }

    @Override
    public String getID() {
        return "GlobeOfTwilightTickEventID";
    }
}
