package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.Prophecy;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class ProphecyTickLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        int x = tag.getInt("prophecyEarthquake");
        int y = tag.getInt("prophecyPlague");
        int z = tag.getInt("prophecySinkhole");
        if (x == 0 && y == 0 && z == 0) {
            EventManager.removeFromRegularLoop(livingEntity, EFunctions.PROPHECY.get());
        }
        Prophecy.prophecyTick(event);
    }

    @Override
    public String getID() {
        return "ProphecyEventID";
    }
}
