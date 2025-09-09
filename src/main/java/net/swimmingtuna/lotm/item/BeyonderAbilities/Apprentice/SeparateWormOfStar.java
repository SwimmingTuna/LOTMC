package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;


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
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class SeparateWormOfStar extends SimpleAbilityItem {

    public SeparateWormOfStar(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 4, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        separateWormOfStar(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    private void separateWormOfStar(LivingEntity player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            boolean x = tag.getBoolean("wormOfStarChoice");
            tag.putBoolean("wormOfStarChoice", !x);
            String message = "Worms of Star will " + (!x ? "" : "NOT ") + "be used to reduce cooldown";
            if (player instanceof Player pPlayer) {
                pPlayer.displayClientMessage(Component.literal(message).withStyle(BeyonderUtil.getStyle(player)), true);
            }
        }
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, enable or disable your consumption of Worm of Stars. If enabled, your ability cooldowns will be lowered when they're used in exchange for Worms of Star."));
        tooltipComponents.add(Component.literal("Type a number in chat in order to separate that amount of worm of stars from you"));
        tooltipComponents.add(Component.literal("Don't take away too many, as if you don't have enough, you will suffer consequences").withStyle(ChatFormatting.RED));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("0").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICE_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        boolean x = livingEntity.getPersistentData().getBoolean("wormOfStarChoice");
        int wormCount = livingEntity.getPersistentData().getInt("wormOfStar");
        int maxWormCount = 100;
        int sequenceLevel = BeyonderUtil.getSequence(livingEntity);
        if (sequenceLevel == 4) {
            maxWormCount = 200;
        }
        if (sequenceLevel == 3) {
            maxWormCount = 800;
        }
        if (sequenceLevel == 2) {
            maxWormCount = 4000;
        }
        if (sequenceLevel == 1) {
            maxWormCount = 16000;
        }
        if (sequenceLevel == 0) {
            maxWormCount = 80000;
        }
        if (wormCount > maxWormCount * 0.75 && !x) {
            return 100;
        } else if (wormCount < maxWormCount * 0.75 && x && target == null) {
            return 100;
        }
        return 0;
    }
}
