package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.beyonder.MonsterClass;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.ChaosWalkerDisableEnable;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class OnChaosWalkerCombatLayer implements IFunction {

    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        ChaosWalkerDisableEnable.onChaosWalkerCombat(event.getEntity());
    }

    @Override
    public String getID() {
        return "OnChaosWalkerCombatEventID";
    }
}
