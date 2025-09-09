package net.swimmingtuna.lotm.entity;


import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class AqueousLightEntityPull extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(AqueousLightEntityPull.class, EntityDataSerializers.BOOLEAN);

    public AqueousLightEntityPull(EntityType<? extends AqueousLightEntityPull> entityType, Level level) {
        super(entityType, level);
    }

    public AqueousLightEntityPull(Level level, LivingEntity shooter, double offsetX, double offsetY, double offsetZ) {
        super(EntityInit.AQUEOUS_LIGHT_ENTITY_PULL.get(), shooter, offsetX, offsetY, offsetZ, level);
    }


    /**
     * Return the motion factor for this projectile. The factor is multiplied by the original motion.
     */
    @Override
    protected float getInertia() {
        return this.isDangerous() ? 0.99F : super.getInertia();
    }

    /**
     * Returns {@code true} if the entity is on fire. Used by render to add the fire effect on rendering.
     */
    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public @NotNull ParticleOptions getTrailParticle() {
        return ParticleInit.NULL_PARTICLE.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide() || !(result.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        if (this.getOwner() != null) {
            if (entity == this.getOwner()) {
                return;
            }
        }
        if (this.getOwner() == null) {
            return;
        }
        LivingEntity owner = (LivingEntity) this.getOwner();
        double x = owner.getX() - entity.getX();
        double y = Math.min(5, owner.getY() - entity.getY());
        double z = owner.getZ() - entity.getZ();
        entity.setDeltaMovement(x * 0.3, y * 0.3, z * 0.3);
        CompoundTag ownerTag = owner.getPersistentData();
        boolean sailorLightning = ownerTag.getBoolean("SailorLightning");
        if (!(owner instanceof Player player)) {
            this.discard();
            return;
        }
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        if (entity.level().isClientSide() || owner.level().isClientSide()) {
            this.discard();
            return;
        }
        int damage = (int) (BeyonderUtil.getScale(this) * 2.5);
        if (this.getOwner() == null) {
            entity.hurt(BeyonderUtil.genericSource(this, entity), damage);
        } else {
            entity.hurt(BeyonderUtil.genericSource(this.getOwner(), entity), damage);
        }
        if (holder.getSequence() > 7) {
            this.discard();
            return;
        }
        double chanceOfDamage = (100.0 - (holder.getSequence() * 12.5)); // Decrease chance by 12.5% for each level below 9
        if (Math.random() * 100 < chanceOfDamage && sailorLightning) {
            LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, entity.level());
            lightningBolt.setDamage(3);
            lightningBolt.moveTo(entity.getX(), entity.getY(), entity.getZ());
            entity.level().addFreshEntity(lightningBolt);
        }
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide && !this.level().dimension().equals(Level.NETHER)) {
            this.level().broadcastEntityEvent(this, ((byte) 3));
            this.level().setBlock(blockPosition(), Blocks.WATER.defaultBlockState(), 3);
            this.discard();
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_DANGEROUS, false);
    }

    public boolean isDangerous() {
        return this.entityData.get(DATA_DANGEROUS);
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    public static void summonEntityWithSpeed(Vec3 direction, Vec3 initialVelocity, Vec3 eyePosition, double x, double y, double z, LivingEntity player, float scale) {
        if (!player.level().isClientSide()) {
            AqueousLightEntityPull aqueousLightEntity = new AqueousLightEntityPull(player.level(), player, initialVelocity.x, initialVelocity.y, initialVelocity.z);
            aqueousLightEntity.setDeltaMovement(initialVelocity);
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(aqueousLightEntity);
            scaleData.setScale(scale);
            Vec3 lightPosition = eyePosition.add(direction.scale(2.0));
            aqueousLightEntity.setPos(lightPosition);
            aqueousLightEntity.setOwner(player);
            player.level().addFreshEntity(aqueousLightEntity);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount % 20 == 0) {
            if (this.tickCount >= 100) {
                this.discard();
            }
        }
    }
}
