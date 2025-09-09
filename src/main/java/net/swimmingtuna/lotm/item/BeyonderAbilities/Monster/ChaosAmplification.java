package net.swimmingtuna.lotm.item.BeyonderAbilities.Monster;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.CalamityEnhancementLeftClickC2S;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkill;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import net.swimmingtuna.lotm.world.worlddata.CalamityEnhancementData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ChaosAmplification extends LeftClickHandlerSkill {

    public ChaosAmplification(Properties properties) {
        super(properties, BeyonderClassInit.MONSTER, 1, 2000, 1200);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        changeChaosAmplification(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof Player player) {
            if (player.tickCount % 2 == 0 && !level.isClientSide()) {
                if (player.getMainHandItem().getItem() instanceof ChaosAmplification) {
                    player.displayClientMessage(Component.literal("Current Calamity Enhancement Value will be: " + player.getPersistentData().getInt("calamityEnhancementItemValue")).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GRAY), true);
                }
            }
        }
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    private void changeChaosAmplification(LivingEntity player) {
        int value = player.getPersistentData().getInt("calamityEnhancementItemValue");
        if (!player.level().isClientSide() && player.level() instanceof ServerLevel serverLevel) {
            CalamityEnhancementData data = CalamityEnhancementData.getInstance(serverLevel);
            data.setCalamityEnhancement(value);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, increases the strength of all calamities and unlucky events by the amount set"));
        tooltipComponents.add(Component.literal("Left Click to increase amplification"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("2000").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Minute").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("MONSTER_ABILITY", ChatFormatting.GRAY);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (!livingEntity.level().isClientSide() && livingEntity.level() instanceof ServerLevel serverLevel) {
            CalamityEnhancementData data = CalamityEnhancementData.getInstance(serverLevel);
            if (data.getCalamityEnhancement() < 2) {
                livingEntity.getPersistentData().putInt("calamityEnhancementItemValue", 2);
                return 50;
            }
        }
        return 0;
    }
    @Override
    public LeftClickType getleftClickEmpty() {
        return new CalamityEnhancementLeftClickC2S();
    }
}
