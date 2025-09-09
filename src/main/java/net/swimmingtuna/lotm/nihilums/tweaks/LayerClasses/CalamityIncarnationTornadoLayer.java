package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.MonsterCalamityIncarnation;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.CalamityIncarnationTornado;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.CalamityIncarnationTsunami;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class CalamityIncarnationTornadoLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        MonsterCalamityIncarnation.calamityIncarnationTornado(event.getEntity());
    }

    @Override
    public String getID() {
        return "CalamityIncarnationTornadoEventID";
    }
}
