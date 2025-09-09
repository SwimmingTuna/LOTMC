package net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public abstract class LeftClickHandlerSword extends SwordItem {
    public LeftClickHandlerSword(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    public abstract LeftClickType getleftClickEmpty();
}
