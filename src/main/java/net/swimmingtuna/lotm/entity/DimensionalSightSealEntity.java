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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.BlockInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.EnvisionLocation;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.ClientShouldntRenderS2C;
import net.swimmingtuna.lotm.networking.packet.SendPlayerRenderDataS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ClientData.ClientIgnoreShouldntRenderData;
import org.jetbrains.annotations.NotNull;

public class DimensionalSightSealEntity extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Float> SEAL_X = SynchedEntityData.defineId(DimensionalSightSealEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SEAL_Y = SynchedEntityData.defineId(DimensionalSightSealEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SEAL_Z = SynchedEntityData.defineId(DimensionalSightSealEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> MAX_LIFE = SynchedEntityData.defineId(DimensionalSightSealEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SHOULD_MESSAGE = SynchedEntityData.defineId(DimensionalSightSealEntity.class, EntityDataSerializers.BOOLEAN);

    public DimensionalSightSealEntity(EntityType<? extends DimensionalSightSealEntity> entityType, Level level, boolean absorb) {
        super(entityType, level);

    }

    public DimensionalSightSealEntity(EntityType<DimensionalSightSealEntity> dimensionalSightSealEntityEntityType, Level level) {
        super(dimensionalSightSealEntityEntityType, level);
    }

    public boolean isOnFire() {
        return false;
    }

    protected float getInertia() {
        return 0.8f;
    }


    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("SealX", this.getSealX());
        compound.putFloat("SealY", this.getSealY());
        compound.putFloat("SealZ", this.getSealZ());
        compound.putInt("MaxLife", this.getMaxLife());
        compound.putBoolean("ShouldMessage", this.getShouldMessage());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setSealX(compound.getFloat("SealX"));
        this.setSealY(compound.getFloat("SealY"));
        this.setSealZ(compound.getFloat("SealZ"));
        this.setMaxLife(compound.getInt("MaxLife"));
        this.setShouldMessage(compound.getBoolean("ShouldMessage"));
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        return;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        return;
    }

    @Override
    public @NotNull ParticleOptions getTrailParticle() {
        return ParticleInit.NULL_PARTICLE.get();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        getEntityData().define(SEAL_X, 0.0f);
        getEntityData().define(SEAL_Y, 0.0f);
        getEntityData().define(SEAL_Z, 0.0f);
        getEntityData().define(MAX_LIFE, 200);
        getEntityData().define(SHOULD_MESSAGE, true);
    }


    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public boolean canHitEntity(Entity entity) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.getOwner() != null) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    int chunkRadius = 5;
                    ChunkPos centerChunk = new ChunkPos(new BlockPos((int) this.getSealX(), (int) this.getSealY(), (int) this.getSealZ()));
                    for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                        for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                            ChunkPos chunkPos = new ChunkPos(centerChunk.x + dx, centerChunk.z + dz);
                            serverLevel.getChunkSource().addRegionTicket(TicketType.PLAYER, chunkPos, 3, chunkPos);
                        }
                    }
                }
                Entity entity = this.getOwner();
                if (entity instanceof LivingEntity owner) {
                    if (this.distanceTo(owner) >= 20) {
                        Vec3 negLookVec5 = owner.getLookAngle().scale(-5);
                        BlockPos pos = owner.getOnPos();
                        this.teleportTo(pos.getX() + negLookVec5.x(), pos.getY() + negLookVec5.y(), pos.getZ() + negLookVec5.z());
                    } else if (this.distanceTo(owner) >= 5) {
                        double x = owner.getX() - this.getX();
                        double y = Math.min(5, owner.getY() - this.getY());
                        double z = owner.getZ() - this.getZ();
                        this.setDeltaMovement(x * 0.15, y * 0.15, z * 0.15);
                        this.hurtMarked = true;
                    }
                    if (this.tickCount == 1) {
                        BlockPos sealPos = new BlockPos((int) getSealX(), (int) getSealY(), (int) getSealZ());
                        int radius = 20;
                        for (int x = -radius; x <= radius; x++) {
                            for (int y = -radius; y <= radius; y++) {
                                for (int z = -radius; z <= radius; z++) {
                                    double distance = Math.sqrt(x * x + y * y + z * z);
                                    if (distance >= radius - 0.5 && distance <= radius + 0.5) {
                                        BlockPos blockPos = sealPos.offset(x, y, z);
                                        this.level().setBlock(blockPos, BlockInit.VOID_BLOCK.get().defaultBlockState(), 3);
                                    }
                                }
                            }
                        }
                    }
                    if (this.tickCount < this.getMaxLife()) {
                        if (this.tickCount % 60 == 0) {
                            BlockPos sealPos = new BlockPos((int) getSealX(), (int) getSealY(), (int) getSealZ());
                            int radius = 20;
                            for (int x = -radius; x <= radius; x++) {
                                for (int y = -radius; y <= radius; y++) {
                                    for (int z = -radius; z <= radius; z++) {
                                        double distance = Math.sqrt(x * x + y * y + z * z);
                                        if (distance >= radius - 0.5 && distance <= radius + 0.5) {
                                            BlockPos blockPos = sealPos.offset(x, y, z);
                                            this.level().setBlock(blockPos, BlockInit.VOID_BLOCK.get().defaultBlockState(), 3);
                                        }
                                    }
                                }
                            }
                        }
                    } else if (this.tickCount == this.getMaxLife()) {
                        BlockPos sealPos = new BlockPos((int) getSealX(), (int) getSealY(), (int) getSealZ());
                        int radius = 20;
                        for (int x = -radius; x <= radius; x++) {
                            for (int y = -radius; y <= radius; y++) {
                                for (int z = -radius; z <= radius; z++) {
                                    double distance = Math.sqrt(x * x + y * y + z * z);
                                    if (distance >= radius - 0.5 && distance <= radius + 0.5) {
                                        BlockPos blockPos = sealPos.offset(x, y, z);
                                        if (this.level().getBlockState(blockPos) == BlockInit.VOID_BLOCK.get().defaultBlockState()) {
                                            this.level().setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        this.discard();
                        if (this.getOwner() != null && this.getShouldMessage()) {
                            this.getOwner().sendSystemMessage(Component.literal("The entities trapped in your sealed space were let go due to too much time passing").withStyle(ChatFormatting.RED));
                        }
                    }
                    for (LivingEntity livingEntity : BeyonderUtil.checkEntitiesInLocation(owner, 20.0f, this.getSealX(), this.getSealY(), this.getSealZ())) {
                        if (livingEntity != owner && !BeyonderUtil.areAllies(owner, livingEntity)) {
                            livingEntity.getPersistentData().putInt("dimensionalSightSeal", 20);
                        }
                    }
                }
            }
        }
    }

    public static void dimensionalSightSealTick(LivingEntity livingEntity) {
        int value = ClientIgnoreShouldntRenderData.getIgnoreData(livingEntity.getUUID());
        if (value >= 1) {
            LOTMNetworkHandler.sendToAllPlayers(new ClientShouldntRenderS2C(livingEntity.getUUID(), value - 1));
        }
        if (livingEntity.getPersistentData().getInt("ignoreShouldntRender") >= 1) {
            if (livingEntity.getPersistentData().contains("dimensionalSightPlayerUUID") && livingEntity instanceof ServerPlayer) {
                LivingEntity living = BeyonderUtil.getLivingEntityFromUUID(livingEntity.level(), livingEntity.getPersistentData().getUUID("dimensionalSightPlayerUUID"));
                if (living instanceof ServerPlayer) {
                    Vec3 displayCenter = new Vec3(0, 0, 0);
                    SendPlayerRenderDataS2C packet = new SendPlayerRenderDataS2C(
                            livingEntity.getUUID(),
                            livingEntity.getYRot(),
                            livingEntity.getXRot(),
                            livingEntity.yHeadRot,
                            livingEntity.yBodyRot,
                            livingEntity.getDeltaMovement().x,
                            livingEntity.getDeltaMovement().y,
                            livingEntity.getDeltaMovement().z,
                            livingEntity.attackAnim,
                            livingEntity.getX(),
                            livingEntity.getY(),
                            livingEntity.getZ(),
                            livingEntity.onGround(),
                            livingEntity.fallDistance,
                            displayCenter,
                            livingEntity.isOnFire()
                    );
                    LOTMNetworkHandler.sendToAllPlayers(packet);
                }
            }
            livingEntity.getPersistentData().putInt("ignoreShouldntRender", livingEntity.getPersistentData().getInt("ignoreShouldntRender") - 1);
        }
        if (livingEntity.getPersistentData().getInt("dimensionalSightSeal") >= 1) {
            livingEntity.getPersistentData().putInt("dimensionalSightSeal", livingEntity.getPersistentData().getInt("dimensionalSightSeal") - 1);
        }
        if (livingEntity.getPersistentData().getInt("dimensionalSightSealTeleportTimer") >= 1) {
            livingEntity.getPersistentData().putInt("dimensionalSightSealTeleportTimer", 0);
            int x = livingEntity.getPersistentData().getInt("dimensionalSightSealX");
            int y = livingEntity.getPersistentData().getInt("dimensionalSightSealY");
            int z = livingEntity.getPersistentData().getInt("dimensionalSightSealZ");
            livingEntity.getPersistentData().putInt("dimensionalSightSealTeleportBackTimer", 1);
            EnvisionLocation.envisionLocationTeleport(livingEntity, x, y, z);
        }
        if (livingEntity.getPersistentData().getInt("dimensionalSightSealTeleportBackTimer") >= 1) {
            int x1 = livingEntity.getPersistentData().getInt("dimensionalSightSealX");
            int y1 = livingEntity.getPersistentData().getInt("dimensionalSightSealY");
            int z1 = livingEntity.getPersistentData().getInt("dimensionalSightSealZ");
            BlockPos sealPos = new BlockPos(x1, y1, z1);
            int x2 = livingEntity.getPersistentData().getInt("dimensionalSightSealBackX");
            int y2 = livingEntity.getPersistentData().getInt("dimensionalSightSealBackY");
            int z2 = livingEntity.getPersistentData().getInt("dimensionalSightSealBackZ");
            for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(20))) {
                EnvisionLocation.envisionLocationTeleport(living, x2, y2, z2);
            }
            int radius = 20;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        double distance = Math.sqrt(x * x + y * y + z * z);
                        if (distance >= radius - 0.5 && distance <= radius + 0.5) {
                            BlockPos blockPos = sealPos.offset(x, y, z);
                            if (livingEntity.level().getBlockState(blockPos) == BlockInit.VOID_BLOCK.get().defaultBlockState()) {
                                livingEntity.level().setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }
            livingEntity.getPersistentData().putInt("dimensionalSightSealTeleportBackTimer", 0);
        }
    }

    public float getSealX() {
        return this.entityData.get(SEAL_X);
    }

    public float getSealY() {
        return this.entityData.get(SEAL_Y);
    }

    public float getSealZ() {
        return this.entityData.get(SEAL_Z);
    }

    public void setSealX(float pos) {
        this.entityData.set(SEAL_X, pos);
    }

    public void setSealY(float pos) {
        this.entityData.set(SEAL_Y, pos);
    }

    public void setSealZ(float pos) {
        this.entityData.set(SEAL_Z, pos);
    }

    public int getMaxLife() {
        return this.entityData.get(MAX_LIFE);
    }

    public void setMaxLife(int maxLife) {
        this.entityData.set(MAX_LIFE, maxLife);
    }

    public boolean getShouldMessage() {
        return this.entityData.get(SHOULD_MESSAGE);
    }

    public void setShouldMessage(boolean shouldMessage) {
        this.entityData.set(SHOULD_MESSAGE, shouldMessage);
    }
}
