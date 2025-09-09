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
import net.swimmingtuna.lotm.networking.packet.UpdateItemInHandC2S;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ManipulateEmotion extends LeftClickHandlerSkillP {

    public ManipulateEmotion(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 4, 500, 1200);
    }


    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        int dreamIntoReality = BeyonderUtil.getDreamIntoReality(player);
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player, this, 1200 / dreamIntoReality);
        useSpirituality(player, 500);
        manipulateEmotion(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, all entities around you that are being manipulated hurt themself for great damage, corresponding to their max health."));
        tooltipComponents.add(Component.literal("Left Click for Manipulate Movement"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("500").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Minute").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    private static void manipulateEmotion(LivingEntity player) {
        if (!player.level().isClientSide()) {
            float damage = (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.MANIPULATE_EMOTION.get());
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(250))) {
                if (entity != player && BeyonderUtil.hasManipulation(entity) && !BeyonderUtil.areAllies(player, entity)) {
                    int sequence = BeyonderUtil.getSequence(player);
                    int newSequence = 4;
                    if (sequence == 0) {
                        newSequence = 1;
                    }
                    entity.hurt(BeyonderUtil.genericSource(player, entity), Math.min(entity.getMaxHealth() / 3, damage * (5 - newSequence)));
                    BeyonderUtil.removeManipulation(entity);
                }
            }
        }
    }
    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SPECTATOR_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null && BeyonderUtil.hasManipulation(target)) {
            return 100;
        }
        return 0;
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.MANIPULATE_MOVEMENT.get()));
    }
}
