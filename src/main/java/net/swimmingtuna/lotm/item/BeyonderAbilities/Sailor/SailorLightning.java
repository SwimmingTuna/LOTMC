package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.LightningEntity;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static net.swimmingtuna.lotm.util.BeyonderUtil.findSurfaceY;

public class SailorLightning extends SimpleAbilityItem {

    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    public SailorLightning(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 5, 0, 30,150,150);
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
        tooltipComponents.add(Component.literal("Shoots out a lightning bolt in the direction you look, or on the targetted block or entity."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("200 for blocks/entities, 120 when using on air").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("~1 second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(BeyonderClassInit.SAILOR.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(5, BeyonderClassInit.SAILOR.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) { //add if cursor is on a projectile, lightning goes to projectile and pwoers it
        if (!checkAll(player, BeyonderClassInit.SAILOR.get(), 5, 120, true)) {
            return InteractionResult.FAIL;
        }
        lightningDirection(player, level);
        addCooldown(player, this, 10 + BeyonderUtil.getSequence(player));
        useSpirituality(player, 200);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack pStack, LivingEntity player, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        if (!player.level().isClientSide()) {
            if (!checkAll(player, BeyonderClassInit.SAILOR.get(), 5, 200, true)) {
                return InteractionResult.FAIL;
            }
            addCooldown(player, this, 10 + (BeyonderUtil.getSequence(player) * 2));
            useSpirituality(player, 200);
            lightningTargetEntity(pInteractionTarget, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbilityOnBlock(UseOnContext context) {
        if (context.getPlayer() == null) {
            Entity entity = context.getItemInHand().getEntityRepresentation();
            if (entity instanceof LivingEntity user) {
                if (!checkAll(user, BeyonderClassInit.SAILOR.get(), 5, 200, true)) {
                    return InteractionResult.FAIL;
                }
                lightningblock(user, user.level(), context.getClickLocation());
                addCooldown(user, this, 10 + BeyonderUtil.getSequence(user) * 2);
                return InteractionResult.SUCCESS;
            }
        } else {
            Player player = context.getPlayer();
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            if (!checkAll(player, BeyonderClassInit.SAILOR.get(), 5, 200, true)) {
                return InteractionResult.FAIL;
            }
            lightningblock(player, player.level(), context.getClickLocation());
            addCooldown(player, this, 10 + holder.getSequence() * 2);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }


    private static void lightningDirection(LivingEntity player, Level level) {
        if (!level.isClientSide()) {
            Vec3 lookVec = player.getLookAngle();
            BeyonderUtil.useSpirituality(player, 100);
            float speed = 17 - BeyonderUtil.getSequence(player);
            LightningEntity lightningEntity = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), level);
            lightningEntity.setSpeed(speed);
            lightningEntity.setDamage((int) (float) BeyonderUtil.getDamage(player).get(ItemInit.SAILOR_LIGHTNING.get()));
            lightningEntity.setDeltaMovement(lookVec.x, lookVec.y, lookVec.z);
            lightningEntity.setMaxLength(30);
            lightningEntity.setOwner(player);
            lightningEntity.setOwner(player);
            lightningEntity.setMentalDamage(lightningEntity.getMentalDamage());
            lightningEntity.teleportTo(player.getX(), player.getEyeY(), player.getZ());
            level.addFreshEntity(lightningEntity);
        }
    }

    private static void lightningblock(LivingEntity player, Level level, Vec3 targetPos) {
        if (!level.isClientSide()) {
            Vec3 lookVec = player.getLookAngle();
            BeyonderUtil.useSpirituality(player, 200);
            float speed = 17 - BeyonderUtil.getSequence(player);
            if (player instanceof Player pPlayer) {
                ItemStack itemStack = player.getUseItem();
                pPlayer.getCooldowns().addCooldown(itemStack.getItem(), 10 + (BeyonderUtil.getSequence(player) * 2));
            }
            LightningEntity lightningEntity = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), level);
            lightningEntity.setSpeed(speed);
            lightningEntity.setDeltaMovement(lookVec.x, lookVec.y, lookVec.z);
            lightningEntity.setMaxLength(30);
            lightningEntity.setDamage((int) (float) BeyonderUtil.getDamage(player).get(ItemInit.SAILOR_LIGHTNING.get()));
            lightningEntity.setOwner(player);
            lightningEntity.setMentalDamage(lightningEntity.getMentalDamage());
            lightningEntity.teleportTo(player.getX(), player.getEyeY(), player.getZ());
            lightningEntity.setTargetPos(targetPos);
            level.addFreshEntity(lightningEntity);
        }
    }

    public static void lightningHigh(LivingEntity livingEntity, Level level) {
        if (!level.isClientSide()) {
            List<LivingEntity> nearbyEntities = BeyonderUtil.getNonAlliesNearby(livingEntity, 75);
            List<LivingEntity> validTargets = new ArrayList<>();
            int lowestSequence = Integer.MAX_VALUE;
            for (LivingEntity living : nearbyEntities) {
                int sequence = BeyonderUtil.getSequence(living);
                if (sequence == -1 || sequence == 10) {
                    continue;
                }
                if (sequence < lowestSequence) {
                    lowestSequence = sequence;
                    validTargets.clear();
                    validTargets.add(living);
                } else if (sequence == lowestSequence) {
                    validTargets.add(living);
                }
            }
            double targetX, targetZ;
            if (!validTargets.isEmpty()) {
                LivingEntity target = validTargets.get((int) (Math.random() * validTargets.size()));
                targetX = target.getX();
                targetZ = target.getZ();
            } else {
                targetX = livingEntity.getX() + ((Math.random() * 150) - 75);
                targetZ = livingEntity.getZ() + ((Math.random() * 150) - 75);
            }
            double surfaceY = findSurfaceY(livingEntity, targetX, targetZ, livingEntity.level().dimension());
            if (surfaceY == -1) {
                targetX = livingEntity.getX() + ((Math.random() * 150) - 75);
                targetZ = livingEntity.getZ() + ((Math.random() * 150) - 75);
                surfaceY = livingEntity.getY();
            }

            float speed = 10.0f;
            LightningEntity lightningEntity = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), level);
            lightningEntity.setSpeed(speed);
            lightningEntity.setDeltaMovement(0, -2, 0);
            lightningEntity.setMaxLength(60);
            lightningEntity.setOwner(livingEntity);
            lightningEntity.setMentalDamage(lightningEntity.getMentalDamage());
            lightningEntity.setDamage((int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.SAILOR_LIGHTNING.get()));
            lightningEntity.setOwner(livingEntity);
            lightningEntity.teleportTo(targetX, surfaceY + 60, targetZ);
            level.addFreshEntity(lightningEntity);
        }
    }

