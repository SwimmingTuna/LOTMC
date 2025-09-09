package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.beyonder.MonsterClass;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.MonsterDangerSense;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class MonsterDangerSenseLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        MonsterDangerSense.monsterDangerSense(event);
    }

    @Override
    public String getID() {
        return "MonsterDangerSenseEventID";
    }
}
