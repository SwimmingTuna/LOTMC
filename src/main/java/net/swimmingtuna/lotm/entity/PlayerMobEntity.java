package net.swimmingtuna.lotm.entity;


import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.beyonder.*;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.client.Configs;
import net.swimmingtuna.lotm.init.*;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.EntityUtil.behaviour.GroupBeyondersBehaviour;
import net.swimmingtuna.lotm.util.EntityUtil.behaviour.GroupTargetBehaviour;
import net.swimmingtuna.lotm.util.EntityUtil.behaviour.PassiveAttackBehavior;
import net.swimmingtuna.lotm.util.EntityUtil.behaviour.task.*;
import net.swimmingtuna.lotm.util.PlayerMobs.ItemManager;
import net.swimmingtuna.lotm.util.PlayerMobs.NameManager;
import net.swimmingtuna.lotm.util.PlayerMobs.PlayerName;
import net.swimmingtuna.lotm.util.PlayerMobs.ProfileUpdater;
import net.swimmingtuna.lotm.world.worlddata.BeyonderEntityData;
import net.swimmingtuna.lotm.world.worlddata.PlayerMobTracker;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class PlayerMobEntity extends Monster implements RangedAttackMob, CrossbowAttackMob, SmartBrainOwner<PlayerMobEntity> {

    public enum ColorMode {
        NORMAL,
        GRAY,
        BLACK,
        RED,
        BLUE,
        YELLOW,
        PURPLE,
        GREEN
    }

    @Nullable
    private GameProfile profile;
    @Nullable
    private ResourceLocation skin;
    @Nullable
    private ResourceLocation cape;
    private boolean skinAvailable;
    private boolean capeAvailable;


    public double xCloakO;
    public double yCloakO;
    public double zCloakO;
    public double xCloak;
    public double yCloak;
    public double zCloak;

    private static final UUID BABY_SPEED_BOOST_ID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    private static final AttributeModifier BABY_SPEED_BOOST = new AttributeModifier(BABY_SPEED_BOOST_ID, "Baby speed boost", 0.5D, AttributeModifier.Operation.MULTIPLY_BASE);

    private static final EntityDataAccessor<String> PATHWAY = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> MENTAL_STRENGTH = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_CHILD = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> REGEN_SPIRITUALITY = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> CREATOR_UUID = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> NAME = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> SEQUENCE = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_LIFE = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SPIRITUALITY = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SPIRITUALITY_REGEN = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAXSPIRITUALITY = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_CLONE = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_CHANCE = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DISTANCE_FROM_TARGET = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> FLY_SPEED = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> HAS_ABILITY_CAP = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MAX_ABILITIES_USE = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> COLOR_MODE = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.STRING);


    private boolean canBreakDoors;
    private final BreakDoorGoal breakDoorGoal = new BreakDoorGoal(this, (difficulty) -> difficulty == Difficulty.HARD);
    private final BeyonderRangedAttackGoal<PlayerMobEntity> bowAttackGoal = new BeyonderRangedAttackGoal<>(this, 1.0D, 20, 15.0F);
    private final BeyonderRangedCrossbowAttackGoal<PlayerMobEntity> crossbowAttackGoal = new BeyonderRangedCrossbowAttackGoal<>(this, 1.0D, 15.0F);


    public PlayerMobEntity(Level worldIn, BeyonderClass requiredClass, int sequence) {
        this(EntityInit.PLAYER_MOB_ENTITY.get(), worldIn, requiredClass, sequence, 1);
    }

    public PlayerMobEntity(EntityType<? extends Monster> entityType, Level worldIn, BeyonderClass requiredClass, int sequence, int attack_chance) {
        super(entityType, worldIn);
        this.setSequence(sequence);
        this.setPathway(requiredClass);
        setAttackChance(attack_chance);
    }

    public PlayerMobEntity(EntityType<PlayerMobEntity> playerMobEntityEntityType, Level level) {
        super(playerMobEntityEntityType, level);
    }

    public boolean shouldIgnoreGamerule() {
        if (this.getIsClone()) {
            return true;
        }
        if (this.getPersistentData().getBoolean("canAttackOwner")) {
            return true;
        }
        return false;
    }

    public static AttributeSupplier.Builder registerAttributes() {
        return LivingEntity.createLivingAttributes().add
                (Attributes.MAX_HEALTH, 20).add
                (Attributes.ATTACK_KNOCKBACK).add
                (Attributes.FOLLOW_RANGE, 50f).add
                (Attributes.ARMOR, 3.0D).add
                (Attributes.ATTACK_DAMAGE, 2f).add
                (Attributes.MOVEMENT_SPEED, 0.250f);

    }

    private boolean targetTwin(LivingEntity livingEntity) {
        return Configs.COMMON.attackTwin.get() || !(livingEntity instanceof Player && livingEntity.getName().getString().equals(getUsername().getDisplayName()));
    }

    private boolean canOpenDoor() {
        return Configs.COMMON.openDoors.get() && level().getDifficulty().getId() >= Configs.COMMON.openDoorsDifficulty.get().getId();
    }

    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        addBehaviourGoals();
    }

    private void addBehaviourGoals() {
        if (canOpenDoor()) {
            goalSelector.addGoal(1, new OpenDoorGoal(this, true));
            ((GroundPathNavigation) getNavigation()).setCanOpenDoors(true);
        }

        goalSelector.addGoal(3, new BeyonderMeleeAttackGoal(this, 1.2D, false));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        targetSelector.addGoal(1, new BeyonderAwareHurtByTargetGoal(this, ZombifiedPiglin.class));
        //targetSelector.addGoal(2, new BeyonderNearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::targetTwin));
        //targetSelector.addGoal(3, new BeyonderNearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        getEntityData().define(NAME, "");
        getEntityData().define(PATHWAY, "");
        getEntityData().define(IS_CHILD, false);
        getEntityData().define(IS_CHARGING_CROSSBOW, false);
        getEntityData().define(FLYING, false);
        getEntityData().define(SEQUENCE, -1);
        getEntityData().define(DISTANCE_FROM_TARGET, 0);
        getEntityData().define(MENTAL_STRENGTH, 10);
        getEntityData().define(MAXSPIRITUALITY, 100);
        getEntityData().define(SPIRITUALITY, 0);
        getEntityData().define(SPIRITUALITY_REGEN, 0);
        getEntityData().define(MAX_LIFE, 0);
        getEntityData().define(IS_CLONE, false);
        getEntityData().define(REGEN_SPIRITUALITY, false);
        getEntityData().define(ATTACK_CHANCE, 0);
        getEntityData().define(FLY_SPEED, 1.0f);
        getEntityData().define(CREATOR_UUID, Optional.empty());
        getEntityData().define(MAX_ABILITIES_USE, 1);
        getEntityData().define(HAS_ABILITY_CAP, false);
        getEntityData().define(COLOR_MODE, ColorMode.NORMAL.name());

    }


    @Override
    public void rideTick() {
        super.rideTick();
        if (getVehicle() instanceof PathfinderMob mob) {
            yBodyRot = mob.yBodyRot;
        }
    }

    public static boolean isCopyOf(LivingEntity livingEntity, LivingEntity copy) {
        if (copy instanceof PlayerMobEntity playerMobEntity) {
            if (playerMobEntity.getIsClone() && playerMobEntity.getCreator() != null) {
                if (playerMobEntity.getCreator().getUUID().equals(livingEntity.getUUID())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isCopy(LivingEntity living) {
        if (living instanceof PlayerMobEntity playerMob) {
            if (playerMob.getIsClone() && playerMob.getCreator() != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);
        boolean force = Configs.COMMON.forceSpawnItem.get();
        if (force || random.nextFloat() < (level().getDifficulty() == Difficulty.HARD ? 0.5F : 0.1F)) {
            ItemStack stack = ItemManager.INSTANCE.getRandomMainHand(random);
            setItemSlot(EquipmentSlot.MAINHAND, stack);
            if (level().getDifficulty().getId() >= Configs.COMMON.offhandDifficultyLimit.get().getId() && random.nextDouble() > Configs.COMMON.offhandSpawnChance.get()) {
                if (stack.getItem() instanceof ProjectileWeaponItem && Configs.COMMON.allowTippedArrows.get()) {
                    var potions = new ArrayList<>(ForgeRegistries.POTIONS.getKeys());
                    potions.removeAll(Configs.COMMON.tippedArrowBlocklist);
                    if (!potions.isEmpty()) {
                        var potion = ForgeRegistries.POTIONS.getValue(potions.get(random.nextInt(potions.size())));
                        setItemSlot(EquipmentSlot.OFFHAND, PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), potion));
                    }
                } else {
                    setItemSlot(EquipmentSlot.OFFHAND, ItemManager.INSTANCE.getRandomOffHand(random));
                    getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("Shield Bonus", random.nextDouble() * 3.0 + 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            }
        }
    }

    @Override
    @NotNull
    protected Brain.Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    @NotNull
    public Iterable<ItemStack> getArmorSlots() {
        return List.of();
    }


    @Override
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        super.setItemSlot(equipmentSlot, itemStack);
    }

    @Override
    public @NotNull ItemStack getItemBySlot(EquipmentSlot slot) {
        return super.getItemBySlot(slot);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (IS_CHILD.equals(key)) {
            refreshDimensions();
        }

        super.onSyncedDataUpdated(key);
    }

    @Override
    public int getExperienceReward() {
        if (isBaby()) {
            xpReward = (int) ((float) xpReward * 2.5F);
        }

        return super.getExperienceReward();
    }

    @Override
    public float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return isBaby() ? 0.93F : 1.62F;
    }

    @Override
    public boolean isFallFlying() {
        return false;
    }

    @Override
    public double getMyRidingOffset() {
        return isBaby() ? 0.0D : -0.45D;
    }

    @Override
    public boolean canHoldItem(ItemStack stack) {
        return (stack.getItem() != Items.EGG || !isBaby() || !isPassenger()) && super.canHoldItem(stack);
    }

    @Override
    public void tick() {
        CompoundTag tag = this.getPersistentData();
        if (!this.level().isClientSide()) {
            if (this.tickCount % 59 == 0) {
                if (this.getIsClone() && this.getCreator() != null && this.getCreator().isAlive()) {
                    if (!this.getPersistentData().getBoolean("canAttackOwner")) {
                        BeyonderUtil.forceAlly(this, this.getCreator());
                    }
                }
            }
            if (this.tickCount % 5 == 0) {
                if (this.getPersistentData().getBoolean("canAttackOwner")) {
                    if (this.getCreator() != null && this.getCreator().isAlive()) {
                        this.setTarget(this.getCreator());
                        if (BeyonderUtil.areAllies(this, this.getCreator())) {
                            BeyonderUtil.forceRemoveAlly(this.getCreator(), this);
                        }

                    }
                }
                if (this.getPersistentData().getBoolean("shouldFlicker")) {
                    if (!this.getIsClone()) {
                        this.remove(RemovalReason.DISCARDED);
                    }
                    if (this.getCreator() != null) {
                        LivingEntity player = this.getCreator();
                        for (EquipmentSlot slot : EquipmentSlot.values()) {
                            this.setItemSlot(slot, player.getItemBySlot(slot).copy());
                        }
                        CompoundTag playerData = player.getPersistentData();
                        CompoundTag cloneData = this.getPersistentData();
                        cloneData.merge(playerData.copy());
                        for (Attribute attribute : ForgeRegistries.ATTRIBUTES.getValues()) {
                            AttributeInstance playerAttribute = player.getAttribute(attribute);
                            AttributeInstance cloneAttribute = this.getAttribute(attribute);
                            if (playerAttribute != null && cloneAttribute != null) {
                                cloneAttribute.setBaseValue(playerAttribute.getBaseValue());
                                for (AttributeModifier modifier : playerAttribute.getModifiers()) {
                                    if (!cloneAttribute.hasModifier(modifier)) {
                                        cloneAttribute.addPermanentModifier(modifier);
                                    }
                                }
                            }
                        }
                        BeyonderUtil.setScale(this, BeyonderUtil.getScale(player));
                        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(player.getMaxHealth());
                        this.setHealth(player.getHealth());
                        for (MobEffectInstance effect : player.getActiveEffects()) {
                            this.addEffect(new MobEffectInstance(effect));
                        }

                        Set<String> playerTags = player.getTags();
                        for (String tag2 : playerTags) {
                            this.addTag(tag2);
                        }
                        if (BeyonderUtil.canFly(player)) {
                            BeyonderUtil.startFlying(this, Math.max(0.08f, player.getPersistentData().getFloat("LOTMFlySpeed")), 10);
                        }
                        this.getCreator().getPersistentData().putInt("ignoreShouldntRender", 10);
                    }
                    BeyonderUtil.setInvisible(this, true, 1);
                    this.getPersistentData().putInt("ignoreShouldntRender", 10);
                }
            }
            if (this.tickCount % 200 == 0) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    PlayerMobTracker tracker = PlayerMobTracker.get(serverLevel);
                    tracker.updatePlayerMobPosition(this);
                }
            }
            //if (this.tickCount == 10 && this.getCurrentPathway() != null && this.getCurrentSequence() != -1) {
            //    BeyonderHolder.updateMaxHealthModifier(this, this.getCurrentPathway().maxHealth().get(getCurrentSequence()));
            //}
            if (!this.level().getLevelData().getGameRules().getBoolean(GameRuleInit.NPC_SHOULD_SPAWN) && !shouldIgnoreGamerule()) {
                this.discard();
            }
            if (this.getRegenSpirituality() && this.getCurrentPathway() != null && this.getCurrentSequence() != -1) {
                int sequence = this.getCurrentSequence();
                RandomSource random = this.getRandom();
                double increase = ((Mth.nextDouble(random, 0.1, 1.0) * (this.getCurrentPathway().spiritualityRegen().get(sequence) * 1.5f)) / 5) * 20.0;
                BeyonderUtil.addSpirituality(this, (int) increase);
            }
            if (this.getSpirituality() < this.getMaxSpirituality() / 10 && this.tickCount >= 10 && !this.getRegenSpirituality()) {
                this.remove(RemovalReason.DISCARDED);
            }
        }
        super.tick();
        if (!this.level().isClientSide()) {

            if (this.getPersistentData().getInt("playerMobAbilityCooldown") == 0) {
                if (this.getCurrentPathway() != null) {
                    this.getPersistentData().putInt("playerMobAbilityCooldown", 30 + (this.getCurrentSequence() * 3));
                } else {
                    this.getPersistentData().putInt("playerMobAbilityCooldown", 60);
                }
                BeyonderEntityData.selectAndUseAbility(this);
            } else {
                this.getPersistentData().putInt("playerMobAbilityCooldown", this.getPersistentData().getInt("playerMobAbilityCooldown") - 1);
            }

            if (getMaxlife() != 0) {
                if (this.tickCount > getMaxlife()) {
                    if (this.getPersistentData().getBoolean("shouldDropWormOfStar")) {
                        ItemEntity wormOfStarEntity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack(ItemInit.WORM_OF_STAR.get()));
                        this.level().addFreshEntity(wormOfStarEntity);
                    }
                    this.discard();
                }
            }
            if (this.getIsFlying()) {
                this.fallDistance = 0.0F;
                LivingEntity target = this.getTarget();
                if (target != null) {
                    double idealDistance = this.getIdealDistanceFromTarget();
                    double currentDistance = this.distanceTo(target);
                    double deltaX = this.getX() - target.getX();
                    double deltaY = this.getY() - target.getY();
                    double deltaZ = this.getZ() - target.getZ();
                    double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
                    if (distance > 0.1) {
                        deltaX /= distance;
                        deltaY /= distance;
                        deltaZ /= distance;
                        double targetX = target.getX() + deltaX * idealDistance;
                        double targetY = target.getY() + deltaY * idealDistance + 3.0;
                        double targetZ = target.getZ() + deltaZ * idealDistance;
                        double moveStrength = this.getFlySpeed();
                        if (Math.abs(currentDistance - idealDistance) > 1.0) {
                            double moveX = (targetX - this.getX()) * moveStrength;
                            double moveY = (targetY - this.getY()) * moveStrength;
                            double moveZ = (targetZ - this.getZ()) * moveStrength;
                            this.setDeltaMovement(this.getDeltaMovement().add(moveX, moveY, moveZ));
                            this.hurtMarked = true;
                        }
                    }
                } else {
                    if (this.getDeltaMovement().y < 0) {
                        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.5, 1.0));
                        this.hurtMarked = true;
                    }
                }
            }
        }
        xCloakO = xCloak;
        yCloakO = yCloak;
        zCloakO = zCloak;
        double x = getX() - xCloak;
        double y = getY() - yCloak;
        double z = getZ() - zCloak;
        double maxCapeAngle = 10.0D;
        if (x > maxCapeAngle) {
            xCloak = getX();
            xCloakO = xCloak;
        }

        if (z > maxCapeAngle) {
            zCloak = getZ();
            zCloakO = zCloak;
        }

        if (y > maxCapeAngle) {
            yCloak = getY();
            yCloakO = yCloak;
        }

        if (x < -maxCapeAngle) {
            xCloak = getX();
            xCloakO = xCloak;
        }

        if (z < -maxCapeAngle) {
            zCloak = getZ();
            zCloakO = zCloak;
        }

        if (y < -maxCapeAngle) {
            yCloak = getY();
            yCloakO = yCloak;
        }

        xCloak += x * 0.25D;
        zCloak += z * 0.25D;
        yCloak += y * 0.25D;
    }

    @Override
    public LivingEntity getTarget() {
        return BrainUtils.getMemory(this, MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target == null) {
            BrainUtils.clearMemory(this, MemoryModuleType.ATTACK_TARGET);
        } else {
            BrainUtils.setMemory(this, MemoryModuleType.ATTACK_TARGET, target);
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (BeyonderUtil.areAllies(target, this)) {
            return false;
        }
        if (this.getPersistentData().getBoolean("canAttackOwner")) {
            return true;
        } else if (this.getIsClone() && ((this.getCreator() != null && target == this.getCreator()) || this.getUsername().getDisplayName() == target.getScoreboardName())) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    public boolean doHurtTarget(Entity entityIn) {
        boolean result = super.doHurtTarget(entityIn);
        if (result)
            swing(InteractionHand.MAIN_HAND);
        return result;
    }

    @Override
    public boolean isBaby() {
        return getEntityData().get(IS_CHILD);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setBaby(boolean isChild) {
        super.setBaby(isChild);
        getEntityData().set(IS_CHILD, isChild);
        if (!level().isClientSide) {
            AttributeInstance attribute = getAttribute(Attributes.MOVEMENT_SPEED);
            attribute.removeModifier(BABY_SPEED_BOOST);
            if (isChild) {
                attribute.addTransientModifier(BABY_SPEED_BOOST);
            }
        }
    }

    @Override
    protected void onOffspringSpawnedFromEgg(Player player, Mob child) {
        if (child instanceof PlayerMobEntity) {
            ((PlayerMobEntity) child).setUsername(getUsername());
        }
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        RandomSource randomSource = level.getRandom();
        populateDefaultEquipmentSlots(randomSource, difficulty);
        populateDefaultEquipmentEnchantments(randomSource, difficulty);

        if (!hasUsername())
            setUsername(NameManager.INSTANCE.getRandomName());

        setCombatTask();
        float specialMultiplier = difficulty.getSpecialMultiplier();
        setCanPickUpLoot(randomSource.nextFloat() < Configs.COMMON.pickupItemsChance.get() * specialMultiplier);
        setCanBreakDoors(randomSource.nextFloat() < specialMultiplier * 0.1F);

        double rangeBonus = randomSource.nextDouble() * 1.5 * specialMultiplier;
        if (rangeBonus > 1.0)
            getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(new AttributeModifier("Range Bonus", rangeBonus, AttributeModifier.Operation.MULTIPLY_TOTAL));

        if (randomSource.nextFloat() < specialMultiplier * 0.05F)
            getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("Health Bonus", randomSource.nextDouble() * 3.0 + 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));

        if (randomSource.nextFloat() < specialMultiplier * 0.15F)
            getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier("Damage Bonus", randomSource.nextDouble() + 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));

        if (randomSource.nextFloat() < specialMultiplier * 0.2F)
            getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier("Speed Bonus", randomSource.nextDouble() * 2.0 * 0.24 + 0.01, AttributeModifier.Operation.MULTIPLY_TOTAL));

        if (randomSource.nextDouble() < Configs.COMMON.babySpawnChance.get())
            setBaby(true);

        return spawnData;
    }

    public void setCombatTask() {
        if (!level().isClientSide) {
            goalSelector.removeGoal(bowAttackGoal);
            goalSelector.removeGoal(crossbowAttackGoal);

            ItemStack itemstack = getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, this::canFireProjectileWeapon));
            if (itemstack.getItem() instanceof CrossbowItem) {
                goalSelector.addGoal(2, crossbowAttackGoal);
            } else if (itemstack.getItem() instanceof BowItem) {
                bowAttackGoal.setMinAttackInterval(level().getDifficulty() != Difficulty.HARD ? 20 : 40);
                goalSelector.addGoal(2, bowAttackGoal);
            }
        }
    }

    public void setCanBreakDoors(boolean enabled) {
        if (GoalUtils.hasGroundPathNavigation(this)) {
            if (canBreakDoors != enabled) {
                canBreakDoors = enabled;
                ((GroundPathNavigation) getNavigation()).setCanOpenDoors(enabled || canOpenDoor());
                if (enabled)
                    goalSelector.addGoal(1, breakDoorGoal);
                else
                    goalSelector.removeGoal(breakDoorGoal);
            }
        } else if (canBreakDoors) {
            goalSelector.removeGoal(breakDoorGoal);
            canBreakDoors = false;
        }
    }


    public boolean canFireProjectileWeapon(Item item) {
        return item instanceof ProjectileWeaponItem weaponItem && canFireProjectileWeapon(weaponItem);
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem item) {
        return item instanceof BowItem || item instanceof CrossbowItem;
    }

    @Override
    public void shootCrossbowProjectile(LivingEntity target, ItemStack crossbow, Projectile projectile, float angle) {
        shootCrossbowProjectile(this, target, projectile, angle, 1.6F);
    }

    public boolean isChargingCrossbow() {
        return entityData.get(IS_CHARGING_CROSSBOW);
    }

    public void setAttackChance(int newChance) {
        this.entityData.set(ATTACK_CHANCE, newChance);
    }

    public int getAttackChance() {
        return this.entityData.get(ATTACK_CHANCE);
    }

    @Override
    public List<ExtendedSensor<PlayerMobEntity>> getSensors() {
        return ObjectArrayList.of(new NearbyLivingEntitySensor<>(), new NearbyPlayersSensor<>(), new HurtBySensor<>());
    }

    @Override
    protected void customServerAiStep() {
        tickBrain(this);
    }

    @Override
    public BrainActivityGroup<PlayerMobEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(new GroupBeyondersBehaviour<>(), new MoveToWalkTarget<>(), new GroupTargetBehaviour<>(), new PassiveAttackBehavior<>());
    }

    @Override
    public BrainActivityGroup<PlayerMobEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(new FirstApplicableBehaviour<>(new SetPlayerLookTarget<>(), new SetRandomLookTarget<>(), new SetRandomWalkTarget<>().dontAvoidWater()));
    }


    @Override
    public BrainActivityGroup<PlayerMobEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(new InvalidateAttackTarget<>(), new SetWalkTargetToAttackTarget<>(), new BeyonderAttack<>());
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 5;
    }


    @Override
    @NotNull
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void setChargingCrossbow(boolean isCharging) {
        entityData.set(IS_CHARGING_CROSSBOW, isCharging);
    }

    @Override
    public void onCrossbowAttackPerformed() {
        noActionTime = 0;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        ItemStack weaponStack = getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, this::canFireProjectileWeapon));
        if (weaponStack.getItem() instanceof CrossbowItem) {
            performCrossbowAttack(this, 1.6F);
        } else {
            ItemStack itemstack = getProjectile(weaponStack);
            AbstractArrow mobArrow = ProjectileUtil.getMobArrow(this, itemstack, velocity);
            if (getMainHandItem().getItem() instanceof BowItem bowItem)
                mobArrow = bowItem.customArrow(mobArrow);
            double x = target.getX() - getX();
            double y = target.getY(1D / 3D) - mobArrow.getY();
            double z = target.getZ() - getZ();
            double d3 = Math.sqrt(x * x + z * z);
            mobArrow.shoot(x, y + d3 * 0.2F, z, 1.6F, 14 - level().getDifficulty().getId() * 4);
            playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (getRandom().nextFloat() * 0.4F + 0.8F));
            level().addFreshEntity(mobArrow);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (getCustomName() != null && getCustomName().getString().isEmpty()) {
            compound.remove("CustomName");
        }
        String username = getUsername().getCombinedNames();
        if (!StringUtil.isNullOrEmpty(username)) {
            compound.putString("Username", username);
        }
        compound.putBoolean("IsBaby", isBaby());
        if (profile != null && profile.isComplete()) {
            compound.put("Profile", NbtUtils.writeGameProfile(new CompoundTag(), profile));
        }
        compound.putString("colorMode", this.getColorMode());
        compound.putString("Pathway", this.entityData.get(PATHWAY));
        compound.putInt("MentalStrength", this.getMentalStrength());
        compound.putInt("Sequence", this.getCurrentSequence());
        compound.putInt("Spirituality", this.getSpirituality());
        compound.putInt("MaxSpirituality", this.getMaxSpirituality());
        compound.putInt("MaxLife", this.getMaxlife());
        compound.putBoolean("Clone", this.getIsClone());
        compound.putBoolean("RegenSpirituality", this.getRegenSpirituality());
        compound.putInt("AttackChance", this.getAttackChance());
        compound.putFloat("FlySpeed", this.getFlySpeed());
        compound.putInt("DistanceFromTarget", this.getIdealDistanceFromTarget());
        compound.putBoolean("Flying", this.getIsFlying());
        Optional<UUID> creatorUUID = this.entityData.get(CREATOR_UUID);
        if (creatorUUID.isPresent()) {
            compound.putUUID("Creator", creatorUUID.get());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        String username = compound.getString("Username");
        if (!StringUtil.isNullOrEmpty(username)) {
            setUsername(username);
        } else {
            setUsername(NameManager.INSTANCE.getRandomName());
        }
        setBaby(compound.getBoolean("IsBaby"));
        if (compound.contains("colorMode")) {
            this.setColorMode(compound.getString("colorMode"));
        }
        if (compound.contains("Profile", Tag.TAG_COMPOUND)) {
            profile = NbtUtils.readGameProfile(compound.getCompound("Profile"));
        }
        if (compound.contains("Sequence")) {
            this.setSequence(compound.getInt("Sequence"));
        }
        if (compound.contains("Pathway")) {
            this.setPathway(BeyonderUtil.getPathwayByName(compound.getString("Pathway")));
        }
        if (compound.contains("Creator")) {
            this.setCreator(compound.getUUID("Creator"));
        }
        if (compound.contains("MentalStrength")) {
            this.setMentalStrength(compound.getInt("MentalStrength"));
        }
        if (compound.contains("Sequence")) {
            this.setSequence(compound.getInt("Sequence"));
        }
        if (compound.contains("Spirituality")) {
            this.setSpirituality(compound.getInt("Spirituality"));
        }
        if (compound.contains("MaxSpirituality")) {
            this.setMaxSpirituality(compound.getInt("MaxSpirituality"));
        }
        if (compound.contains("MaxLife")) {
            this.setMaxLife(compound.getInt("MaxLife"));
        }
        if (compound.contains("Clone")) {
            this.setIsClone(compound.getBoolean("Clone"));
        }
        if (compound.contains("RegenSpirituality")) {
            this.setMaxLife(compound.getInt("RegenSpirituality"));
        }
        if (compound.contains("AttackChance")) {
            this.setAttackChance(compound.getInt("AttackChance"));
        }
        if (compound.contains("Flying")) {
            this.setIsFlying(compound.getBoolean("Flying"));
        }
        if (compound.contains("DistanceFromTarget")) {
            this.setIdealDistanceFromTarget(compound.getInt("DistanceFromTarget"));
        }
        if (compound.contains("FlySpeed")) {
            this.setFlySpeed(compound.getFloat("FlySpeed"));
        }
    }


    public void setPathway(BeyonderClass pathway) {
        if (pathway == null) {
            this.entityData.set(PATHWAY, "");
            BeyonderHolder.resetMaxHealthModifier(this);
        } else {
            this.entityData.set(PATHWAY, BeyonderUtil.getPathwayName(pathway));
            //if (getCurrentSequence() != -1) {
            //    BeyonderHolder.updateMaxHealthModifier(this, pathway.maxHealth().get(getCurrentSequence()));
            //}
        }
    }

    public void setSpirituality(int spirituality) {
        this.entityData.set(SPIRITUALITY, spirituality);
    }

    public void setSpiritualityRegen(int spiritualityRegen) {
        this.entityData.set(SPIRITUALITY_REGEN, spiritualityRegen);
    }

    public int getSpirituality() {
        if (this.getCreator() != null && this.getIsClone()) {
            if (this.getPersistentData().getBoolean("shouldFlicker") && this.getCreator().isAlive()) {
                return BeyonderUtil.getSpirituality(this.getCreator());
            }
        }
        return this.entityData.get(SPIRITUALITY);
    }

    public void setIsClone(boolean isClone) {
        this.entityData.set(IS_CLONE, isClone);
    }

    public boolean getIsClone() {
        return this.entityData.get(IS_CLONE);
    }

    public void setIsFlying(boolean flying) {
        this.entityData.set(FLYING, flying);
    }

    public boolean getIsFlying() {
        return this.entityData.get(FLYING);
    }

    public void setIdealDistanceFromTarget(int distanceFromTarget) {
        this.entityData.set(DISTANCE_FROM_TARGET, distanceFromTarget);
    }

    public int getIdealDistanceFromTarget() {
        return this.entityData.get(DISTANCE_FROM_TARGET);
    }

    public void setFlySpeed(float flySpeed) {
        this.entityData.set(FLY_SPEED, flySpeed);
    }

    public float getFlySpeed() {
        return this.entityData.get(FLY_SPEED);
    }


    public void setRegenSpirituality(boolean regenSpirituality) {
        this.entityData.set(REGEN_SPIRITUALITY, regenSpirituality);
    }

    public boolean getRegenSpirituality() {
        return this.entityData.get(REGEN_SPIRITUALITY);
    }

    public void setHasAbilityCap(boolean abilityCap) {
        this.entityData.set(HAS_ABILITY_CAP, abilityCap);
    }

    public boolean getHasAbilityCap() {
        return this.entityData.get(HAS_ABILITY_CAP);
    }

    public void setMaxAbilitiesUse(int maxAbilitiesUse) {
        this.entityData.set(MAX_ABILITIES_USE, maxAbilitiesUse);
    }

    public int getMaxAbilitiesUse() {
        return this.entityData.get(MAX_ABILITIES_USE);
    }

    public int getSpiritualityRegen() {
        return this.entityData.get(SPIRITUALITY_REGEN);
    }

    public void setMaxSpirituality(int maxSpirituality) {
        this.entityData.set(MAXSPIRITUALITY, maxSpirituality);
    }

    public int getMaxSpirituality() {
        return this.entityData.get(MAXSPIRITUALITY);
    }

    public void setMaxLife(int maxLife) {
        this.entityData.set(MAX_LIFE, maxLife);
    }

    public int getMaxlife() {
        return this.entityData.get(MAX_LIFE);
    }

    public String getColorMode() {
        return this.getEntityData().get(COLOR_MODE);
    }

    public void setColorMode(ColorMode colorMode) {
        this.getEntityData().set(COLOR_MODE, colorMode.name());
    }

    public void setColorMode(String colorMode) {
        try {
            ColorMode mode = ColorMode.valueOf(colorMode.toUpperCase());
            this.getEntityData().set(COLOR_MODE, mode.name());
        } catch (IllegalArgumentException e) {
            // If invalid color mode, default to normal
            this.getEntityData().set(COLOR_MODE, ColorMode.NORMAL.name());
        }
    }

    public ColorMode getColorModeEnum() {
        try {
            return ColorMode.valueOf(getColorMode());
        } catch (IllegalArgumentException e) {
            return ColorMode.NORMAL;
        }
    }

    public void setRandomColorMode() {
        ColorMode[] modes = ColorMode.values();
        // Skip NORMAL (index 0) and pick from colored modes
        ColorMode randomMode = modes[1 + this.random.nextInt(modes.length - 1)];
        setColorMode(randomMode);
    }


    public void cycleColorMode() {
        ColorMode[] modes = ColorMode.values();
        ColorMode currentMode;
        try {
            currentMode = ColorMode.valueOf(getColorMode());
        } catch (IllegalArgumentException e) {
            currentMode = ColorMode.NORMAL;
        }

        int currentIndex = currentMode.ordinal();
        int nextIndex = (currentIndex + 1) % modes.length;
        setColorMode(modes[nextIndex]);
    }


    public BeyonderClass getCurrentPathway() {
        if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof SpectatorClass) {
            return BeyonderClassInit.SPECTATOR.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof SailorClass) {
            return BeyonderClassInit.SAILOR.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof SeerClass) {
            return BeyonderClassInit.SEER.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof ApprenticeClass) {
            return BeyonderClassInit.APPRENTICE.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof MarauderClass) {
            return BeyonderClassInit.MARAUDER.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof SecretsSupplicantClass) {
            return BeyonderClassInit.SECRETSSUPPLICANT.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof BardClass) {
            return BeyonderClassInit.BARD.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof ReaderClass) {
            return BeyonderClassInit.READER.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof SleeplessClass) {
            return BeyonderClassInit.SLEEPLESS.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof WarriorClass) {
            return BeyonderClassInit.WARRIOR.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof HunterClass) {
            return BeyonderClassInit.HUNTER.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof AssassinClass) {
            return BeyonderClassInit.ASSASSIN.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof SavantClass) {
            return BeyonderClassInit.SAVANT.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof MysteryPryerClass) {
            return BeyonderClassInit.MYSTERYPRYER.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof CorpseCollectorClass) {
            return BeyonderClassInit.CORPSECOLLECTOR.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof LawyerClass) {
            return BeyonderClassInit.LAWYER.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof MonsterClass) {
            return BeyonderClassInit.MONSTER.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof ApothecaryClass) {
            return BeyonderClassInit.APOTHECARY.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof PlanterClass) {
            return BeyonderClassInit.PLANTER.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof ArbiterClass) {
            return BeyonderClassInit.ARBITER.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof PrisonerClass) {
            return BeyonderClassInit.PRISONER.get();
        } else if (BeyonderUtil.getPathwayByName(entityData.get(PATHWAY)) instanceof CriminalClass) {
            return BeyonderClassInit.CRIMINAL.get();
        }
        return null;
    }

    @Override
    public Component getCustomName() {
        Component customName = super.getCustomName();
        String displayName = getUsername().getDisplayName();
        return customName != null && !customName.getString().isEmpty() ? customName : !StringUtil.isNullOrEmpty(displayName) ? Component.literal(displayName) : null;
    }

    @Override
    public boolean hasCustomName() {
        return super.hasCustomName() || !StringUtil.isNullOrEmpty(getUsername().getDisplayName());
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundInit.PLAYER_MOB_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundInit.PLAYER_MOB_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundInit.PLAYER_MOB_AMBIENT.get();
    }

    @Nullable
    public GameProfile getProfile() {
        if (profile == null && hasUsername()) {
            profile = new GameProfile(null, getUsername().getSkinName());
            ProfileUpdater.updateProfile(this);
        }
        return profile;
    }

    public void setProfile(@Nullable GameProfile profile) {
        this.profile = profile;
    }

    public boolean hasUsername() {
        return !StringUtil.isNullOrEmpty(getEntityData().get(NAME));
    }

    public PlayerName getUsername() {
        if (!hasUsername() && !level().isClientSide()) {
            setUsername(NameManager.INSTANCE.getRandomName());
        }
        return new PlayerName(getEntityData().get(NAME));
    }

    public int getCurrentSequence() {
        return this.entityData.get(SEQUENCE);
    }

    public void setSequence(int sequence) {
        this.entityData.set(SEQUENCE, sequence);
    }

    public int getMentalStrength() {
        return this.entityData.get(MENTAL_STRENGTH);
    }

    public void setMentalStrength(int mentalStrength) {
        this.entityData.set(MENTAL_STRENGTH, mentalStrength);
    }


    public void setUsername(String username) {
        PlayerName playerName = new PlayerName(username);
        if (playerName.noDisplayName()) {
            Optional<PlayerName> name = NameManager.INSTANCE.findName(username);
            if (name.isPresent())
                playerName = name.get();
        }
        NameManager.INSTANCE.useName(playerName);
        setUsername(playerName);
    }

    public void setUsername(PlayerName name) {
        PlayerName oldName = hasUsername() ? getUsername() : null;
        getEntityData().set(NAME, name.getCombinedNames());

        if ("Herobrine".equals(name.getDisplayName())) {
            getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier("Herobrine Damage Bonus", 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
            getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier("Herobrine Speed Bonus", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        if (!Objects.equals(oldName, name)) {
            setProfile(null);
            getProfile();
        }
    }

    public boolean useSpirituality(int amount) {
        if (this.getSpirituality() - amount < 0) {
            return false;
        }
        if (this.getCreator() != null && this.getIsClone()) {
            if (this.getPersistentData().getBoolean("shouldFlicker") && this.getCreator().isAlive()) {
                BeyonderUtil.useSpirituality(this.getCreator(), amount);
                return true;
            }
        } else {
            this.setSpirituality(Mth.clamp(this.getSpirituality() - amount, 0, getMaxSpirituality()));
        }
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public SkinManager.SkinTextureCallback getSkinCallback() {
        return (type, location, profileTexture) -> {
            switch (type) {
                case SKIN -> {
                    skin = location;
                    skinAvailable = true;
                }
                case CAPE -> {
                    cape = location;
                    capeAvailable = true;
                }
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isTextureAvailable(MinecraftProfileTexture.Type type) {
        if (type == MinecraftProfileTexture.Type.SKIN)
            return skinAvailable;
        return capeAvailable;
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getTexture(MinecraftProfileTexture.Type type) {
        if (type == MinecraftProfileTexture.Type.SKIN)
            return skin;
        return cape;
    }

    public LivingEntity getCreator() {
        Optional<UUID> creatorUUID = this.entityData.get(CREATOR_UUID);
        if (creatorUUID.isPresent()) {
            return BeyonderUtil.getLivingEntityFromUUID(this.level(), creatorUUID.get());
        }
        return null;
    }

    public void setCreator(UUID creatorUUID) {
        this.entityData.set(CREATOR_UUID, Optional.ofNullable(creatorUUID));
    }

    public void setCreator(LivingEntity creator) {
        if (creator != null) {
            setCreator(creator.getUUID());
        } else {
            setCreator((UUID) null);
        }
    }

    public static ItemStack getDrop(LivingEntity entity, DamageSource source, int looting) {
        if (entity.level().isClientSide() || entity.getHealth() > 0)
            return ItemStack.EMPTY;
        if (entity.isBaby())
            return ItemStack.EMPTY;
        double baseChance = entity instanceof PlayerMobEntity ? Configs.COMMON.mobHeadDropChance.get() : Configs.COMMON.playerHeadDropChance.get();
        if (baseChance <= 0)
            return ItemStack.EMPTY;

        if (poweredCreeper(source) || randomDrop(entity.level().getRandom(), baseChance, looting)) {
            ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
            GameProfile profile = entity instanceof PlayerMobEntity ?
                    ((PlayerMobEntity) entity).getProfile() :
                    ((Player) entity).getGameProfile();
            if (entity instanceof PlayerMobEntity playerMob) {
                String skinName = playerMob.getUsername().getSkinName();
                String displayName = playerMob.getUsername().getDisplayName();
                if (playerMob.getCustomName() != null) {
                    displayName = playerMob.getCustomName().getString();
                }

                if (!skinName.equals(displayName)) {
                    stack.setHoverName(Component.translatable("block.minecraft.player_head.named", displayName));
                }
            }
            if (profile != null)
                stack.getOrCreateTag().put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), profile));
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        if (this.level() instanceof ServerLevel serverLevel) {
            PlayerMobTracker tracker = PlayerMobTracker.get(serverLevel);
            tracker.addPlayerMob(this);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide() && shouldRemoveFromTracker(reason)) {
            PlayerMobTracker tracker = PlayerMobTracker.get((ServerLevel) level());
            tracker.removePlayerMob(this.getUUID(), (ServerLevel) level());
        }

        super.remove(reason);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!level().isClientSide()) {
            PlayerMobTracker tracker = PlayerMobTracker.get((ServerLevel) level());
            tracker.removePlayerMob(this.getUUID(), (ServerLevel) level());
        }

        super.die(damageSource);
    }

    private boolean shouldRemoveFromTracker(RemovalReason reason) {
        switch (reason) {
            case KILLED:
            case DISCARDED:
            case CHANGED_DIMENSION:
                return true;
            case UNLOADED_TO_CHUNK:
            case UNLOADED_WITH_PLAYER:
                return false;
            default:
                return true;
        }
    }


    private static boolean poweredCreeper(DamageSource source) {
        return source.is(DamageTypeTags.IS_EXPLOSION) && source.getEntity() instanceof Creeper creeper && creeper.isPowered();
    }

    private static boolean randomDrop(RandomSource rand, double baseChance, int looting) {
        return rand.nextDouble() <= Math.max(0, baseChance * Math.max(looting + 1, 1));
    }

    public static PlayerMobEntity playerCopy(LivingEntity player) {
        PlayerMobEntity playerMobEntity = new PlayerMobEntity(EntityInit.PLAYER_MOB_ENTITY.get(), player.level());
        playerMobEntity.setSequence(BeyonderUtil.getSequence(player));
        playerMobEntity.setPathway(BeyonderUtil.getPathway(player));
        CompoundTag playerData = player.getPersistentData();
        CompoundTag cloneData = playerMobEntity.getPersistentData();
        cloneData.merge(playerData.copy());
        for (Attribute attribute : ForgeRegistries.ATTRIBUTES.getValues()) {
            AttributeInstance playerAttribute = player.getAttribute(attribute);
            AttributeInstance cloneAttribute = playerMobEntity.getAttribute(attribute);
            if (playerAttribute != null && cloneAttribute != null) {
                cloneAttribute.setBaseValue(playerAttribute.getBaseValue());
                for (AttributeModifier modifier : playerAttribute.getModifiers()) {
                    if (!cloneAttribute.hasModifier(modifier)) {
                        cloneAttribute.addPermanentModifier(modifier);
                    }
                }
            }
        }
        BeyonderUtil.setScale(playerMobEntity, BeyonderUtil.getScale(player));
        playerMobEntity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(player.getMaxHealth());
        playerMobEntity.setHealth(player.getHealth());
        for (MobEffectInstance effect : player.getActiveEffects()) {
            playerMobEntity.addEffect(new MobEffectInstance(effect));
        }

        Set<String> playerTags = player.getTags();
        for (String tag : playerTags) {
            playerMobEntity.addTag(tag);
        }


        if (BeyonderUtil.canFly(player)) {
            BeyonderUtil.startFlying(playerMobEntity, Math.max(0.08f, player.getPersistentData().getFloat("LOTMFlySpeed")), 20);
        }
        playerMobEntity.setUsername(player.getScoreboardName());
        playerMobEntity.setIsClone(true);
        if (player.hasCustomName()) {
            playerMobEntity.setCustomName(player.getCustomName());
        }
        return playerMobEntity;
    }
}