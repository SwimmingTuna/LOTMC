package net.swimmingtuna.lotm.item.OtherItems;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.EnchantmentInit;

import java.util.Map;

public class ConfusionRune extends Item {

    public ConfusionRune(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        if (!pLevel.isClientSide() && pPlayer.getMainHandItem().getItem() instanceof ConfusionRune) {
            if (!pPlayer.isShiftKeyDown()) {
                ItemStack offStack = pPlayer.getItemInHand(InteractionHand.OFF_HAND);
                Enchantment confusionEnchant = EnchantmentInit.CONFUSION_ENCHANTMENT.get();
                if (!offStack.isEmpty() && confusionEnchant.canEnchant(offStack)) {
                    Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(offStack);
                    enchantments.remove(confusionEnchant);
                    enchantments.put(confusionEnchant, 50);
                    EnchantmentHelper.setEnchantments(enchantments, offStack);
                    pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

                    pPlayer.awardStat(Stats.ITEM_USED.get(this));
                    if (!pPlayer.getAbilities().instabuild) {
                        stack.shrink(1);
                    }

                    return InteractionResultHolder.success(stack);
                }
            }
            return InteractionResultHolder.pass(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.EPIC;
    }
}

