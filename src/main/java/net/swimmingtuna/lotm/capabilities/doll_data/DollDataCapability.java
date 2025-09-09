package net.swimmingtuna.lotm.capabilities.doll_data;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

public class DollDataCapability implements IDollDataCapability, INBTSerializable<CompoundTag> {
    private boolean isDoll = false;
    private boolean markedForDeath = false;
    private float dollX = 0;
    private float dollY = 0;
    private float dollZ = 0;
    private ResourceKey<Level> dollDimension = Level.OVERWORLD;
    private int timer = 0;
    private int detectTimer = 20;

    @Override
    public boolean isDoll() {
        return this.isDoll;
    }

    @Override
    public boolean markedForDeath() {
        return this.markedForDeath;
    }

    @Override
    public float dollX() {
        return this.dollX;
    }

    @Override
    public float dollY() {
        return this.dollY;
    }

    @Override
    public float dollZ() {
        return this.dollZ;
    }

    @Override
    public ResourceKey<Level> dollDimension(){
        return this.dollDimension;
    }

    @Override
    public int dollTimer() {
        return this.timer;
    }

    @Override
    public int dollDetectTimer() {
        return this.detectTimer;
    }

    @Override
    public void setIsDoll(boolean isDoll) {
        this.isDoll = isDoll;
    }

    @Override
    public void setMarkedForDeath(boolean markedForDeath) {
        this.markedForDeath = markedForDeath;
    }

    @Override
    public void setDollX(float x) {
        this.dollX = x;
    }

    @Override
    public void setDollY(float y) {
        this.dollY = y;
    }

    @Override
    public void setDollZ(float z) {
        this.dollZ = z;
    }

    @Override
    public void setDollDimension(ResourceKey<Level> dollDimension){
        this.dollDimension = dollDimension;
    }

    @Override
    public void setDollTimer(int timer) {
        this.timer = timer;
    }

    @Override
    public void setDetectDollTimer(int timer) {
        this.detectTimer = timer;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putBoolean("isDoll", this.isDoll);
        tag.putBoolean("markedForDeath", this.markedForDeath);
        tag.putFloat("dollX", this.dollX);
        tag.putFloat("dollY", this.dollY);
        tag.putFloat("dollZ", this.dollZ);
        tag.putString("dollDimension", this.dollDimension.location().toString());
        tag.putInt("dollTimer", this.timer);
        tag.putInt("dollDetectTimer", this.detectTimer);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.isDoll = tag.getBoolean("isDoll");
        this.markedForDeath = tag.getBoolean("markedForDeath");
        this.dollX = tag.getFloat("dollX");
        this.dollY = tag.getFloat("dollY");
        this.dollZ = tag.getFloat("dollZ");
        this.dollDimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString("dollDimension")));
        this.timer = tag.getInt("dollTimer");
        this.detectTimer = tag.getInt("dollDetectTimer");
    }

    public void copyFrom(DollDataCapability other) {
        this.isDoll = other.isDoll;
        this.markedForDeath = other.markedForDeath;
        this.dollX = other.dollX;
        this.dollY = other.dollY;
        this.dollZ = other.dollZ;
        this.dollDimension = other.dollDimension;
        this.timer = other.timer;
        this.detectTimer = other.detectTimer;
    }
}