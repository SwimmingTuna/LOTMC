package net.swimmingtuna.lotm.nihilums.tweaks.PathwaysPassiveEvents;

import net.minecraft.world.entity.LivingEntity;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;

public class MonsterPassiveEvents {
    public static void removeAllEvents(LivingEntity entity) {
        EventManager.removeFromWorldLoop(entity, EFunctions.MONSTER_TICK.get());

        EventManager.removeFromRegularLoop(entity, EFunctions.REGENERATE_SPIRITUALITY.get());
    }

    public static void addAllEvents(LivingEntity entity, int sequence) {
        EventManager.addToWorldLoop(entity, EFunctions.MONSTER_TICK.get());

        EventManager.addToRegularLoop(entity, EFunctions.REGENERATE_SPIRITUALITY.get());
    }
}
