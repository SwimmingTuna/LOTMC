package net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler;

import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;

import java.util.function.Supplier;

public abstract class LeftClickHandlerSkillP extends SimpleAbilityItem {

    protected LeftClickHandlerSkillP(Properties properties, BeyonderClass requiredClass, int requiredSequence, int requiredSpirituality, int cooldown) {
        super(properties, requiredClass, requiredSequence, requiredSpirituality, cooldown);
    }

    protected LeftClickHandlerSkillP(Properties properties, Supplier<? extends BeyonderClass> requiredClass, int requiredSequence, int requiredSpirituality, int cooldown) {
        super(properties, requiredClass, requiredSequence, requiredSpirituality, cooldown);
    }

    protected LeftClickHandlerSkillP(Properties properties, BeyonderClass requiredClass, int requiredSequence, int requiredSpirituality, int cooldown, double entityReach, double blockReach) {
        super(properties, requiredClass, requiredSequence, requiredSpirituality, cooldown, entityReach, blockReach);
    }

    protected LeftClickHandlerSkillP(Properties properties, Supplier<? extends BeyonderClass> requiredClass, int requiredSequence, int requiredSpirituality, int cooldown, double entityReach, double blockReach) {
        super(properties, requiredClass, requiredSequence, requiredSpirituality, cooldown, entityReach, blockReach);
    }

    public abstract <T> LeftClickType getleftClickEmpty(T item);
}
