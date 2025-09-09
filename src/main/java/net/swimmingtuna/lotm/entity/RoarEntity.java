package net.swimmingtuna.lotm.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.List;
import java.util.Random;

public class RoarEntity extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(RoarEntity.class, EntityDataSerializers.BOOLEAN);

    public RoarEntity(EntityType<? extends RoarEntity> entityType, Level level) {
        super(entityType, level);
    }

    public RoarEntity(Level level, LivingEntity shooter, double offsetX, double offsetY, double offsetZ) {
        super(EntityInit.ROAR_ENTITY.get(), shooter, offsetX, offsetY, offsetZ, level);
    }

    @Override
    protected float getInertia() {
        return 1.0f;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public @NotNull ParticleOptions getTrailParticle() {
        return ParticleInit.NULL_PARTICLE.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide() && this.getOwner() != null) {
            Entity entity = result.getEntity();
            if (entity.distanceTo(this) < 5) {
                ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);
                if (entity instanceof Projectile) {
                    float explosionRadius = 3 * scaleData.getScale();
                    BeyonderUtil.destroyBlocksInSphereNotHittingOwner(this, this.getOnPos(), explosionRadius, explosionRadius);
                }
                if (entity instanceof LivingEntity livingEntity) {
                    if (getOwner() != null && getOwner() instanceof LivingEntity owner) {
                        if (!BeyonderUtil.areAllies(livingEntity, owner) && livingEntity != owner) {
                            livingEntity.hurt(BeyonderUtil.genericSource(this.getOwner(), livingEntity), (int) (20 * scaleData.getScale()));
                            float explosionRadius = 3 * scaleData.getScale();
                            BeyonderUtil.destroyBlocksInSphereNotHittingOwner(this, this.getOnPos(), explosionRadius, explosionRadius);
                        }
                    } else {
                        livingEntity.hurt(BeyonderUtil.genericSource(this, livingEntity), (int) (20 * scaleData.getScale()));
                        float explosionRadius = 3 * scaleData.getScale();
                        BeyonderUtil.destroyBlocksInSphereNotHittingOwner(this, this.getOnPos(), explosionRadius, explosionRadius);
                    }
                }
                this.discard();
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide()) {

        }
    }

    public boolean isPickable() {
        return false;
    }

    protected void defineSynchedData() {
        this.entityData.define(DATA_DANGEROUS, false);
    }

    public boolean isDangerous() {
        return this.entityData.get(DATA_DANGEROUS);
    }

    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);
        float radius = 1.5f * scaleData.getScale();
        if (!this.level().isClientSide()) {
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY(), this.getZ(), 0,0,0,0,0);
            }
            BlockPos center = new BlockPos((int) this.getX(), (int) this.getY(), (int) this.getZ());
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-(int) radius, -(int) radius, -(int) radius), center.offset((int) radius, (int) radius, (int) radius))) {
                Vec3i vec = new Vec3i(pos.getX(), pos.getY(), pos.getZ());
                if (pos.distSqr(vec) <= radius * radius) {
                    BlockState state = this.level().getBlockState(pos);
                    Block block = state.getBlock();
                    float blockStrength = block.defaultDestroyTime();
                    float obsidianStrength = Blocks.OBSIDIAN.defaultDestroyTime();

                    if (blockStrength <= obsidianStrength && block != Blocks.BEDROCK) {
                        this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
            if (this.getOwner() instanceof Player player) {
                if (this.tickCount == 1) {
                    BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                    int sequence = holder.getSequence();
                    if (sequence <= 4) {
                        scaleData.setTargetScale(7 - (sequence * 1.0f));
                        scaleData.markForSync(true);
                    }
                }
            }
            if (this.tickCount % 5 == 0) {
                int numParticles = (int) Math.min(100, 20 * scaleData.getScale());
                Random random = new Random();
                for (int i = 0; i < numParticles; i++) {
                    double angle = random.nextDouble() * 2 * Math.PI;
                    double distance = random.nextDouble() * radius;
                    double offsetX = distance * Math.cos(angle);
                    double offsetZ = distance * Math.sin(angle);
                    double offsetY = random.nextDouble() * radius * 2 - radius;
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0,0,0,0,0);
                    }
                }
            }
            float damage = 10.0F * scaleData.getScale();
            float explosionRadius = 1.5f * scaleData.getScale();
            for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(explosionRadius))) {
                if (this.getOwner() != null && this.getOwner() instanceof LivingEntity owner) {
                    if (entity != this.getOwner()) {
                        int sequence = BeyonderUtil.getSequence(owner);
                        int hitSequence = BeyonderUtil.getSequence(entity);
                        if (hitSequence > sequence + 2) {
                            roarExplode(damage / 2, this.getOnPos(), BeyonderUtil.getScale(this));
                            this.level().playSound(null, this.getOnPos(), SoundEvents.GENERIC_EXPLODE, SoundSource.AMBIENT, 10.0f, 1.5f);
                            this.discard();
                        } else if (this.tickCount % 5 == 0) {
                            entity.hurt(BeyonderUtil.explosionSource(owner, entity), damage / 3);
                        }
                    }
                }
            }

            if (this.tickCount >= 75) {
                this.discard();
            }
        }
    }

    public void roarExplode(double radius, BlockPos hitPos, float scale) {
        List<Entity> entities = this.level().getEntities(this,
                new AABB(hitPos.offset((int) -radius, (int) -radius, (int) -radius),
                        hitPos.offset((int) radius, (int) radius, (int) radius)));
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity) {
                if (this.getOwner() == null) {
                    livingEntity.hurt(BeyonderUtil.genericSource(this, livingEntity), 10 * scale);
                } else {
                    livingEntity.hurt(BeyonderUtil.genericSource(this.getOwner(), livingEntity), 10 * scale);
                }
            }
        }
    }
}
