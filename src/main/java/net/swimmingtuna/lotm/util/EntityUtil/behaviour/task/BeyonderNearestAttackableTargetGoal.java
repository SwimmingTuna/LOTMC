package net.swimmingtuna.lotm.util.EntityUtil.behaviour.task;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class BeyonderNearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private final PlayerMobEntity entity;

    public BeyonderNearestAttackableTargetGoal(PlayerMobEntity entity, Class<T> targetType, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(entity, targetType, 10, mustSee, mustReach, targetPredicate); // Use the full constructor with default interval of 10
        this.entity = entity;
    }

    public BeyonderNearestAttackableTargetGoal(PlayerMobEntity entity, Class<T> targetType, boolean mustSee) {
        super(entity, targetType, mustSee); // This constructor exists
        this.entity = entity;
    }

    public BeyonderNearestAttackableTargetGoal(PlayerMobEntity entity, Class<T> targetType, int randomInterval, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(entity, targetType, randomInterval, mustSee, mustReach, targetPredicate);
        this.entity = entity;
    }

    @Override
    public void start() {
        super.start();
        // Set the found target in brain memory
        LivingEntity foundTarget = entity.getTarget();
        if (foundTarget != null) {
            BrainUtils.setMemory(entity, MemoryModuleType.ATTACK_TARGET, foundTarget);
        }
    }

    @Override
    public boolean canUse() {
        // Check if we already have a target in brain memory
        LivingEntity currentTarget = BrainUtils.getMemory(entity, MemoryModuleType.ATTACK_TARGET);
        if (currentTarget != null && currentTarget.isAlive() && entity.canAttack(currentTarget)) {
            return false; // Don't look for new targets if we have a valid one
        }
        return super.canUse();
    }
}