package net.swimmingtuna.lotm.nihilums.tweaks.EventManager;

import net.minecraftforge.event.entity.living.LivingEvent;

public interface IFunction {
    void use(LivingEvent.LivingTickEvent event);

    String getID();
}
