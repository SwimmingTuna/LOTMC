package net.swimmingtuna.lotm.item.BeyonderAbilities.Monster;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
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

public class MisfortuneBestowal extends SimpleAbilityItem {
    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    public MisfortuneBestowal(Properties properties) {
        super(properties, BeyonderClassInit.MONSTER, 5, 250, 200, 50, 50);
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!player.level().isClientSide() && !interactionTarget.level().isClientSide()) {
            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }
            useSpirituality(player);
            addCooldown(player);
            misfortuneBestowal(interactionTarget, player);
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
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 50, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 50, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, gives the target misfortune and takes away some of your own"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("250").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("20 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }


    private static void misfortuneBestowal(LivingEntity interactionTarget, LivingEntity player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            CompoundTag pTag = interactionTarget.getPersistentData();
            double misfortune = tag.getDouble("misfortune");
            double pMisfortune = pTag.getDouble("misfortune");
            Style style = BeyonderUtil.getStyle(player);
            int misfortuneAddValue = (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.MISFORTUNEBESTOWAL.get());
            pTag.putDouble("misfortune", Math.min(200, pMisfortune + misfortuneAddValue));
            tag.putDouble("misfortune", Math.min(0, misfortune - ((double) misfortuneAddValue / 2)));
            float random = BeyonderUtil.getPositiveRandomInRange(900);
            if (random <= 5) {
                pTag.putInt("luckMeteor", 2);
                player.sendSystemMessage(Component.literal("You randomly bestowed a Meteor on " + interactionTarget.getName().getString()).withStyle(style));
            } else if (random <= 70) {
                player.sendSystemMessage(Component.literal("You randomly bestowed a Lightning Bolt on " + interactionTarget.getName().getString()).withStyle(style));
                pTag.putInt("luckLightningLOTM", 2);
            } else if (random <= 100) {
                player.sendSystemMessage(Component.literal("You randomly bestowed the next ability use to not work on " + interactionTarget.getName().getString()).withStyle(style));
                pTag.putInt("cantUseAbility", pTag.getInt("cantUseAbility") + 1);
            } else if (random <= 170) {
                player.sendSystemMessage(Component.literal("You randomly bestowed tripping on " + interactionTarget.getName().getString()).withStyle(style));
                pTag.putInt("luckParalysis", 2);
            } else if (random <= 240) {
                player.sendSystemMessage(Component.literal("You randomly bestowed unequipping armor on " + interactionTarget.getName().getString()).withStyle(style));
                pTag.putInt("luckUnequipArmor", 2);
            } else if (random <= 260) {
                player.sendSystemMessage(Component.literal("You randomly bestowed a warden on " + interactionTarget.getName().getString()).withStyle(style));
                pTag.putInt("luckWarden", 2);
            } else if (random <= 300) {
                player.sendSystemMessage(Component.literal("You randomly bestowed a lightning bolt on " + interactionTarget.getName().getString()).withStyle(style));
                pTag.putInt("luckLightningMC", 2);
            } else if (random <= 380) {
                player.sendSystemMessage(Component.literal("You randomly bestowed an illness on " + interactionTarget.getName().getString()).withStyle(style));
                pTag.putInt("luckPoison", 2);
            } else if (random <= 395) {
                player.sendSystemMessage(Component.literal("You randomly bestowed a tornado on " + interactionTarget.getName().getString()).withStyle(style));
                pTag.putInt("luckTornado", 2);
            } else if (random <= 440) {
                player.sendSystemMessage(Component.literal("You randomly bestowed a falling stone block on " + interactionTarget.getName().getString()).withStyle(style));
                pTag.putInt("luckStone", 2);
            } else if (random <= 500) {
                player.sendSystemMessage(Component.literal("You randomly bestowed next damage taken to be doubled on " + interactionTarget.getName().getString()).withStyle(style));
                pTag.putInt("luckDoubleDamage", pTag.getInt("luckDoubleDamage") + 1);
            } else if (random <= 510) {
                player.sendSystemMessage(Component.literal("You randomly bestowed a lightning storm on " + interactionTarget.getName().getString()).withStyle(style));
                pTag.putInt("calamityLightningStorm", 2);
            } else if (random <= 570) {
                player.sendSystemMessage(Component.literal("You randomly bestowed a ground tremor on " + interactionTarget.getName().getString()).withStyle(style));
                pTag.putInt("calamityGroundTremor", 2);
            } else if (random <= 600) {
                player.sendSystemMessage(Component.literal("You randomly bestowed a gaze that causes corruption on " + interactionTarget.getName().getString()).withStyle(style));
                pTag.putInt("calamityGaze", 2);
            } else if (random <= 650) {
                player.sendSystemMessage(Component.literal("You randomly bestowed an undead army on " + interactionTarget.getName().getString()).withStyle(style));
                pTag.putInt("calamityUndeadArmy", 2);
            } else if (random <= 690) {
                player.sendSystemMessage(Component.literal("You randomly bestowed a strengthened baby zombie on " + interactionTarget.getName().getString()).withStyle(style));
                pTag.putInt("calamityBabyZombie", 2);
            } else if (random <= 740) {
                player.sendSystemMessage(Component.literal("You randomly bestowed a cold breeze on " + interactionTarget.getName().getString()).withStyle(style));
                pTag.putInt("calamityBreeze", 2);
            } else if (random <= 800) {
                player.sendSystemMessage(Component.literal("You randomly bestowed a heat wave on " + interactionTarget.getName().getString()).withStyle(style));
                pTag.putInt("calamityWave", 2);
            } else if (random <= 900) {
                player.sendSystemMessage(Component.literal("You randomly bestowed a gas explosion on " + interactionTarget.getName().getString()).withStyle(style));
                pTag.putInt("calamityExplosion", 2);
            }
        }
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("MONSTER_ABILITY", ChatFormatting.GRAY);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null) {
            return 60;
        }
        return 0;
    }

}