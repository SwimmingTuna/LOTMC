package net.swimmingtuna.lotm.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.SMath;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WindBladeEntity extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(WindBladeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_LIFE_COUNT = SynchedEntityData.defineId(WindBladeEntity.class, EntityDataSerializers.INT);
    private static final List<Block> EXCLUDED_BLOCKS = List.of(Blocks.BEDROCK, Blocks.OBSIDIAN);

    // Track entities that have already been hit to avoid multiple hits
    private final Set<Integer> hitEntities = new HashSet<>();

    public WindBladeEntity(EntityType<? extends WindBladeEntity> entityType, Level level) {
        super(entityType, level);
    }

    public WindBladeEntity(Level level, LivingEntity shooter, double offsetX, double offsetY, double offsetZ) {
        super(EntityInit.WIND_BLADE_ENTITY.get(), shooter, offsetX, offsetY, offsetZ, level);
    }

    public static void summonEntityWithSpeed(Vec3 direction, Vec3 initialVelocity, Vec3 eyePosition, double x, double y, double z, Player player, float yRotation, float xRotation) {
        if (player.level().isClientSide()) {
            return;
        }
        WindBladeEntity windBladeEntity = new WindBladeEntity(player.level(), player, initialVelocity.x, initialVelocity.y, initialVelocity.z);
        windBladeEntity.getDeltaMovement().add(initialVelocity);
        windBladeEntity.hurtMarked = true;
        Vec3 lightPosition = eyePosition.add(direction.scale(3.0));
        windBladeEntity.setPos(lightPosition);
        windBladeEntity.setOwner(player);
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        if (!windBladeEntity.level().isClientSide()) {
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(windBladeEntity);
            scaleData.setTargetScale((7 - (holder.getSequence())));
            scaleData.markForSync(true);
        }
        player.level().addFreshEntity(windBladeEntity);
    }

    public static void testShoot(LivingEntity player) {
        if (player.level().isClientSide()) {
            return;
        }
        Vec3 direction = player.getViewVector(1.0f);
        Vec3 velocity = direction.scale(3.0);
        Vec3 lookVec = player.getLookAngle();
        WindBladeEntity windBladeEntity = new WindBladeEntity(player.level(), player, velocity.x(), velocity.y(), velocity.z());
        windBladeEntity.shoot(lookVec.x, lookVec.y, lookVec.z, 2.0f, 0.1f);
        windBladeEntity.hurtMarked = true;
        windBladeEntity.setXRot((float) direction.x);
        windBladeEntity.setYRot((float) direction.y);
        int x = (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.WIND_MANIPULATION_BLADE.get());
        if (!windBladeEntity.level().isClientSide()) {
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(windBladeEntity);
            scaleData.setScale(x);
            scaleData.markForSync(true);
        }
        player.level().addFreshEntity(windBladeEntity);
    }

    @Override
    protected float getInertia() {
        return 1.0F;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    // Handle entity hit logic
    private void handleEntityHit(LivingEntity entity) {
        if (this.level().isClientSide() || !(this.getOwner() instanceof Player player)) {
            return;
        }

        // Skip if the entity has already been hit by this wind blade
        if (hitEntities.contains(entity.getId())) {
            return;
        }

        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        int currentLifeCount = this.entityData.get(DATA_LIFE_COUNT);
        int decrease = (holder.getSequence() * 9) + 30;
        currentLifeCount = currentLifeCount - decrease;
        if (this.getOwner() == null) {
            entity.hurt(BeyonderUtil.genericSource(this, entity), (float) currentLifeCount / 10);
        } else {
            entity.hurt(BeyonderUtil.genericSource(this.getOwner(), entity), (float) currentLifeCount / 10);
        }
        this.entityData.set(DATA_LIFE_COUNT, currentLifeCount);

        // Mark this entity as hit
        hitEntities.add(entity.getId());

        if (currentLifeCount <= 0) {
            this.discard();
        }
    }

    // Handle block hit logic
    private void handleBlockHit(Block block) {
        if (this.level().isClientSide) {
            return;
        }

        if (!EXCLUDED_BLOCKS.contains(block)) {
            int currentLifeCount = this.entityData.get(DATA_LIFE_COUNT);
            int decrease = (BeyonderUtil.getSequence((LivingEntity) this.getOwner()) * 4) + 10;
            currentLifeCount = currentLifeCount - decrease;
            this.entityData.set(DATA_LIFE_COUNT, currentLifeCount);

            if (currentLifeCount <= 0) {
                this.discard();
            }
        }
    }

    // Keep these methods for compatibility but don't do anything in them
    @Override
    protected void onHitEntity(EntityHitResult result) {
        // Hit detection is now handled in tick()
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        // Hit detection is now handled in tick()
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
        this.entityData.define(DATA_DANGEROUS, true);
        this.entityData.define(DATA_LIFE_COUNT, 400);
    }

    public boolean isDangerous() {
        return this.entityData.get(DATA_DANGEROUS);
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 vector3d = (new Vec3(x, y, z)).normalize().add(this.random.nextGaussian() * (double) 0.0075F * (double) inaccuracy, this.random.nextGaussian() * (double) 0.0075F * (double) inaccuracy, this.random.nextGaussian() * (double) 0.0075F * (double) inaccuracy).scale(velocity);
        this.setDeltaMovement(vector3d);
        float f = (float) Math.sqrt(SMath.getHorizontalDistanceSqr(vector3d));
        this.setYRot((float) (Mth.atan2(vector3d.x, vector3d.z) * (double) (180F / (float) Math.PI)));
        this.setXRot((float) (Mth.atan2(vector3d.y, f) * (double) (180F / (float) Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    public void tick() {
        super.tick();

        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();

        // Check for lifetime expiration
        if (this.tickCount % 20 == 0) {
            if (this.tickCount >= 240) {
                this.discard();
                return;
            }
        }

        // Skip client-side processing
        if (this.level().isClientSide()) {
            return;
        }
        if (this.getOwner() == null) {
            return;
        }
        float scale = BeyonderUtil.getScale(this);
        AABB expandedBox = this.getBoundingBox().inflate(scale * 2);
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, expandedBox, entity -> entity != this.getOwner() && entity.isAlive());
        for (LivingEntity entity : nearbyEntities) {
            handleEntityHit(entity);
        }
        int minX = Mth.floor(expandedBox.minX);
        int minY = Mth.floor(expandedBox.minY);
        int minZ = Mth.floor(expandedBox.minZ);
        int maxX = Mth.ceil(expandedBox.maxX);
        int maxY = Mth.ceil(expandedBox.maxY);
        int maxZ = Mth.ceil(expandedBox.maxZ);
        boolean hitBlock = false;
        for (int x = minX; x <= maxX && !hitBlock; x++) {
            for (int y = minY; y <= maxY && !hitBlock; y++) {
                for (int z = minZ; z <= maxZ && !hitBlock; z++) {
                    Block block = this.level().getBlockState(new net.minecraft.core.BlockPos(x, y, z)).getBlock();
                    if (block != Blocks.AIR && block != Blocks.CAVE_AIR && block != Blocks.VOID_AIR) {
                        handleBlockHit(block);
                        hitBlock = true;
                    }
                }
            }
        }
    }
}