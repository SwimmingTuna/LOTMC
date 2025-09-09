package net.swimmingtuna.lotm.util.EntityUtil.behaviour.task;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.tslat.smartbrainlib.util.BrainUtils;

public class BeyonderMeleeAttackGoal extends MeleeAttackGoal {
    private final PlayerMobEntity entity;

    public BeyonderMeleeAttackGoal(PlayerMobEntity entity, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(entity, speedModifier, followingTargetEvenIfNotSeen);
        this.entity = entity;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = BrainUtils.getMemory(entity, MemoryModuleType.ATTACK_TARGET);
        if (target == null || !target.isAlive()) {
            return false;
        }
        return entity.canAttack(target);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = BrainUtils.getMemory(entity, MemoryModuleType.ATTACK_TARGET);
        return target != null && target.isAlive() && entity.canAttack(target);
    }

    @Override
    public void tick() {
        LivingEntity target = BrainUtils.getMemory(entity, MemoryModuleType.ATTACK_TARGET);
        if (target != null) {
            LivingEntity originalTarget = entity.getTarget();
            entity.setTarget(target);
            super.tick();
            if (originalTarget != target) {
                entity.setTarget(originalTarget);
            }
        }
    }
}