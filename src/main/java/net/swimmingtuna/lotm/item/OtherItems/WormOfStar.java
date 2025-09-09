package net.swimmingtuna.lotm.item.OtherItems;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class WormOfStar extends Item {


    public WormOfStar(Properties properties) {
        super(properties);

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        wormOfStarCopy(pPlayer, pUsedHand);
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return LOTM.getMaxStackCount();
    }

    public static void wormOfStarCopy(LivingEntity player, InteractionHand hand) {
        if (!player.level().isClientSide()) {
            int spiritualityImbuedAmount = player.getPersistentData().getInt("wormOfStarSpiritualityAmount");
            PlayerMobEntity playerMobEntity = new PlayerMobEntity(EntityInit.PLAYER_MOB_ENTITY.get(), player.level());
            playerMobEntity.setIsClone(true);
            BeyonderUtil.useSpirituality(player, spiritualityImbuedAmount);
            playerMobEntity.setUsername(player.getScoreboardName());
            playerMobEntity.setMaxSpirituality(spiritualityImbuedAmount);
            playerMobEntity.setSpirituality(spiritualityImbuedAmount);
            playerMobEntity.teleportTo(player.getX(), player.getY(), player.getZ());
            playerMobEntity.setSequence(Math.min(9,BeyonderUtil.getSequence(player) + 2));
            playerMobEntity.setPathway(BeyonderUtil.getPathway(player));
            playerMobEntity.setRegenSpirituality(false);
            playerMobEntity.setCreator(player);
            player.getItemInHand(hand).setCount(player.getItemInHand(hand).getCount() - 1);
            player.level().addFreshEntity(playerMobEntity);
        }
    }


    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return pStack.isDamaged();
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("A worm made from high sequences of the Apprentice Pathway.").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.literal("If you're a high sequence beyonder of the Apprentice Pathway, you can shift while holding this item to choose how much spirituality that should be put into it.").withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.BOLD));
        tooltipComponents.add(Component.literal("Depending on the amount of spirituality, a copy of yourself will be made which can attack, however it will be unable to regenerate spirituality.").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
        tooltipComponents.add(Component.literal("The sequence will always be 2 less than your own.").withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}