package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.networking.packet.UpdateItemInHandC2S;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class WindManipulationFlight extends LeftClickHandlerSkillP {


    public WindManipulationFlight(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 6, 0, 100);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        if (BeyonderUtil.getSequence(player) <= 4 && player instanceof Player) {
            toggleFlying(player);
        } else {
            useSpirituality(player,40);
            flightRegular(player);
        }
        CompoundTag tag = player.getPersistentData();
        boolean sailorFlight1 = tag.getBoolean("sailorFlight1");
        if ( !sailorFlight1) {
            addCooldown(player);
        }
        return InteractionResult.SUCCESS;
    }

    public static void flightRegular(LivingEntity player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            EventManager.addToRegularLoop(player, EFunctions.WIND_MANIPULATION_FLIGHT.get());
            tag.putInt("sailorFlight", 1);
            tag.putInt("sailorFlightDamageCancel", 1);
        }
    }

    public static void startFlying(LivingEntity player) { //marked
        if (!player.level().isClientSide() && player instanceof Player) {
            player.getPersistentData().putBoolean("sailorFlight1", true);
            EventManager.addToRegularLoop(player, EFunctions.WIND_MANIPULATION_FLIGHT.get());
            BeyonderUtil.startFlying(player, 0.1f, 20);
        }
    }
    public static void toggleFlying(LivingEntity player) {
        if (!player.level().isClientSide()) {
            boolean canFly = player.getPersistentData().getBoolean("sailorFlight1");
            if (canFly) {
                player.sendSystemMessage(Component.literal("Wind Manipulation (Flight) turned off").withStyle(ChatFormatting.RED));
                stopFlying(player);
            } else {
                player.sendSystemMessage(Component.literal("Wind Manipulation (Flight) turned on").withStyle(ChatFormatting.GREEN));
                startFlying(player);
            }
        }
    }

    public static void stopFlying(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide() && livingEntity instanceof Player player) { //marked
            player.getPersistentData().putBoolean("sailorFlight1", false);
            BeyonderUtil.stopFlying(livingEntity);
            EventManager.removeFromRegularLoop(player, EFunctions.WIND_MANIPULATION_FLIGHT.get());
        }
    }

    public static void windManipulationGuide(LivingEntity livingEntity) {
        //WIND MANIPULATION GLIDE
        CompoundTag tag = livingEntity.getPersistentData();
        boolean enhancedFlight = tag.getBoolean("sailorFlight1");
        if (BeyonderUtil.currentPathwayAndSequenceMatchesNoException(livingEntity, BeyonderClassInit.SAILOR.get(), 6) && livingEntity.isShiftKeyDown() && livingEntity.fallDistance >= 3 && !(livingEntity instanceof Player player && player.getAbilities().instabuild) && !enhancedFlight) {
            Vec3 movement = livingEntity.getDeltaMovement();
            if (movement.y() < 0) {
                double deltaX = Math.cos(Math.toRadians(livingEntity.getYRot() + 90)) * 0.06;
                double deltaZ = Math.sin(Math.toRadians(livingEntity.getYRot() + 90)) * 0.06;
                livingEntity.setDeltaMovement(movement.x + deltaX, -0.05, movement.z + deltaZ);
                livingEntity.fallDistance = 5;
                livingEntity.hurtMarked = true;
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.literal("Upon use, uses the wind to burst forward in the direction the player is looking three times or allow the user to fly, depending on the sequence"));
        tooltipComponents.add(Component.literal("Left Click for Wind Manipulation (Sense)"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("40 / 40 Per Second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("5 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, isAdvanced);
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null && target.distanceTo(livingEntity) >= 10) {
            return 50;
        }
        return 0;
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.WIND_MANIPULATION_SENSE.get()));
    }

}
