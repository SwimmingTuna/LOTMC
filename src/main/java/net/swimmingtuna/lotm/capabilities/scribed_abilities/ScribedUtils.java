package net.swimmingtuna.lotm.capabilities.scribed_abilities;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ScribedUtils {

    public static Optional<IScribedAbilitiesCapability> getScribedAbilitiesData(LivingEntity entity){
        return entity.getCapability(ScribedAbilitiesProvider.SCRIBED_ABILITIES).resolve();
    }

    public static Map<Item, Integer> getScribedAbilities(LivingEntity entity){
        return getScribedAbilitiesData(entity).map(IScribedAbilitiesCapability::getScribedAbilities).orElse(new HashMap<>());
    }

    public static void copyAbility(LivingEntity entity, Item ability){
        entity.getCapability(ScribedAbilitiesProvider.SCRIBED_ABILITIES).ifPresent(data -> {
            data.copyScribeAbility(ability);
        });
    }

    public static boolean hasAbility(LivingEntity entity, Item ability){
        return getScribedAbilitiesData(entity).map(data -> data.hasScribedAbility(ability)).orElse(false);
    }

    public static void useScribedAbility(LivingEntity entity, Item ability){
        entity.getCapability(ScribedAbilitiesProvider.SCRIBED_ABILITIES).ifPresent(data -> {
            data.useScribeAbility(ability);
        });
    }

    public static int getRemainingUses(LivingEntity entity, Item ability){
        return getScribedAbilitiesData(entity).map(data -> data.getRemainUses(ability)).orElse(0);
    }

    public static int getAbilitiesCount(LivingEntity entity){
        return getScribedAbilitiesData(entity).map(IScribedAbilitiesCapability::getScribedAbilitiesCount).orElse(0);
    }
}