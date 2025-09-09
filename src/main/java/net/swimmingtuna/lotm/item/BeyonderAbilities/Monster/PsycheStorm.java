package net.swimmingtuna.lotm.item.BeyonderAbilities.Monster;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import net.swimmingtuna.lotm.util.SpamClass;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class PsycheStorm extends SimpleAbilityItem {

    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    public PsycheStorm(Properties properties) {
        super(properties, BeyonderClassInit.MONSTER, 6, 100, 300, 25, 25);
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
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 25, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 25, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public InteractionResult useAbilityOnBlock(UseOnContext pContext) {
        if (pContext.getPlayer() == null) {
            Entity entity = pContext.getItemInHand().getEntityRepresentation();
            if (entity instanceof LivingEntity user) {
                if (!checkAll(user)) {
                    return InteractionResult.FAIL;
                }
                psycheStorm(user, pContext.getLevel(), pContext.getClickedPos());
                return InteractionResult.SUCCESS;
            }
        } else {
            Player player = pContext.getPlayer();
            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }
            addCooldown(player);
            psycheStorm(player, pContext.getLevel(), pContext.getClickedPos());
            useSpirituality(player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!player.level().isClientSide()) {
            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }
            addCooldown(player);
            useSpirituality(player);
            psycheStorm(player, player.level(), BlockPos.containing(interactionTarget.position()));
        }
        return InteractionResult.SUCCESS;
    }


    private void psycheStorm(LivingEntity player, Level level, BlockPos targetPos) {
        if (!player.level().isClientSide()) {
            float damage =  BeyonderUtil.getDamage(player).get(ItemInit.PSYCHESTORM.get());
            double radius = damage * 0.4;
            int duration = (int) (damage * 4);
            AABB boundingBox = new AABB(targetPos).inflate(radius);
            level.getEntitiesOfClass(LivingEntity.class, boundingBox, LivingEntity::isAlive).forEach(livingEntity -> {
                if (livingEntity != player && !BeyonderUtil.areAllies(player, livingEntity)) {
                    livingEntity.getPersistentData().putInt("psycheStormTick", 85);
                    livingEntity.getPersistentData().putFloat("psycheStormDamage", damage);
                    livingEntity.getPersistentData().putUUID("psycheStormUUID", player.getUUID());
                    livingEntity.invulnerableTime = 0;
                    livingEntity.hurtTime = 0;
                    livingEntity.hurtDuration = 0;
                }
            });
        }
    }

    public static void psycheStormTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!livingEntity.level().isClientSide() && livingEntity.getPersistentData().getInt("psycheStormTick") >= 1) {
            int counter = livingEntity.getPersistentData().getInt("psycheStormTick");
            livingEntity.getPersistentData().putInt("psycheStormTick", counter - 1);
            if (counter <= 55 && counter % 5 == 0 && counter >= 35) {
                livingEntity.invulnerableTime = 4;
                livingEntity.hurtTime = 4;
                livingEntity.hurtDuration = 4;
                float damage = livingEntity.getPersistentData().getFloat("psycheStormDamage");
                if (livingEntity.getPersistentData().contains("psycheStormUUID")) {
                    LivingEntity living = BeyonderUtil.getLivingEntityFromUUID(livingEntity.level(), livingEntity.getPersistentData().getUUID("psycheStormUUID"));
                    if (living != null) {
                        int duration = (int) (damage * 4);
                        BeyonderUtil.applyMentalDamage(living, livingEntity, damage / 4f);
                        livingEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 1, false, false));
                    } else {
                        livingEntity.hurt(livingEntity.damageSources().magic(), 10);
                        livingEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 1, false, false));
                    }
                }
            }
            if (counter > 70) {
                float damage = livingEntity.getPersistentData().getFloat("psycheStormDamage");
                BeyonderUtil.createSphereOfParticlesToCenter(livingEntity.level(), livingEntity.getOnPos().getCenter(), ParticleTypes.ENCHANT,  (int) damage * 5, damage, 20);
            } if (counter < 25) {
                if (livingEntity instanceof Player player) {
                    SpamClass.sendMonsterMessage(player);
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use on a block or entity, attacks the psyche and corrupts them. Increasing their corruption value, hurting them, and confusing them."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("100").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("15 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
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