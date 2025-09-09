package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;


import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.SpatialCageEntity;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class SpatialCageLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        SpatialCageEntity.cageTick(event.getEntity());
    }

    @Override
    public String getID() {
        return "SpatialCageEventID";
    }
}
