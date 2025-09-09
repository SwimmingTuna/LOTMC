package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class Awe extends SimpleAbilityItem {

    public Awe(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 7, 75, 300);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        useSpirituality(player);
        addCooldown(player);
        applyPotionEffectToEntities(player);
        return InteractionResult.SUCCESS;
    }


    public static void applyPotionEffectToEntities(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            int sequence = BeyonderUtil.getSequence(livingEntity);
            int dir = BeyonderUtil.getDreamIntoReality(livingEntity);
            double radius = (18.0 - sequence) * dir;
            int damage = (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.AWE.get());
            for (LivingEntity entity : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(radius))) {
                if (entity != livingEntity && !BeyonderUtil.areAllies(livingEntity, entity)) {
                    BeyonderUtil.applyAwe(entity, damage);
                    BeyonderUtil.applyMentalDamage(livingEntity, entity, (float) damage / 10);
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, makes all living entities around the user freeze in place and take damage"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("75").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("12 Seconds").withStyle(ChatFormatting.YELLOW)));
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
        int dreamIntoReality = BeyonderUtil.getDreamIntoReality(livingEntity);
        if (target != null && target.distanceTo(livingEntity) <= (18 - BeyonderUtil.getSequence(livingEntity)) * dreamIntoReality) {
            return (int) (100 - (target.distanceTo(livingEntity) * 2));
        }
        return 0;
    }
}
