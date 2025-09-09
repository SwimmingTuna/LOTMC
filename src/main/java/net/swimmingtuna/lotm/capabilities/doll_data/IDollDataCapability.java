package net.swimmingtuna.lotm.capabilities.doll_data;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface IDollDataCapability {
    boolean isDoll();
    boolean markedForDeath();
    float dollX();
    float dollY();
    float dollZ();
    ResourceKey<Level> dollDimension();
    int dollTimer();
    int dollDetectTimer();

    void setIsDoll(boolean isDoll);
    void setMarkedForDeath(boolean markedForDeath);
    void setDollX(float x);
    void setDollY(float y);
    void setDollZ(float z);
    void setDollDimension(ResourceKey<Level> dollDimension);
    void setDollTimer(int timer);
    void setDetectDollTimer(int timer);
}