package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.FateReincarnation;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.DawnArmory;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class DawnArmorTickEventLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        DawnArmory.dawnArmorTickEvent(event);
    }

    @Override
    public String getID() {
        return "DawnArmorEventID";
    }
}
