package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class BlinkState extends SimpleAbilityItem {


    public BlinkState(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 5, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        useSpirituality(player);
        addCooldown(player);
        enableDisableBlinkState(player);
        return InteractionResult.SUCCESS;
    }

    public static void enableDisableBlinkState(LivingEntity player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            boolean blinkState = tag.getBoolean("doorBlinkState");
            tag.putBoolean("doorBlinkState", !blinkState);

            if (blinkState) {
                BeyonderUtil.stopFlying(player);
                tag.putInt("doorBlinkStateDistance", 0);
                EventManager.removeFromRegularLoop(player, EFunctions.BLINK_STATE.get());
            }
            else {
                BeyonderUtil.startFlying(player, 0.2f, 20);
                EventManager.addToRegularLoop(player, EFunctions.BLINK_STATE.get());
            }

            if (player instanceof Player pPlayer) {
                pPlayer.displayClientMessage(Component.literal("Blink State Turned " + (blinkState ? "Off" : "On")).withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE), true);
            }
        }
    }


    public static void secretsSorcererBlinkState(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();

        if (!livingEntity.level().isClientSide()) {
            if (tag.getBoolean("doorBlinkState")) {
                if (livingEntity.tickCount % 2 == 0) {
                    tag.putInt("doorBlinkStateX1", (int) livingEntity.getX());
                    tag.putInt("doorBlinkStateY1", (int) livingEntity.getY());
                    tag.putInt("doorBlinkStateZ1", (int) livingEntity.getZ());
                }
                else {
                    tag.putInt("doorBlinkStateX2", (int) livingEntity.getX());
                    tag.putInt("doorBlinkStateY2", (int) livingEntity.getY());
                    tag.putInt("doorBlinkStateZ2", (int) livingEntity.getZ());
                }

                if (livingEntity.onGround()) {
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SPEED, 20, 5, false, false);
                }
                else if (livingEntity instanceof Mob) {
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SPEED, 20, 5, false, false);
                }

                BeyonderUtil.startFlying(livingEntity, 0.2f, 20);
                if (tag.getInt("doorBlinkStateX1") != 0 && tag.getInt("doorBlinkStateX2") != 0) {
                    int x1 = tag.getInt("doorBlinkStateX1");
                    int y1 = tag.getInt("doorBlinkStateY1");
                    int z1 = tag.getInt("doorBlinkStateZ1");
                    int x2 = tag.getInt("doorBlinkStateX2");
                    int y2 = tag.getInt("doorBlinkStateY2");
                    int z2 = tag.getInt("doorBlinkStateZ2");
                    BlockPos blockPos1 = new BlockPos(x1, y1, z1);
                    BlockPos blockPos2 = new BlockPos(x2, y2, z2);
                    int distance = blockPos1.distManhattan(blockPos2);
                    int blinkStateDistance = tag.getInt("doorBlinkStateDistance");
                    tag.putInt("doorBlinkStateDistance", blinkStateDistance + distance);

                    if (BeyonderUtil.getSpirituality(livingEntity) >= distance *  (Math.max(1, BeyonderUtil.getDamage(livingEntity).get(ItemInit.BLINK_STATE.get()) / 2))) {
                        BeyonderUtil.useSpirituality(livingEntity, distance * (int) (float) (Math.max(1,BeyonderUtil.getDamage(livingEntity).get(ItemInit.BLINK_STATE.get()) / 2)));
                    }
                    else {
                        tag.putBoolean("doorBlinkState", false);
                        BeyonderUtil.stopFlying(livingEntity);

                    }

                    if (blinkStateDistance >= 12) {
                        tag.putBoolean("wasInvisibleLastTick", false);
                        BeyonderUtil.setInvisible(livingEntity, false, 0);
                    }
                }

                int blinkStateTimer = tag.getInt("blinkStateTimer");
                boolean wasInvisible = tag.getBoolean("wasInvisibleLastTick");
                boolean shouldBeInvisible;

                if (blinkStateTimer >= 2) {
                    tag.putInt("blinkStateTimer", blinkStateTimer - 1);
                    shouldBeInvisible = true;
                }
                else if (blinkStateTimer == 1) {
                    shouldBeInvisible = false;
                    tag.putInt("blinkStateTimer", 10);
                }
                else {
                    shouldBeInvisible = true;
                    tag.putInt("blinkStateTimer", 10);
                }

                if (shouldBeInvisible != wasInvisible) {
                    tag.putBoolean("wasInvisibleLastTick", shouldBeInvisible);
                    BeyonderUtil.setInvisible(livingEntity, true, 1);
                }
            }
            else {
                boolean wasInvisible = tag.getBoolean("wasInvisibleLastTick");

                if (wasInvisible) {
                    tag.putBoolean("wasInvisibleLastTick", false);
                    BeyonderUtil.setInvisible(livingEntity, false, 0);
                }
            }
        }
    }

    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

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

        //reach should be___
        attributeBuilder.putAll(super.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND));
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 12, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 12, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Use in order to enable or disable your blink state. If enabled, you will be able to fly, and for each block you travel, spirituality will be used, but the next ability you use will have it's cooldown reduced depending on the distance traveled since you last used an ability."));
        tooltipComponents.add(Component.literal("Left Click for Blink"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("50 + Amount of damage").withStyle(ChatFormatting.YELLOW)));
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
        if (PlayerMobEntity.isCopy(livingEntity)) {
            return 0;
        }
        if (target != null && BeyonderUtil.getSpirituality(livingEntity) < BeyonderUtil.getMaxSpirituality(livingEntity) / 3 && livingEntity.getPersistentData().getBoolean("blinkStateTimer")) {
            return 80;
        }
        if (target != null && !livingEntity.getPersistentData().getBoolean("blinkStateTimer") && BeyonderUtil.getSpirituality(livingEntity) > BeyonderUtil.getMaxSpirituality(livingEntity) / 3) {
            return 80;
        }
        if (target == null && livingEntity.getPersistentData().getBoolean("blinkStateTimer")) {
            return 100;
        }
        return 0;
    }
}