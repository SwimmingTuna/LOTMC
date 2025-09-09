package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.beyonder.MonsterClass;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.MonsterCalamityIncarnation;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class CalamityLightningStormLayer implements IFunction {

    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        MonsterCalamityIncarnation.calamityLightningStorm(event.getEntity());
    }

    @Override
    public String getID() {
        return "CalamityUndeadArmyEventID";
    }
}
