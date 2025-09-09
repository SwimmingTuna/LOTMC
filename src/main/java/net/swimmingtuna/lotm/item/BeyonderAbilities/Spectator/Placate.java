package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Placate extends SimpleAbilityItem {

    public Placate(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 7, 125, 15);
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!player.level().isClientSide()) {
            if (!checkAll(player, BeyonderClassInit.SPECTATOR.get(), 7, 125, true)) {
                return InteractionResult.FAIL;
            }
            placate(player, interactionTarget);
            addCooldown(player);
            useSpirituality(player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player, BeyonderClassInit.SPECTATOR.get(), 7, 125, true)) {
            return InteractionResult.FAIL;
        }
        placate(player, player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    public static void placate(LivingEntity player, LivingEntity interactionTarget) {
        if (!interactionTarget.level().isClientSide()) {
            CompoundTag tag = interactionTarget.getPersistentData();
            int awe = tag.getInt("LOTMAwe");
            int manipulation = tag.getInt("LOTMManipulation");
            int frenzy = tag.getInt("LOTMFrenzy");
            int stun = tag.getInt("LOTMStun");
            int mentalPlague = tag.getInt("LOTMMentalPlague");
            int sequence = BeyonderUtil.getSequence(player);
            if (sequence > 5) {
                if (BeyonderUtil.hasAwe(interactionTarget)) {
                    interactionTarget.getPersistentData().putInt("LOTMAwe", awe / 2);
                }
                if (BeyonderUtil.hasManipulation(interactionTarget)) {
                    interactionTarget.getPersistentData().putInt("LOTMManipulation", manipulation / 2);
                }
                if (BeyonderUtil.hasFrenzy(interactionTarget)) {
                    interactionTarget.getPersistentData().putInt("LOTMFrenzy", frenzy / 2);
                }
                if (BeyonderUtil.hasStun(interactionTarget)) {
                    interactionTarget.getPersistentData().putInt("LOTMStun", stun / 2);
                }
                if (BeyonderUtil.hasMentalPlague(interactionTarget)) {
                    interactionTarget.getPersistentData().putInt("LOTMMentalPlague", 0);
                }
            } else {
                if (BeyonderUtil.hasAwe(interactionTarget)) {
                    interactionTarget.getPersistentData().putInt("LOTMAwe", 0);
                }
                if (BeyonderUtil.hasManipulation(interactionTarget)) {
                    interactionTarget.getPersistentData().putInt("LOTMManipulation", 0);
                }
                if (BeyonderUtil.hasFrenzy(interactionTarget)) {
                    interactionTarget.getPersistentData().putInt("LOTMFrenzy", 0);
                }
                if (BeyonderUtil.hasStun(interactionTarget)) {
                    interactionTarget.getPersistentData().putInt("LOTMStun", 0);
                }
                if (BeyonderUtil.hasMentalPlague(interactionTarget)) {
                    interactionTarget.getPersistentData().putInt("LOTMMentalPlague", 0);
                }
            }
            if (sequence == 4 || sequence == 3) {
                halfHarmfulEffects(interactionTarget);
            } else if (sequence == 2) {
                seventyFivePercentHarmfulEffects(interactionTarget);
            } else if (sequence == 1 || sequence == 0) {
                removeHarmfulEffects(interactionTarget);
            }
        }
    }

    public static void removeHarmfulEffects(LivingEntity entity) {
        if (!entity.level().isClientSide()) {
            List<MobEffect> effectsToRemove = new ArrayList<>();
            for (MobEffectInstance effect : entity.getActiveEffects()) {
                MobEffect type = effect.getEffect();
                if (!type.isBeneficial()) {
                    effectsToRemove.add(type);
                }
            }
            for (MobEffect effect : effectsToRemove) {
                entity.removeEffect(effect);
            }
        }
    }

    public static void halfHarmfulEffects(LivingEntity entity) {
        if (!entity.level().isClientSide()) {
            List<MobEffectInstance> effectsToModify = new ArrayList<>();
            for (MobEffectInstance effect : entity.getActiveEffects()) {
                MobEffect type = effect.getEffect();
                if (!type.isBeneficial()) {
                    effectsToModify.add(effect);
                }
            }
            for (MobEffectInstance effect : effectsToModify) {
                MobEffect type = effect.getEffect();
                int newDuration = (effect.getDuration() + 1) / 2;
                entity.removeEffect(type);
                entity.addEffect(new MobEffectInstance(type, newDuration, effect.getAmplifier(), effect.isAmbient(), effect.isVisible()));
            }
        }
    }

    public static void seventyFivePercentHarmfulEffects(LivingEntity entity) {
        if (!entity.level().isClientSide()) {
            List<MobEffectInstance> effectsToModify = new ArrayList<>();
            for (MobEffectInstance effect : entity.getActiveEffects()) {
                MobEffect type = effect.getEffect();
                if (!type.isBeneficial()) {
                    effectsToModify.add(effect);
                }
            }
            for (MobEffectInstance effect : effectsToModify) {
                MobEffect type = effect.getEffect();
                int newDuration = (int) ((effect.getDuration() + 1) * 0.25);
                entity.removeEffect(type);
                entity.addEffect(new MobEffectInstance(type, newDuration, effect.getAmplifier(), effect.isAmbient(), effect.isVisible()));
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use on an entity, placate them, removing or halving their harmful potion effects, depending on your sequence. "));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("250").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("10 Seconds").withStyle(ChatFormatting.YELLOW)));
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
        int basePriority = 0;
        Collection<MobEffectInstance> activeEffects = livingEntity.getActiveEffects();
        int harmfulEffectsCount = 0;
        for (MobEffectInstance effect : activeEffects) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                harmfulEffectsCount++;
            }
        }
        return Math.min(100, basePriority + (harmfulEffectsCount * 20));
    }
}
