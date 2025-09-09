package net.swimmingtuna.lotm.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.capabilities.sealed_data.ABILITIES_SEAL_TYPES;
import net.swimmingtuna.lotm.capabilities.sealed_data.SealedUtils;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.util.BeyonderUtil;

import java.util.UUID;

public class SpatialCageEntity extends Entity{

    private static final EntityDataAccessor<Float> WIDTH = SynchedEntityData.defineId(SpatialCageEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HEIGHT = SynchedEntityData.defineId(SpatialCageEntity.class, EntityDataSerializers.FLOAT);

    public SpatialCageEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public SpatialCageEntity(Level level, LivingEntity target){
        this(EntityInit.SPATIAL_CAGE_ENTITY.get(), level);
        this.entityData.set(WIDTH, target.getBbWidth());
        this.entityData.set(HEIGHT, target.getBbHeight());
    }

    @Override
    public void tick(){
        super.tick();
        if(!this.level().isClientSide()) if (tickCount > 1) this.discard();
    }

    public float getBBWidth(){
        return this.entityData.get(WIDTH);
    }

    public float getBBHeight(){
        return this.entityData.get(HEIGHT);
    }

    public static void cageTick(LivingEntity entity){
        Level level = entity.level();
        CompoundTag tag = entity.getPersistentData();

        if(level.isClientSide || !tag.getBoolean("spatialCageIsSealed")) return;


        if(tag.contains("spatialCageSealUUID") && SealedUtils.hasSpecificSeal(entity, tag.getUUID("spatialCageSealUUID"))) {
            double x = tag.getDouble("spatialCageX");
            double y = tag.getDouble("spatialCageY");
            double z = tag.getDouble("spatialCageZ");

            entity.teleportTo(x, y, z);
            entity.setInvisible(true);
            BeyonderUtil.setInvisible(entity, true, 5);
            entity.fallDistance = 0;
            entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 10, 1 ,false ,false));
            SpatialCageEntity cage = new SpatialCageEntity(level, entity);
            cage.moveTo(x, y, z);
            level.addFreshEntity(cage);
        }
        else {
            unsetSealed(entity);
        }
    }

    public static void setSealed(LivingEntity entity, LivingEntity user, int sequence, int time){
        if(entity.level().isClientSide) return;

        UUID sealUUID = SealedUtils.seal(entity, user.getUUID(), sequence, time, ABILITIES_SEAL_TYPES.ALL, null, false, null);

        CompoundTag tag = entity.getPersistentData();
        tag.putBoolean("spatialCageIsSealed", true);
        tag.putDouble("spatialCageX", entity.getX());
        tag.putDouble("spatialCageY", entity.getY());
        tag.putDouble("spatialCageZ", entity.getZ());
        tag.putUUID("spatialCageSealUUID", sealUUID);

        EventManager.addToRegularLoop(entity, EFunctions.SPATIAL_CAGE.get());
    }

    public static void unsetSealed(LivingEntity entity){
        if(entity.level().isClientSide) return;

        entity.setInvisible(false);

        CompoundTag tag = entity.getPersistentData();
        UUID sealUUID = tag.getUUID("spatialCageSealUUID");
        SealedUtils.removeSeal(entity, sealUUID);
        tag.putBoolean("spatialCageIsSealed", false);
        tag.remove("spatialCageIsSealed");
        tag.remove("spatialCageX");
        tag.remove("spatialCageY");
        tag.remove("spatialCageZ");

        EventManager.removeFromRegularLoop(entity, EFunctions.SPATIAL_CAGE.get());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(WIDTH, 1f);
        this.entityData.define(HEIGHT, 1f);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if(tag.contains("width")){
            this.entityData.set(WIDTH, tag.getFloat("width"));
        }
        if(tag.contains("height")){
            this.entityData.set(HEIGHT, tag.getFloat("height"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("width", entityData.get(WIDTH));
        tag.putFloat("height", entityData.get(HEIGHT));
    }
}