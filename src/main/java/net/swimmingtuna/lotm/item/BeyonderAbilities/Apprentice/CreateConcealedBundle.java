package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class CreateConcealedBundle extends SimpleAbilityItem {
    public CreateConcealedBundle(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 4, 300, 300);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity livingEntity, InteractionHand hand){
        if(!checkAll(livingEntity) && checkBundle(livingEntity)){
            return InteractionResult.FAIL;
        }
        createBundle(livingEntity);
        addCooldown(livingEntity, this, 300 * (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.CREATE_CONCEALED_BUNDLE.get()));
        useSpirituality(livingEntity);
        return InteractionResult.SUCCESS;
    }

    public static void createBundle(LivingEntity entity){
        if(entity.level().isClientSide()) return;
        if(checkBundle(entity)){
            ItemStack stack = new ItemStack(ItemInit.CONCEALED_BUNDLE.get());
            DimensionalSightTileEntity dimensionalSightTileEntity = BeyonderUtil.findNearbyDimensionalSight(entity);
            if (dimensionalSightTileEntity != null && dimensionalSightTileEntity.getScryTarget() != null && dimensionalSightTileEntity.getScryTarget() instanceof Player player) {
                entity.sendSystemMessage(Component.literal("You gave your Concealed Bundle to your Dimensional Sight Target").withStyle(ChatFormatting.AQUA));
                player.setItemInHand(InteractionHand.OFF_HAND, stack);
            } else {
            entity.setItemInHand(InteractionHand.OFF_HAND, stack);
            }
            stack.getOrCreateTag().putInt("concealedBundleRows",  9 - BeyonderUtil.getSequence(entity));
            if(BeyonderUtil.getSequence(entity) > 4) stack.getOrCreateTag().putInt("concealedBundleMaxDurability",  (9 - BeyonderUtil.getSequence(entity)) * 5);
            else stack.getOrCreateTag().putInt("concealedBundleMaxDurability",  (9 - BeyonderUtil.getSequence(entity) - 4) * 50);
            if(BeyonderUtil.getSequence(entity) == 0) stack.getOrCreateTag().putBoolean("concealedBundleUnbreakable",  true);
        }
    }

    public static boolean checkBundle(LivingEntity entity){
        boolean isBundle = entity.getItemInHand(InteractionHand.OFF_HAND).getItem() == Items.BUNDLE;
        if(!isBundle && entity instanceof Player player) player.displayClientMessage(Component.literal("No bundle in you off-hand.").withStyle(BeyonderUtil.getStyle(player)), true);
        return isBundle;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, transform a bundle in your off-hand into a special mystical item, containing a pocket dimension which can store many materials"));
        tooltipComponents.add(Component.literal("Be careful, as the bag will break and all items will be lost after a certain amount of uses.").withStyle(ChatFormatting.RED));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("300").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("15 Seconds").withStyle(ChatFormatting.YELLOW)));
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
        return 0;
    }
}