package net.swimmingtuna.lotm.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.UpdateEntityLocationS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.Random;

public class StoneEntity extends AbstractArrow {

    private static final EntityDataAccessor<Integer> DATA_BB = SynchedEntityData.defineId(StoneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STONE_DAMAGE = SynchedEntityData.defineId(StoneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(StoneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_STONE_XROT = SynchedEntityData.defineId(StoneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STONE_YROT = SynchedEntityData.defineId(StoneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_STONE_STAYATX = SynchedEntityData.defineId(StoneEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_STONE_STAYATY = SynchedEntityData.defineId(StoneEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_STONE_STAYATZ = SynchedEntityData.defineId(StoneEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> REMOVE_AND_HURT = SynchedEntityData.defineId(StoneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SENT = SynchedEntityData.defineId(StoneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHOULDNT_DAMAGE = SynchedEntityData.defineId(StoneEntity.class, EntityDataSerializers.BOOLEAN);

    public StoneEntity(EntityType<? extends StoneEntity> entityType, Level level) {
        super(entityType, level);
    }

    public StoneEntity(Level level, LivingEntity shooter) {
        super(EntityInit.STONE_ENTITY.get(), shooter, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STONE_DAMAGE, 10);
        this.entityData.define(DATA_DANGEROUS, false);
        this.entityData.define(REMOVE_AND_HURT, false);
        this.entityData.define(SENT, false);
        this.entityData.define(SHOULDNT_DAMAGE, false);
        this.entityData.define(DATA_STONE_XROT, 0);
        this.entityData.define(DATA_STONE_STAYATX, 0.0f);
        this.entityData.define(DATA_STONE_STAYATY, 0.0f);
        this.entityData.define(DATA_STONE_STAYATZ, 0.0f);
        this.entityData.define(DATA_STONE_YROT, 0);
        this.entityData.define(DATA_BB, 6);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("xxRot")) {
            this.setStoneXRot(compound.getInt("xxRot"));
        }
        if (compound.contains("yyRot")) {
            this.setStoneYRot(compound.getInt("yyRot"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("xxRot", this.getStoneXRot());
        compound.putInt("yyRot", this.getStoneYRot());
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide && !(result.getEntity() instanceof StoneEntity) && !(result.getEntity() instanceof LavaEntity) && !getShouldntDamage()) {
            Vec3 hitPos = result.getLocation();
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);
            this.level().explode(this, hitPos.x, hitPos.y, hitPos.z, (5.0f * scaleData.getScale() / 3), Level.ExplosionInteraction.TNT);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.AMBIENT, 5.0F, 5.0F);
            if (!getRemoveAndHurt()) {
                if (this.getOwner() != null && this.getOwner() instanceof LivingEntity owner) {
                    if (result.getEntity() instanceof LivingEntity entity && entity != owner) {
                        if (this.getOwner() == null) {
                            entity.hurt(BeyonderUtil.genericSource(this, entity), getDamage() * scaleData.getScale());
                        } else {
                            entity.hurt(BeyonderUtil.genericSource(this.getOwner(), entity), getDamage() * scaleData.getScale());
                        }
                    }
                }
            }
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide) {
            if (!getRemoveAndHurt() && !getShouldntDamage()) {
                Random random = new Random();
                if (random.nextInt(10) == 1) {
                    this.level().broadcastEntityEvent(this, (byte) 3);
                    this.level().setBlock(blockPosition(), Blocks.STONE.defaultBlockState(), 3);
                }
            } else if (getRemoveAndHurt()) {
                BeyonderUtil.destroyBlocksInSphere(this, result.getBlockPos(), 12, 30);
            }
            this.discard();
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
        this.setOldPosAndRot();
        int xRot = this.getStoneXRot();
        int yRot = this.getStoneXRot();
        this.setXRot(this.getXRot() + xRot);
        this.setYRot(this.getYRot() + yRot);
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        if (!this.level().isClientSide() && this.tickCount > 100 && !getRemoveAndHurt()) {
            this.discard();
        }
        if (!this.level().isClientSide()) {
            Vec3 currentPos = this.position();
            if (this.getSent()) {
                for (ServerPlayer player : level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(100))) {
                    LOTMNetworkHandler.sendToPlayer(new UpdateEntityLocationS2C(currentPos.x(), currentPos.y(), currentPos.z(), this.getDeltaMovement().x(), this.getDeltaMovement().y(), this.getDeltaMovement().z(), this.getId()), player);
                }
            }
            if (getRemoveAndHurt()) {
                if (!getSent() && this.getOwner() != null) {
                    this.hurtMarked = true;
                    this.setDeltaMovement(this.getOwner().getX() - this.getX() + getStoneStayAtX(), this.getOwner().getY() - this.getY() + getStoneStayAtY(), this.getOwner().getZ() - this.getZ() + getStoneStayAtX());
                }
                BlockPos entityPos = this.blockPosition();
                int amount = 0;
                for (int x = -2; x <= 2; x++) {
                    for (int y = -2; y <= 2; y++) {
                        for (int z = -2; z <= 2; z++) {
                            BlockPos pos = entityPos.offset(x, y, z);
                            BlockState state = this.level().getBlockState(pos);
                            Block block = state.getBlock();
                            float blockStrength = block.defaultDestroyTime();
                            float bedrockStrength = Blocks.BEDROCK.defaultDestroyTime();
                            if (blockStrength <= bedrockStrength) {
                                this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                            }
                        }
                    }
                }
                for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(getBB()))) {
                    if (entity != this.getOwner()) {
                        entity.hurt(BeyonderUtil.genericSource(this.getOwner(), entity), this.getDamage());
                        BeyonderUtil.destroyBlocksInSphere(entity, entity.getOnPos(), 9,0);
                        this.discard();
                    }
                }
                if (this.tickCount >= 160) {
                    this.discard();
                }
            }
        }
    }


    public void setStoneXRot(int xRot) {
        this.entityData.set(DATA_STONE_XROT, xRot);
    }

    public void setStoneYRot(int yRot) {
        this.entityData.set(DATA_STONE_YROT, yRot);
    }

    public int getStoneXRot() {
        return this.entityData.get(DATA_STONE_XROT);
    }

    public void setDamage(int damage) {
        this.entityData.set(DATA_STONE_DAMAGE, damage);
    }

    public int getDamage() {
        return this.entityData.get(DATA_STONE_DAMAGE);
    }

    public void setBB(int bb) {
        this.entityData.set(DATA_BB, bb);
    }

    public int getBB() {
        return this.entityData.get(DATA_BB);
    }

    public float getStoneStayAtX() {
        return this.entityData.get(DATA_STONE_STAYATX);
    }

    public float getStoneStayAtY() {
        return this.entityData.get(DATA_STONE_STAYATY);
    }

    public float getStoneStayAtZ() {
        return this.entityData.get(DATA_STONE_STAYATZ);
    }

    public void setStoneStayAtX(float stayAtX) {
        this.entityData.set(DATA_STONE_STAYATX, stayAtX);
    }

    public void setStoneStayAtY(float stayAtY) {
        this.entityData.set(DATA_STONE_STAYATY, stayAtY);
    }

    public void setStoneStayAtZ(float stayAtZ) {
        this.entityData.set(DATA_STONE_STAYATZ, stayAtZ);
    }


    public int getStoneYRot() {
        return this.entityData.get(DATA_STONE_YROT);
    }

    public void setRemoveAndHurt(boolean removeAndHurt) {
        this.entityData.set(REMOVE_AND_HURT, removeAndHurt);
    }

    public boolean getRemoveAndHurt() {
        return this.entityData.get(REMOVE_AND_HURT);
    }

    public void setSent(boolean sent) {
        this.entityData.set(SENT, sent);
    }

    public boolean getSent() {
        return this.entityData.get(SENT);
    }

    public void setShouldntDamage(boolean shouldntDamage) {
        this.entityData.set(SHOULDNT_DAMAGE, shouldntDamage);
    }

    public boolean getShouldntDamage() {
        return this.entityData.get(SHOULDNT_DAMAGE);
    }

    public void setTickCount(int tickCount) {
        this.tickCount = tickCount;
    }


    public static void summonStoneRandom(LivingEntity livingEntity) {
        StoneEntity stoneEntity = new StoneEntity(EntityInit.STONE_ENTITY.get(), livingEntity.level());
        int random = (int) ((Math.random() * 40) - 20);
        stoneEntity.teleportTo(random, random, random);
        stoneEntity.setDeltaMovement(0, -2, 0);
        stoneEntity.setStoneXRot(4);
        stoneEntity.setStoneYRot(4);
        stoneEntity.setShouldntDamage(false);
        stoneEntity.setTickCount(80);
        livingEntity.level().addFreshEntity(stoneEntity);
    }
}