package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.CalamityIncarnationTsunami;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.ManipulateMovement;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class ManipulateMovementLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        ManipulateMovement.manipulateMovement(event.getEntity());
    }

    @Override
    public String getID() {
        return "ManipulateMovementEventID";
    }
}
