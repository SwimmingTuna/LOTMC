package net.swimmingtuna.lotm.nihilums.tweaks.PathwaysPassiveEvents;

import net.minecraft.world.entity.LivingEntity;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;

public class ApprenticePassiveEvents  {
    public static void removeAllEvents(LivingEntity entity) {
        EventManager.removeFromWorldLoop(entity, EFunctions.WATER_WALKING.get());
        EventManager.removeFromWorldLoop(entity, EFunctions.APPRENTICE_SP_TICK.get());
        EventManager.removeFromWorldLoop(entity, EFunctions.APPRENTICE_TICK.get());

        EventManager.removeFromRegularLoop(entity, EFunctions.REGENERATE_SPIRITUALITY.get());
    }

    public static void addAllEvents(LivingEntity entity, int sequence) {
        switch(sequence){
            case 0:
            case 1:
            case 2:
            case 3:
                EventManager.addToWorldLoop(entity, EFunctions.WATER_WALKING.get());
            case 4:
                EventManager.addToWorldLoop(entity, EFunctions.APPRENTICE_SP_TICK.get());
            case 5:
            case 6:
                EventManager.addToWorldLoop(entity, EFunctions.APPRENTICE_TICK.get());
            case 7:
            case 8:
            case 9:
                EventManager.addToRegularLoop(entity, EFunctions.REGENERATE_SPIRITUALITY.get());
        }
    }
}
