package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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

public class Nightmare extends SimpleAbilityItem {

    public Nightmare(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 5, 100, 110, 35, 35);
    }

    @Override
    public InteractionResult useAbilityOnBlock(UseOnContext pContext) {
        if (pContext.getPlayer() == null) {
            Entity entity = pContext.getItemInHand().getEntityRepresentation();
            if (entity instanceof LivingEntity user) {
                if (!checkAll(user)) {
                    return InteractionResult.FAIL;
                }
                nightmareNew(user, pContext.getClickedPos());
                return InteractionResult.SUCCESS;
            }
        } else {
            Player player = pContext.getPlayer();
            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }
            addCooldown(player);
            useSpirituality(player);
            nightmareNew(player, pContext.getClickedPos());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack pStack, LivingEntity player, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        if (!player.level().isClientSide()) {
            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }
            addCooldown(player);
            useSpirituality(player);
            nightmareNew(player, pInteractionTarget.getOnPos());
        }
        return InteractionResult.SUCCESS;
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
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 35, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 35, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    public static void nightmareNew(LivingEntity livingEntity, BlockPos targetPos) {
        EventManager.addToRegularLoop(livingEntity, EFunctions.NIGHTMARE_TICK.get());
        int sequence = BeyonderUtil.getSequence(livingEntity);
        Level level = livingEntity.level();
        int dir = BeyonderUtil.getDreamIntoReality(livingEntity);
        double radius = BeyonderUtil.getDamage(livingEntity).get(ItemInit.NIGHTMARE.get());
        float damage = ((float) (120.0 * dir) - (sequence * 10));
        int duration = 300 - (sequence * 20);
        AABB boundingBox = new AABB(targetPos).inflate(radius);
        level.getEntitiesOfClass(LivingEntity.class, boundingBox, LivingEntity::isAlive).forEach(living -> {
            String name = living.getDisplayName().getString();
            CompoundTag tag = living.getPersistentData();
            int amountToAdd = 100;
            if (sequence < 3) {
                amountToAdd = 200;
            }
            if (living != livingEntity && !BeyonderUtil.areAllies(livingEntity, living)) {
                living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, duration, 1, false, false));
                if (tag.getInt("NightmareTimer") < 300 - amountToAdd) {
                    tag.putInt("NightmareTimer", tag.getInt("NightmareTimer") + amountToAdd);
                    ChatFormatting style;
                    int entitySequence = BeyonderUtil.getSequence(living);
                    if (entitySequence >= 9 || entitySequence == -1) {
                        style = ChatFormatting.WHITE;
                    } else if (entitySequence >= 6) {
                        style = ChatFormatting.YELLOW;
                    } else if (entitySequence >= 4) {
                        style = ChatFormatting.RED;
                    } else {
                        style = ChatFormatting.DARK_RED;
                    }
                    if (sequence + 2 <= entitySequence) {
                        livingEntity.sendSystemMessage(Component.literal(name + "'s nightmare value is " + tag.getInt("NightmareTimer") + " / 300").withStyle(style));
                    }
                } else {
                    tag.putInt("NightmareTimer", 0);
                    if (living instanceof Player) {
                        BeyonderUtil.applyMentalDamage(livingEntity, living, damage / 2);
                    } else {
                        BeyonderUtil.applyMentalDamage(livingEntity, living, damage);
                    }
                }
            }
        });
    }


    public static void nightmareTick(LivingEntity player) {
        if (player.tickCount % 10 == 0) {
            CompoundTag tag = player.getPersistentData();
            int nightmareTimer = tag.getInt("NightmareTimer");
            int matterAccelerationBlockTimer = player.getPersistentData().getInt("matterAccelerationBlockTimer");
            if (matterAccelerationBlockTimer >= 1) {
                player.getPersistentData().putInt("matterAccelerationBlockTimer", matterAccelerationBlockTimer - 1);
            }
            if (nightmareTimer >= 1) {
                tag.putInt("NightmareTimer", nightmareTimer - 1);
            }
            if (nightmareTimer == 0 && matterAccelerationBlockTimer == 0) {
                EventManager.removeFromRegularLoop(player, EFunctions.NIGHTMARE_TICK.get());
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, makes all entities around the clicked block or entity enter a nightmare, plunging them into darkness. If an entity is hit by this three times in 30 seconds, they take immense damage."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("1500").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("45 Seconds").withStyle(ChatFormatting.YELLOW)));
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
            return target.getPersistentData().getInt("NightmareTimer") / 3;
        }
        return 0;
    }
}
