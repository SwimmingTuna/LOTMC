package net.swimmingtuna.lotm.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.LightningEntityPacketS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class LightningEntity extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Integer> DAMAGE = SynchedEntityData.defineId(LightningEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MENTAL_DAMAGE = SynchedEntityData.defineId(LightningEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_LENGTH = SynchedEntityData.defineId(LightningEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(LightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> FALL_DOWN = SynchedEntityData.defineId(LightningEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BRANCH_OUT = SynchedEntityData.defineId(LightningEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> NO_UP = SynchedEntityData.defineId(LightningEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STAR = SynchedEntityData.defineId(LightningEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SYNCHED_MOVEMENT = SynchedEntityData.defineId(LightningEntity.class, EntityDataSerializers.BOOLEAN);

    private int interpolationSteps = 10;
    private int currentStep = 0;
    private List<Vec3> positions = new ArrayList<>();
    private List<AABB> boundingBoxes = new ArrayList<>();
    private Random random = new Random();
    private Vec3 startPos;
    private LivingEntity owner;
    private Entity targetEntity;
    private Vec3 targetPos;
    private Vec3 lastPos; // New field for last position
    private boolean shouldDiscard = false; // Flag to track if entity should be discarded

    public LightningEntity(EntityType<? extends LightningEntity> entityType, Level level) {
        super(entityType, level);
    }

    public LightningEntity(EntityType<? extends LightningEntity> entityType, double x, double y, double z, double dX, double dY, double dZ, Level level) {
        super(entityType, x, y, z, dX, dY, dZ, level);
        this.startPos = new Vec3(x, y, z);
    }

    public LightningEntity(EntityType<? extends LightningEntity> entityType, LivingEntity shooter, double dX, double dY, double dZ, Level level) {
        super(entityType, shooter, dX, dY, dZ, level);
        this.startPos = shooter.position();
        this.owner = shooter;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return pDistance < 8000000; // 128 blocks squared
    }

    @Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }

    @Override
    public @NotNull AABB getBoundingBoxForCulling() {
        return new AABB(
                this.getX() - 3000,
                this.getY() - 3000,
                this.getZ() - 3000,
                this.getX() + 3000,
                this.getY() + 3000,
                this.getZ() + 3000
        );
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MAX_LENGTH, 100);
        this.entityData.define(DAMAGE, 30);
        this.entityData.define(MENTAL_DAMAGE, 0);
        this.entityData.define(SPEED, 1.0f);
        this.entityData.define(FALL_DOWN, false);
        this.entityData.define(BRANCH_OUT, false);
        this.entityData.define(NO_UP, false);
        this.entityData.define(STAR, false);
        this.entityData.define(SYNCHED_MOVEMENT, false);
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("MaxLength")) {
            this.setMaxLength(compound.getInt("MaxLength"));
        }
        if (compound.contains("Damage")) {
            this.setDamage(compound.getInt("Damage"));
        }
        if (compound.contains("MentalDamage")) {
            this.setMentalDamage(compound.getInt("MentalDamage"));
        }
        if (compound.contains("NoUp")) {
            this.setNoUp(compound.getBoolean("NoUp"));
        }
        if (compound.contains("Star")) {
            this.setStar(compound.getBoolean("Star"));
        }
        if (compound.contains("SynchedMovement")) {
            this.setSynchedMovement(compound.getBoolean("SynchedMovement"));
        }
        if (compound.contains("fallDown")) {
            this.setFallDown(compound.getBoolean("fallDown"));
        }
        if (compound.contains("Speed")) {
            this.setSpeed(compound.getFloat("Speed"));
        }
        if (compound.contains("BranchOut")) {
            this.setBranchOut(compound.getBoolean("BranchOut"));
        }
        if (compound.contains("Positions")) {
            ListTag posList = compound.getList("Positions", 10);
            this.positions.clear();
            for (int i = 0; i < posList.size(); i++) {
                CompoundTag posTag = posList.getCompound(i);
                this.positions.add(new Vec3(posTag.getDouble("X"), posTag.getDouble("Y"), posTag.getDouble("Z")));
            }
        }
        if (compound.contains("StartPos")) {
            CompoundTag startPosTag = compound.getCompound("StartPos");
            this.startPos = new Vec3(
                    startPosTag.getDouble("sX"),
                    startPosTag.getDouble("sY"),
                    startPosTag.getDouble("sZ")
            );
        }
        if (compound.contains("OwnerUUID")) {
            UUID ownerUUID = compound.getUUID("OwnerUUID");
            Level level = this.level();
            if (ownerUUID != null && level != null) {
                this.owner = (LivingEntity) ((ServerLevel) level).getEntity(ownerUUID);
            }
        }
        if (compound.contains("TargetEntity")) {
            UUID targetUUID = compound.getUUID("TargetUUID");
            Level level = this.level();
            if (targetUUID != null && level != null) {
                this.targetEntity = ((ServerLevel) level).getEntity(targetUUID);
            }
        }
        if (compound.contains("TargetPos")) {
            CompoundTag targetPosTag = compound.getCompound("TargetPos");
            this.targetPos = new Vec3(
                    targetPosTag.getDouble("tX"),
                    targetPosTag.getDouble("tY"),
                    targetPosTag.getDouble("tZ")
            );
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("MaxLength", this.getMaxLength());
        compound.putInt("Damage", this.getDamage());
        compound.putInt("MentalDamage", this.getMentalDamage());
        compound.putFloat("Speed", this.getSpeed());
        compound.putBoolean("NoUp", this.getNoUp());
        compound.putBoolean("Star", this.getStar());
        ListTag posList = new ListTag();
        for (Vec3 pos : this.positions) {
            CompoundTag posTag = new CompoundTag();
            posTag.putDouble("X", pos.x);
            posTag.putDouble("Y", pos.y);
            posTag.putDouble("Z", pos.z);
            posList.add(posTag);
        }
        compound.put("Positions", posList);
        if (startPos != null) {
            CompoundTag startPosTag = new CompoundTag();
            startPosTag.putDouble("sX", startPos.x);
            startPosTag.putDouble("sY", startPos.y);
            startPosTag.putDouble("sZ", startPos.z);
            compound.put("StartPos", startPosTag);
        }
        if (this.owner != null) {
            compound.putUUID("OwnerUUID", this.owner.getUUID());
        }
        if (this.targetEntity != null) {
            compound.putUUID("TargetUUID", this.targetEntity.getUUID());
        }
        if (targetPos != null) {
            CompoundTag targetPosTag = new CompoundTag();
            targetPosTag.putDouble("tX", targetPos.x);
            targetPosTag.putDouble("tY", targetPos.y);
            targetPosTag.putDouble("tZ", targetPos.z);
            compound.put("TargetPos", targetPosTag);
        }
    }

    @Override
    public void tick() {
        if (this.shouldDiscard) {
            super.tick();
            this.discard();
            return;
        }

        try {
            super.tick();
            MinecraftServer server = this.getServer();
            if (server != null) {
                List<ServerPlayer> players = server.getPlayerList().getPlayers();
                for (Player player : players) {
                    if (player.distanceTo(this) < 300 && player instanceof ServerPlayer serverPlayer) {
                        LOTMNetworkHandler.sendToPlayer(new LightningEntityPacketS2C(this), serverPlayer);
                    }
                }
            }
            float speed = this.getSpeed();

            if (startPos == null) {
                startPos = this.position();
            }

            if (this.positions.isEmpty()) {
                this.positions.add(startPos);
            }

            this.lastPos = this.positions.get(this.positions.size() - 1);
            Vec3 targetVector = null;

            if (this.targetEntity != null) {
                targetVector = this.targetEntity.position().subtract(lastPos).normalize();
            } else if (this.targetPos != null) {
                targetVector = this.targetPos.subtract(lastPos).normalize();
            }

            Vec3 newPos;
            if (targetVector != null) {
                newPos = lastPos.add(
                        targetVector.x * speed + random.nextGaussian() * 0.1 * speed,
                        targetVector.y * speed + random.nextGaussian() * 0.1 * speed,
                        targetVector.z * speed + random.nextGaussian() * 0.1 * speed
                );
            } else {
                Vec3 movement = this.getDeltaMovement().scale(speed);
                if (this.getFallDown()) {
                    movement = movement.add(0, -0.5, 0);
                }
                newPos = lastPos.add(movement.add(new Vec3(
                        random.nextGaussian() * 0.1 * speed,
                        random.nextGaussian() * 0.1 * speed,
                        random.nextGaussian() * 0.1 * speed
                )));
            }
            if (targetPos != null) {
                if (lastPos.distanceToSqr(targetPos) < 1) {
                    targetPos = null;
                }
            }

            if (this.positions.size() < this.getMaxLength()) {
                this.positions.add(newPos);
            }

            if (!this.level().isClientSide() && this.tickCount >= 2) {
                float detectionRadius = Math.min(18, getDamage() * 0.05f);

                // Validate detection radius to prevent invalid bounding boxes
                if (detectionRadius <= 0) {
                    detectionRadius = 0.1f;  // Minimum safe radius
                }

                double minX = lastPos.x - detectionRadius;
                double minY = lastPos.y - detectionRadius;
                double minZ = lastPos.z - detectionRadius;
                double maxX = lastPos.x + detectionRadius;
                double maxY = lastPos.y + detectionRadius;
                double maxZ = lastPos.z + detectionRadius;

                // Extra check to ensure min/max values are correct
                if (minX > maxX) {
                    double temp = minX;
                    minX = maxX;
                    maxX = temp;
                }
                if (minY > maxY) {
                    double temp = minY;
                    minY = maxY;
                    maxY = temp;
                }
                if (minZ > maxZ) {
                    double temp = minZ;
                    minZ = maxZ;
                    maxZ = temp;
                }

                // Check for extreme values that might cause overflow
                if (minX < -30000000 || maxX > 30000000 || minY < -30000000 || maxY > 30000000 ||
                        minZ < -30000000 || maxZ > 30000000) {
                    LOTM.LOGGER.warn("Lightning entity with extreme coordinates detected, discarding");
                    this.shouldDiscard = true;
                    return;
                }

                AABB detectionBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
                List<Entity> nearbyEntities = new ArrayList<>(this.level().getEntitiesOfClass(Entity.class, detectionBox));
                boolean foundValidTarget = false;

                for (Entity entity : nearbyEntities) {
                    if (entity instanceof LivingEntity && entity != this.owner) {
                        foundValidTarget = true;
                        try {
                            explodeLightningBlock(BlockPos.containing(lastPos), getDamage() * 0.25);
                        } catch (IllegalArgumentException e) {
                            LOTM.LOGGER.error("Error in lightning explosion, discarding entity: " + e.getMessage());
                        }
                        this.shouldDiscard = true;
                        break;
                    }
                }

                if (foundValidTarget) {
                    return;
                }
            }

            boolean hasExploded = false;
            if (this.tickCount >= 2) {
                for (int i = 0; i < this.positions.size() - 1 && !hasExploded; i++) {
                    Vec3 pos1 = this.positions.get(i);
                    Vec3 pos2 = this.positions.get(i + 1);
                    double distance = pos1.distanceTo(pos2);
                    Vec3 direction = pos2.subtract(pos1).normalize();
                    for (double d = 0; d < distance && !hasExploded; d += 3.0) {
                        Vec3 currentPos = pos1.add(direction.scale(d));

                        // Check for extreme values
                        if (Double.isNaN(currentPos.x) || Double.isNaN(currentPos.y) || Double.isNaN(currentPos.z) ||
                                Double.isInfinite(currentPos.x) || Double.isInfinite(currentPos.y) || Double.isInfinite(currentPos.z) ||
                                Math.abs(currentPos.x) > 30000000 || Math.abs(currentPos.y) > 30000000 || Math.abs(currentPos.z) > 30000000) {
                            LOTM.LOGGER.warn("Lightning entity with invalid position detected, discarding");
                            this.shouldDiscard = true;
                            return;
                        }

                        AABB checkArea = createBoundingBox(currentPos);

                        // Verify the bounding box is valid
                        if (checkArea.minX > checkArea.maxX || checkArea.minY > checkArea.maxY || checkArea.minZ > checkArea.maxZ) {
                            LOTM.LOGGER.warn("Lightning entity with invalid bounding box, discarding");
                            this.shouldDiscard = true;
                            return;
                        }

                        if (!this.level().isClientSide()) {
                            try {
                                for (BlockPos blockPos : BlockPos.betweenClosed(
                                        new BlockPos((int) checkArea.minX, (int) checkArea.minY, (int) checkArea.minZ),
                                        new BlockPos((int) checkArea.maxX, (int) checkArea.maxY, (int) checkArea.maxZ))) {
                                    if (!this.level().getBlockState(blockPos).isAir() && !this.level().getBlockState(blockPos).getBlock().equals(Blocks.WATER)) {
                                        Vec3 hitPos = currentPos;
                                        try {
                                            explodeLightningBlock(BlockPos.containing(hitPos), getDamage() * 0.25);
                                        } catch (IllegalArgumentException e) {
                                            LOTM.LOGGER.error("Error in lightning explosion, discarding entity: " + e.getMessage());
                                        }
                                        hasExploded = true;
                                        this.shouldDiscard = true;  // Mark for discard instead of immediate discard
                                        break;
                                    }
                                }
                            } catch (IllegalArgumentException e) {
                                LOTM.LOGGER.error("Error in block iteration, discarding entity: " + e.getMessage());
                                this.shouldDiscard = true;
                                return;
                            }
                        }
                        double offsetX = random.nextGaussian() * 1;
                        double offsetY = random.nextGaussian() * 1;
                        double offsetZ = random.nextGaussian() * 1;
                        if (level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, checkArea.minX + offsetX, checkArea.minY + offsetY, checkArea.minZ + offsetZ, 0, 0, 0, 0, 0);
                            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, checkArea.minX + offsetX, checkArea.minY + offsetY, checkArea.minZ + offsetZ, 0, 0, 0, 0, 0);
                        }
                    }
                    if (hasExploded) break;
                }
            }

            this.setPos(startPos.x, startPos.y, startPos.z);

            try {
                this.setBoundingBox(createBoundingBox(newPos));
            } catch (IllegalArgumentException e) {
                LOTM.LOGGER.error("Error setting bounding box, discarding entity: " + e.getMessage());
                this.shouldDiscard = true;
                return;
            }

            if (this.tickCount > this.getMaxLength()) {
                this.shouldDiscard = true;  // Mark for discard instead of immediate discard
                return;
            }

            boolean branchOut = getBranchOut();
            boolean noUp = getNoUp();
            boolean synchedMovement = getSynchedMovement();
            if (!this.level().isClientSide()) {
                if (synchedMovement) {
                    if (this.tickCount >= 2) {
                        this.setDeltaMovement(this.getPersistentData().getDouble("lightningBranchDMX"), this.getPersistentData().getDouble("lightningBranchDMY"), this.getPersistentData().getDouble("lightningBranchDMZ"));
                    }
                }
                if (branchOut) {
                    if (this.tickCount == getMaxLength()) {
                        Vec3 pos = new Vec3(lastPos.x, lastPos.y, lastPos.z);
                        try {
                            this.explodeLightningBlock(BlockPos.containing(pos), getDamage() * 0.25);
                        } catch (IllegalArgumentException e) {
                            LOTM.LOGGER.error("Error in lightning explosion during branch out, discarding: " + e.getMessage());
                        }
                    }

                    // Batch creation of lightning entities after iteration
                    if (!this.shouldDiscard) {
                        LightningEntity lightningEntity = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), this.level());
                        lightningEntity.setSpeed(8.0f);
                        lightningEntity.setDeltaMovement(this.getPersistentData().getDouble("sailorLightningDMX") + (Math.random() * 0.5) - 0.25, this.getPersistentData().getDouble("sailorLightningDMY") + (Math.random() * 0.5) - 0.25, this.getPersistentData().getDouble("sailorLightningDMZ") + (Math.random() * 0.5) - 0.25);
                        lightningEntity.setMaxLength(100);
                        lightningEntity.setDamage(8);
                        lightningEntity.setDamage(mentalDamageAmount());
                        lightningEntity.teleportTo(lastPos.x(), lastPos.y(), lastPos.z());
                        lightningEntity.setSynchedMovement(true);

                        lightningEntity.getPersistentData().putDouble("lightningBranchDMY", this.getPersistentData().getDouble("sailorLightningDMY") + (Math.random() * 0.8) - 0.4);
                        lightningEntity.getPersistentData().putDouble("lightningBranchDMX", this.getPersistentData().getDouble("sailorLightningDMX") + (Math.random() * 0.8) - 0.4);
                        lightningEntity.getPersistentData().putDouble("lightningBranchDMZ", this.getPersistentData().getDouble("sailorLightningDMZ") + (Math.random() * 0.8) - 0.4);
                        this.level().addFreshEntity(lightningEntity);
                    }

                    if (this.tickCount == 1) {
                        this.getPersistentData().putDouble("sailorLightningDMY", this.getDeltaMovement().y());
                        this.getPersistentData().putDouble("sailorLightningDMX", this.getDeltaMovement().x());
                        this.getPersistentData().putDouble("sailorLightningDMZ", this.getDeltaMovement().z());
                    }
                    this.setDeltaMovement(this.getPersistentData().getDouble("sailorLightningDMX"), this.getPersistentData().getDouble("sailorLightningDMY"), this.getPersistentData().getDouble("sailorLightningDMZ"));
                }
                if (noUp) {
                    if (this.getDeltaMovement().y() >= -0.5f) {
                        this.shouldDiscard = true;  // Mark for discard instead of immediate discard

                        // Create lightning entity in the next tick to avoid concurrent modification
                        LightningEntity lightningEntity = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), this.level());
                        lightningEntity.setSpeed(5.0f);
                        lightningEntity.setDeltaMovement((Math.random() * 0.6) - 0.3, -2, (Math.random() * 0.6) - 0.3);
                        lightningEntity.setMaxLength(100);
                        lightningEntity.teleportTo(lastPos.x(), lastPos.y(), lastPos.z());
                        lightningEntity.setBranchOut(true);
                        this.level().addFreshEntity(lightningEntity);
                    }
                }
            }

            if (!this.level().isClientSide() && owner != null) {
                CompoundTag tag = owner.getPersistentData();
                if (tag.getInt("sailorLightningTravel") >= 1 && lastPos != null) {
                    owner.teleportTo(lastPos.x(), lastPos.y, lastPos.z);
                    owner.getPersistentData().putInt("sailorLightningTravel", 10);
                    Vec3 lookVec = owner.getLookAngle();
                    this.setDeltaMovement(lookVec.x, lookVec.y, lookVec.z);
                }
            }
        } catch (IllegalArgumentException e) {
            // If we catch the specific exception from the crash report, log and discard
            if (e.getMessage() != null && e.getMessage().contains("Start element") && e.getMessage().contains("is larger than end element")) {
                LOTM.LOGGER.error("Caught bounding box error in lightning entity, discarding: " + e.getMessage());
                this.discard();
                return;
            } else {
                throw e;
            }
        } catch (Exception e) {
            // Catch any other exceptions that might occur
            LOTM.LOGGER.error("Unexpected error in lightning entity tick, discarding: " + e.getMessage());
            this.discard();
            return;
        }
    }

    public void explodeLightningBlock(BlockPos hitPos, double radius) {
        // Add safety checks for extreme values
        if (hitPos == null ||
                hitPos.getX() < -30000000 || hitPos.getX() > 30000000 ||
                hitPos.getY() < -30000000 || hitPos.getY() > 30000000 ||
                hitPos.getZ() < -30000000 || hitPos.getZ() > 30000000 ||
                Double.isNaN(radius) || radius <= 0 || radius > 200) {

            LOTM.LOGGER.warn("Lightning entity at invalid position or radius: " + hitPos + ", " + radius);
            this.discard();
            return;
        }

        try {
            // Calculate safe bounds for BlockPos.betweenClosed
            BlockPos minPos = hitPos.offset((int) -radius, (int) -radius, (int) -radius);
            BlockPos maxPos = hitPos.offset((int) radius, (int) radius, (int) radius);

            // Safety check for block iteration
            if (minPos.getX() > maxPos.getX() || minPos.getY() > maxPos.getY() || minPos.getZ() > maxPos.getZ()) {
                LOTM.LOGGER.warn("Invalid block range in lightning explosion, discarding");
                this.discard();
                return;
            }
            if (!this.getStar()) {
                if (this.getOwner() != null && BeyonderUtil.getSequence(getOwner()) >= 5 && BeyonderUtil.currentPathwayMatchesNoException(this.getOwner(), BeyonderClassInit.SAILOR.get())) {
                    radius *= 0.6f;
                }
                BeyonderUtil.destroyBlocksInSphere(this, hitPos, radius * 0.7f, 0);
            } else {
                BeyonderUtil.destroyBlocksInSphere(this, hitPos, radius * 0.35f, 0);
            }

            // Create explosion AABB
            double newRadius = radius * 0.7;
            double minX = hitPos.getX() - newRadius;
            double minY = hitPos.getY() - newRadius;
            double minZ = hitPos.getZ() - newRadius;
            double maxX = hitPos.getX() + newRadius;
            double maxY = hitPos.getY() + newRadius;
            double maxZ = hitPos.getZ() + newRadius;

            // Ensure min values are less than max values
            if (minX > maxX) {
                double temp = minX;
                minX = maxX;
                maxX = temp;
            }
            if (minY > maxY) {
                double temp = minY;
                minY = maxY;
                maxY = temp;
            }
            if (minZ > maxZ) {
                double temp = minZ;
                minZ = maxZ;
                maxZ = temp;
            }

            if (Double.isNaN(minX) || Double.isNaN(minY) || Double.isNaN(minZ) ||
                    Double.isNaN(maxX) || Double.isNaN(maxY) || Double.isNaN(maxZ) ||
                    Double.isInfinite(minX) || Double.isInfinite(minY) || Double.isInfinite(minZ) ||
                    Double.isInfinite(maxX) || Double.isInfinite(maxY) || Double.isInfinite(maxZ)) {
                LOTM.LOGGER.warn("Invalid coordinates for explosion AABB, discarding entity");
                this.discard();
                return;
            }

            AABB explosionArea = new AABB(minX, minY, minZ, maxX, maxY, maxZ);

            List<Entity> entities = new ArrayList<>(this.level().getEntities(this, explosionArea));
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity livingEntity) {
                    float damage = (float) Math.max((double) getDamage() / 5, (getDamage() - (entity.distanceToSqr(hitPos.getCenter()))) * 1.5);
                    if (getOwner() != null && (BeyonderUtil.areAllies(livingEntity, getOwner())) || entity == getOwner()) {
                        damage /= 2;
                    }
                    if (this.getOwner() == null) {
                        if (!BeyonderUtil.isBeyonderCapable(livingEntity)) {
                            livingEntity.hurt(BeyonderUtil.lightningSource(this, livingEntity), damage);
                        } else {
                            livingEntity.hurt(BeyonderUtil.lightningSource(this, livingEntity), damage * 1.2f);
                        }
                        if (getMentalDamage() != 0) {
                            BeyonderUtil.applyMentalDamage(livingEntity, livingEntity, getMentalDamage());
                        }
                    } else {
                        if (!BeyonderUtil.isBeyonderCapable(livingEntity)) {
                            if (this.getOwner() != null) {
                                if (getMentalDamage() != 0) {
                                    BeyonderUtil.applyMentalDamage(getOwner(), livingEntity, getMentalDamage());
                                }
                                livingEntity.hurt(BeyonderUtil.lightningSource(this.getOwner(), livingEntity), damage * 1.4f);
                            } else {
                                if (getMentalDamage() != 0) {
                                    BeyonderUtil.applyMentalDamage(livingEntity, livingEntity, getMentalDamage());
                                }
                                livingEntity.hurt(BeyonderUtil.lightningSource(this, livingEntity), damage * 1.4f);
                            }
                        } else {
                            if (this.getOwner() != null) {
                                if (getMentalDamage() != 0) {
                                    BeyonderUtil.applyMentalDamage(getOwner(), livingEntity, getMentalDamage());
                                }
                                livingEntity.hurt(BeyonderUtil.lightningSource(this.getOwner(), livingEntity), damage * 0.9f);
                            } else {
                                if (getMentalDamage() != 0) {
                                    BeyonderUtil.applyMentalDamage(livingEntity, livingEntity, getMentalDamage());
                                }
                                livingEntity.hurt(BeyonderUtil.lightningSource(this, livingEntity), damage * 0.9f);
                            }
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            // Specific check for the error in the crash report
            if (e.getMessage() != null && e.getMessage().contains("Start element") && e.getMessage().contains("is larger than end element")) {
                LOTM.LOGGER.error("Caught specific error in explodeLightningBlock, discarding entity: " + e.getMessage());
                this.discard();
            } else {
                LOTM.LOGGER.error("IllegalArgumentException in explodeLightningBlock, discarding entity: " + e.getMessage());
                this.discard();
            }
        } catch (Exception e) {
            LOTM.LOGGER.error("Unexpected error in explodeLightningBlock, discarding entity: " + e.getMessage());
            this.discard();
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    private AABB createBoundingBox(Vec3 position) {
        double boxSize = 0.2;
        return new AABB(
                position.x - boxSize, position.y - boxSize, position.z - boxSize,
                position.x + boxSize, position.y + boxSize, position.z + boxSize
        );
    }

    public void setNewStartPos(Vec3 newStartPos) {
        this.startPos = newStartPos;
    }

    @Override
    public @NotNull ParticleOptions getTrailParticle() {
        return ParticleInit.NULL_PARTICLE.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide()) {
            if (result.getEntity() instanceof LivingEntity entity) {
                if (this.getOwner() == null) {
                    entity.hurt(BeyonderUtil.lightningSource(this, entity), getDamage());
                } else {
                    entity.hurt(BeyonderUtil.lightningSource(this.getOwner(), entity), getDamage());
                }
                if (this.getOwner() != null && getMentalDamage() != 0) {
                    BeyonderUtil.applyMentalDamage(owner, entity, getMentalDamage());
                }
                this.shouldDiscard = true;  // Mark for discard instead of immediate discard
            }
        }
        super.onHitEntity(result);
    }

    public int mentalDamageAmount() {
        int amount = 0;
        if (this.getOwner() != null) {
            if (BeyonderUtil.currentPathwayAndSequenceMatches(this.getOwner(), BeyonderClassInit.SAILOR.get(), 1)) {
                if (BeyonderUtil.getSequence(this.getOwner()) == 1) {
                    return 2;
                } else {
                    return 5;
                }
            }
        }
        return amount;
    }

    public int getMaxLength() {
        return this.entityData.get(MAX_LENGTH);
    }

    public void setMaxLength(int length) {
        this.entityData.set(MAX_LENGTH, length);
    }

    public float getSpeed() {
        return this.entityData.get(SPEED);
    }

    public void setSpeed(float speed) {
        this.entityData.set(SPEED, speed);
    }

    public List<Vec3> getPositions() {
        return positions;
    }

    public List<AABB> getBoundingBoxes() {
        return boundingBoxes;
    }

    @Override
    public LivingEntity getOwner() {
        return this.owner;
    }

    public Level getLevel() {
        return this.level();
    }

    public void setTargetPos(Vec3 targetPos) {
        this.targetPos = targetPos;
    }

    public Vec3 getTargetPos() {
        return this.targetPos;
    }


    public void setTargetEntity(Entity targetEntity) {
        this.targetEntity = targetEntity;
    }

    public Entity getTargetEntity() {
        return this.targetEntity;
    }

    public void setOwner(LivingEntity entity) {
        this.owner = entity;
    }

    public boolean getFallDown() {
        return this.entityData.get(FALL_DOWN);
    }

    public void setFallDown(boolean fallDown) {
        this.entityData.set(FALL_DOWN, fallDown);
    }

    public boolean getBranchOut() {
        return this.entityData.get(BRANCH_OUT);
    }

    public void setBranchOut(boolean branchOut) {
        this.entityData.set(BRANCH_OUT, branchOut);
    }

    public boolean getNoUp() {
        return this.entityData.get(NO_UP);
    }

    public void setNoUp(boolean noUp) {
        this.entityData.set(NO_UP, noUp);
    }

    public boolean getStar() {
        return this.entityData.get(STAR);
    }

    public void setStar(boolean star) {
        this.entityData.set(STAR, star);
    }


    public boolean getSynchedMovement() {
        return this.entityData.get(SYNCHED_MOVEMENT);
    }

    public void setSynchedMovement(boolean synchedMovement) {
        this.entityData.set(SYNCHED_MOVEMENT, synchedMovement);
    }

    public Vec3 getLastPos() {
        return this.lastPos;
    }

    public void setLastPos(Vec3 lastPos) {
        this.lastPos = lastPos;
    }

    public void setDamage(int damage) { //increase by 3x
        this.entityData.set(DAMAGE, damage * 3);
    }

    public int getDamage() {
        return this.entityData.get(DAMAGE);
    }

    public void setMentalDamage(int damage) { //increase by 3x
        this.entityData.set(MENTAL_DAMAGE, damage);
    }

    public int getMentalDamage() {
        return this.entityData.get(MENTAL_DAMAGE);
    }
}