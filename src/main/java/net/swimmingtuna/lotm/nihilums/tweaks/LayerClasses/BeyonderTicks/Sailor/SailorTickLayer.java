package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses.BeyonderTicks.Sailor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;
import net.swimmingtuna.lotm.util.BeyonderUtil;

import static net.swimmingtuna.lotm.util.BeyonderUtil.applyMobEffect;

public class SailorTickLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        LivingEntity player = event.getEntity();
        int sequenceLevel = BeyonderUtil.getSequence(player);

        if (player.tickCount % 60 != 0) {
            return;
        }
        CompoundTag tag = player.getPersistentData();
        int flightCancel = tag.getInt("sailorFlightDamageCancel");
        if (flightCancel >= 1) {
            player.fallDistance = 0;
            tag.putInt("sailorFlightDamageCancel", flightCancel + 1);
            if (flightCancel >= 300) {
                tag.putInt("sailorFlightDamageCancel", 0);
            }
        }
        boolean sailorFlight1 = tag.getBoolean("sailorFlight1");

        boolean enhancedFlight = tag.getBoolean("sailorFlight1");
        if (BeyonderUtil.currentPathwayAndSequenceMatchesNoException(player, BeyonderClassInit.SAILOR.get(), 6) && player.isShiftKeyDown() && player.fallDistance >= 3 && !(player instanceof Player pPlayer && pPlayer.getAbilities().instabuild) && !enhancedFlight) {
            Vec3 movement = player.getDeltaMovement();
            if (movement.y() < 0) {
                double deltaX = Math.cos(Math.toRadians(player.getYRot() + 90)) * 0.06;
                double deltaZ = Math.sin(Math.toRadians(player.getYRot() + 90)) * 0.06;
                player.setDeltaMovement(movement.x + deltaX, -0.05, movement.z + deltaZ);
                player.fallDistance = 5;
                player.hurtMarked = true;
            }
        }

        if (player.isInWaterOrRain()) {
            if (player instanceof Player pPlayer) {
                Abilities playerAbilites = pPlayer.getAbilities();
                playerAbilites.setFlyingSpeed(0.1F);
                pPlayer.onUpdateAbilities();

                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(serverPlayer.getAbilities()));
                }
            }
            if (sequenceLevel <= 4) {
                applyMobEffect(player, MobEffects.DOLPHINS_GRACE, 300, 2, false, false);
                applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 1, false, false);
                applyMobEffect(player, MobEffects.DIG_SPEED, 300, 1, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 1, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 2, false, false);
                applyMobEffect(player, MobEffects.REGENERATION, 300, 2, false, false);
            }
            else if (sequenceLevel <= 6) {
                applyMobEffect(player, MobEffects.DOLPHINS_GRACE, 300, 1, false, false);
                applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 1, false, false);
                applyMobEffect(player, MobEffects.DIG_SPEED, 300, 1, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 1, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 1, false, false);
                applyMobEffect(player, MobEffects.REGENERATION, 300, 1, false, false);
            }
            else {
                applyMobEffect(player, MobEffects.DOLPHINS_GRACE, 300, 1, false, false);
                applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 1, false, false);
                applyMobEffect(player, MobEffects.DIG_SPEED, 300, 1, false, false);
                applyMobEffect(player, MobEffects.REGENERATION, 300, 1, false, false);
            }
        }
        if (!player.level().isRaining() && !sailorFlight1 && player instanceof Player pPlayer) {
            Abilities playerAbilites = pPlayer.getAbilities();
            playerAbilites.setFlyingSpeed(0.05F);
            pPlayer.onUpdateAbilities();

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(serverPlayer.getAbilities()));
            }
        }
    }

    @Override
    public String getID() {
        return "SailorTickEventID";
    }
}
