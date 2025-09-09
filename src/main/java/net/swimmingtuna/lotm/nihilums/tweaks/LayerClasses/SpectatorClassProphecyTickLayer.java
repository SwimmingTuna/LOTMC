package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.beyonder.SpectatorClass;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class SpectatorClassProphecyTickLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        int meteor = tag.getInt("spectatorProphesizedMeteor");
        int tornado = tag.getInt("spectatorProphesizedTornado");
        int earthquake = tag.getInt("spectatorProphesizedEarthquake");
        int plague = tag.getInt("spectatorProphesizedPlague");
        int potion = tag.getInt("spectatorProphesizedGuaranteedPotion");
        int weakness = tag.getInt("spectatorProphesizedWeakness");
        int healed = tag.getInt("spectatorProphesizedHealed");
        int luck = tag.getInt("spectatorProphesizedLuck");
        int sinkhole = tag.getInt("spectatorProphesizedSinkhole");
        int misfortune = tag.getInt("spectatorProphesizedMisfortune");
        if (meteor == 0 && tornado == 0 && earthquake == 0 && plague == 0 && potion == 0 && weakness == 0 && healed == 0 && luck == 0 && sinkhole == 0 && misfortune == 0) {
            EventManager.removeFromRegularLoop(livingEntity, EFunctions.SPECTATORPROPHECY.get());
        }
        SpectatorClass.prophecyTickEvent(event);
    }

    @Override
    public String getID() {
        return "SpectatorProphecyEventID";
    }
}
