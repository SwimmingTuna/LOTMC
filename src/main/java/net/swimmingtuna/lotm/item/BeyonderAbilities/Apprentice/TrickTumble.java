package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.UpdateItemInHandC2S;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class TrickTumble extends LeftClickHandlerSkillP {


    public TrickTumble(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 8, 35, 240);
    }


    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        useSpirituality(player);
        addCooldown(player);
        tumble(player);
        return InteractionResult.SUCCESS;
    }

    public static void tumble(LivingEntity living) {
        if (!living.level().isClientSide()) {
            DimensionalSightTileEntity dimensionalSightTileEntity = BeyonderUtil.findNearbyDimensionalSight(living);
            if (dimensionalSightTileEntity != null && dimensionalSightTileEntity.getScryTarget() != null) {
                LivingEntity scry = dimensionalSightTileEntity.getScryTarget();
                living.sendSystemMessage(Component.literal("You created a slippery area around your Dimensional Sight Target").withStyle(ChatFormatting.AQUA));
                for (LivingEntity livingEntity : BeyonderUtil.checkEntitiesInLocation(scry, 25F, (float) scry.getX(), (float) scry.getY(), (float) scry.getZ())) {
                    if (livingEntity != living && !BeyonderUtil.areAllies(livingEntity, living)) {
                        Vec3 movement = livingEntity.getDeltaMovement();
                        if (BeyonderUtil.getSequence(living) >= 5) {
                            if (livingEntity.onGround()) {
                                BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TRICKTUMBLE.get()), 1, false, false);
                                BeyonderUtil.applyMobEffect(livingEntity, ModEffects.TUMBLE.get(), (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TRICKTUMBLE.get()), 1, false, false);
                                livingEntity.setDeltaMovement(Math.min(3, movement.x * 1.5f), movement.y(), Math.min(3, movement.z() * 1.5f));
                                livingEntity.hurtMarked = true;
                            }
                        } else {
                            BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TRICKTUMBLE.get()), 1, false, false);
                            BeyonderUtil.applyMobEffect(livingEntity, ModEffects.TUMBLE.get(), (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TRICKTUMBLE.get()), 1, false, false);
                            livingEntity.setDeltaMovement(Math.min(3, movement.x * 1.5f), movement.y(), Math.min(3, movement.z() * 1.5f));
                            livingEntity.hurtMarked = true;
                        }
                    }
                }
            } else {
                for (LivingEntity livingEntity : BeyonderUtil.getNonAlliesNearby(living, 25)) {
                    Vec3 movement = livingEntity.getDeltaMovement();
                    if (BeyonderUtil.getSequence(living) >= 5) {
                        if (livingEntity.onGround()) {
                            BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, (int) (float) BeyonderUtil.getDamage(living).get(ItemInit.TRICKTUMBLE.get()), 1, false, false);
                            BeyonderUtil.applyMobEffect(livingEntity, ModEffects.TUMBLE.get(), (int) (float) BeyonderUtil.getDamage(living).get(ItemInit.TRICKTUMBLE.get()), 1, false, false);
                            livingEntity.setDeltaMovement(Math.min(3, movement.x * 1.5f), movement.y(), Math.min(3, movement.z() * 1.5f));
                            livingEntity.hurtMarked = true;
                        }
                    } else {
                        BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, (int) (float) BeyonderUtil.getDamage(living).get(ItemInit.TRICKTUMBLE.get()), 1, false, false);
                        BeyonderUtil.applyMobEffect(livingEntity, ModEffects.TUMBLE.get(), (int) (float) BeyonderUtil.getDamage(living).get(ItemInit.TRICKTUMBLE.get()), 1, false, false);
                        livingEntity.setDeltaMovement(Math.min(3, movement.x * 1.5f), movement.y(), Math.min(3, movement.z() * 1.5f));
                        livingEntity.hurtMarked = true;
                    }
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
        tooltipComponents.add(Component.literal("Upon use, all entities around you trip, going forward briefly before losing the ability to move."));
        tooltipComponents.add(Component.literal("Left click for Trick: Wind"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("35").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("12 Seconds").withStyle(ChatFormatting.YELLOW)));
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
        if (target != null) {
            if (target.distanceTo(livingEntity) < 25) {
                return 70;
            }
        }
        return 0;
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.TRICKWIND.get()));
    }
}