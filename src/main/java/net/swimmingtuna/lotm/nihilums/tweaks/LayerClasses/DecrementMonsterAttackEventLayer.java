package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.beyonder.MonsterClass;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class DecrementMonsterAttackEventLayer implements IFunction {

    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        MonsterClass.decrementMonsterAttackEvent(event.getEntity());
    }

    @Override
    public String getID() {
        return "DecrementMonsterAttackEventID";
    }
}
