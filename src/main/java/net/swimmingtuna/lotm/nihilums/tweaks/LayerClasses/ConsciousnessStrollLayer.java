package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.ConsciousnessStroll;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.ManipulateMovement;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class ConsciousnessStrollLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        ConsciousnessStroll.consciousnessStroll(event.getEntity());
    }

    @Override
    public String getID() {
        return "ManipulateMovementEventID";
    }
}
