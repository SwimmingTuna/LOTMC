package net.swimmingtuna.lotm.item.BeyonderAbilities.Monster;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class LuckDenial extends SimpleAbilityItem {
    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    public LuckDenial(Properties properties) {
        super(properties, BeyonderClassInit.MONSTER, 4, 175, 100, 100, 100);
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!player.level().isClientSide()) {
            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }
            useSpirituality(player);
            addCooldown(player);
            giftLuck(player, interactionTarget);
        }
        return InteractionResult.SUCCESS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.lazyAttributeMap.get();
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    private Multimap<Attribute, AttributeModifier> createAttributeMap() {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> attributeBuilder = ImmutableMultimap.builder();
        attributeBuilder.putAll(super.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND));
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 100, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 100, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, doesnt allow a target to gain any luck or beneficial effects for a long period of time. If your sequence is less than two, then it will also not allow their misfortune value to go below that amount."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("175").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("5 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }


    private static void giftLuck(LivingEntity interactionTarget, LivingEntity player) {
        if (!player.level().isClientSide() && !interactionTarget.level().isClientSide()) {
            CompoundTag tag = interactionTarget.getPersistentData();
            double luck = tag.getDouble("luck");
            double misfortune = tag.getDouble("misfortune");
            double beneficialEffectBlocker = BeyonderUtil.getDamage(player).get(ItemInit.LUCKDENIAL.get()) / 5;
            double damage = BeyonderUtil.getDamage(player).get(ItemInit.MONSTERREBOOT.get());
            if (BeyonderUtil.getSequence(player) <= 2) {
                tag.putDouble("luckDenialTimer", damage * 27);
                tag.putDouble("luckDenialLuck", luck);
                tag.putDouble("luckDenialMisfortune", misfortune);
            } else {
                tag.putDouble("luckDenialTimer", damage * 27);
                tag.putDouble("luckDenialLuck", luck);
            }
            BeyonderUtil.applyBeneficialEffectBlocker(interactionTarget, (int) beneficialEffectBlocker);
        }
    }

    public static void luckDenial(LivingEntity livingEntity) {
        CompoundTag tag = livingEntity.getPersistentData();
        double luck = tag.getDouble("luck");
        double misfortune = tag.getDouble("misfortune");
        double luckDenialTimer = tag.getDouble("luckDenialTimer");
        double luckDenialLuck = tag.getDouble("luckDenialLuck");
        double luckDenialMisfortune = tag.getDouble("luckDenialMisfortune");
        if (luckDenialTimer >= 1) {
            tag.putDouble("luckDenialTimer", luckDenialTimer - 1);
            if (luck >= luckDenialLuck) {
                tag.putDouble("luck", luckDenialLuck);
            } else if (luck < luckDenialLuck) {
                tag.putDouble("luckDenialLuck", luck);
            }
            if (misfortune <= luckDenialMisfortune) {
                tag.putDouble("misfortune", luckDenialMisfortune);
            } else if (misfortune > luckDenialMisfortune) {
                tag.putDouble("luckDenialMisfortune", misfortune);
            }
        }
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("MONSTER_ABILITY", ChatFormatting.GRAY);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null && BeyonderUtil.getPathway(livingEntity) == BeyonderClassInit.MONSTER.get()) {
            return 80;
        }
        return 0;
    }

}