package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

import static net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.EnvisionKingdom.envisionKingdom;

public class EnvisionKingdomLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();

        envisionKingdom(livingEntity, livingEntity.level());
    }

    @Override
    public String getID() {
        return "EnvisionKingdomEventID";
    }
}
