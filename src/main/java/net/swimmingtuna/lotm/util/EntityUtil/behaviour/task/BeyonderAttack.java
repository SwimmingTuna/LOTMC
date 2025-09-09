package net.swimmingtuna.lotm.util.EntityUtil.behaviour.task;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.function.Predicate;

public class BeyonderAttack<E extends LivingEntity> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = List.of(
            Pair.of(
                    MemoryModuleType.ATTACK_TARGET,
                    MemoryStatus.VALUE_PRESENT
            )
    );

    protected Predicate<? extends LivingEntity> attackTarget = entity -> true;
    protected Predicate<E> canAttack = entity -> true;

    public BeyonderAttack<E> targetPredicate(Predicate<? extends LivingEntity> predicate) {
        this.attackTarget = predicate;
        return this;
    }

    public BeyonderAttack<E> canTargetPredicate(Predicate<E> predicate) {
        this.canAttack = predicate;
        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return this.canAttack.test(entity);
    }

    @Override
    protected void start(E entity) {
        LivingEntity target = BrainUtils.getMemory(entity, MemoryModuleType.ATTACK_TARGET);
        if (entity instanceof Mob mob){
            if (mob.getTarget() != null) {
                target = mob.getTarget();
            }
            //BeyonderEntityData.selectAndUseAbility(mob, target);
        }
    }
}
