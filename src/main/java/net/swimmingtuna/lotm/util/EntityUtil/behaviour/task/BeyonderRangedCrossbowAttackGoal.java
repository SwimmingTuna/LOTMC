package net.swimmingtuna.lotm.util.EntityUtil.behaviour.task;

import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.EnumSet;
// Import your custom classes

public class BeyonderRangedCrossbowAttackGoal<T extends PlayerMobEntity & RangedAttackMob & CrossbowAttackMob> extends Goal {
    public static final UniformInt PATHFINDING_DELAY_RANGE = TimeUtil.rangeOfSeconds(1, 2);
    private final T entity;
    private CrossbowState crossbowState;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private int seeTime;
    private int attackDelay;
    private int updatePathDelay;

    public BeyonderRangedCrossbowAttackGoal(T entity, double speedModifier, float attackRadius) {
        this.crossbowState = CrossbowState.UNCHARGED;
        this.entity = entity;
        this.speedModifier = speedModifier;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.isValidTarget() && this.isHoldingCrossbow();
    }

    private boolean isHoldingCrossbow() {
        return this.entity.isHolding((is) -> {
            return is.getItem() instanceof CrossbowItem;
        });
    }

    @Override
    public boolean canContinueToUse() {
        return this.isValidTarget() && (this.canUse() || !this.entity.getNavigation().isDone()) && this.isHoldingCrossbow();
    }

    private boolean isValidTarget() {
        LivingEntity target = BrainUtils.getMemory(entity, MemoryModuleType.ATTACK_TARGET);
        return target != null && target.isAlive() && entity.canAttack(target);
    }

    @Override
    public void stop() {
        super.stop();
        this.entity.setAggressive(false);
        this.entity.setTarget(null);
        this.seeTime = 0;
        if (this.entity.isUsingItem()) {
            this.entity.stopUsingItem();
            ((CrossbowAttackMob)this.entity).setChargingCrossbow(false);
            CrossbowItem.setCharged(this.entity.getUseItem(), false);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = BrainUtils.getMemory(entity, MemoryModuleType.ATTACK_TARGET);
        if (target != null) {
            LivingEntity originalTarget = entity.getTarget();
            entity.setTarget(target);

            boolean flag = this.entity.getSensing().hasLineOfSight(target);
            boolean flag1 = this.seeTime > 0;
            if (flag != flag1) {
                this.seeTime = 0;
            }

            if (flag) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }

            double d0 = this.entity.distanceToSqr(target);
            boolean flag2 = (d0 > (double)this.attackRadiusSqr || this.seeTime < 5) && this.attackDelay == 0;
            if (flag2) {
                --this.updatePathDelay;
                if (this.updatePathDelay <= 0) {
                    this.entity.getNavigation().moveTo(target, this.canRun() ? this.speedModifier : this.speedModifier * 0.5);
                    this.updatePathDelay = PATHFINDING_DELAY_RANGE.sample(this.entity.getRandom());
                }
            } else {
                this.updatePathDelay = 0;
                this.entity.getNavigation().stop();
            }

            this.entity.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (this.crossbowState == CrossbowState.UNCHARGED) {
                if (!flag2) {
                    this.entity.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.entity, (item) -> {
                        return item instanceof CrossbowItem;
                    }));
                    this.crossbowState = CrossbowState.CHARGING;
                    ((CrossbowAttackMob)this.entity).setChargingCrossbow(true);
                }
            } else if (this.crossbowState == CrossbowState.CHARGING) {
                if (!this.entity.isUsingItem()) {
                    this.crossbowState = CrossbowState.UNCHARGED;
                }

                int i = this.entity.getTicksUsingItem();
                ItemStack itemstack = this.entity.getUseItem();
                if (i >= CrossbowItem.getChargeDuration(itemstack)) {
                    this.entity.releaseUsingItem();
                    this.crossbowState = CrossbowState.CHARGED;
                    this.attackDelay = 20 + this.entity.getRandom().nextInt(20);
                    ((CrossbowAttackMob)this.entity).setChargingCrossbow(false);
                }
            } else if (this.crossbowState == CrossbowState.CHARGED) {
                --this.attackDelay;
                if (this.attackDelay == 0) {
                    this.crossbowState = CrossbowState.READY_TO_ATTACK;
                }
            } else if (this.crossbowState == CrossbowState.READY_TO_ATTACK && flag) {
                ((RangedAttackMob)this.entity).performRangedAttack(target, 1.0F);
                ItemStack itemstack1 = this.entity.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this.entity, (item) -> {
                    return item instanceof CrossbowItem;
                }));
                CrossbowItem.setCharged(itemstack1, false);
                this.crossbowState = CrossbowState.UNCHARGED;
            }

            // Restore original target if it was different
            if (originalTarget != target) {
                entity.setTarget(originalTarget);
            }
        }
    }

    private boolean canRun() {
        return this.crossbowState == CrossbowState.UNCHARGED;
    }

    enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK;
    }
}