    public static void lightningHighPlayerMob(PlayerMobEntity player, Level level) {
        if (!level.isClientSide()) {
            float speed = 17 - BeyonderUtil.getSequence(player);
            LightningEntity lightningEntity = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), level);
            lightningEntity.setSpeed(speed);
            lightningEntity.setDeltaMovement(0, -2, 0);
            lightningEntity.setMaxLength(60);
            lightningEntity.setDamage(20 - (player.getCurrentSequence() * 2));
            lightningEntity.setOwner(player);
            lightningEntity.setOwner(player);
            lightningEntity.setMentalDamage(lightningEntity.getMentalDamage());
            lightningEntity.teleportTo(player.getX() + ((Math.random() * 150) - 75), player.getY() + 60, player.getZ() + ((Math.random() * 150) - 75));
            level.addFreshEntity(lightningEntity);
        }
    }

    public static void lightningTargetEntity(LivingEntity targetEntity, LivingEntity player) {
        if (!player.level().isClientSide()) {
            LightningEntity lightningEntity = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), player.level());
            lightningEntity.setSpeed(18.0f - BeyonderUtil.getSequence(player));
            BeyonderUtil.useSpirituality(player,100);
            Vec3 lookVec = player.getLookAngle();
            lightningEntity.setDeltaMovement(lookVec.x, lookVec.y, lookVec.z);
            lightningEntity.setMaxLength(30);
            lightningEntity.setDamage((int) (float) BeyonderUtil.getDamage(player).get(ItemInit.SAILOR_LIGHTNING.get()));            lightningEntity.teleportTo(player.getX(), player.getY(), player.getZ());
            lightningEntity.setTargetPos(targetEntity.position());
            lightningEntity.setOwner(player);
            lightningEntity.setOwner(player);
            lightningEntity.setMentalDamage(lightningEntity.getMentalDamage());
            player.level().addFreshEntity(lightningEntity);
        }
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null) {
            return 40;
        }
        return 0;
    }
}
