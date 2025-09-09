package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses.BeyonderTicks.Spectator;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;
import net.swimmingtuna.lotm.util.BeyonderUtil;

public class SpectatorTickLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        LivingEntity player = event.getEntity();

        if (!player.level().isClientSide()) {
            if (player instanceof Player pPlayer) {
                if (pPlayer.isCrouching()) {
                    player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 5, -1, false, false));
                }
            }
            else {
                if (player.tickCount % 200 == 0) {
                    BeyonderUtil.applyMobEffect(player, MobEffects.INVISIBILITY, 50, 1, false, false);
                }
            }
        }
    }

    @Override
    public String getID() {
        return "SpectatorTickEventID";
    }
}
