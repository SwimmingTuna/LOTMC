package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.ConsciousnessStrollC2S;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkill;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ConsciousnessStroll extends LeftClickHandlerSkill {

    public ConsciousnessStroll (Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 3, 300, 400);
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Type a player's name in chat to teleport to their location in the form of your spirit body, not being able to be seen or hurt. Teleporting back after a few seconds, with the duration increasing if the target is another dimension to account for loading"));
        tooltipComponents.add(Component.literal("Left click in order to choose whether or not you return to your original location after three seconds."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("500").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("20 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof Player player) {
            if (player.tickCount % 2 == 0 && !level.isClientSide()) {
                if (player.getMainHandItem().getItem() instanceof ConsciousnessStroll) {
                    player.displayClientMessage(Component.literal(misfortuneManipulationString(player)).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GRAY), true);
                }
            }
        }
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    public static String misfortuneManipulationString(Player pPlayer) {
        CompoundTag tag = pPlayer.getPersistentData();
        boolean cs = tag.getBoolean("consciousnessStrollChoice");
        if (!cs) {
            return "You will stay at the target's location after briefly viewing them";
        }
        return "You will NOT stay at the target's location after briefly viewing them";
    }
    public static void consciousnessStroll(LivingEntity livingEntity) {
        //CONSCIOUSNESS STROLL
        if (!(livingEntity instanceof ServerPlayer serverPlayer)) return;

        CompoundTag tag = livingEntity.getPersistentData();
        boolean cs = tag.getBoolean("consciousnessStrollChoice");
        int strollCounter = tag.getInt("consciousnessStrollActivated");
        int consciousnessStrollActivatedX = tag.getInt("consciousnessStrollActivatedX");
        int consciousnessStrollActivatedY = tag.getInt("consciousnessStrollActivatedY");
        int consciousnessStrollActivatedZ = tag.getInt("consciousnessStrollActivatedZ");
        String originalDimension = tag.getString("consciousnessStrollDimension");
        ResourceLocation dimLocation = ResourceLocation.tryParse(originalDimension);
        ResourceKey<Level> targetDimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.tryParse(originalDimension));


        if (strollCounter >= 1) {
            tag.putInt("consciousnessStrollActivated", strollCounter - 1);
            serverPlayer.setGameMode(GameType.SPECTATOR);
        }
        ServerLevel targetLevel = serverPlayer.getServer().getLevel(targetDimension);
        if (strollCounter == 1) {
            if (cs) {
                if (targetLevel != null) {
                    livingEntity.changeDimension(targetLevel);
                }
                livingEntity.teleportTo(consciousnessStrollActivatedX, consciousnessStrollActivatedY, consciousnessStrollActivatedZ);
            }
            String string = serverPlayer.getPersistentData().getString("consciousnessStrollGamemode");
            if (string.equalsIgnoreCase("creative")) {
                serverPlayer.setGameMode(GameType.CREATIVE);
            } else if (string.equalsIgnoreCase("spectator")) {
                serverPlayer.setGameMode(GameType.SPECTATOR);
            } else {
                serverPlayer.setGameMode(GameType.SURVIVAL);
            }
            EventManager.removeFromRegularLoop(livingEntity, EFunctions.CONSCIOUSNESS_STROLL.get());
        }
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SPECTATOR_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        return 0;
    }

    @Override
    public LeftClickType getleftClickEmpty() {
        return new ConsciousnessStrollC2S();
    }
}
