package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses.BeyonderTicks.Warrior;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import static net.swimmingtuna.lotm.util.BeyonderUtil.applyMobEffect;

public class WarriorTickLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        LivingEntity player = event.getEntity();
        int sequenceLevel = BeyonderUtil.getSequence(player);

        if (!player.level().isClientSide()) {
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(player);
            boolean isGiant = player.getPersistentData().getBoolean("warriorGiant");
            boolean isHoGGiant = player.getPersistentData().getBoolean("handOfGodGiant");
            boolean isTwilightGiant = player.getPersistentData().getBoolean("twilightGiant");
            boolean x = !isGiant && !isHoGGiant && !isTwilightGiant;

            if (player.tickCount % 10 == 0) {
                if (player instanceof Player) {
                    if (sequenceLevel == 8) {
                        if (player.getMainHandItem().getItem() instanceof ShieldItem || player.getOffhandItem().getItem() instanceof ShieldItem) {
                            applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 20, 1, true, true);
                        }
                        if (player.getMainHandItem().getItem() instanceof SwordItem) {
                            applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 20, 1, true, true);
                        }
                    }
                    else if (sequenceLevel <= 7 && sequenceLevel >= 6) {
                        if (player.getMainHandItem().getItem() instanceof SwordItem) {
                            applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 20, 1, true, true);
                        }
                        if (player.getMainHandItem().getItem() instanceof AxeItem) {
                            applyMobEffect(player, MobEffects.DAMAGE_BOOST, 20, 1, true, true);
                        }
                        if (player.getMainHandItem().getItem() instanceof PickaxeItem || player.getMainHandItem().getItem() instanceof ShovelItem) {
                            applyMobEffect(player, MobEffects.DIG_SPEED, 20, 1, true, true);
                        }
                        if (player.getMainHandItem().getItem() instanceof BowItem || player.getMainHandItem().getItem() instanceof CrossbowItem) {
                            applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 20, 1, true, true);
                        }
                        if (player.getMainHandItem().getItem() instanceof ShieldItem || player.getOffhandItem().getItem() instanceof ShieldItem) {
                            applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 20,  1, true, true);
                        }
                    }
                    else if (sequenceLevel <= 5) {
                        if (player.getMainHandItem().getItem() instanceof SwordItem) {
                            applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 20, 2, true, true);
                            applyMobEffect(player, MobEffects.DIG_SPEED, 20, 0, true, true);
                        }
                        if (player.getMainHandItem().getItem() instanceof AxeItem) {
                            applyMobEffect(player, MobEffects.DAMAGE_BOOST, 20, 1, true, true);
                            applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 20, 1, true, true);
                        }
                        if (player.getMainHandItem().getItem() instanceof PickaxeItem || player.getMainHandItem().getItem() instanceof ShovelItem) {
                            applyMobEffect(player, MobEffects.DIG_SPEED, 20, 3, true, true);
                        }
                        if (player.getMainHandItem().getItem() instanceof BowItem || player.getMainHandItem().getItem() instanceof CrossbowItem) {
                            applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 20, 2, true, true);
                            applyMobEffect(player, MobEffects.REGENERATION, 20, 1, true, true);
                        }
                        if (player.getMainHandItem().getItem() instanceof ShieldItem || player.getOffhandItem().getItem() instanceof ShieldItem) {
                            applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 20,  1, true, true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getID() {
        return "WarriorTickEventID";
    }
}
