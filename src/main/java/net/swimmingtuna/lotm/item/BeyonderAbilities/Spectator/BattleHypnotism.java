package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class BattleHypnotism extends SimpleAbilityItem {

    public BattleHypnotism(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 6, 150, 300, 50, 50);
    }

    @Override
    public InteractionResult useAbilityOnBlock(UseOnContext pContext) {
        if (pContext.getPlayer() == null) {
            Entity entity = pContext.getItemInHand().getEntityRepresentation();
            if (entity instanceof LivingEntity user) {
                if (!checkAll(user)) {
                    return InteractionResult.FAIL;
                }
                makesEntitiesAttackEachOther(user, user.level(), pContext.getClickedPos());
                return InteractionResult.SUCCESS;
            }
        } else {
            Player player = pContext.getPlayer();
            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }
            useSpirituality(player);
            addCooldown(player);
            makesEntitiesAttackEachOther(player, player.level(), pContext.getClickedPos());
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
            makesEntitiesAttackEachOther(player, player.level(), interactionTarget.getOnPos());
        }
        return InteractionResult.SUCCESS;
    }

    private void makesEntitiesAttackEachOther(LivingEntity player, Level level, BlockPos targetPos) {
        if (!player.level().isClientSide()) {
            if (player instanceof Player) {
                int duration = (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.BATTLE_HYPNOTISM.get());
                AABB boundingBox = new AABB(targetPos).inflate((double) duration / 20);
                level.getEntitiesOfClass(LivingEntity.class, boundingBox, LivingEntity::isAlive).forEach(livingEntity -> {
                    if (livingEntity != player) {
                        BeyonderUtil.applyBattleHypnotism(livingEntity, duration);
                    }
                });
            } else {
                LivingEntity playerTarget = null;
                int duration = (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.BATTLE_HYPNOTISM.get());
                for (LivingEntity livingEntity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(duration))) {
                    if (livingEntity instanceof Player) {
                        playerTarget = livingEntity;
                    }
                    if (livingEntity instanceof Mob mob) {
                        if (playerTarget != null) {
                            mob.setTarget(playerTarget);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, makes all living entities around the clicked location target the nearest player if one is present and each other if there isn't one"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("150").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("15 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
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
        attributeBuilder.putAll(super.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND));
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 50, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 50, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SPECTATOR_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        int mobTotalHP = 0;
        if (target != null) {
            int sequence = BeyonderUtil.getSequence(livingEntity);
            BeyonderUtil.getDreamIntoReality(livingEntity);
            for (Mob mob : target.level().getEntitiesOfClass(Mob.class, target.getBoundingBox().inflate((20 - sequence) * BeyonderUtil.getDreamIntoReality(livingEntity)))) {
                if (mob != target) {
                    mobTotalHP += (int) (mob.getMaxHealth() / 10);
                    if (mobTotalHP >= 100) {
                        mobTotalHP = 100;
                        break;
                    }
                }
            }
        }
        return mobTotalHP;
    }
}