package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.UpdateItemInHandC2S;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class EnvisionHealth extends LeftClickHandlerSkillP {

    public EnvisionHealth(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 0, 0, 600);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player, BeyonderClassInit.SPECTATOR.get(), 0, 3500 / BeyonderUtil.getDreamIntoReality(player), true)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player, 3500 / BeyonderUtil.getDreamIntoReality(player));
        envisionHealth(player);
        return InteractionResult.SUCCESS;
    }

    private void envisionHealth(LivingEntity player) {
        if (!player.level().isClientSide()) {
            AttributeInstance maxHP = player.getAttribute(Attributes.MAX_HEALTH);
            double maxHealth = maxHP.getValue();
            double health = player.getHealth();
            double x = (health + ((maxHealth - health) * (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_HEALTH.get())));
            player.setHealth((float) x);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, envision a chunk of your missing health"));
        tooltipComponents.add(Component.literal("Left Click for Envision Life"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("3500").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("30 seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SPECTATOR_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (livingEntity.getHealth() <= 25) {
            return 85;
        } else {
            return 25;
        }
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.ENVISION_LIFE.get()));
    }
}
