package net.swimmingtuna.lotm.entity;


import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.UpdateEntityLocationS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SwordOfTwilightEntity extends AbstractHurtingProjectile implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(SwordOfTwilightEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(SwordOfTwilightEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> HAS_PLAYED_ANIMATION = SynchedEntityData.defineId(SwordOfTwilightEntity.class, EntityDataSerializers.BOOLEAN);

    public SwordOfTwilightEntity(EntityType<? extends SwordOfTwilightEntity> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
        this.blocksBuilding = true;
    }



    @Override
    protected float getInertia() {
        return 0.99F;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }


    @Override
    public void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HAS_PLAYED_ANIMATION, false);
        this.entityData.define(YAW, 0.0f);
        this.entityData.define(PITCH, 0.0f);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return pDistance < 1000000; // 1000 blocks squared (much larger than before)
    }


    @Override
    public boolean isAlwaysTicking() {
        return true;
    }

    @Override
    public void checkDespawn() {

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
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("yaw")) {
            this.entityData.set(YAW, tag.getFloat("yaw"));
        }
        if (tag.contains("hasPlayedAnimation")) {
            this.entityData.set(HAS_PLAYED_ANIMATION, tag.getBoolean("hasPlayedAnimation"));
        }
        if (tag.contains("pitch")) {
            this.entityData.set(PITCH, tag.getFloat("pitch"));
        }
    }

    public float getYaw() {
        return this.entityData.get(YAW);
    }

    public void setPitch(float pitch) {
        this.entityData.set(PITCH, pitch);
    }

    public float getPitch() {
        return this.entityData.get(PITCH);
    }


    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("yaw", this.entityData.get(YAW));
        tag.putBoolean("hasPlayedAnimation", this.entityData.get(HAS_PLAYED_ANIMATION));
        tag.putFloat("pitch", this.entityData.get(PITCH));
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
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            float scale = BeyonderUtil.getScale(this);
            Vec3 currentPos = this.position();
            if (this.tickCount >= 20) {
                this.discard();
            }
            for (ServerPlayer player : level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(250))) {
                LOTMNetworkHandler.sendToPlayer(new UpdateEntityLocationS2C(currentPos.x(), currentPos.y(), currentPos.z(), this.getDeltaMovement().x(), this.getDeltaMovement().y(), this.getDeltaMovement().z(), this.getId()), player);
            }
            AABB aabb;
            if (this.tickCount == 14 && this.getOwner() != null && this.getOwner() instanceof LivingEntity owner) {
                int caseInt = this.getPersistentData().getInt("swordOfTwilightCase");
                if (caseInt == 0) {
                    aabb = new AABB(this.getX() + (scale * 0.66) - 5, this.getY() + (scale) - 5, this.getZ() - (scale * 1.1), this.getX() + (scale * 0.66) + 5, this.getY() + (scale * 2) + 5, this.getZ() + (scale  * 1.1));
                    for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, aabb)) {
                        if (livingEntity != owner && !BeyonderUtil.areAllies(owner, livingEntity)) {
                            CompoundTag tag = livingEntity.getPersistentData();
                            tag.putUUID("ageUUID", owner.getUUID());
                            tag.putInt("age", tag.getInt("age") + 800);
                            if (livingEntity instanceof Player player) {
                                player.displayClientMessage(Component.literal("You are getting rapidly aged").withStyle(BeyonderUtil.ageStyle(livingEntity)).withStyle(ChatFormatting.BOLD),true);
                            }
                            livingEntity.hurt(BeyonderUtil.genericSource(owner, livingEntity), 80);
                            livingEntity.getPersistentData().putInt("inTwilight", Math.max(tag.getInt("inTwilight"), 25));
                        }
                    }
                } else if (caseInt == 1) {
                    aabb = new AABB(this.getX() - (scale * 0.66) - 5, this.getY() + (scale) - 5, this.getZ() - (scale * 1.1), this.getX() - (scale * 0.66) + 5, this.getY() + (scale * 2) + 5, this.getZ() + (scale  * 1.1));
                    for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, aabb)) {
                        if (livingEntity != owner && !BeyonderUtil.areAllies(owner, livingEntity)) {
                            CompoundTag tag = livingEntity.getPersistentData();
                            tag.putUUID("ageUUID", owner.getUUID());
                            tag.putInt("age", tag.getInt("age") + 800);
                            if (livingEntity instanceof Player player) {
                                player.displayClientMessage(Component.literal("You are getting rapidly aged").withStyle(BeyonderUtil.ageStyle(livingEntity)).withStyle(ChatFormatting.BOLD),true);
                            }
                            livingEntity.hurt(BeyonderUtil.genericSource(owner, livingEntity), 80);
                            livingEntity.getPersistentData().putInt("inTwilight", Math.max(tag.getInt("inTwilight"), 25));
                        }
                    }
                } else if (caseInt == 2) {
                    aabb = new AABB(this.getX() - (scale * 1.1),this.getY() + (scale) - 5, this.getZ() - (scale * 0.66) - 5, this.getX() + (scale * 1.1), this.getY() + (scale * 2) + 5, this.getZ() - (scale * 0.66) + 5);
                    for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, aabb)) {
                        if (livingEntity != owner && !BeyonderUtil.areAllies(owner, livingEntity)) {
                            CompoundTag tag = livingEntity.getPersistentData();
                            tag.putUUID("ageUUID", owner.getUUID());
                            tag.putInt("age", tag.getInt("age") + 800);
                            if (livingEntity instanceof Player player) {
                                player.displayClientMessage(Component.literal("You are getting rapidly aged").withStyle(BeyonderUtil.ageStyle(livingEntity)).withStyle(ChatFormatting.BOLD),true);
                            }
                            livingEntity.hurt(BeyonderUtil.genericSource(owner, livingEntity), 80);
                            livingEntity.getPersistentData().putInt("inTwilight", Math.max(tag.getInt("inTwilight"), 25));
                        }
                    }

                } else if (caseInt == 3) {
                    aabb = new AABB(this.getX() - (scale * 0.66) - 5, this.getY() + (scale) - 5, this.getZ() - (scale * 1.1), this.getX() - (scale * 0.66) + 5, this.getY() + (scale * 2) + 5, this.getZ() + (scale * 1.1));
                    for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, aabb)) {
                        if (livingEntity != owner && !BeyonderUtil.areAllies(owner, livingEntity)) {
                            CompoundTag tag = livingEntity.getPersistentData();
                            tag.putUUID("ageUUID", owner.getUUID());
                            tag.putInt("age", tag.getInt("age") + 800);
                            if (livingEntity instanceof Player player) {
                                player.displayClientMessage(Component.literal("You are getting rapidly aged").withStyle(BeyonderUtil.ageStyle(livingEntity)).withStyle(ChatFormatting.BOLD),true);
                            }
                            livingEntity.hurt(BeyonderUtil.genericSource(owner, livingEntity), 80);
                            livingEntity.getPersistentData().putInt("inTwilight", Math.max(tag.getInt("inTwilight"), 25));
                        }
                    }
                }
            }
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            int chunkRadius = 8;
            ChunkPos centerChunk = new ChunkPos(this.blockPosition());
            for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                    ChunkPos chunkPos = new ChunkPos(centerChunk.x + dx, centerChunk.z + dz);
                    serverLevel.getChunkSource().addRegionTicket(TicketType.PLAYER, chunkPos, 3, chunkPos);
                }
            }
        }
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }

    public void destroyBlocksAround(int radius) {
        BlockPos centerPos = this.blockPosition();
        BlockState obsidianState = Blocks.OBSIDIAN.defaultBlockState();
        float obsidianHardness = obsidianState.getDestroySpeed(level(), centerPos);
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos targetPos = centerPos.offset(x, y, z);
                    BlockState blockState = level().getBlockState(targetPos);
                    if (blockState.getDestroySpeed(level(), targetPos) < obsidianHardness && !blockState.isAir() && !(blockState.getBlock() == Blocks.BEDROCK)) {
                        level().destroyBlock(targetPos, false);
                    }
                }
            }
        }
    }

    public void setYaw(float yaw) {
        this.entityData.set(YAW, yaw);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<SwordOfTwilightEntity> animationState) {
        AnimationController<SwordOfTwilightEntity> controller = animationState.getController();

        if (!this.entityData.get(HAS_PLAYED_ANIMATION)) {
            controller.setAnimation(RawAnimation.begin().then("swing", Animation.LoopType.PLAY_ONCE));
            this.entityData.set(HAS_PLAYED_ANIMATION, true);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
