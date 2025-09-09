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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.UpdateItemInHandC2S;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ProbabilityManipulationFortune extends LeftClickHandlerSkillP {

    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    public ProbabilityManipulationFortune(Properties properties) {
        super(properties, BeyonderClassInit.MONSTER, 0, 1000, 400, 777, 777);
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
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 150, AttributeModifier.Operation.ADDITION)); // adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 150, AttributeModifier.Operation.ADDITION)); // adds a 12 block reach for interacting with blocks, pretty much useless for this item
        return attributeBuilder.build();
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use on a target or typing a player's name in chat, gives them all fortunate events. If not used on a target, causes everything around you to gain them."));
        tooltipComponents.add(Component.literal("Left click for Probability Manipulation: Misfortune"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("1000 if on a single target, 3500 otherwise").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("20 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) { //add if cursor is on a projectile, lightning goes to projectile and pwoers it
        if (!checkAll(player, BeyonderClassInit.MONSTER.get(), 3500, 1000, true)) {
            return InteractionResult.FAIL;
        }
        probabilityWipeWorld(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack pStack, LivingEntity player, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        if (!player.level().isClientSide() && !pInteractionTarget.level().isClientSide()) {
            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }
            probabilityWipeEntity(pInteractionTarget);
            addCooldown(player, this, 10 + BeyonderUtil.getSequence(player));
            useSpirituality(player, 200);
        }
        return InteractionResult.SUCCESS;

    }

    public static void probabilityWipeEntity(LivingEntity pInteractionTarget) {
        if (!pInteractionTarget.level().isClientSide()) {
            giveFortuneEvents(pInteractionTarget);
        }
    }

    public static void probabilityWipeWorld(LivingEntity player) {
        Level level = player.level();
        if (!level.isClientSide()) {
            for (Player pPlayer : level.players()) {
                for (LivingEntity livingEntity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().inflate(BeyonderUtil.getDamage(player).get(ItemInit.PROBABILITYFORTUNE.get())))) {
                    giveFortuneEvents(livingEntity);
                }
            }
        }
    }

    public static void giveFortuneEvents(LivingEntity livingEntity) {
        CompoundTag tag = livingEntity.getPersistentData();
        tag.putInt("luckIgnoreDamage", tag.getInt("luckIgnoreDamage" + 5));
        tag.putInt("luckDiamonds", tag.getInt("luckDiamonds" + 10));
        tag.putInt("luckRegeneration", tag.getInt("luckRegeneration" + 10));
        tag.putInt("windMovingProjectilesCounter", tag.getInt("windMovingProjectilesCounter" + 10));
        tag.putInt("luckHalveDamage", tag.getInt("luckHalveDamage" + 15));
        tag.putInt("luckIgnoreMobs", tag.getInt("luckIgnoreMobs" + 10));
        tag.putInt("luckAttackerPoisoned", tag.getInt("luckIgnoreDamage" + 10));
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("MONSTER_ABILITY", ChatFormatting.GRAY);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) { //marked make it work on self
        return 0;
    }

    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.PROBABILITYMISFORTUNE.get()));
    }
}
