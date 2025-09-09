package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.WaterSphere;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.WindManipulationFlight;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;
import net.swimmingtuna.lotm.util.BeyonderUtil;

public class WindManipulationFlightLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        Vec3 lookVector = livingEntity.getLookAngle();
        boolean sailorTrueFlight = tag.getBoolean("sailorFlight1");
        int flight = tag.getInt("sailorFlight");
        if (sailorTrueFlight) {
            if (livingEntity instanceof Player player && player.getAbilities().flying) {
                BeyonderUtil.useSpirituality(player, 2);
            }
            BeyonderUtil.startFlying(livingEntity, 0.1f, 10);
        }
        if (flight >= 1) {
            tag.putInt("sailorFlight", flight + 1);
            if (flight <= 45 && flight % 15 == 0) {
                if (livingEntity.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), 0, 0,0,0,0);
                }
                livingEntity.setDeltaMovement(lookVector.x * 2, lookVector.y * 2, lookVector.z * 2);
                livingEntity.hurtMarked = true;
            }
            if (flight > 45) {
                tag.putInt("sailorFlight", 0);
            }
        }
        if (!sailorTrueFlight && flight == 0) {
            EventManager.removeFromRegularLoop(livingEntity, EFunctions.WIND_MANIPULATION_FLIGHT.get());
        }
    }

    @Override
    public String getID() {
        return "WindManipulationEventID";
    }
}
