package net.swimmingtuna.lotm.util.EntityUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.swimmingtuna.lotm.entity.DragonBreathEntity;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.UpdateDragonBreathS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.RotationUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BeamEntity extends LOTMProjectile {
    public double endPosX;
    public double endPosY;
    public double endPosZ;
    public Vec3 endPos;
    public double collidePosX;
    public double collidePosY;
    public double collidePosZ;
    public Vec3 collidePos;
    public double prevCollidePosX;
    public double prevCollidePosY;
    public double prevCollidePosZ;
    public Vec3 prevCollidePos;
    public float renderYaw;
    public float renderPitch;

    public boolean on = true;

    public @Nullable Direction side = null;

    private static final EntityDataAccessor<Boolean> TWILIGHT = SynchedEntityData.defineId(BeamEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DESTROY_BLOCKS = SynchedEntityData.defineId(BeamEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DRAGON_BREATH = SynchedEntityData.defineId(BeamEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(BeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> SIZE = SynchedEntityData.defineId(BeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FRENZY_TIME = SynchedEntityData.defineId(BeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_YAW = SynchedEntityData.defineId(BeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PITCH = SynchedEntityData.defineId(BeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> RANGE = SynchedEntityData.defineId(BeamEntity.class, EntityDataSerializers.INT);

    public float prevYaw;
    public float prevPitch;

    public int animation;

    protected BeamEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);

        this.noCulling = true;

        this.update();
    }

    protected BeamEntity(EntityType<? extends Projectile> entityType, LivingEntity owner, float power) {
        this(entityType, owner.level());

        this.setOwner(owner);
        this.setPower(power);
    }


    public abstract int getFrames();

    public double getRange() {
        return this.entityData.get(RANGE);
    }

    public void setRange(int range) {
        this.entityData.set(RANGE, range);
    }



    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }

    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, damage);
    }

    public int getFrenzyTime() {
        return this.entityData.get(FRENZY_TIME);
    }

    public void setFrenzyTime(int frenzyTime) {
        this.entityData.set(FRENZY_TIME, frenzyTime);
    }

    public boolean getIsDragonBreath() {
        return this.entityData.get(DRAGON_BREATH);
    }

    public void setIsDragonbreath(boolean isDragonBreath) {
        this.entityData.set(DRAGON_BREATH, isDragonBreath);
    }

    public boolean getIsTwilight() {
        return this.entityData.get(TWILIGHT);
    }

    public void setIsTwilight(boolean isTwilight) {
        this.entityData.set(TWILIGHT, isTwilight);
    }

    public boolean getDestroyBlocks() {
        return this.entityData.get(DESTROY_BLOCKS);
    }

    public void setDestroyBlocks(boolean destroyBlocks) {
        this.entityData.set(DESTROY_BLOCKS, destroyBlocks);
    }

    public abstract int getDuration();


    public abstract int getCharge();

    protected boolean causesFire() {
        return false;
    }

    protected boolean breaksBlocks() {
        return true;
    }

    protected boolean isStill() {
        return false;
    }


    protected Vec3 calculateSpawnPos(LivingEntity owner) {
        return new Vec3(owner.getX(), owner.getEyeY() - (this.getBbHeight() / 2.0F) + 0.5, owner.getZ()).add(RotationUtil.getTargetAdjustedLookAngle(owner));
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        this.update();
        this.calculateEndPos();
    }

    @Override
    public void tick() {
        super.tick();

        // Update previous positions and rotations
        this.prevCollidePos = this.collidePos;
        this.prevYaw = this.renderYaw;
        this.prevPitch = this.renderPitch;
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        if (!this.isStill()) {
            this.update();
        }


        if (this.getOwner() instanceof LivingEntity owner) {
            if (!this.on && this.animation == 0) {
                this.discard();
            }

            if (this.getFrames() > 0) {
                if (this.on) {
                    if (this.animation < this.getFrames()) {
                        this.animation++;
                    }
                } else {
                    if (this.animation > 0) {
                        this.animation--;
                    }
                }
            }

            if (this.getTime() >= this.getCharge()) {
                if (!this.level().isClientSide) {
                    LOTMNetworkHandler.sendToAllPlayers(new UpdateDragonBreathS2C(
                            this.getX(), this.getY(), this.getZ(),  // start positions
                            this.endPosX, this.endPosY, this.endPosZ,  // end positions
                            this.getId(),  // entity ID
                            this.prevYaw, this.renderYaw,  // yaw data
                            this.prevPitch, this.renderPitch,  // pitch data
                            this.getTime(),  // current time
                            this.getCharge(),  // charge time
                            this.getDuration(),  // duration
                            (float) this.animation,  // animation progress
                            (float) this.getSize(),  // size
                            this.causesFire(),  // causes fire flag
                            this.prevCollidePosX, this.prevCollidePosY, this.prevCollidePosZ,  // previous collision pos
                            this.collidePosX, this.collidePosY, this.collidePosZ  // current collision pos
                    ));
                }

                if (!this.isStill()) {
                    this.calculateEndPos();
                }

                // Corrected collision detection start position
                List<Entity> entities = this.checkCollisions(
                        new Vec3(this.getX(), this.getY(), this.getZ()), // Corrected start position
                        new Vec3(this.endPosX, this.endPosY, this.endPosZ)
                );

                // Handle entity collisions and effects
                for (Entity entity : entities) {
                    if (entity == owner) continue;
                    if (getIsDragonBreath() && this.getOwner() != null && this.getOwner() instanceof LivingEntity livingOwner && entity instanceof LivingEntity livingEntity && !BeyonderUtil.areAllies(livingOwner, livingEntity)) {
                        BeyonderUtil.applyMentalDamage(livingOwner, livingEntity, this.getDamage());
                    } else if (getIsDragonBreath() && this.getOwner() == null && entity instanceof LivingEntity livingEntity) {
                        livingEntity.hurt(BeyonderUtil.magicSource(this, livingEntity), getDamage());
                    }
                    if (getIsTwilight() && entity instanceof LivingEntity livingEntity && this.getOwner() instanceof LivingEntity pOwner && !BeyonderUtil.areAllies(pOwner, livingEntity)) {
                        int age = livingEntity.getPersistentData().getInt("age");
                        livingEntity.hurt(BeyonderUtil.genericSource(owner, livingEntity), 10);
                        int ageDivisibleAmount = 1;
                        if (pOwner instanceof Mob mob) {
                            ageDivisibleAmount = 3;
                        }
                        if (this.tickCount % 3 == 0) {
                            if (livingEntity instanceof Player player) {
                                player.displayClientMessage(Component.literal("You are getting rapidly aged").withStyle(BeyonderUtil.ageStyle(livingEntity)).withStyle(ChatFormatting.BOLD), true);
                            }
                            if (BeyonderUtil.getSequence(pOwner) != 0) {
                                livingEntity.getPersistentData().putUUID("ageUUID", pOwner.getUUID());
                                livingEntity.getPersistentData().putInt("age", ((age + (30 - BeyonderUtil.getSequence(pOwner))) * 9) / ageDivisibleAmount);
                            } else {
                                livingEntity.getPersistentData().putUUID("ageUUID", pOwner.getUUID());
                                livingEntity.getPersistentData().putInt("age", (age + (50)) / ageDivisibleAmount);
                            }
                        }
                    }
                    if (entity instanceof LivingEntity livingEntity && getIsDragonBreath() && this.getOwner() != null && this.getOwner() instanceof LivingEntity livingOwner && !BeyonderUtil.areAllies(livingOwner, livingEntity) && this.tickCount >= this.getCharge() + this.getDuration() - 20) {
                        BeyonderUtil.applyFrenzy(livingEntity, this.getFrenzyTime());
                    }

                    if (this.causesFire()) {
                        entity.setSecondsOnFire(5);
                    }
                }

                // Handle block breaking and fire
            }

            if (this.getTime() - this.getCharge() >= this.getDuration()) {
                this.on = false;
            }
        }
    }

    private static final List<Block> EXCLUDED_BLOCKS = List.of(Blocks.BEDROCK, Blocks.OBSIDIAN);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DAMAGE, 20.0F);
        this.entityData.define(DATA_YAW, 0.0F);
        this.entityData.define(DATA_PITCH, 0.0F);
        this.entityData.define(DRAGON_BREATH, false);
        this.entityData.define(FRENZY_TIME, 1);
        this.entityData.define(SIZE, 1);
        this.entityData.define(DESTROY_BLOCKS, true);
        this.entityData.define(TWILIGHT, false);
        this.entityData.define(RANGE, 64);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("damage")) {
            this.setDamage(compound.getFloat("damage"));
        }
        if (compound.contains("range")) {
            this.setRange(compound.getInt("range"));
        }
        if (compound.contains("data_yaw")) {
            this.setYaw(compound.getFloat("data_yaw"));
        }
        if (compound.contains("data_pitch")) {
            this.setPitch(compound.getFloat("data_pitch"));
        }
        if (compound.contains("dragon_breath")) {
            this.setIsDragonbreath(compound.getBoolean("dragon_breath"));
        }
        if (compound.contains("frenzy_time")) {
            this.setFrenzyTime(compound.getInt("frenzy_time"));
        }
        if (compound.contains("size")) {
            this.setSize(compound.getInt("size"));
        }
        if (compound.contains("destroy_blocks")) {
            this.setSize(compound.getInt("destroy_blocks"));
        }
        if (compound.contains("twilight")) {
            this.setSize(compound.getInt("twilight"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("damage", this.getDamage());
        compound.putFloat("data_yaw", this.getYaw());
        compound.putFloat("data_pitch", this.getPitch());
        compound.putBoolean("dragon_breath", this.getIsDragonBreath());
        compound.putInt("frenzy_time", this.getFrenzyTime());
        compound.putInt("size", this.getSize());
        compound.putBoolean("destroy_blocks", this.getDestroyBlocks());
        compound.putBoolean("twilight", this.getIsTwilight());
        compound.putInt("range", (int) this.getRange());
    }


    public int getSize() {
        return this.entityData.get(SIZE);
    }

    public void setSize(int size) {
        this.entityData.set(SIZE, size);
    }

    public float getYaw() {
        return this.entityData.get(DATA_YAW);
    }

    public void setYaw(float yaw) {
        this.entityData.set(DATA_YAW, yaw);
    }

    public float getPitch() {
        return this.entityData.get(DATA_PITCH);
    }

    public void setPitch(float pitch) {
        this.entityData.set(DATA_PITCH, pitch);
    }


    public List<Entity> checkCollisions(Vec3 from, Vec3 to) {
        if (!(this.getOwner() instanceof LivingEntity owner)) return List.of();
        BlockHitResult result = this.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (result.getType() != HitResult.Type.MISS) {
            Vec3 pos = result.getLocation();
            this.collidePosX = pos.x;
            this.collidePosY = pos.y;
            this.collidePosZ = pos.z;
            this.side = result.getDirection();
        } else {
            this.collidePosX = to.x;
            this.collidePosY = to.y;
            this.collidePosZ = to.z;
            this.side = null;
        }
        Vec3 dir = to.subtract(from).normalize();
        double radius = this.getSize();
        AABB bounds = new AABB(
                Math.min(from.x, this.collidePosX) - radius,
                Math.min(from.y, this.collidePosY) - radius,
                Math.min(from.z, this.collidePosZ) - radius,
                Math.max(from.x, this.collidePosX) + radius,
                Math.max(from.y, this.collidePosY) + radius,
                Math.max(from.z, this.collidePosZ) + radius
        );

        // Replace the block destruction logic in the checkCollisions method
// (around lines 250-290 in your original code)

        if (!this.level().isClientSide) {
            // Check if owner is looking downward (pitch > 70 degrees - really looking at ground)
            boolean isLookingDown = false;
            if (owner != null) {
                float pitch = owner.getXRot(); // Positive values = looking down
                isLookingDown = pitch > 70.0f; // 90 degrees is straight down, so 70 is 20 degrees from straight down
            }

            // Get owner's ground level and size for comparison
            double ownerGroundY = owner != null ? owner.getY() : Double.MAX_VALUE;
            double ownerRadius = owner != null ? owner.getBbWidth() / 2.0 : 1.0; // Use owner's actual width

            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
            for (int x = (int) Math.floor(bounds.minX); x <= Math.ceil(bounds.maxX); x++) {
                for (int y = (int) Math.floor(bounds.minY); y <= Math.ceil(bounds.maxY); y++) {
                    for (int z = (int) Math.floor(bounds.minZ); z <= Math.ceil(bounds.maxZ); z++) {
                        mutablePos.set(x, y, z);
                        Vec3 point = new Vec3(x + 0.5, y + 0.5, z + 0.5);
                        Vec3 fromToPoint = point.subtract(from);
                        double dot = fromToPoint.dot(dir);
                        Vec3 projection = dir.scale(dot);
                        Vec3 distanceVec = fromToPoint.subtract(projection);
                        double distance = distanceVec.length();

                        if (distance <= radius) {
                            boolean isNearOwnerFeet = false;
                            double blockDistanceFromOwner = Math.sqrt(Math.pow(x + 0.5 - owner.getX(), 2) + Math.pow(z + 0.5 - owner.getZ(), 2));
                            isNearOwnerFeet = blockDistanceFromOwner <= (ownerRadius + 0.5) && y <= ownerGroundY + 1;
                            boolean shouldDestroy = getDestroyBlocks() && (!isNearOwnerFeet || isLookingDown);

                            if (shouldDestroy) {
                                if (this.breaksBlocks() && !EXCLUDED_BLOCKS.contains(this.level().getBlockState(mutablePos).getBlock())) {
                                    this.level().destroyBlock(mutablePos, false);
                                }
                            } else if (this.tickCount % 5 == 0 && getIsTwilight() &&
                                    this.level().getBlockState(mutablePos) != Blocks.BEDROCK.defaultBlockState() &&
                                    this.level().getBlockState(mutablePos) != Blocks.WATER.defaultBlockState() &&
                                    (!isNearOwnerFeet || isLookingDown)) { // Same logic for twilight effect
                                if (this.level().getBlockState(mutablePos) != Blocks.DIRT.defaultBlockState() &&
                                        this.level().getBlockState(mutablePos) != Blocks.AIR.defaultBlockState()) {
                                    this.level().setBlock(mutablePos, Blocks.DIRT.defaultBlockState(), 11);
                                } else {
                                    this.level().destroyBlock(mutablePos, false);
                                }
                            }

                            if (this.causesFire()) {
                                if (this.random.nextInt(3) == 0 &&
                                        this.level().getBlockState(mutablePos).isAir() &&
                                        this.level().getBlockState(mutablePos.below()).isSolidRender(this.level(), mutablePos.below())) {
                                    this.level().setBlockAndUpdate(mutablePos, BaseFireBlock.getState(this.level(), mutablePos));
                                }
                            }
                        }
                    }
                }
            }
        }

        double entityDetectionRadius = radius * 1.5;

        AABB entityBounds = new AABB(
                Math.min(from.x, this.collidePosX) - radius * 0.8,
                Math.min(from.y, this.collidePosY) - radius * 0.8,
                Math.min(from.z, this.collidePosZ) - radius * 0.8,
                Math.max(from.x, this.collidePosX) + radius * 0.8,
                Math.max(from.y, this.collidePosY) + radius,
                Math.max(from.z, this.collidePosZ) + radius * 0.8
        );
        List<Entity> entities = new ArrayList<>();
        for (Entity entity : this.level().getEntitiesOfClass(Entity.class, entityBounds)) {
            if (entity == this.getOwner() || entity == this) continue;
            AABB entityBox = entity.getBoundingBox();
            if (rayIntersectsBox(from, to, entityBox)) {
                entities.add(entity);
                continue;
            }
            Vec3[] checkPoints = {
                    new Vec3(entity.getX(), entity.getY(), entity.getZ()),
                    new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.3, entity.getZ()),
                    new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.6, entity.getZ()),
                    new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.45, entity.getZ())
            };

            for (Vec3 point : checkPoints) {
                Vec3 nearestPoint = getNearestPointOnLine(from, to, point);
                double distance = point.distanceTo(nearestPoint);

                if (distance <= entityDetectionRadius) {
                    entities.add(entity);
                    break;
                }
            }
        }
        return entities;
    }

    private boolean rayIntersectsBox(Vec3 rayStart, Vec3 rayEnd, AABB box) {
        Vec3 rayDir = rayEnd.subtract(rayStart).normalize();

        // Calculate intersection with each face of the box
        double tMin = (box.minX - rayStart.x) / (rayDir.x == 0 ? 0.00001 : rayDir.x);
        double tMax = (box.maxX - rayStart.x) / (rayDir.x == 0 ? 0.00001 : rayDir.x);

        if (tMin > tMax) {
            double temp = tMin;
            tMin = tMax;
            tMax = temp;
        }

        double tyMin = (box.minY - rayStart.y) / (rayDir.y == 0 ? 0.00001 : rayDir.y);
        double tyMax = (box.maxY - rayStart.y) / (rayDir.y == 0 ? 0.00001 : rayDir.y);

        if (tyMin > tyMax) {
            double temp = tyMin;
            tyMin = tyMax;
            tyMax = temp;
        }

        if ((tMin > tyMax) || (tyMin > tMax)) {
            return false;
        }

        if (tyMin > tMin) {
            tMin = tyMin;
        }

        if (tyMax < tMax) {
            tMax = tyMax;
        }

        double tzMin = (box.minZ - rayStart.z) / (rayDir.z == 0 ? 0.00001 : rayDir.z);
        double tzMax = (box.maxZ - rayStart.z) / (rayDir.z == 0 ? 0.00001 : rayDir.z);

        if (tzMin > tzMax) {
            double temp = tzMin;
            tzMin = tzMax;
            tzMax = temp;
        }

        if ((tMin > tzMax) || (tzMin > tMax)) {
            return false;
        }

        // Check if intersection is within ray length
        double maxDistance = rayStart.distanceTo(rayEnd);
        return tMin >= 0 && tMin <= maxDistance;
    }

    private Vec3 getNearestPointOnLine(Vec3 lineStart, Vec3 lineEnd, Vec3 point) {
        Vec3 line = lineEnd.subtract(lineStart);
        double len = line.length();
        line = line.normalize();

        Vec3 v = point.subtract(lineStart);
        double d = v.dot(line);
        d = Math.max(0, Math.min(len, d));

        return lineStart.add(line.scale(d));
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }



    private void update() {
        if (this.getOwner() instanceof LivingEntity owner) {
            float yaw = owner.getYRot();
            float pitch = owner.getXRot();
            this.renderYaw = (float) Math.toRadians(yaw + 90.0F);
            this.renderPitch = (float) Math.toRadians(-pitch);
            this.setYaw((float) Math.toRadians(yaw + 90.0F));
            this.setPitch((float) Math.toRadians(-pitch));
            Vec3 spawn = this.calculateSpawnPos(owner);
            double yOffset = (this.getFrames() <= this.getCharge()) ? 0.5 : 0.0;
            this.setPos(spawn.x, spawn.y + yOffset, spawn.z);
        }
        //LOTMNetworkHandler.sendToAllPlayers(new UpdateDragonBreathS2C(this.getX(), this.getY(), this.getZ(), this.endPosX, this.endPosY, this.endPosZ, this.getId()));
    }


    private void calculateEndPos() {
        Vec3 direction;
        Vec3 startPos;

        if (this.getOwner() instanceof LivingEntity owner) {
            float scale = 1.0f;
            try {
                scale = BeyonderUtil.getScale(owner);
            } catch (Exception ignored) {
            }

            // Get the player's look direction
            direction = owner.getLookAngle();

            // Start raycast from player's eye position
            startPos = owner.getEyePosition();

            // Perform raycast to find what the player is looking at
            Vec3 endPos = performRaycast(startPos, direction, scale);

            this.endPosX = endPos.x;
            this.endPosY = endPos.y;
            this.endPosZ = endPos.z;
            this.endPos = endPos;

        } else {
            // Fallback for non-living entities
            if (this.level().isClientSide) {
                direction = new Vec3(
                        Math.cos(this.renderYaw) * Math.cos(this.renderPitch),
                        Math.sin(this.renderPitch),
                        Math.sin(this.renderYaw) * Math.cos(this.renderPitch)
                ).normalize();
            } else {
                direction = new Vec3(Math.cos(this.getYaw()) * Math.cos(this.getPitch()), Math.sin(this.getPitch()), Math.sin(this.getYaw()) * Math.cos(this.getPitch())).normalize();
            }

            startPos = new Vec3(this.getX(), this.getY(), this.getZ());
            Vec3 end = startPos.add(direction.scale(this.getRange()));
            this.endPosX = end.x;
            this.endPosY = end.y;
            this.endPosZ = end.z;
            this.endPos = end;
        }
    }

    private Vec3 performRaycast(Vec3 startPos, Vec3 direction, float scale) {
        double maxDistance = 50.0;
        maxDistance *= Math.max(1.0, scale * 0.5);
        Vec3 endPos = startPos.add(direction.scale(maxDistance));
        BlockHitResult blockHit = this.level().clip(new ClipContext(startPos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, this.getOwner()));
        EntityHitResult entityHit = getEntityHitResult(startPos, direction, maxDistance);
        Vec3 finalEndPos;
        if (entityHit != null && blockHit.getType() != HitResult.Type.MISS) {
            double entityDistance = startPos.distanceTo(entityHit.getLocation());
            double blockDistance = startPos.distanceTo(blockHit.getLocation());
            if (entityDistance < blockDistance) {
                finalEndPos = entityHit.getLocation();
            } else {
                finalEndPos = blockHit.getLocation();
            }
        } else if (entityHit != null) {
            finalEndPos = entityHit.getLocation();
        } else if (blockHit.getType() != HitResult.Type.MISS) {
            finalEndPos = blockHit.getLocation();
        } else {
            finalEndPos = startPos.add(direction.scale(50.0));
        }

        return finalEndPos;
    }

    private EntityHitResult getEntityHitResult(Vec3 startPos, Vec3 direction, double maxDistance) {
        Vec3 endPos = startPos.add(direction.scale(maxDistance));
        AABB searchBox = new AABB(startPos, endPos).inflate(1.0);
        Entity closestEntity = null;
        Vec3 closestHitPos = null;
        double closestDistance = maxDistance;
        for (Entity entity : this.level().getEntities(this.getOwner(), searchBox)) {
            if (entity == this.getOwner() || entity == this) {
                continue;
            }

            AABB entityBox = entity.getBoundingBox();
            Optional<Vec3> hitResult = entityBox.clip(startPos, endPos);
            if (hitResult.isPresent()) {
                Vec3 hitPos = hitResult.get();
                double distance = startPos.distanceTo(hitPos);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entity;
                    closestHitPos = hitPos;
                }
            }
        }

        if (closestEntity != null) {
            return new EntityHitResult(closestEntity, closestHitPos);
        }

        return null;
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
}
