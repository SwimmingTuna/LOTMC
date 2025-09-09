package net.swimmingtuna.lotm.util.EntityUtil.behaviour.task;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.EnumSet;

public class BeyonderRangedAttackGoal<T extends PlayerMobEntity & RangedAttackMob> extends Goal {
    private final T entity;
    private final double speedModifier;
    private int attackIntervalMin;
    private final float attackRadiusSqr;
    private int attackTime;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime;

    public BeyonderRangedAttackGoal(T entity, double speedModifier, int attackIntervalMin, float attackRadius) {
        this.attackTime = -1;
        this.strafingTime = -1;
        this.entity = entity;
        this.speedModifier = speedModifier;
        this.attackIntervalMin = attackIntervalMin;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public void setMinAttackInterval(int attackCooldown) {
        this.attackIntervalMin = attackCooldown;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = BrainUtils.getMemory(entity, MemoryModuleType.ATTACK_TARGET);
        if (target == null || !target.isAlive()) {
            return false;
        }
        return entity.canAttack(target) && this.isHoldingBow();
    }

    protected boolean isHoldingBow() {
        return this.entity.isHolding((is) -> {
            return is.getItem() instanceof BowItem;
        });
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = BrainUtils.getMemory(entity, MemoryModuleType.ATTACK_TARGET);
        return target != null && target.isAlive() && entity.canAttack(target) && (this.canUse() || !this.entity.getNavigation().isDone()) && this.isHoldingBow();
    }

    @Override
    public void start() {
        super.start();
        this.entity.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.entity.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.entity.stopUsingItem();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = BrainUtils.getMemory(entity, MemoryModuleType.ATTACK_TARGET);
        if (target != null) {
            // Temporarily set target for compatibility with existing systems
            LivingEntity originalTarget = entity.getTarget();
            entity.setTarget(target);

            double d0 = this.entity.distanceToSqr(target.getX(), target.getY(), target.getZ());
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

            if (!(d0 > (double)this.attackRadiusSqr) && this.seeTime >= 20) {
                this.entity.getNavigation().stop();
                ++this.strafingTime;
            } else {
                this.entity.getNavigation().moveTo(target, this.speedModifier);
                this.strafingTime = -1;
            }

            if (this.strafingTime >= 20) {
                if ((double)this.entity.getRandom().nextFloat() < 0.3) {
                    this.strafingClockwise = !this.strafingClockwise;
                }

                if ((double)this.entity.getRandom().nextFloat() < 0.3) {
                    this.strafingBackwards = !this.strafingBackwards;
                }

                this.strafingTime = 0;
            }

            if (this.strafingTime > -1) {
                if (d0 > (double)(this.attackRadiusSqr * 0.75F)) {
                    this.strafingBackwards = false;
                } else if (d0 < (double)(this.attackRadiusSqr * 0.25F)) {
                    this.strafingBackwards = true;
                }

                this.entity.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
                Entity entity = this.entity.getControlledVehicle();
                if (entity instanceof Mob) {
                    Mob mob = (Mob)entity;
                    mob.lookAt(target, 30.0F, 30.0F);
                }

                this.entity.lookAt(target, 30.0F, 30.0F);
            } else {
                this.entity.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }

            if (this.entity.isUsingItem()) {
                if (!flag && this.seeTime < -60) {
                    this.entity.stopUsingItem();
                } else if (flag) {
                    int i = this.entity.getTicksUsingItem();
                    if (i >= 20) {
                        this.entity.stopUsingItem();
                        ((RangedAttackMob)this.entity).performRangedAttack(target, BowItem.getPowerForTime(i));
                        this.attackTime = this.attackIntervalMin;
                    }
                }
            } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
                this.entity.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.entity, (item) -> {
                    return item instanceof BowItem;
                }));
            }

            // Restore original target if it was different
            if (originalTarget != target) {
                entity.setTarget(originalTarget);
            }
        }
    }
}
