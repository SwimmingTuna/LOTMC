package net.swimmingtuna.lotm.util;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import static net.swimmingtuna.lotm.util.BeyonderUtil.getMentalStrength;

public class MentalDamageSource extends DamageSource {
    private final LivingEntity attacker;
    private final LivingEntity target;

    public MentalDamageSource(Holder<DamageType> type, LivingEntity attacker, LivingEntity target) {
        super(type, attacker, attacker);
        this.attacker = attacker;
        this.target = target;
    }


    @Override
    public @NotNull Component getLocalizedDeathMessage(@NotNull LivingEntity pLivingEntity) {
        return Component.empty();
    }

    public float calculateDamage(float baseAmount) {
        if (attacker == null || target == null) return baseAmount;
        float attackerMental = getMentalStrength(attacker);
        float targetMental = getMentalStrength(target);
        if (Float.isNaN(attackerMental) || Float.isInfinite(attackerMental) || attackerMental < 0) {
            attackerMental = 10.0f;
        }
        if (Float.isNaN(targetMental) || Float.isInfinite(targetMental) || targetMental <= 0) {
            targetMental = 10.0f;
        }
        float multiplier = Math.min(2.0f, attackerMental / targetMental);
        float result = baseAmount * multiplier;
        if (Float.isNaN(result) || Float.isInfinite(result)) {
            return baseAmount * 1.2f;
        }
        target.getPersistentData().putInt("gotHitByMentalAttack", 20);
        return baseAmount * multiplier;
    }
}
