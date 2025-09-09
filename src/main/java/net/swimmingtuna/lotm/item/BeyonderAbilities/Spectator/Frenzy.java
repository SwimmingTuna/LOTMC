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

public class Frenzy extends SimpleAbilityItem {

    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    public Frenzy(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 7, 125, 300, 15, 15);
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
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 15, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 15, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public InteractionResult useAbilityOnBlock(UseOnContext pContext) {
        if (pContext.getPlayer() == null) {
            Entity entity = pContext.getItemInHand().getEntityRepresentation();
            if (entity instanceof LivingEntity user) {
                Level level = pContext.getLevel();
                BlockPos targetPos = pContext.getClickedPos();

                if (!checkAll(user)) {
                    return InteractionResult.FAIL;
                }
                frenzy(user, level, targetPos);
                return InteractionResult.SUCCESS;
            }
        } else {
            Player player = pContext.getPlayer();
            Level level = player.level();
            BlockPos targetPos = pContext.getClickedPos();

            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }
            frenzy(player, level, targetPos);
            addCooldown(player);
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
            frenzy(player, player.level(), BlockPos.containing(interactionTarget.position()));
        }
        return InteractionResult.SUCCESS;
    }

    private void frenzy(LivingEntity player, Level level, BlockPos targetPos) {
        if (!player.level().isClientSide()) {
            int sequence = BeyonderUtil.getSequence(player);
            if (sequence <= 4) {
                player.sendSystemMessage(Component.literal("You can't use this ability anymore, it's been updated to Dragon Breath.").withStyle(ChatFormatting.RED));
                return;
            }
            double radius = BeyonderUtil.getDamage(player).get(ItemInit.FRENZY.get());
            float damage = (float) (40 - (sequence * 3));
            int duration = (int) (radius * 18);
            AABB boundingBox = new AABB(targetPos).inflate(radius);
            level.getEntitiesOfClass(LivingEntity.class, boundingBox, LivingEntity::isAlive).forEach(livingEntity -> {
                if (livingEntity != player && !BeyonderUtil.areAllies(player, livingEntity)) {
                    BeyonderUtil.applyFrenzy(livingEntity, duration);
                    BeyonderUtil.applyMentalDamage(player, livingEntity, damage);
                }
            });
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use on a block or entity, make all entities in the area take damage and go into a frenzy where they move randomly."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("125").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("15 Seconds").withStyle(ChatFormatting.YELLOW)));
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
        if (target != null) {
            return 40;
        }
        return 0;
    }
}