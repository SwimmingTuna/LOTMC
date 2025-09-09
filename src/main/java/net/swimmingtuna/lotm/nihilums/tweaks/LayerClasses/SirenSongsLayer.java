package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.BlinkState;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.SirenSongHarm;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;

public class SirenSongsLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        SirenSongHarm.sirenSongsTick(event.getEntity());
    }

    @Override
    public String getID() {
        return "SirenSongEventID";
    }
}
