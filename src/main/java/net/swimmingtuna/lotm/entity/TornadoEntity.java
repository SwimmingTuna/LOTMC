package net.swimmingtuna.lotm.entity;

import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class TornadoEntity extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(TornadoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TORNADO_RADIUS = SynchedEntityData.defineId(TornadoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TORNADO_HEIGHT = SynchedEntityData.defineId(TornadoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LIFECOUNT = SynchedEntityData.defineId(TornadoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Vector3f> DATA_TORNADO_MOV = SynchedEntityData.defineId(TornadoEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Boolean> RANDOM_MOVEMENT = SynchedEntityData.defineId(TornadoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PICK_UP_BLOCKS = SynchedEntityData.defineId(TornadoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SUMMON_LIGHTNING = SynchedEntityData.defineId(TornadoEntity.class, EntityDataSerializers.BOOLEAN);

    public TornadoEntity(EntityType<? extends TornadoEntity> entityType, Level level) {
        super(entityType, level);
    }

    public TornadoEntity(Level level, LivingEntity shooter, double offsetX, double offsetY, double offsetZ) {
        super(EntityInit.TORNADO_ENTITY.get(), shooter, offsetX, offsetY, offsetZ, level);
    }

    @Override
    public @NotNull ParticleOptions getTrailParticle() {
        return ParticleInit.NULL_PARTICLE.get();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DANGEROUS, false);
        this.entityData.define(SUMMON_LIGHTNING, false);
        this.entityData.define(DATA_LIFECOUNT, 300);
        this.entityData.define(DATA_TORNADO_RADIUS, 4);
        this.entityData.define(DATA_TORNADO_HEIGHT, 20);
        this.entityData.define(DATA_TORNADO_MOV, new Vector3f());
        this.entityData.define(RANDOM_MOVEMENT, false);
        this.entityData.define(PICK_UP_BLOCKS, true);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("TornadoRandomMovement")) {
            this.setTornadoRandom(compound.getBoolean("TornadoRandomMovement"));
        }
        if (compound.contains("TornadoPickupBlocks")) {
            this.setTornadoPickup(compound.getBoolean("TornadoPickupBlocks"));
        }
        if (compound.contains("SummonLightning")) {
            this.setTornadoLightning(compound.getBoolean("SummonLightning"));
        }
        if (compound.contains("TornadoRadius")) {
            this.setTornadoRadius(compound.getInt("TornadoRadius"));
        }
        if (compound.contains("TornadoHeight")) {
            this.setTornadoHeight(compound.getInt("TornadoHeight"));
        }
        if (compound.contains("TornadoLifeCount")) {
            this.setTornadoLifecount(compound.getInt("TornadoLifeCount"));
        }
        if (compound.contains("TornadoMov")) {
            ExtraCodecs.VECTOR3F.decode(NbtOps.INSTANCE, compound.get("TornadoMov")).result()
                    .ifPresent(tornadoMovAndCompoundPair -> this.setTornadoMov(tornadoMovAndCompoundPair.getFirst()));
        }

    }

    public static void summonTornado(LivingEntity player) {
        if (!player.level().isClientSide()) {
            TornadoEntity tornado = new TornadoEntity(player.level(), player, 0, 0, 0);
            tornado.setTornadoHeight(50);
            tornado.setTornadoRadius(20);
            tornado.setTornadoLifecount(150);
            tornado.setTornadoMov(player.getLookAngle().scale(0.5f).toVector3f());
            tornado.setTornadoPickup(false);
            tornado.teleportTo(player.getX(), player.getY(), player.getZ());
            player.level().addFreshEntity(tornado);
        }
    }
    public static void summonTornadoRandom(LivingEntity player) {
        if (!player.level().isClientSide()) {
            int random = (int) ((Math.random() * 25) - 12);
            TornadoEntity tornado = new TornadoEntity(player.level(), player, random, random, random);
            tornado.setTornadoHeight(50);
            tornado.setTornadoRandom(true);
            tornado.setTornadoRadius(13);
            tornado.setTornadoLifecount((int) (Math.random() * 300));
            player.level().addFreshEntity(tornado);
        }
    }

    public static void summonCalamityTornado(LivingEntity player) {
        if (!player.level().isClientSide()) {
            int sequence = BeyonderUtil.getSequence(player);
            TornadoEntity tornado = new TornadoEntity(player.level(), player, 0, 0, 0);
            tornado.setTornadoHeight((int) (float) BeyonderUtil.getDamage(player).get(ItemInit.CALAMITY_INCARNATION_TORNADO.get()) / 4);
            tornado.setTornadoRadius((int) (float) BeyonderUtil.getDamage(player).get(ItemInit.CALAMITY_INCARNATION_TORNADO.get()) / 10);
            tornado.setTornadoLifecount(300);
            tornado.setTornadoPickup(true);
            player.level().addFreshEntity(tornado);
        }
    }

    public static void summonTyrantTornado(Player player) {
        if (!player.level().isClientSide()) {
            TornadoEntity tornado = new TornadoEntity(player.level(), player, 0, 0, 0);
            tornado.setTornadoHeight(200);
            tornado.setTornadoLightning(true);
            tornado.setTornadoRadius(75);
            tornado.setTornadoLifecount(400);
            tornado.setTornadoMov(player.getLookAngle().scale(0.5f).toVector3f());
            player.level().addFreshEntity(tornado);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("TornadoRadius", this.getTornadoRadius());
        compound.putInt("TornadoLifeCount", this.getTornadoLifecount());
        compound.putInt("TornadoHeight", this.getTornadoHeight());
        DataResult<Tag> tagDataResult = ExtraCodecs.VECTOR3F.encodeStart(NbtOps.INSTANCE, this.getTornadoMov());
        tagDataResult.result().ifPresent(tag -> compound.put("TornadoMov", tag));
    }


    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide()) {
            BlockPos hitPos = result.getBlockPos();
            BlockPos tornadoPos = this.blockPosition();
            if (hitPos.getY() == tornadoPos.getY() - 1 && hitPos.getX() == tornadoPos.getX() && hitPos.getZ() == tornadoPos.getZ()) {
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

    @Override
    public void tick() {
        super.tick();

        int tornadoRadius = getTornadoRadius();
        int tornadoHeight = getTornadoHeight();
        double minX = this.getX() - tornadoRadius;
        double minY = this.getY() - 10; // 10 blocks below the tornado
        double minZ = this.getZ() - tornadoRadius;
        double maxX = this.getX() + tornadoRadius;
        double maxY = this.getY() + tornadoHeight;
        double maxZ = this.getZ() + tornadoRadius;
        boolean pickup = getTornadoPickup();
        AABB boundingBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        Vector3f tornadoMov = this.getTornadoMov();
        this.setXRot(this.getXRot() + 2);
        this.setYRot(this.getYRot() + 2);
        this.setOldPosAndRot();

        if (this.level().isClientSide) {
            double sizeFactor = (tornadoRadius + 1) * (tornadoHeight + 1) / 1000.0;
            int particleCount = Math.max(20, (int) (100 * sizeFactor));
            double baseX = this.getX();
            double baseY = this.getY();
            double baseZ = this.getZ();
            double height = tornadoHeight;
            double radius1 = (double) tornadoRadius / 8;
            double radius2 = tornadoRadius;
            double riseSpeed = 0.2;
            for (int i = 0; i < particleCount; i++) {
                double h = this.random.nextDouble() * height;
                double radiusRatio = h / height;
                double currentRadius = radius1 + (radius2 - radius1) * radiusRatio;
                double angle = this.random.nextDouble() * Math.PI * 2;
                double offsetX = currentRadius * Math.cos(angle);
                double offsetZ = currentRadius * Math.sin(angle);
                double particleX = baseX + offsetX;
                double particleY = baseY + h;
                double particleZ = baseZ + offsetZ;
                double velocityY = riseSpeed;
                this.level().addAlwaysVisibleParticle(
                        ParticleTypes.CLOUD, true,
                        particleX, particleY, particleZ,
                        this.getDeltaMovement().x(), this.getDeltaMovement().y() + velocityY, this.getDeltaMovement().z()
                );
            }
        } else {
            Random random = new Random();
            List<Entity> entities = this.level().getEntities(this, boundingBox);
            entities.removeIf(Objects::isNull);

            for (Entity entity : entities) {
                if (entity == null || entity.isRemoved()) {
                    continue;
                }
                if (entity instanceof TornadoEntity || entity == this.getOwner() || !(entity instanceof LivingEntity)) {
                    continue;
                }

                double dx = entity.getX() - this.getX();
                double dz = entity.getZ() - this.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (this.tickCount % 10 == 0) {
                    if (!getTornadoLightning()) {
                        if (entity.getPersistentData().getInt("luckTornadoResistance") >= 1) {
                            if (this.getOwner() == null) {
                                entity.hurt(BeyonderUtil.genericSource(this, entity), 2);
                            } else {
                                entity.hurt(BeyonderUtil.genericSource(this.getOwner(), entity), 2);
                            }
                        } else if (entity.getPersistentData().getInt("luckTornadoImmunity") >= 1) {
                            continue;
                        } else {
                            if (this.getOwner() == null) {
                                entity.hurt(BeyonderUtil.genericSource(this, entity), (float) this.getTornadoHeight() / 4);
                            } else {
                                entity.hurt(BeyonderUtil.genericSource(this.getOwner(), entity), (float) this.getTornadoHeight() / 4);
                            }
                        }
                    } else if (getTornadoLightning() && getOwner() != null && getOwner() instanceof LivingEntity owner && entity instanceof LivingEntity living){
                        if (entity.getPersistentData().getInt("luckTornadoResistance") >= 1) {
                            BeyonderUtil.applyMentalDamage(owner, living, 5);
                        } else if (entity.getPersistentData().getInt("luckTornadoImmunity") >= 1) {
                            continue;
                        } else {
                            BeyonderUtil.applyMentalDamage(owner, living, 12);
                        }
                    }
                }
                if (distance < tornadoRadius) {
                    double angle = Math.atan2(dz, dx) + Math.PI / 2; // Change angle to push outward
                    double orbitRadius = tornadoRadius * 0.9; // Radius slightly less than the tornado radius
                    double newX = this.getX() + orbitRadius * Math.cos(angle);
                    double newZ = this.getZ() + orbitRadius * Math.sin(angle);

                    double heightRatio = (entity.getY() - this.getY()) / tornadoHeight;
                    double baseMotionY = 0.2 + (1 - heightRatio) * 0.4; // Stronger upward force at the bottom

                    double motionY = baseMotionY;
                    double horizontalForce = 0.3; // Increased for stronger outward push

                    // Apply the movement away from the center of the tornado
                    double outwardX = (newX - entity.getX()) * horizontalForce;
                    double outwardZ = (newZ - entity.getZ()) * horizontalForce;

                    // Normalize outward force to ensure it's not too strong
                    double outwardDistance = Math.sqrt(outwardX * outwardX + outwardZ * outwardZ);
                    if (outwardDistance > 0.1) { // Avoid division by zero and too small movements
                        outwardX /= outwardDistance;
                        outwardZ /= outwardDistance;
                    }
                    if (entity.getPersistentData().getInt("luckTornadoResistance") >= 1) {
                        entity.setDeltaMovement(outwardX/2,motionY/2, outwardZ/2);
                    } else if (entity.getPersistentData().getInt("luckTornadoImmunity") >= 1) {
                        continue;
                    } else
                        entity.setDeltaMovement(
                            outwardX,
                            motionY,
                            outwardZ
                    );
                }
                entity.hurtMarked = true;
            }
            if (pickup) {
                pickupBlocksOptimized(tornadoRadius, tornadoHeight);
            }

            this.setDeltaMovement(new Vec3(tornadoMov));
            this.hurtMarked = true;
            boolean randomMovement = getTornadoRandomness();
            if (randomMovement) {
                if (this.tickCount % 60 == 0) {
                    float newTornadoX = (float) (Math.random() * 2 - 1); // Random value between -1 and 1
                    float newTornadoZ = (float) (Math.random() * 2 - 1); // Random value between -1 and 1
                    this.setTornadoMov(new Vector3f(newTornadoX, tornadoMov.y, newTornadoZ));

                }
            }
            if (this.tickCount >= getTornadoLifecount()) {
                this.discard();
            }
            if (getTornadoLightning()) {
                LightningEntity lightningEntity = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), this.level());
                lightningEntity.setSpeed(20.0f);
                lightningEntity.setDamage(8);
                if (this.getOwner() != null && this.getOwner() instanceof LivingEntity owner) {
                    lightningEntity.setOwner(owner);
                }
                lightningEntity.setMentalDamage(lightningEntity.mentalDamageAmount());
                lightningEntity.setDeltaMovement((Math.random() * 0.4) - 0.2, -1, (Math.random() * 0.4) - 0.2);
                lightningEntity.setMaxLength(10);
                lightningEntity.setNoUp(true);
                double x1 = this.getX() + ((Math.random() * getTornadoRadius()) - (double) getTornadoRadius() / 2);
                double y1 = this.getY() + 200;
                double z1 = this.getZ() + ((Math.random() * getTornadoRadius()) - (double) getTornadoRadius() / 2);
                if (this.tickCount % 5 == 0) {
                lightningEntity.teleportTo(x1, y1, z1);
                this.level().addFreshEntity(lightningEntity);}
                for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(getTornadoRadius() * 1.5))) {
                    if (entity != this.getOwner() && this.tickCount % 40 == 0) {
                        BeyonderUtil.applyStun(entity, 10);
                    }
                }
            }
            if (this.getOwner() != null) {
                if (this.getOwner().getPersistentData().getInt("calamityIncarnationTornado") >= 1) {
                    this.teleportTo(this.getOwner().getX(),this.getOwner().getY() - 30,this.getOwner().getZ());
                }
            }
        }
    }

    private void pickupBlocksOptimized(int tornadoRadius, int tornadoHeight) {
        List<BlockPos> candidateBlocks = new ArrayList<>();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        Random random = new Random();
        int centerX = (int) this.getX();
        int centerZ = (int) this.getZ();
        for (int x = centerX - tornadoRadius; x <= centerX + tornadoRadius; x++) {
            for (int z = centerZ - tornadoRadius; z <= centerZ + tornadoRadius; z++) {
                double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2));
                if (distance <= tornadoRadius) {
                    int surfaceY = findHighestSolidBlock(x, z, tornadoHeight);
                    if (surfaceY != -1) {
                        mutablePos.set(x, surfaceY, z);
                        BlockState state = this.level().getBlockState(mutablePos);
                        if (!state.isAir() &&
                                !state.is(BlockTags.TALL_FLOWERS) &&
                                state.getBlock() != Blocks.BEDROCK &&
                                state.getDestroySpeed(this.level(), mutablePos) >= 0) {
                            candidateBlocks.add(new BlockPos(x, surfaceY, z));
                        }
                    }
                }
            }
        }

        int maxPickupPerTick = 5;
        int pickedUpBlocks = 0;
        for (BlockPos blockPos : candidateBlocks) {
            if (pickedUpBlocks >= maxPickupPerTick) break;
            if (random.nextInt(2) == 0 && this.tickCount % 5 == 0) {
                BlockState state = this.level().getBlockState(blockPos);
                FallingBlockEntity fallingBlock = FallingBlockEntity.fall(this.level(), blockPos, state);
                fallingBlock.time = 1;
                double dx = blockPos.getX() - this.getX();
                double dz = blockPos.getZ() - this.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                double normalizedX = distance > 0 ? dx / distance : 0;
                double normalizedZ = distance > 0 ? dz / distance : 0;

                double randomDirectionX = normalizedX * 0.5 + (random.nextDouble() - 0.5) * 1.5;
                double randomDirectionY = random.nextDouble() * 2.0 + 1.0; // Always upward
                double randomDirectionZ = normalizedZ * 0.5 + (random.nextDouble() - 0.5) * 1.5;

                fallingBlock.setDeltaMovement(randomDirectionX, randomDirectionY, randomDirectionZ);
                this.level().setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                this.level().addFreshEntity(fallingBlock);
                pickedUpBlocks++;
            }
        }
    }

    @SuppressWarnings("deprecation")
    private int findHighestSolidBlock(int x, int z, int tornadoHeight) {
        int maxY = Math.min(this.level().getMaxBuildHeight() - 1, (int) this.getY() + tornadoHeight);
        int minY = Math.max(this.level().getMinBuildHeight(), (int) this.getY() - 10);
        for (int y = (int) this.getY(); y >= minY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = this.level().getBlockState(pos);
            if (!state.isAir() && state.isSolid()) {
                return y;
            }
        }
        for (int y = (int) this.getY() + 1; y <= maxY; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = this.level().getBlockState(pos);
            if (!state.isAir() && state.isSolid()) {
                return y;
            }
        }
        return -1;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    public int getTornadoRadius() {
        return this.entityData.get(DATA_TORNADO_RADIUS);
    }

    public void setTornadoRadius(int radius) {
        this.entityData.set(DATA_TORNADO_RADIUS, radius);
    }

    public int getTornadoHeight() {
        return this.entityData.get(DATA_TORNADO_HEIGHT);
    }

    public void setTornadoHeight(int height) {
        this.entityData.set(DATA_TORNADO_HEIGHT, height);
    }

    public int getTornadoLifecount() {
        return this.entityData.get(DATA_LIFECOUNT);
    }

    public void setTornadoLifecount(int lifeCount) {
        this.entityData.set(DATA_LIFECOUNT, lifeCount);
    }

    public Vector3f getTornadoMov() {
        return this.entityData.get(DATA_TORNADO_MOV);
    }

    public void setTornadoMov(Vector3f tornadoMov) {
        this.entityData.set(DATA_TORNADO_MOV, tornadoMov);
    }

    public void setTornadoRandom(boolean random) {
        this.entityData.set(RANDOM_MOVEMENT, random);
    }

    public boolean getTornadoRandomness() {
        return this.entityData.get(RANDOM_MOVEMENT);
    }

    public boolean getTornadoPickup() {
        return this.entityData.get(PICK_UP_BLOCKS);
    }

    public void setTornadoPickup(boolean pickup) {
        this.entityData.set(PICK_UP_BLOCKS, pickup);
    }

    public boolean getTornadoLightning() {
        return this.entityData.get(SUMMON_LIGHTNING);
    }

    public void setTornadoLightning(boolean summonLightning) {
        this.entityData.set(SUMMON_LIGHTNING, summonLightning);
    }
}
