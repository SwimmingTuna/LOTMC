package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.BlinkState;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.MisfortuneImplosion;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class MisfortuneImplosionLightningLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().getPersistentData().getInt("monsterImplosionLightning") == 0) {
            EventManager.removeFromRegularLoop(event.getEntity(), EFunctions.MISFORTUNEIMPLOSIONLIGHTNING.get());
        }
        MisfortuneImplosion.misfortuneImplosionLightning(event);
    }

    @Override
    public String getID() {
        return "MisfortuneImplosionLightningEventID";
    }
}
