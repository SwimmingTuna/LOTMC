package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses.BeyonderTicks.Monster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;
import net.swimmingtuna.lotm.util.BeyonderUtil;

import static net.swimmingtuna.lotm.util.BeyonderUtil.applyMobEffect;

public class MonsterTickLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        int speed = 0;
        int resistance = 0;
        int strength = 0;
        int regen = 0;

        LivingEntity player = event.getEntity();
        int sequenceLevel = BeyonderUtil.getSequence(player);

        CompoundTag tag = player.getPersistentData();
        if (player.tickCount % 20 == 0) {
            if (player instanceof Player) {
                if (sequenceLevel == 8 || sequenceLevel == 7) {
                    if (player.getMainHandItem().getItem() instanceof SwordItem) {
                        applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 60, speed + 1, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof AxeItem) {
                        applyMobEffect(player, MobEffects.DAMAGE_BOOST, 60, strength + 1, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof PickaxeItem || player.getMainHandItem().getItem() instanceof ShovelItem) {
                        applyMobEffect(player, MobEffects.DIG_SPEED, 60, 1, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof BowItem || player.getMainHandItem().getItem() instanceof CrossbowItem) {
                        applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 60, speed + 1, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof ShieldItem || player.getOffhandItem().getItem() instanceof ShieldItem) {
                        applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 60, resistance + 1, true, true);
                    }
                } else if (sequenceLevel == 6 || sequenceLevel == 5) {
                    if (player.getMainHandItem().getItem() instanceof SwordItem) {
                        applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 60, speed + 1, true, true);
                        applyMobEffect(player, MobEffects.DIG_SPEED, 60, 0, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof AxeItem) {
                        applyMobEffect(player, MobEffects.DAMAGE_BOOST, 60, strength + 1, true, true);
                        applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 60, resistance + 1, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof PickaxeItem || player.getMainHandItem().getItem() instanceof ShovelItem) {
                        applyMobEffect(player, MobEffects.DIG_SPEED, 60, 2, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof BowItem || player.getMainHandItem().getItem() instanceof CrossbowItem) {
                        applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 60, speed + 1, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof ShieldItem || player.getOffhandItem().getItem() instanceof ShieldItem) {
                        applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 60, resistance + 1, true, true);
                    }
                } else if (sequenceLevel <= 4) {
                    if (player.getMainHandItem().getItem() instanceof SwordItem) {
                        applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 60, speed + 2, true, true);
                        applyMobEffect(player, MobEffects.DIG_SPEED, 60, 0, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof AxeItem) {
                        applyMobEffect(player, MobEffects.DAMAGE_BOOST, 60, strength + 1, true, true);
                        applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 60, resistance + 1, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof PickaxeItem || player.getMainHandItem().getItem() instanceof ShovelItem) {
                        applyMobEffect(player, MobEffects.DIG_SPEED, 60, 3, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof BowItem || player.getMainHandItem().getItem() instanceof CrossbowItem) {
                        applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 60, speed + 2, true, true);
                        applyMobEffect(player, MobEffects.REGENERATION, 60, regen + 1, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof ShieldItem || player.getOffhandItem().getItem() instanceof ShieldItem) {
                        applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 60, resistance + 2, true, true);
                    }
                }
            }
        }
    }

    @Override
    public String getID() {
        return "MonsterTickEventID";
    }
}
