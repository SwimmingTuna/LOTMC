package net.swimmingtuna.lotm.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.BlockInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpaceRiftEntity extends AbstractHurtingProjectile implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Integer> MAX_LIFE = SynchedEntityData.defineId(SpaceRiftEntity.class, EntityDataSerializers.INT);


    public SpaceRiftEntity(EntityType<? extends SpaceRiftEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected float getInertia() {
        return 1.0F;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleInit.NULL_PARTICLE.get();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
         return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.getOwner() != null && this.getOwner() instanceof LivingEntity owner) {
            if (this.tickCount >= this.getMaxLife()) {
                this.discard();
            }
            suckBlocks(20 - (BeyonderUtil.getSequence(owner) * 5));
            suckEntities(20 - (BeyonderUtil.getSequence(owner) * 5));
        }
    }

    private static final Set<Block> BLOCKED_BLOCKS = new HashSet<>();

    static {
        BLOCKED_BLOCKS.add(Blocks.BEDROCK);
        BLOCKED_BLOCKS.add(BlockInit.VOID_BLOCK.get());
        BLOCKED_BLOCKS.add(BlockInit.REAL_VOID_BLOCK.get());
    }

    public void suckBlocks(int radius) {
        Level level = this.level();
        if (level.isClientSide) return;
        BlockPos center = this.blockPosition();
        float lifePercentage = (float) this.getMaxLife() / this.tickCount;
        int radiusSquared = (int) ((radius * radius) * lifePercentage);
        List<BlockPos> blocksToRemove = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    int distanceSquared = x * x + y * y + z * z;
                    if (distanceSquared <= radiusSquared) {
                        BlockPos pos = center.offset(x, y, z);
                        BlockState state = level.getBlockState(pos);

                        if (!BLOCKED_BLOCKS.contains(state.getBlock())) {
                            if (!state.isAir()) {
                                blocksToRemove.add(pos);
                            }
                        }
                    }
                }
            }
        }
        blocksToRemove.sort((pos1, pos2) -> {
            double dist1 = center.distSqr(pos1);
            double dist2 = center.distSqr(pos2);
            return Double.compare(dist1, dist2);
        });
        int blocksRemoved = 0;
        for (BlockPos pos : blocksToRemove) {
            if (blocksRemoved >= 40) break;
            level.removeBlock(pos, false);
            blocksRemoved++;
        }
    }

    public void suckEntities(int radius) {
        if (!this.level().isClientSide() && this.getOwner() != null && this.getOwner() instanceof LivingEntity owner) {
            float lifePercentage = (float) this.getMaxLife() / this.tickCount;
            float scale = BeyonderUtil.getScale(this) * lifePercentage;
            for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius * lifePercentage))) {
                if (livingEntity != owner && !BeyonderUtil.areAllies(owner, livingEntity)) {
                    if (livingEntity.distanceTo(this) < 5) {
                        livingEntity.teleportTo(this.getX(), this.getY(), this.getZ());
                        if (this.tickCount % 15 == 0) {
                            float damage = (float) Math.max((double) scale, (scale * 10 - (livingEntity.distanceTo(this) * 1.5f)));
                            livingEntity.hurt(BeyonderUtil.genericSource(owner, livingEntity), damage * 0.8f);
                        }
                    }
                    Vec3 direction = this.position().subtract(livingEntity.position());
                    double distance = direction.length();
                    if (distance < 0.1) continue;
                    if (this.tickCount % 20 == 0) {
                        float damage = (float) Math.max((double) scale, (scale * 10 - (livingEntity.distanceTo(this) * 1.5f)));
                        livingEntity.hurt(BeyonderUtil.genericSource(owner, livingEntity), damage);
                    }
                    Vec3 pullDirection = direction.normalize();
                    double pullStrength = Math.max(0.1, 3.5 / distance);
                    Vec3 pullForce = pullDirection.scale(pullStrength);
                    livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(pullForce));
                    livingEntity.hurtMarked = true;

                }
            }
        }
    }

    public int getMaxLife() {
        return this.entityData.get(MAX_LIFE);
    }

    public void setMaxLife(int maxLife) {
        this.entityData.set(MAX_LIFE, maxLife);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(MAX_LIFE, 20);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("maxLife")) {
            this.entityData.set(MAX_LIFE, tag.getInt("maxLife"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("maxLife", this.entityData.get(MAX_LIFE));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<SpaceRiftEntity> animationState) {
        AnimationController<SpaceRiftEntity> controller = animationState.getController();
        controller.setAnimationSpeed(1.0 / ((double) this.entityData.get(MAX_LIFE) / 20.0));
        controller.setAnimation(RawAnimation.begin().then("close", Animation.LoopType.HOLD_ON_LAST_FRAME));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}