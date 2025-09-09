package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;
import net.swimmingtuna.lotm.world.worlddata.BeyonderEntityData;

public class RegenerateSpiritualityLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        BeyonderEntityData.regenerateSpirituality(event);
    }

    @Override
    public String getID() {
        return "RegenerateSpiritualityEventID";
    }
}
