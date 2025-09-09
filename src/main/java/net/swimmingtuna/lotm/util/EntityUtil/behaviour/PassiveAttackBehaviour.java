package net.swimmingtuna.lotm.util.EntityUtil.behaviour;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
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
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class PassiveAttackBehaviour<E extends LivingEntity> extends ExtendedBehaviour<E> {

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

    public PassiveAttackBehaviour<E> targetPredicate(Predicate<? extends LivingEntity> predicate) {
        this.groupTarget = predicate;
        return this;
    }

    public PassiveAttackBehaviour<E> canTargetPredicate(Predicate<E> predicate) {
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

    private E getTarget(E entity){
        List<LivingEntity> nearbyEntities = BrainUtils.getMemory(entity, MemoryModuleType.NEAREST_LIVING_ENTITIES);
        PlayerAllyData allyData = entity.getCommandSenderWorld().getServer().getLevel(entity.getCommandSenderWorld().dimension()).getDataStorage().computeIfAbsent(PlayerAllyData::load, PlayerAllyData::create, "player_allies");
        Set<UUID> alliesUUID = allyData.getAllies(entity.getUUID());
        List<Entity> entities = nearbyEntities.stream().map(entity1 -> (Entity) entity1).toList();
        for (UUID uuid : alliesUUID) {
            Entity entityFromUUID = ((ServerLevel) entity.level()).getEntity(uuid);
            if (!entities.contains(entityFromUUID)) {
                if (entity instanceof PlayerMobEntity beyonderEntity && entityFromUUID instanceof PlayerMobEntity targetPlayerMob) {
                    if (isSameOwner(beyonderEntity, targetPlayerMob)) {
                        continue; // Skip this target, look for another
                    }
                }
                return (E) entityFromUUID;
            }
        }
        return null;
    }

    private boolean isSameOwner(PlayerMobEntity entity1, PlayerMobEntity entity2) {
        LivingEntity owner1 = entity1.getCreator();
        LivingEntity owner2 = entity2.getCreator();

        return owner1 != null && owner2 != null && owner1.equals(owner2);
    }

    @Override
    protected void start(E entity) {
        if (entity instanceof PlayerMobEntity beyonderEntity){
            try {
                E target = getTarget(entity);
                boolean canAttack = true;
                if (target != null) {
                    if (!beyonderEntity.canAttack(target)) {
                        canAttack = false;
                    }
                }
                if (beyonderEntity.getAttackChance() >= new Random().nextFloat(0, 100)) {
                    if (!BrainUtils.hasMemory(entity.getBrain(), MemoryModuleType.ATTACK_TARGET) && canAttack) { // prob gets checked often, but need to be sure memory exists
                        BrainUtils.addMemories(entity.getBrain(), MemoryModuleType.ATTACK_TARGET);
                    }
                    if (target != null && !BeyonderUtil.areAllies(target, beyonderEntity) && canAttack) {
                        BrainUtils.setMemory(entity.getBrain(), MemoryModuleType.ATTACK_TARGET, target);
                    }
                }
            } catch (NullPointerException e){
                LogUtils.getLogger().warn("LOTM Carft: Attack Chance of Entity was not present ");
            }
        }
    }
}