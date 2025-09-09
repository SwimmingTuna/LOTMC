package net.swimmingtuna.lotm.util.EntityUtil.behaviour;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.util.AllyInformation.PlayerAllyData;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class PassiveAttackBehavior<E extends LivingEntity> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = List.of(
            Pair.of(
                    MemoryModuleType.NEAREST_LIVING_ENTITIES,
                    MemoryStatus.VALUE_PRESENT
            ),
            Pair.of(
                    MemoryModuleType.ATTACK_TARGET,
                    MemoryStatus.REGISTERED
            )
    );

    protected Predicate<? extends LivingEntity> groupTarget = entity -> true;
    protected Predicate<E> canFightTogether = entity -> true;

    public PassiveAttackBehavior<E> targetPredicate(Predicate<? extends LivingEntity> predicate) {
        this.groupTarget = predicate;
        return this;
    }

    public PassiveAttackBehavior<E> canTargetPredicate(Predicate<E> predicate) {
        this.canFightTogether = predicate;
        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return this.canFightTogether.test(entity);
    }

    private E getTarget(E entity) {
        List<LivingEntity> nearbyEntities = BrainUtils.getMemory(entity, MemoryModuleType.NEAREST_LIVING_ENTITIES);
        PlayerAllyData allyData = entity.getCommandSenderWorld().getServer().getLevel(entity.getCommandSenderWorld().dimension()).getDataStorage().computeIfAbsent(PlayerAllyData::load, PlayerAllyData::create, "player_allies");
        List<LivingEntity> entities = nearbyEntities.stream().map(entity1 -> (LivingEntity) entity1).toList();
        for (LivingEntity livingEntity : entities) {
            if (!BeyonderUtil.areAllies(livingEntity, entity)) {
                return (E) livingEntity;
            }
        }
        return null;
    }

    @Override
    protected void start(E entity) {
        if (entity instanceof PlayerMobEntity beyonderEntity && entity.tickCount % 100 == 0) {
            try {
                E target = getTarget(entity);
                boolean canAttack = true;
                if (target != null) {
                    if (!beyonderEntity.canAttack(target)) {
                        canAttack = false;
                    }
                }
                float random = new Random().nextFloat(0, 100);
                if (beyonderEntity.getAttackChance() >= random) {
                    if (!BrainUtils.hasMemory(entity.getBrain(), MemoryModuleType.ATTACK_TARGET)) {
                        BrainUtils.addMemories(entity.getBrain(), MemoryModuleType.ATTACK_TARGET);
                    }
                    if (target != null && !isSameOwner(beyonderEntity, target) && canAttack) {
                        BrainUtils.setMemory(entity.getBrain(), MemoryModuleType.ATTACK_TARGET, target);
                    }
                }
                if (beyonderEntity.getCreator() != null && beyonderEntity.getIsClone()) {
                    LivingEntity livingEntity = beyonderEntity.getCreator();
                    LivingEntity lastHurtMob = livingEntity.getLastHurtMob();

                    if (lastHurtMob instanceof PlayerMobEntity playerMobEntity && playerMobEntity.getCreator() != null && playerMobEntity.getCreator().equals(beyonderEntity.getCreator())) {
                        return;
                    }
                    if (lastHurtMob != null && !BeyonderUtil.areAllies(livingEntity, lastHurtMob)) {
                        BrainUtils.setMemory(entity.getBrain(), MemoryModuleType.ATTACK_TARGET, lastHurtMob);
                    }
                }
            } catch (NullPointerException e) {
                LogUtils.getLogger().warn("LOTMC: Attack Chance of Entity was not present");
            }
        }
    }

    private static boolean isSameOwner(PlayerMobEntity entity1, LivingEntity entity2) {
        if (!(entity2 instanceof PlayerMobEntity playerMob2)) {
            return false;
        }
        LivingEntity owner1 = entity1.getCreator();
        LivingEntity owner2 = playerMob2.getCreator();

        return owner1 != null && owner2 != null && owner1.equals(owner2);
    }
}