package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.ExtremeColdness;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class ExtremeColdnessLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        CompoundTag tag = entity.getPersistentData();
        int affectedBySailorExtremeColdness = tag.getInt("affectedBySailorExtremeColdness");
        if (affectedBySailorExtremeColdness == 0) {
            EventManager.removeFromRegularLoop(entity, EFunctions.AFFECTEDBYEXTREMECOLDNESS.get());
        }
        ExtremeColdness.extremeColdnessTick(event);
    }

    @Override
    public String getID() {
        return "ExtremeColdnessEventID";
    }
}
