package net.swimmingtuna.lotm.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.capabilities.concealed_data.ConcealedUtils;
import net.swimmingtuna.lotm.capabilities.is_concealed_data.IsConcealedUtils;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.CustomEntityDataSerializers;
import net.swimmingtuna.lotm.world.worldgen.dimension.DimensionInit;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Random;
import java.util.UUID;

public class ApprenticeDoorEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public enum DoorMode {
        TELEPORT_ONLY,
        DOOR_MIRAGE,
        EXILE,
        CONCEALED_SPACE,
        MAZE
    }

    public enum DoorAnimationKind {
        BEHIND,
        BELLOW,
        FADE_IN
    }


    private static final EntityDataAccessor<DoorMode> DOOR_MODE = SynchedEntityData.defineId(ApprenticeDoorEntity.class, CustomEntityDataSerializers.DOOR_MODE);
    private static final EntityDataAccessor<DoorAnimationKind> DOOR_ANIMATION_KIND = SynchedEntityData.defineId(ApprenticeDoorEntity.class, CustomEntityDataSerializers.DOOR_ANIMATION_KIND);
    private static final EntityDataAccessor<Boolean> HAS_PLAYED_ANIMATION = SynchedEntityData.defineId(ApprenticeDoorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_DYING = SynchedEntityData.defineId(ApprenticeDoorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FREE_TO_USE = SynchedEntityData.defineId(ApprenticeDoorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> SEQUENCE = SynchedEntityData.defineId(ApprenticeDoorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LIFE = SynchedEntityData.defineId(ApprenticeDoorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FULL_LIFE = SynchedEntityData.defineId(ApprenticeDoorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(ApprenticeDoorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_ENTERING_CONCEALED_SPACE = SynchedEntityData.defineId(ApprenticeDoorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> X = SynchedEntityData.defineId(ApprenticeDoorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> Y = SynchedEntityData.defineId(ApprenticeDoorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> Z = SynchedEntityData.defineId(ApprenticeDoorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> DESTINATION = SynchedEntityData.defineId(ApprenticeDoorEntity.class, EntityDataSerializers.STRING);

    private UUID creator;
    private UUID sealUUID;

    public ApprenticeDoorEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    //Concealed Spaces
    public ApprenticeDoorEntity(Level level, UUID creator, int sequence, int life, float yaw, float x, float y, float z, boolean isEntering, Level dimensionDestination, DoorAnimationKind animationKind) {
        this(EntityInit.APPRENTICE_DOOR_ENTITY.get(), level);
        this.entityData.set(DOOR_MODE, DoorMode.CONCEALED_SPACE);
        this.entityData.set(DOOR_ANIMATION_KIND, animationKind);
        this.entityData.set(SEQUENCE, sequence);
        this.entityData.set(LIFE, life);
        this.entityData.set(FULL_LIFE, life);
        this.entityData.set(YAW, yaw);
        this.entityData.set(X, x);
        this.entityData.set(Y, y);
        this.entityData.set(Z, z);
        this.entityData.set(IS_ENTERING_CONCEALED_SPACE, isEntering);
        this.entityData.set(DESTINATION, dimensionDestination.dimension().location().toString());

        this.creator = creator;
    }

    //Maze
    public ApprenticeDoorEntity(Level level, UUID creator, UUID sealUUID, int sequence, float yaw, float x, float y, float z, Level dimensionDestination){
        this(EntityInit.APPRENTICE_DOOR_ENTITY.get(), level);
        this.entityData.set(DOOR_MODE, DoorMode.MAZE);
        this.entityData.set(DOOR_ANIMATION_KIND, DoorAnimationKind.FADE_IN);
        this.entityData.set(SEQUENCE, sequence);
        this.entityData.set(LIFE, 10);
        this.entityData.set(FULL_LIFE, 10);
        this.entityData.set(YAW, yaw);
        this.entityData.set(X, x);
        this.entityData.set(Y, y);
        this.entityData.set(Z, z);
        this.entityData.set(DESTINATION, dimensionDestination.dimension().location().toString());

        this.creator = creator;
        this.sealUUID = sealUUID;
    }

    //Teleport only
    public ApprenticeDoorEntity(Level level, LivingEntity creator, int sequence, int life, float yaw, float x, float y, float z, Level dimensionDestination, DoorAnimationKind animationKind) {
        this(EntityInit.APPRENTICE_DOOR_ENTITY.get(), level);
        this.entityData.set(DOOR_MODE, DoorMode.TELEPORT_ONLY);
        this.entityData.set(DOOR_ANIMATION_KIND, animationKind);
        this.entityData.set(SEQUENCE, sequence);
        this.entityData.set(LIFE, life);
        this.entityData.set(FULL_LIFE, life);
        this.entityData.set(YAW, yaw);
        this.entityData.set(X, x);
        this.entityData.set(Y, y);
        this.entityData.set(Z, z);
        this.entityData.set(DESTINATION, dimensionDestination.dimension().location().toString());

        this.creator = creator.getUUID();
    }

    //Door mirage
    public ApprenticeDoorEntity(Level level, LivingEntity target, int sequence, float yaw, float x, float y, float z) {
        this(EntityInit.APPRENTICE_DOOR_ENTITY.get(), level);
        this.entityData.set(DOOR_MODE, DoorMode.DOOR_MIRAGE);
        this.entityData.set(DOOR_ANIMATION_KIND, DoorAnimationKind.FADE_IN);
        this.entityData.set(SEQUENCE, sequence);
        this.entityData.set(LIFE, 30);
        this.entityData.set(FULL_LIFE, 30);
        this.entityData.set(YAW, yaw);
        this.entityData.set(X, x);
        this.entityData.set(Y, y);
        this.entityData.set(Z, z);
        this.entityData.set(DESTINATION, level.dimension().location().toString());

        this.creator = target.getUUID();
    }

    //Exile
    public ApprenticeDoorEntity(Level level, LivingEntity creator, float yaw, int life) {
        this(EntityInit.APPRENTICE_DOOR_ENTITY.get(), level);
        this.entityData.set(DOOR_MODE, DoorMode.EXILE);
        this.entityData.set(DOOR_ANIMATION_KIND, DoorAnimationKind.FADE_IN);
        this.entityData.set(SEQUENCE, BeyonderUtil.getSequence(creator));
        this.entityData.set(LIFE, life);
        this.entityData.set(FULL_LIFE, life);
        this.entityData.set(YAW, yaw);
        this.creator = creator.getUUID();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (getDoorMode() == DoorMode.TELEPORT_ONLY) {
                if (!this.level().isClientSide) {
                    handleLife();
                    if (BeyonderUtil.isEntityColliding(this, this.level(), 1.0)) {
                        LivingEntity entity = BeyonderUtil.checkLivingEntityCollision(this, this.level(), 1.0);
                        if (entity != null && entity.isShiftKeyDown()) teleport(entity);
                    }
                }
            }
            if (getDoorMode() == DoorMode.CONCEALED_SPACE) {
                if (!this.level().isClientSide) {
                    handleLife();
                    if (BeyonderUtil.isEntityColliding(this, this.level(), 1.0)) {
                        LivingEntity entity = BeyonderUtil.checkLivingEntityCollision(this, this.level(), 1.0);
                        if (entity != null && entity.isShiftKeyDown()) teleport(entity);
                    }
                }
            }

            if (getDoorMode() == DoorMode.DOOR_MIRAGE) {
                if (!this.level().isClientSide) {
                    handleLife();
                    LivingEntity target = getCreator();
                    target.getPersistentData().putDouble("xDoorMirageStuck", this.getX());
                    target.getPersistentData().putDouble("yDoorMirageStuck", this.getY());
                    target.getPersistentData().putDouble("zDoorMirageStuck", this.getZ());
                    teleport(target);
                }
            }
            if (!this.level().isClientSide()) {
                if (getDoorMode() == DoorMode.EXILE && getCreator() != null) {
                    handleLife();
                    int chunkRadius = 5;
                    ChunkPos centerChunk = new ChunkPos(this.blockPosition());
                    if (this.level() instanceof ServerLevel serverLevel) {
                        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                                ChunkPos chunkPos = new ChunkPos(centerChunk.x + dx, centerChunk.z + dz);
                                serverLevel.getChunkSource().addRegionTicket(TicketType.PLAYER, chunkPos, 3, chunkPos);
                            }
                        }
                    }
                    for (LivingEntity livingEntity : BeyonderUtil.getNonAlliesNearby(getCreator(), BeyonderUtil.getDamage(getCreator()).get(ItemInit.EXILE.get()))) {
                        double x = this.getX() - livingEntity.getX();
                        double y = this.getY() - livingEntity.getY();
                        double z = this.getZ() - livingEntity.getZ();
                        double magnitude = Math.sqrt(x * x + y * y + z * z);
                        livingEntity.setDeltaMovement(x / magnitude * 2, y / magnitude * 2, z / magnitude * 2);
                        livingEntity.hurtMarked = true;
                    }
                    for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2))) {
                        if (livingEntity.getPersistentData().getInt("shouldntExileWithDoor") == 0) {
                            double x = livingEntity.getX();
                            double z = livingEntity.getZ();
                            double surfaceY = BeyonderUtil.findSurfaceY(livingEntity, x, z, DimensionInit.EXILED_DIMENSION_LEVEL_KEY);
                            CompoundTag tag = livingEntity.getPersistentData();
                            tag.putInt("exileDoorX", (int) x);
                            tag.putInt("exileDoorY", (int) livingEntity.getY());
                            tag.putInt("exileDoorZ", (int) z);
                            tag.putInt("exileDoorTimer", 400);
                            tag.putString("exileDoorDimension", livingEntity.level().dimension().location().toString());
                            if (livingEntity instanceof Player player) {
                                if (surfaceY != -1) {
                                    BeyonderUtil.teleportEntity(player, DimensionInit.EXILED_DIMENSION_LEVEL_KEY.location(), x, surfaceY, z);
                                }
                            } else {
                                Random random = new Random();
                                livingEntity.getPersistentData().putInt("exileDoorMob", random.nextInt(3));
                                livingEntity.teleportTo(0, livingEntity.getY() + 110, 0);
                                BeyonderUtil.applyStun(livingEntity, 400);
                            }
                        }
                    }
                } else if (getCreator() == null && getDoorMode() == DoorMode.EXILE) {
                    this.delete();
                }
            }
            if (getDoorMode() == DoorMode.MAZE){
                if (BeyonderUtil.isEntityColliding(this, this.level(), 1.0)) {
                    LivingEntity entity = BeyonderUtil.checkLivingEntityCollision(this, this.level(), 1.0);
                    if (entity != null && entity.isShiftKeyDown()) teleport(entity);
                }
            }
        }
    }

    public boolean getEnterConcealedSpace(){
        return this.entityData.get(IS_ENTERING_CONCEALED_SPACE);
    }

    public DoorMode getDoorMode() {
        return this.entityData.get(DOOR_MODE);
    }

    public void setYaw(float yaw){
        this.entityData.set(YAW, yaw);
    }

    public UUID getSealUUID(){
        return this.sealUUID;
    }


    public DoorAnimationKind getAnimationKind() {
        return this.entityData.get(DOOR_ANIMATION_KIND);
    }

    public boolean isFreeToUse() {
        return this.entityData.get(FREE_TO_USE);
    }


    public int getLife() {
        return this.entityData.get(LIFE);
    }

    public int getFullLife() {
        return this.entityData.get(FULL_LIFE);
    }

    public LivingEntity getCreator() {
        if (this.creator == null) return null;

        // Try to get the entity, but don't fail if it's not loaded yet
        LivingEntity entity = BeyonderUtil.getLivingEntityFromUUID(this.level(), this.creator);
        if (entity == null && !this.level().isClientSide()) {
            LOTM.LOGGER.warn("Creator with UUID {} not found in level {}", this.creator, this.level().dimension().location());
        }
        return entity;
    }

    public int getSequence() {
        return this.entityData.get(SEQUENCE);
    }

    public float getTeleportX() {
        return this.entityData.get(X);
    }

    public float getTeleportY() {
        return this.entityData.get(Y);
    }

    public float getTeleportZ() {
        return this.entityData.get(Z);
    }

    public Level getDimensionDestination() {
        ResourceLocation location = new ResourceLocation(this.entityData.get(DESTINATION));
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, location);
        MinecraftServer server = this.getServer();
        return server.getLevel(dimensionKey);
    }

    public float getYaw() {
        return this.entityData.get(YAW);
    }

    public void delete() {
        this.remove(RemovalReason.DISCARDED);
    }

    private void handleLife() {
        int life = getLife();
        if (getDoorMode() == DoorMode.TELEPORT_ONLY) {
            if (life < getFullLife() - 30) {
                this.entityData.set(HAS_PLAYED_ANIMATION, true);
                this.entityData.set(FREE_TO_USE, true);
            } else {
                this.entityData.set(HAS_PLAYED_ANIMATION, false);
                this.entityData.set(FREE_TO_USE, false);
            }

            if (life > 0) {
                if (life <= 30) {
                    this.entityData.set(IS_DYING, true);
                    this.entityData.set(FREE_TO_USE, false);
                }
                this.entityData.set(LIFE, life - 1);
            } else {
                delete();
            }
        }

        if (getDoorMode() == DoorMode.DOOR_MIRAGE) {
            if (life > 15) {
                this.entityData.set(HAS_PLAYED_ANIMATION, false);
                this.entityData.set(FREE_TO_USE, false);
            } else {
                this.entityData.set(HAS_PLAYED_ANIMATION, true);
                this.entityData.set(IS_DYING, true);
                this.entityData.set(FREE_TO_USE, true);
            }
            if (life > 0) this.entityData.set(LIFE, life - 1);
            else delete();
        }
        if (getDoorMode() == DoorMode.EXILE) {
            if (life < getFullLife() - 30) {
                this.entityData.set(HAS_PLAYED_ANIMATION, true);
                this.entityData.set(FREE_TO_USE, true);
            } else {
                this.entityData.set(HAS_PLAYED_ANIMATION, false);
                this.entityData.set(FREE_TO_USE, false);
            }

            if (life > 0) {
                if (life <= 30) {
                    this.entityData.set(IS_DYING, true);
                    this.entityData.set(FREE_TO_USE, false);
                }
                this.entityData.set(LIFE, life - 1);
            } else {
                delete();
            }
        }
        if(getDoorMode() == DoorMode.CONCEALED_SPACE) {
            if (life < getFullLife() - 30) {
                this.entityData.set(HAS_PLAYED_ANIMATION, true);
                this.entityData.set(FREE_TO_USE, true);
            }else{
                this.entityData.set(HAS_PLAYED_ANIMATION, false);
                this.entityData.set(FREE_TO_USE, false);
            }

            if (life > 0) {
                if (life <= 30) {
                    this.entityData.set(IS_DYING, true);
                    this.entityData.set(FREE_TO_USE, false);
                }
                this.entityData.set(LIFE, life - 1);
            } else {
                delete();
            }
        }
        if(getDoorMode() == DoorMode.MAZE){
            this.entityData.set(LIFE, 10);
        }
    }

    private void teleport(LivingEntity entity) {
        if (getDoorMode() == DoorMode.TELEPORT_ONLY) {
            if (isFreeToUse()) {
                if (getSequence() > 7) {
                    if (entity != null && entity == getCreator()) {
                        if (getDimensionDestination().dimension().equals(entity.level().dimension())) {
                            entity.teleportTo(getTeleportX(), getTeleportY(), getTeleportZ());
                        } else {
                            BeyonderUtil.teleportEntity(entity, getDimensionDestination(), getTeleportX(), getTeleportY(), getTeleportZ());                        }
                    }
                } else {
                    if (entity != null) {
                        if (getDimensionDestination().dimension().equals(entity.level().dimension())) {
                            entity.teleportTo(getTeleportX(), getTeleportY(), getTeleportZ());
                        } else {
                            BeyonderUtil.teleportEntity(entity, getDimensionDestination(), getTeleportX(), getTeleportY(), getTeleportZ());                        }
                    }
                }
            }
        }

        if (getDoorMode() == DoorMode.DOOR_MIRAGE) {
            if (isFreeToUse() && entity != null) {
                entity.getPersistentData().remove("xDoorMirageStuck");
                entity.getPersistentData().remove("yDoorMirageStuck");
                entity.getPersistentData().remove("zDoorMirageStuck");
                entity.teleportTo(getTeleportX(), getTeleportY(), getTeleportZ());
            }
        }
        if(getDoorMode() == DoorMode.CONCEALED_SPACE){
            if(isFreeToUse() && entity != null) {
                if (getEnterConcealedSpace()) {
                    ConcealedUtils.setConcealedSpaceExit(entity, new BlockPos(entity.getBlockX(), entity.getBlockY(), entity.getBlockZ()));
                    ConcealedUtils.setConcealedSpaceExitDimension(entity, entity.level());

                    IsConcealedUtils.setConcealmentOwner(entity, this.creator);
                    IsConcealedUtils.setIsConcealed(entity, true);
                    IsConcealedUtils.setConcealmentSequence(entity, getSequence());
                } else {
                    IsConcealedUtils.setConcealmentOwner(entity, new UUID(0, 0));
                    IsConcealedUtils.setIsConcealed(entity, false);
                    IsConcealedUtils.setConcealmentSequence(entity, 9);
                }
                this.entityData.set(LIFE, 30);
                BeyonderUtil.teleportEntity(entity, getDimensionDestination(), getTeleportX(), getTeleportY(), getTeleportZ());
            }
        }
    }

    private String getOpenAnimation() {
        if (getAnimationKind() == DoorAnimationKind.BEHIND) return "open_behind";
        if (getAnimationKind() == DoorAnimationKind.BELLOW) return "open_below";
        if (getAnimationKind() == DoorAnimationKind.FADE_IN) return "open_fade_in";
        return "";
    }

    private String getCloseAnimation() {
        if (getAnimationKind() == DoorAnimationKind.BEHIND) return "close_behind";
        if (getAnimationKind() == DoorAnimationKind.BELLOW) return "close_below";
        if (getAnimationKind() == DoorAnimationKind.FADE_IN) return "close_fade_in";
        return "";
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DOOR_MODE, DoorMode.TELEPORT_ONLY);
        this.entityData.define(DOOR_ANIMATION_KIND, DoorAnimationKind.BEHIND);
        this.entityData.define(HAS_PLAYED_ANIMATION, false);
        this.entityData.define(IS_DYING, false);
        this.entityData.define(FREE_TO_USE, false);
        this.entityData.define(SEQUENCE, 9);
        this.entityData.define(LIFE, 20);
        this.entityData.define(IS_ENTERING_CONCEALED_SPACE, false);
        this.entityData.define(FULL_LIFE, 20);
        this.entityData.define(YAW, 0F);
        this.entityData.define(X, 0F);
        this.entityData.define(Y, 0F);
        this.entityData.define(Z, 0F);
        this.entityData.define(DESTINATION, this.level().dimension().location().toString());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("doorMode")) {
            try {
                DoorMode mode = DoorMode.valueOf(tag.getString("doorMode"));
                this.entityData.set(DOOR_MODE, mode);
            } catch (IllegalArgumentException ignored) {
                delete();
            }
        }
        if (tag.contains("sealUUID")) {
            this.sealUUID = tag.getUUID("sealUUID");
        }
        if (tag.contains("doorAnimationKind")) {
            try {
                DoorAnimationKind kind = DoorAnimationKind.valueOf(tag.getString("doorAnimationKind"));
                this.entityData.set(DOOR_ANIMATION_KIND, kind);
            } catch (IllegalArgumentException ignored) {
                delete();
            }
        }
        if(tag.contains("isEnteringConcealedSpace")){
            this.entityData.set(IS_ENTERING_CONCEALED_SPACE, tag.getBoolean("isEnteringConcealedSpace"));
        }
        if (tag.contains("hasPlayedAnimation")) {
            this.entityData.set(HAS_PLAYED_ANIMATION, tag.getBoolean("hasPlayedAnimation"));
        }
        if (tag.contains("isDying")) {
            this.entityData.set(IS_DYING, tag.getBoolean("isDying"));
        }
        if (tag.contains("freeToUse")) {
            this.entityData.set(FREE_TO_USE, tag.getBoolean("freeToUse"));
        }
        if (tag.contains("sequence")) {
            this.entityData.set(SEQUENCE, tag.getInt("sequence"));
        }
        if (tag.contains("life")) {
            this.entityData.set(LIFE, tag.getInt("life"));
        }
        if (tag.contains("fullLife")) {
            this.entityData.set(FULL_LIFE, tag.getInt("fullLife"));
        }
        if (tag.contains("yaw")) {
            this.entityData.set(YAW, tag.getFloat("yaw"));
        }
        if (tag.contains("x")) {
            this.entityData.set(X, tag.getFloat("x"));
        }
        if (tag.contains("y")) {
            this.entityData.set(Y, tag.getFloat("y"));
        }
        if (tag.contains("z")) {
            this.entityData.set(Z, tag.getFloat("z"));
        }
        if (tag.contains("destination")) {
            this.entityData.set(DESTINATION, tag.getString("destination"));
        }

        if (tag.contains("creator")) {
            this.creator = tag.getUUID("creator");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("doorMode", this.entityData.get(DOOR_MODE).name());
        tag.putString("doorAnimationKind", this.entityData.get(DOOR_ANIMATION_KIND).name());
        tag.putBoolean("hasPlayedAnimation", this.entityData.get(HAS_PLAYED_ANIMATION));
        tag.putBoolean("isDying", this.entityData.get(IS_DYING));
        tag.putBoolean("freeToUse", this.entityData.get(FREE_TO_USE));
        tag.putInt("sequence", this.entityData.get(SEQUENCE));
        tag.putBoolean("isEnteringConcealedSpace", this.entityData.get(IS_ENTERING_CONCEALED_SPACE));
        tag.putInt("life", this.entityData.get(LIFE));
        tag.putInt("fullLife", this.entityData.get(FULL_LIFE));
        tag.putFloat("yaw", this.entityData.get(YAW));
        tag.putFloat("x", this.entityData.get(X));
        tag.putFloat("y", this.entityData.get(Y));
        tag.putFloat("z", this.entityData.get(Z));
        tag.putString("destination", this.entityData.get(DESTINATION));

        tag.putUUID("creator", this.creator);
        tag.putUUID("sealUUID", this.sealUUID);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<ApprenticeDoorEntity> animationState) {
        AnimationController<ApprenticeDoorEntity> controller = animationState.getController();

        if (this.getDoorMode() == DoorMode.TELEPORT_ONLY) {
            if (!this.entityData.get(HAS_PLAYED_ANIMATION)) {
                controller.setAnimation(RawAnimation.begin().then(getOpenAnimation(), Animation.LoopType.PLAY_ONCE));
                return PlayState.CONTINUE;
            }

            if (this.entityData.get(IS_DYING)) {
                controller.setAnimation(RawAnimation.begin().then(getCloseAnimation(), Animation.LoopType.PLAY_ONCE));
                return PlayState.CONTINUE;
            }

            controller.setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        }

        if(this.getDoorMode() == DoorMode.CONCEALED_SPACE){
            if (!this.entityData.get(HAS_PLAYED_ANIMATION)) {
                controller.setAnimation(RawAnimation.begin().then(getOpenAnimation(), Animation.LoopType.PLAY_ONCE));
                return PlayState.CONTINUE;
            }

            if (this.entityData.get(IS_DYING)) {
                controller.setAnimation(RawAnimation.begin().then(getCloseAnimation(), Animation.LoopType.PLAY_ONCE));
                return PlayState.CONTINUE;
            }

            controller.setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        }

        if (this.getDoorMode() == DoorMode.DOOR_MIRAGE) {
            controller.setAnimationSpeed(2.0f);

            if (!this.entityData.get(HAS_PLAYED_ANIMATION)) {
                controller.setAnimation(RawAnimation.begin().then(getOpenAnimation(), Animation.LoopType.PLAY_ONCE));
                return PlayState.CONTINUE;
            }

            if (this.entityData.get(IS_DYING)) {
                controller.setAnimation(RawAnimation.begin().then(getCloseAnimation(), Animation.LoopType.PLAY_ONCE));
                return PlayState.CONTINUE;
            }
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
