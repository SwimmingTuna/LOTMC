package net.swimmingtuna.lotm.util.EntityUtil.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.util.AllyInformation.PlayerAllyData;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class GroupTargetBehaviour<E extends LivingEntity> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = List.of(
            Pair.of(
                    MemoryModuleType.HURT_BY_ENTITY,
                    MemoryStatus.VALUE_PRESENT
            ),
            Pair.of(
                    MemoryModuleType.ATTACK_TARGET,
                    MemoryStatus.REGISTERED
            )
    );

    protected Predicate<? extends LivingEntity> groupTarget = entity -> true;
    protected Predicate<E> canFightTogether = entity -> true;

    public GroupTargetBehaviour<E> targetPredicate(Predicate<? extends LivingEntity> predicate) {
        this.groupTarget = predicate;
        return this;
    }

    public GroupTargetBehaviour<E> canTargetPredicate(Predicate<E> predicate) {
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


    @Override
    protected void start(E entity) {
        LivingEntity target = BrainUtils.getMemory(entity, MemoryModuleType.HURT_BY_ENTITY);
        PlayerAllyData allyData = entity.getCommandSenderWorld().getServer().getLevel(entity.getCommandSenderWorld().dimension()).getDataStorage().computeIfAbsent(PlayerAllyData::load, PlayerAllyData::create, "player_allies");
        Set<UUID> alliesUUID = allyData.getAllies(entity.getUUID());
        for (UUID uuid : alliesUUID) {
            Entity entityFromUUID = BeyonderUtil.getEntityFromUUID(entity.level(), uuid);
            if (entityFromUUID == target) return;
        }
        boolean canAttack = true;
        if (target != null) {
            if (entity instanceof PlayerMobEntity playerMobEntity) {
                if (!playerMobEntity.canAttack(target)) {
                    canAttack = false;
                }
            }
        }
        if (canAttack) {
            BrainUtils.setMemory(entity.getBrain(), MemoryModuleType.ATTACK_TARGET, target);
        }
        for (UUID uuid : alliesUUID) {
            Entity entityFromUUID = BeyonderUtil.getEntityFromUUID(entity.level(), uuid);
            if (entityFromUUID instanceof LivingEntity livingAlly) {
                if (!BrainUtils.hasMemory(livingAlly.getBrain(), MemoryModuleType.ATTACK_TARGET)) {
                    BrainUtils.addMemories(livingAlly.getBrain(), MemoryModuleType.ATTACK_TARGET);
                }
                if (BrainUtils.getMemory(livingAlly.getBrain(), MemoryModuleType.ATTACK_TARGET) == null) {
                    BrainUtils.setMemory(livingAlly.getBrain(), MemoryModuleType.ATTACK_TARGET, target);
                }
            }
        }
    }
}
