package net.swimmingtuna.lotm.entity;


import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class FlashEntity extends AbstractHurtingProjectile {



    public FlashEntity(EntityType<? extends FlashEntity> entityType, Level level) {
        super(entityType, level);
    }


    @Override
    protected float getInertia() {
        return 0.8F;
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public @NotNull ParticleOptions getTrailParticle() {
        return ParticleInit.NULL_PARTICLE.get();
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void defineSynchedData() {

    }




    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            if (this.tickCount <= 40) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    Random random = new Random();
                    for (int i = 0; i < 10; i++) {
                        double x = this.getX() + (random.nextDouble() - 0.5) * BeyonderUtil.getScale(this) * 2;
                        double y = this.getY() + 0.5 + (random.nextDouble() - 0.5) * BeyonderUtil.getScale(this) * 2;
                        double z = this.getZ() + (random.nextDouble() - 0.5) * BeyonderUtil.getScale(this) * 2;
                        double velocityX = (random.nextDouble() - 0.5) * 0.5;
                        double velocityY = (random.nextDouble() - 0.5) * 0.5;
                        double velocityZ = (random.nextDouble() - 0.5) * 0.5;
                        serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 0, velocityX, velocityY, velocityZ, 0.1 * BeyonderUtil.getScale(this));
                    }
                    if (this.tickCount == 37 || this.tickCount == 38 || this.tickCount == 39) {
                        for (int i = 0; i < 15; i++) {
                            double x = this.getX() + (random.nextDouble() - 0.5) * BeyonderUtil.getScale(this) * 3;
                            double y = this.getY() + 0.5 + (random.nextDouble() - 0.5) * BeyonderUtil.getScale(this) * 3;
                            double z = this.getZ() + (random.nextDouble() - 0.5) * BeyonderUtil.getScale(this) * 3;
                            serverLevel.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 1, 0, 0, 0, 0);
                        }
                    }
                }
            } if (this.getOwner() != null && this.getOwner() instanceof LivingEntity owner && this.tickCount > 40) {
                for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(BeyonderUtil.getScale(this) * 25))) {
                    if (livingEntity != owner) {
                        if (isEntityLookingAtThis(livingEntity)) {
                            if (livingEntity instanceof Mob mob && mob.getTarget() != null) {
                                mob.setTarget(null);
                            }
                            BeyonderUtil.applyMobEffect(livingEntity, ModEffects.FLASH.get(), (int) ((int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TRICKFLASH.get()) * BeyonderUtil.getScale(this)), 1, true, true);
                        }
                    }
                }
            }
            if (this.tickCount >= 42) {
                this.discard();
            }
        }
    }

    private boolean isEntityLookingAtThis(LivingEntity entity) {
        Vec3 entityPos = entity.getEyePosition();
        Vec3 thisPos = new Vec3(this.getX(), this.getY() + this.getBbHeight() / 2, this.getZ());
        Vec3 dirToThis = thisPos.subtract(entityPos).normalize();
        Vec3 lookDir = entity.getViewVector(1.0F).normalize();

        double dotProduct = dirToThis.dot(lookDir);
        return dotProduct > 0.7;
    }
}
