package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.GlobeOfTwilight;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.MercuryLiquefication;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class MercuryTickLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        MercuryLiquefication.mercuryArmorTick(event);
    }

    @Override
    public String getID() {
        return "MercuryTickEventID";
    }
}
