package net.swimmingtuna.lotm.entity;

import com.mojang.serialization.DataResult;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HurricaneOfLightEntity extends AbstractHurtingProjectile {

    private static final EntityDataAccessor<Boolean> DESTROY_ARMOR = SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_HURRICANE_RADIUS = SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HURRICANE_HEIGHT = SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LIFECOUNT = SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Vector3f> DATA_HURRICANE_MOV = SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Boolean> RANDOM_MOVEMENT = SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DESTROY_BLOCKS = SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> AGE = SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> BLOCK_DESTROY_INTERVAL = SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.INT);
    private static final int PARTICLE_UPDATE_INTERVAL = 2;
    private static final double PARTICLE_DENSITY_FACTOR = 0.75;

    public HurricaneOfLightEntity(EntityType<? extends HurricaneOfLightEntity> entityType, Level level) {
        super(entityType, level);
    }

    public HurricaneOfLightEntity(Level level, LivingEntity shooter, double offsetX, double offsetY, double offsetZ) {
        super(EntityInit.HURRICANE_OF_LIGHT_ENTITY.get(), shooter, offsetX, offsetY, offsetZ, level);
    }

    @Override
    public @NotNull ParticleOptions getTrailParticle() {
        return ParticleInit.NULL_PARTICLE.get();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DANGEROUS, false);
        this.entityData.define(DATA_LIFECOUNT, 300);
        this.entityData.define(DATA_HURRICANE_RADIUS, 4);
        this.entityData.define(DATA_HURRICANE_HEIGHT, 20);
        this.entityData.define(DATA_HURRICANE_MOV, new Vector3f());
        this.entityData.define(RANDOM_MOVEMENT, false);
        this.entityData.define(DESTROY_BLOCKS, false);
        this.entityData.define(DESTROY_ARMOR, false);
        this.entityData.define(AGE, false);
        this.entityData.define(BLOCK_DESTROY_INTERVAL, 10);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("HurricaneRandomMovement")) {
            this.setHurricaneRandom(compound.getBoolean("HurricaneRandomMovement"));
        }
        if (compound.contains("Age")) {
            this.setAge(compound.getBoolean("Age"));
        }
        if (compound.contains("BlockDestroyIntervial")) {
            this.setBlockDestroyInterval(compound.getInt("BlockDestroyIntervial"));
        }
        if (compound.contains("DestroyArmor")) {
            this.setDestroyArmor(compound.getBoolean("DestroyArmor"));
        }
        if (compound.contains("HurricanePickupBlocks")) {
            this.setHurricaneDestroy(compound.getBoolean("HurricanePickupBlocks"));
        }
        if (compound.contains("HurricaneRadius")) {
            this.setHurricaneRadius(compound.getInt("HurricaneRadius"));
        }
        if (compound.contains("HurricaneHeight")) {
            this.setHurricaneHeight(compound.getInt("HurricaneHeight"));
        }
        if (compound.contains("HurricaneLifeCount")) {
            this.setHurricaneLifecount(compound.getInt("HurricaneLifeCount"));
        }
        if (compound.contains("HurricaneMov")) {
            ExtraCodecs.VECTOR3F.decode(NbtOps.INSTANCE, compound.get("HurricaneMov")).result()
                    .ifPresent(HurricaneMovAndCompoundPair -> this.setHurricaneMov(HurricaneMovAndCompoundPair.getFirst()));
        }
    }

    public static void summonHurricaneOfLightDawn(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            int sequence = BeyonderUtil.getSequence(livingEntity);
            HurricaneOfLightEntity hurricaneOfLightEntity = new HurricaneOfLightEntity(livingEntity.level(), livingEntity, 0, 0, 0);
            hurricaneOfLightEntity.setHurricaneRadius((int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.SWORDOFDAWN.get()));
            hurricaneOfLightEntity.setHurricaneHeight((int) ((int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.SWORDOFDAWN.get()) * 0.5f));
            hurricaneOfLightEntity.setHurricaneLifecount(300 - (sequence * 20));
            hurricaneOfLightEntity.setHurricaneDestroy(true);
            hurricaneOfLightEntity.setHurricaneMov(livingEntity.getLookAngle().scale(0.5f).toVector3f());
            livingEntity.level().addFreshEntity(hurricaneOfLightEntity);
        }
    }

    public static void summonHurricaneOfLightDawnMob(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            int sequence = BeyonderUtil.getSequence(livingEntity);
            HurricaneOfLightEntity hurricaneOfLightEntity = new HurricaneOfLightEntity(livingEntity.level(), livingEntity, 0, 0, 0);
            hurricaneOfLightEntity.setHurricaneRadius((int) ((int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.SWORDOFDAWN.get()) * 0.6f));
            hurricaneOfLightEntity.setHurricaneHeight((int) ((int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.SWORDOFDAWN.get()) * 0.6f));
            hurricaneOfLightEntity.setHurricaneLifecount(300 - (sequence * 20));
            hurricaneOfLightEntity.setHurricaneDestroy(true);
            hurricaneOfLightEntity.setHurricaneMov(livingEntity.getLookAngle().scale(0.5f).toVector3f());
            livingEntity.level().addFreshEntity(hurricaneOfLightEntity);
        }
    }

    public static void summonHurricaneOfLightDeity(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            int sequence = BeyonderUtil.getSequence(livingEntity);
            HurricaneOfLightEntity hurricaneOfLightEntity = new HurricaneOfLightEntity(livingEntity.level(), livingEntity, 0, 0, 0);
            hurricaneOfLightEntity.setHurricaneRadius((int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TWILIGHTSWORD.get()));
            hurricaneOfLightEntity.setHurricaneHeight((int) ((int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TWILIGHTSWORD.get()) * 0.5f));
            hurricaneOfLightEntity.setHurricaneLifecount(500 - (sequence * 100));
            hurricaneOfLightEntity.setHurricaneDestroy(true);
            hurricaneOfLightEntity.setHurricaneMov(livingEntity.getLookAngle().scale(0.75f).toVector3f());
            hurricaneOfLightEntity.setDestroyArmor(true);
            hurricaneOfLightEntity.setBlockDestroyInterval(3);
            hurricaneOfLightEntity.setAge(true);
            livingEntity.level().addFreshEntity(hurricaneOfLightEntity);
        }
    }


    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("HurricaneRadius", this.getHurricaneRadius());
        compound.putInt("HurricaneLifeCount", this.getHurricaneLifecount());
        compound.putInt("HurricaneHeight", this.getHurricaneHeight());
        compound.putBoolean("DestroyArmor", this.getDestroyArmor());
        compound.putBoolean("Age", this.getAge());
        compound.putInt("BlockDestroyIntervial", this.getBlockDestroyInterval());
        DataResult<Tag> tagDataResult = ExtraCodecs.VECTOR3F.encodeStart(NbtOps.INSTANCE, this.getHurricaneMov());
        tagDataResult.result().ifPresent(tag -> compound.put("HurricaneMov", tag));
    }


    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide()) {
            BlockPos hitPos = result.getBlockPos();
            BlockPos HurricanePos = this.blockPosition();
            if (hitPos.getY() == HurricanePos.getY() - 1 && hitPos.getX() == HurricanePos.getX() && hitPos.getZ() == HurricanePos.getZ()) {
                this.setPos(this.getX(), this.getY() + 1, this.getZ());
            }
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    public boolean isDangerous() {
        return this.entityData.get(DATA_DANGEROUS);
    }

    public void tick() {
        super.tick();

        int hurricaneRadius = getHurricaneRadius();
        int hurricaneHeight = getHurricaneHeight();
        Vector3f HurricaneMov = this.getHurricaneMov();
        if (this.tickCount % 2 == 0) {
            this.setXRot(this.getXRot() + 2);
            this.setYRot(this.getYRot() + 2);
            this.setOldPosAndRot();
        }
        if (!this.level().isClientSide && getHurricaneDestroy() && this.tickCount % this.getBlockDestroyInterval() == 0) {
            destroyBlocksOptimized(hurricaneRadius, hurricaneHeight);
        }
        if (this.level().isClientSide && this.tickCount % PARTICLE_UPDATE_INTERVAL == 0) {
            spawnOptimizedParticles(hurricaneRadius, hurricaneHeight);
        }
        if (!this.level().isClientSide) {
            handleEntityCollisions(hurricaneRadius, hurricaneHeight);
        }
        updateMovementAndLifecycle(HurricaneMov);
    }

    private void destroyBlocksOptimized(int hurricaneRadius, int hurricaneHeight) {
        List<BlockPos> candidateBlocks = new ArrayList<>();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        Random random = new Random();
        int centerX = (int) this.getX();
        int centerZ = (int) this.getZ();
        for (int x = centerX - hurricaneRadius; x <= centerX + hurricaneRadius; x++) {
            for (int z = centerZ - hurricaneRadius; z <= centerZ + hurricaneRadius; z++) {
                double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2));
                if (distance <= hurricaneRadius) {
                    int surfaceY = findHighestSolidBlock(x, z);
                    if (surfaceY != -1) {
                        mutablePos.set(x, surfaceY, z);
                        if (!this.level().getBlockState(mutablePos).isAir() && !this.level().getBlockState(mutablePos).liquid() && this.level().getBlockState(mutablePos).getDestroySpeed(this.level(), mutablePos) >= 0) {
                            candidateBlocks.add(new BlockPos(x, surfaceY, z));
                        }
                    }
                }
            }
        }
        int blocksToDestroy = Math.min(100, candidateBlocks.size());
        for (int i = 0; i < blocksToDestroy; i++) {
            int randomIndex = random.nextInt(candidateBlocks.size());
            BlockPos selectedBlock = candidateBlocks.remove(randomIndex);
            this.level().removeBlock(selectedBlock, false);
        }
    }

    private int findHighestSolidBlock(int x, int z) {
        int surfaceY = this.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, x, z);
        for (int y = surfaceY; y >= surfaceY - 3; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!this.level().getBlockState(pos).isAir() && this.level().getBlockState(pos).isSolid()) {
                return y;
            }
        }
        for (int y = Math.min(200, (int) this.getY() + 50); y >= this.level().getMinBuildHeight(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = this.level().getBlockState(pos);
            if (!state.isAir() && state.isSolid()) {
                return y;
            }
        }

        return -1;
    }
    private void spawnOptimizedParticles(int hurricaneRadius, int hurricaneHeight) {
        double sizeFactor = (hurricaneRadius + 1) * (hurricaneHeight + 1) / 1000.0;
        int particleCount = Math.max(20, (int) (100 * sizeFactor * PARTICLE_DENSITY_FACTOR));
        double baseX = this.getX();
        double baseY = this.getY();
        double baseZ = this.getZ();
        double radius1 = (double) hurricaneRadius / 8;
        double riseSpeed = 0.2;
        double spinSpeed = 0.1;
        Random random = new Random();
        for (int i = 0; i < particleCount; i++) {
            double h = this.random.nextDouble() * hurricaneHeight;
            double radiusRatio = h / hurricaneHeight;
            double currentRadius = radius1 + (hurricaneRadius - radius1) * radiusRatio;
            double angle = this.random.nextDouble() * Math.PI * 2 + this.tickCount * spinSpeed;
            double offsetX = currentRadius * Math.cos(angle);
            double offsetZ = currentRadius * Math.sin(angle);
            ParticleOptions particle = switch (random.nextInt(3)) {
                case 0 -> ParticleInit.HURRICANE_OF_LIGHT_PARTICLE_1.get();
                case 1 -> ParticleInit.HURRICANE_OF_LIGHT_PARTICLE_2.get();
                default -> ParticleInit.HURRICANE_OF_LIGHT_PARTICLE_3.get();
            };
            this.level().addAlwaysVisibleParticle(particle, true, baseX + offsetX, baseY + h, baseZ + offsetZ, this.getDeltaMovement().x(), this.getDeltaMovement().y() + riseSpeed, this.getDeltaMovement().z());
        }
    }

    private void handleEntityCollisions(int hurricaneRadius, int hurricaneHeight) {
        AABB boundingBox = new AABB(
                this.getX() - hurricaneRadius,
                this.getY() - 10,
                this.getZ() - hurricaneRadius,
                this.getX() + hurricaneRadius,
                this.getY() + hurricaneHeight,
                this.getZ() + hurricaneRadius
        );
        List<Entity> entities = this.level().getEntities(this, boundingBox);
        if (entities.isEmpty()) return;
        double baseRadius = hurricaneRadius / 8.0;
        double radiusBuffer = hurricaneRadius * 0.2;
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity livingEntity) || entity == this.getOwner()) {
                continue;
            }
            processEntityCollision(livingEntity, hurricaneHeight, baseRadius, radiusBuffer);
        }
    }

    private void processEntityCollision(LivingEntity entity, int hurricaneHeight, double baseRadius, double radiusBuffer) {
        if (this.tickCount % 5 != 0) return;
        double entityRelativeHeight = Math.max(0, Math.min(1, (entity.getY() - this.getY()) / hurricaneHeight));
        double heightBasedBuffer = radiusBuffer * entityRelativeHeight;
        double effectiveRadius = (baseRadius + (getHurricaneRadius() - baseRadius) * entityRelativeHeight) + heightBasedBuffer;
        double dx = entity.getX() - this.getX();
        double dz = entity.getZ() - this.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance < effectiveRadius + (getHurricaneRadius() * 0.1)) {
            applyDamageAndEffects(entity, distance, effectiveRadius, entityRelativeHeight);
        }
    }

    private void updateMovementAndLifecycle(Vector3f HurricaneMov) {
        this.setDeltaMovement(new Vec3(HurricaneMov));
        this.hurtMarked = true;

        if (getHurricaneRandomness() && this.tickCount % 60 == 0) {
            float newHurricaneX = (float) (Math.random() * 2 - 1);
            float newHurricaneZ = (float) (Math.random() * 2 - 1);
            this.setHurricaneMov(new Vector3f(newHurricaneX, HurricaneMov.y, newHurricaneZ));
        }

        if (this.tickCount >= getHurricaneLifecount()) {
            this.discard();
        }
    }

    private void applyDamageAndEffects(LivingEntity livingEntity, double distance, double effectiveRadius, double entityRelativeHeight) {
        double distanceRatio = distance / effectiveRadius;
        float damageMultiplier = (0.5f + (float) entityRelativeHeight * 0.5f) * (distanceRatio > 1.0 ? 0.7f : 1.0f);
        float damage = ((float) getHurricaneRadius() * damageMultiplier / 2) / 6;
        if (livingEntity instanceof Mob) {
            damage += livingEntity.getMaxHealth() / 50.0f;
        }
        if (this.getOwner() != null && this.getOwner() instanceof LivingEntity owner) {
            int amplifier = 0;
            if (livingEntity.hasEffect(ModEffects.ARMOR_WEAKNESS.get())) {
                amplifier = livingEntity.getEffect(ModEffects.ARMOR_WEAKNESS.get()).getAmplifier();
            }
            livingEntity.hurt(BeyonderUtil.genericSource(owner, livingEntity), damage);

            if (this.tickCount % 15 == 0) {
                if (getDestroyArmor()) {
                    for (ItemStack armor : livingEntity.getArmorSlots()) {
                        if (!armor.isEmpty() && armor.getEquipmentSlot() != null) {
                            armor.hurtAndBreak(30, livingEntity, (player) -> player.broadcastBreakEvent(armor.getEquipmentSlot()));
                        }
                    }
                    livingEntity.addEffect(new MobEffectInstance(ModEffects.ARMOR_WEAKNESS.get(), 200, amplifier + 1, true, true));
                }

                if (BeyonderUtil.isPurifiable(livingEntity)) {
                    livingEntity.hurt(BeyonderUtil.magicSource(owner, livingEntity),damage);
                }
            }
        } else {
            int amplifier = 0;
            if (livingEntity.hasEffect(ModEffects.ARMOR_WEAKNESS.get())) {
                amplifier = livingEntity.getEffect(ModEffects.ARMOR_WEAKNESS.get()).getAmplifier();
            }
            if (this.getOwner() != null) {
                livingEntity.hurt(BeyonderUtil.genericSource(this.getOwner(), livingEntity), damage);
            } else {
                livingEntity.hurt(BeyonderUtil.genericSource(this, livingEntity), damage);
            }
            if (this.tickCount % 15 == 0) {
                if (this.getOwner() != null && this.getOwner() instanceof LivingEntity owner) {
                    if (!BeyonderUtil.areAllies(owner, livingEntity) && getAge()) {
                        livingEntity.getPersistentData().putUUID("ageUUID", owner.getUUID());
                        livingEntity.getPersistentData().putInt("age", livingEntity.getPersistentData().getInt("age") + 40);
                        if (livingEntity instanceof Player player) {
                            player.displayClientMessage(Component.literal("You are getting rapidly aged").withStyle(BeyonderUtil.ageStyle(livingEntity)).withStyle(ChatFormatting.BOLD),true);
                        }
                    }
                }
                livingEntity.addEffect(new MobEffectInstance(ModEffects.ARMOR_WEAKNESS.get(), 200, amplifier + 1, true, true));
            }
            if (BeyonderUtil.isPurifiable(livingEntity)) {
                if (this.getOwner() != null) {
                    livingEntity.hurt(BeyonderUtil.magicSource(this.getOwner(), livingEntity), damage);
                } else {
                    livingEntity.hurt(BeyonderUtil.magicSource(this, livingEntity), damage);
                }
            }
        }
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    public int getHurricaneRadius() {
        return this.entityData.get(DATA_HURRICANE_RADIUS);
    }

    public void setHurricaneRadius(int radius) {
        this.entityData.set(DATA_HURRICANE_RADIUS, radius);
    }

    public int getBlockDestroyInterval() {
        return this.entityData.get(BLOCK_DESTROY_INTERVAL);
    }

    public void setBlockDestroyInterval(int destroyInterval) {
        this.entityData.set(BLOCK_DESTROY_INTERVAL, destroyInterval);
    }

    public int getHurricaneHeight() {
        return this.entityData.get(DATA_HURRICANE_HEIGHT);
    }

    public void setHurricaneHeight(int height) {
        this.entityData.set(DATA_HURRICANE_HEIGHT, height);
    }

    public int getHurricaneLifecount() {
        return this.entityData.get(DATA_LIFECOUNT);
    }

    public void setHurricaneLifecount(int lifeCount) {
        this.entityData.set(DATA_LIFECOUNT, lifeCount);
    }

    public Vector3f getHurricaneMov() {
        return this.entityData.get(DATA_HURRICANE_MOV);
    }

    public void setHurricaneMov(Vector3f HurricaneMov) {
        this.entityData.set(DATA_HURRICANE_MOV, HurricaneMov);
    }

    public void setHurricaneRandom(boolean random) {
        this.entityData.set(RANDOM_MOVEMENT, random);
    }

    public boolean getHurricaneRandomness() {
        return this.entityData.get(RANDOM_MOVEMENT);
    }

    public boolean getHurricaneDestroy() {
        return this.entityData.get(DESTROY_BLOCKS);
    }

    public void setHurricaneDestroy(boolean destroy) {
        this.entityData.set(DESTROY_BLOCKS, destroy);
    }

    public boolean getAge() {
        return this.entityData.get(AGE);
    }

    public void setAge(boolean age) {
        this.entityData.set(AGE, age);
    }

    public boolean getDestroyArmor() {
        return this.entityData.get(DESTROY_ARMOR);
    }

    public void setDestroyArmor(boolean destroy) {
        this.entityData.set(DESTROY_ARMOR, destroy);
    }

}
