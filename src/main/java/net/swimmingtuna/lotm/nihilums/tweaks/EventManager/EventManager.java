package net.swimmingtuna.lotm.nihilums.tweaks.EventManager;

import net.minecraft.world.entity.LivingEntity;

public class EventManager {

    public static void addToRegularLoop(LivingEntity entity, IFunction func){
        entity.getCapability(EventsProvider.EVENTS_DATA).ifPresent(cap -> {
            cap.addR(func);
        });
    }

    public static void removeFromRegularLoop(LivingEntity entity, IFunction func){
        entity.getCapability(EventsProvider.EVENTS_DATA).ifPresent(cap -> {
            cap.markDeleteR(func);
        });
    }

    public static void addToWorldLoop(LivingEntity entity, IFunction func){
        entity.getCapability(EventsProvider.EVENTS_DATA).ifPresent(cap -> {
            cap.addW(func);
        });
    }

    public static void removeFromWorldLoop(LivingEntity entity, IFunction func){
        entity.getCapability(EventsProvider.EVENTS_DATA).ifPresent(cap -> {
            cap.markDeleteW(func);
        });
    }
}
