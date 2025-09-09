package net.swimmingtuna.lotm.util.EntityUtil.behaviour.task;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.tslat.smartbrainlib.util.BrainUtils;

public class BeyonderAwareHurtByTargetGoal extends HurtByTargetGoal {
    private final PlayerMobEntity entity;

    public BeyonderAwareHurtByTargetGoal(PlayerMobEntity entity, Class<?>... toIgnoreDamage) {
        super(entity, toIgnoreDamage);
        this.entity = entity;
    }

    @Override
    public void start() {
        LivingEntity attacker = entity.getLastHurtByMob();
        if (attacker != null && entity.canAttack(attacker)) {
            BrainUtils.setMemory(entity, MemoryModuleType.ATTACK_TARGET, attacker);
        }
        super.start();
    }
}