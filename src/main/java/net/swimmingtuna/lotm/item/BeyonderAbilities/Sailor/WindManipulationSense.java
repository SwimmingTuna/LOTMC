package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.UpdateItemInHandC2S;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class WindManipulationSense extends LeftClickHandlerSkillP {


    public WindManipulationSense(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 6, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        windManipulationSenseAbility(player);
        return InteractionResult.SUCCESS;
    }

    public static void windManipulationSenseAbility(LivingEntity player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            boolean windManipulationSense = tag.getBoolean("windManipulationSense");
            tag.putBoolean("windManipulationSense", !windManipulationSense);
            if (player instanceof Player pPlayer) {
                pPlayer.displayClientMessage(Component.literal("Wind Sense Turned " + (windManipulationSense ? "Off" : "On")).withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE), true);
            }
            EventManager.addToRegularLoop(player, EFunctions.WIND_MANIPULATION_SENSE.get());
            int sequence = BeyonderUtil.getSequence(player);
            double radius = 100 - (sequence * 10);
            for (LivingEntity otherPlayer : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius))) {
                if (otherPlayer == player) {
                    continue;
                }
                if (!(otherPlayer instanceof Player) && BeyonderUtil.getPathway(otherPlayer) != null) {
                    continue;
                }
                Vec3 directionToPlayer = otherPlayer.position().subtract(player.position()).normalize();
                Vec3 lookAngle = player.getLookAngle();
                double horizontalAngle = Math.atan2(directionToPlayer.x, directionToPlayer.z) - Math.atan2(lookAngle.x, lookAngle.z);

                String horizontalDirection;
                if (Math.abs(horizontalAngle) < Math.PI / 4) {
                    horizontalDirection = "in front of";
                } else if (horizontalAngle < -Math.PI * 3 / 4 || horizontalAngle > Math.PI * 3 / 4) {
                    horizontalDirection = "behind";
                } else if (horizontalAngle < 0) {
                    horizontalDirection = "to the right of";
                } else {
                    horizontalDirection = "to the left of";
                }

                String verticalDirection;
                if (directionToPlayer.y > 0.2) {
                    verticalDirection = "above";
                } else if (directionToPlayer.y < -0.2) {
                    verticalDirection = "below";
                } else {
                    verticalDirection = "at the same level as";
                }

                String message = otherPlayer.getName().getString() + " is " + horizontalDirection + " and " + verticalDirection + " you.";
                if (player.tickCount % 200 == 0) {
                    player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE));
                }
            }
        }
    }

    public static void windManipulationSense(LivingEntity livingEntity) {
        CompoundTag tag = livingEntity.getPersistentData();
        boolean windManipulationSense = tag.getBoolean("windManipulationSense");
        if (!windManipulationSense) {
            EventManager.removeFromRegularLoop(livingEntity, EFunctions.WIND_MANIPULATION_SENSE.get());
            return;
        }
        if (BeyonderUtil.getSpirituality(livingEntity) <= 1) {
            EventManager.removeFromRegularLoop(livingEntity, EFunctions.WIND_MANIPULATION_SENSE.get());
            if (livingEntity instanceof Player player) {
                player.displayClientMessage(Component.literal("Wind Manipulation (Sense) turned off due to lack of spirituality").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD), true);
            }
            return;
        }
        BeyonderUtil.useSpirituality(livingEntity, 1);
        double radius = 100 - (BeyonderUtil.getSequence(livingEntity) * 10);
        for (LivingEntity otherPlayer : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(radius))) {
            if (otherPlayer == livingEntity || BeyonderUtil.areAllies(livingEntity, otherPlayer)) {
                continue;
            }
            int lowestSequence = BeyonderUtil.getSequence(livingEntity) + 2;
            if (BeyonderUtil.getSequence(otherPlayer) > lowestSequence) {
                continue;
            }
            Vec3 directionToPlayer = otherPlayer.position().subtract(livingEntity.position()).normalize();
            Vec3 lookAngle = livingEntity.getLookAngle();
            double horizontalAngle = Math.atan2(directionToPlayer.x, directionToPlayer.z) - Math.atan2(lookAngle.x, lookAngle.z);

            String horizontalDirection;
            if (Math.abs(horizontalAngle) < Math.PI / 4) {
                horizontalDirection = "in front of";
            } else if (horizontalAngle < -Math.PI * 3 / 4 || horizontalAngle > Math.PI * 3 / 4) {
                horizontalDirection = "behind";
            } else if (horizontalAngle < 0) {
                horizontalDirection = "to the right of";
            } else {
                horizontalDirection = "to the left of";
            }

            String verticalDirection;
            if (directionToPlayer.y > 0.2) {
                verticalDirection = "above";
            } else if (directionToPlayer.y < -0.2) {
                verticalDirection = "below";
            } else {
                verticalDirection = "at the same level as";
            }

            String message = otherPlayer.getName().getString() + " is " + horizontalDirection + " and " + verticalDirection + " you and " + (int) otherPlayer.distanceTo(livingEntity) + " blocks away." ;
            if (livingEntity.tickCount % 140 == 0) {
                livingEntity.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE));
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, summon constant winds around you. While active, you'll get the location of nearby entities that aren't far weaker than you"));
        tooltipComponents.add(Component.literal("Left Click for Wind Manipulation (Blade)"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("20 per second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        return 0;
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.WIND_MANIPULATION_BLADE.get()));
    }
}