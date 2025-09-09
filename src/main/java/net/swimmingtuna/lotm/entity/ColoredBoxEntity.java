package net.swimmingtuna.lotm.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.Optional;
import java.util.UUID;

public class ColoredBoxEntity extends Entity {
    private static final EntityDataAccessor<Integer> MAX_HEALTH = SynchedEntityData.defineId(ColoredBoxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CYCLE_FREQUENCY = SynchedEntityData.defineId(ColoredBoxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_SIZE = SynchedEntityData.defineId(ColoredBoxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(ColoredBoxEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> COLOR_MODE = SynchedEntityData.defineId(ColoredBoxEntity.class, EntityDataSerializers.STRING);

    public enum ColorMode {
        GRAY,
        BLACK,
        RED,
        BLUE,
        YELLOW,
        PURPLE,
        GREEN
    }

    public ColoredBoxEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public ColoredBoxEntity(Level level, double x, double y, double z, float maxRadius) {
        this(EntityInit.COLORED_BOX_ENTITY.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(MAX_HEALTH, 100);
        this.entityData.define(MAX_SIZE, 10);
        this.entityData.define(CYCLE_FREQUENCY, 0);
        this.entityData.define(OWNER_UUID, Optional.empty());
        this.entityData.define(COLOR_MODE, ColorMode.YELLOW.name());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return true;
    }

    @Override
    public @NotNull AABB getBoundingBoxForCulling() {
        return new AABB(this.getX() - 600, this.getY() - 600, this.getZ() - 600, this.getX() + 600, this.getY() + 600, this.getZ() + 600);
    }

    public int getMaxHealth() {
        return this.entityData.get(MAX_HEALTH);
    }

    public void setMaxHealth(int maxHealth) {
        this.entityData.set(MAX_HEALTH, maxHealth);
    }

    public Optional<UUID> getOwnerUUID() {
        return this.entityData.get(OWNER_UUID);
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(ownerUUID));
    }

    public int getMaxSize() {
        return this.entityData.get(MAX_SIZE);
    }

    public int getCycleFrequency() {
        return this.entityData.get(CYCLE_FREQUENCY);
    }

    public void setCycleFrequency(int cycleFrequency) {
        this.entityData.set(CYCLE_FREQUENCY, cycleFrequency);
    }

    public void setMaxSize(int maxSize) {
        this.entityData.set(MAX_SIZE, maxSize);
    }

    /**
     * Gets the current color mode of the entity
     */
    public String getColorMode() {
        return this.entityData.get(COLOR_MODE);
    }

    /**
     * Sets the color mode of the entity
     */
    public void setColorMode(ColorMode colorMode) {
        this.entityData.set(COLOR_MODE, colorMode.name());
    }

    /**
     * Sets the color mode of the entity using a string
     */
    public void setColorMode(String colorMode) {
        try {
            ColorMode mode = ColorMode.valueOf(colorMode.toUpperCase());
            this.entityData.set(COLOR_MODE, mode.name());
        } catch (IllegalArgumentException e) {
            this.entityData.set(COLOR_MODE, ColorMode.YELLOW.name());
        }
    }

    /**
     * Cycles to the next color mode in the enum
     */
    public void cycleColorMode() {
        ColorMode[] modes = ColorMode.values();
        ColorMode currentMode;
        try {
            currentMode = ColorMode.valueOf(getColorMode());
        } catch (IllegalArgumentException e) {
            currentMode = ColorMode.YELLOW;
        }

        int currentIndex = currentMode.ordinal();
        int nextIndex = (currentIndex + 1) % modes.length;
        setColorMode(modes[nextIndex]);
    }

    /**
     * Gets the ColorMode enum value
     */
    public ColorMode getColorModeEnum() {
        try {
            return ColorMode.valueOf(getColorMode());
        } catch (IllegalArgumentException e) {
            return ColorMode.YELLOW;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            this.setMaxSize((int) BeyonderUtil.getScale(this));
            if (this.tickCount >= this.getMaxHealth()) {
                this.discard();
            }
            if (this.getCycleFrequency() != 0 && this.tickCount % this.getCycleFrequency() == 0) {
                this.cycleColorMode();
            }
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("maxHealth")) {
            this.setMaxHealth(compound.getInt("maxHealth"));
        }
        if (compound.contains("maxSize")) {
            this.setMaxSize(compound.getInt("maxSize"));
        }
        if (compound.contains("cycleFrequency")) {
            this.setCycleFrequency(compound.getInt("cycleFrequency"));
        }
        if (compound.contains("ownerUUID")) {
            this.setOwnerUUID(compound.getUUID("ownerUUID"));
        }
        if (compound.contains("colorMode")) {
            this.setColorMode(compound.getString("colorMode"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("maxHealth", this.getMaxHealth());
        compound.putInt("maxSize", this.getMaxSize());
        compound.putString("colorMode", this.getColorMode());
        if (this.getOwnerUUID().isPresent()) {
            compound.putUUID("ownerUUID", this.getOwnerUUID().get());
        }
        compound.putInt("cycleFrequency", this.getCycleFrequency());
    }
}