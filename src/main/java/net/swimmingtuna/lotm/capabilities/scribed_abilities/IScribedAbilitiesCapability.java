package net.swimmingtuna.lotm.capabilities.scribed_abilities;

import net.minecraft.world.item.Item;

import java.util.Map;

public interface IScribedAbilitiesCapability {
    Map<Item, Integer> getScribedAbilities();

    void copyScribeAbility(Item ability);

    boolean hasScribedAbility(Item ability);

    void useScribeAbility(Item ability);

    int getRemainUses(Item ability);

    int getScribedAbilitiesCount();
}
