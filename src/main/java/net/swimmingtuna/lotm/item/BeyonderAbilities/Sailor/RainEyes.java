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
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class RainEyes extends SimpleAbilityItem {

    public RainEyes(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 2, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        rainEyesAbility(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, enable or disable your rain eyes. If enabled, any beyonders or players will have their location, sequence, pathway, name, and distance from you shared."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("None").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static void rainEyesAbility(LivingEntity player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            boolean rainEyes = tag.getBoolean("rainEyes");

            if (rainEyes) {
                tag.putBoolean("rainEyes", false);

                EventManager.removeFromRegularLoop(player, EFunctions.RAIN_EYES.get());

                if (player instanceof Player pPlayer) {
                    pPlayer.displayClientMessage(Component.literal("Rain eyes disabled").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE), true);
                }
            } else {
                tag.putBoolean("rainEyes", true);

                EventManager.addToRegularLoop(player, EFunctions.RAIN_EYES.get());

                if (player instanceof Player pPlayer) {
                    pPlayer.displayClientMessage(Component.literal("Rain eyes enabled").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE), true);
                }
            }
        }
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        return 0;
    }
}
