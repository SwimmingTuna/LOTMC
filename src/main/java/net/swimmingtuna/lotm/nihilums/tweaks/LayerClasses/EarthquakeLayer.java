package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.DivineHandRightEntity;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.Earthquake;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class EarthquakeLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        int sailorEarthquake = livingEntity.getPersistentData().getInt("sailorEarthquake");
        if (sailorEarthquake == 0) {
            EventManager.removeFromRegularLoop(livingEntity, EFunctions.EARTHQUAKE.get());
        }
        Earthquake.earthquake(event.getEntity());
    }

    @Override
    public String getID() {
        return "EarthquakeEventID";
    }
}
