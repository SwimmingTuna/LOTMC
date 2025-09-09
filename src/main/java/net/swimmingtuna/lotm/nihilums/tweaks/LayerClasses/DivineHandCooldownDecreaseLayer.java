package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.DivineHandRightEntity;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.BlinkState;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class DivineHandCooldownDecreaseLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        DivineHandRightEntity.divineHandCooldownDecrease(event.getEntity());
    }

    @Override
    public String getID() {
        return "DivineHandCooldownDecreaseEventID";
    }
}
