package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.LightningEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses.LightningRedirectionLayer;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class LightningRedirection extends SimpleAbilityItem {

    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

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
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 200, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 200, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    public LightningRedirection(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 1, 600, 100,200,200);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        useSpirituality(player);
        addCooldown(player);
        enableDisableLightningRedirection(player);
        return InteractionResult.SUCCESS;
    }

    public static void enableDisableLightningRedirection(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            CompoundTag tag = livingEntity.getPersistentData();
            boolean lightningRedirection = tag.getBoolean("lightningRedirection");
            tag.putBoolean("lightningRedirection", !lightningRedirection);
            if (livingEntity instanceof Player pPlayer) {
                pPlayer.displayClientMessage(Component.literal("Lightning Redirection Turned " + (lightningRedirection ? "Off" : "On")).withStyle(BeyonderUtil.getStyle(livingEntity)), true);
            }
            if (!tag.getBoolean("lightningRedirection")) {
                EventManager.removeFromRegularLoop(livingEntity, EFunctions.LIGHTNINGREDIRECTION.get());
            } else {
                EventManager.addToRegularLoop(livingEntity, EFunctions.LIGHTNINGREDIRECTION.get());
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, enable or disable your lightning redireciton. If enabled, all lightning around you will automatically target whatever you're looking at"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("600").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("5 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

    public static void lightningRedirectionTick(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            LivingEntity living = event.getEntity();
            CompoundTag tag = living.getPersistentData();
            if (tag.getBoolean("lightningRedirection")) {
                AABB searchBox = living.getBoundingBox().inflate((int) (float) BeyonderUtil.getDamage(living).get(ItemInit.LIGHTNING_REDIRECTION.get()));
                List<LivingEntity> possibleTargets = living.level().getEntitiesOfClass(LivingEntity.class, searchBox, entity -> !entity.isSpectator() && entity.isPickable() && entity != living);
                LivingEntity bestTarget = null;
                double bestDotProduct = 0.98;
                Vec3 eyePosition = living.getEyePosition();
                Vec3 lookVector = living.getLookAngle();

                for (LivingEntity target : possibleTargets) {
                    Vec3 toEntity = target.getEyePosition().subtract(eyePosition).normalize();
                    double dotProduct = toEntity.dot(lookVector);
                    if (dotProduct > bestDotProduct) {
                        BlockHitResult hitResult = living.level().clip(new ClipContext(eyePosition, target.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, living));
                        if (hitResult.getType() == HitResult.Type.MISS && living.level().isEmptyBlock(target.blockPosition().above())) {
                            bestTarget = target;
                            bestDotProduct = dotProduct;
                        }
                    }
                }
                BlockPos targetBlockPos = null;
                if (bestTarget == null) {
                    double maxDistance = (int) (float) BeyonderUtil.getDamage(living).get(ItemInit.LIGHTNING_REDIRECTION.get());
                    BlockHitResult blockHit = living.level().clip(new ClipContext(
                            eyePosition,
                            eyePosition.add(lookVector.scale(maxDistance)),
                            ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE,
                            living
                    ));

                    if (blockHit.getType() == HitResult.Type.BLOCK) {
                        BlockPos hitPos = blockHit.getBlockPos();
                        BlockState blockState = living.level().getBlockState(hitPos);
                        if (!blockState.isAir() && blockState.isSolidRender(living.level(), hitPos)) {
                            targetBlockPos = hitPos;
                        }
                    }
                }

                AABB aabb = new AABB(living.getX() - 175, living.getY() - 75, living.getZ() - 175, living.getX() + 175, living.getY() + 300, living.getZ() + 175);
                for (LightningEntity lightning : living.level().getEntitiesOfClass(LightningEntity.class, aabb)) {
                    if (bestTarget != null && lightning.getTargetEntity() != bestTarget && !BeyonderUtil.areAllies(bestTarget, living)) {
                        lightning.setTargetEntity(bestTarget);
                        if (lightning.getNoUp()) {
                            lightning.setNoUp(false);
                        }
                    } else if (bestTarget == null && targetBlockPos != null) {
                        lightning.setTargetEntity(null);
                        lightning.teleportTo(targetBlockPos.getX() + 0.5, targetBlockPos.getY() + 1, targetBlockPos.getZ() + 0.5);
                        if (lightning.getNoUp()) {
                            lightning.setNoUp(false);
                        }
                    }
                }
                for (LightningBolt lightning : living.level().getEntitiesOfClass(LightningBolt.class, aabb)) {
                    if (bestTarget != null && lightning.getOnPos() != bestTarget.getOnPos()) {
                        lightning.teleportTo(bestTarget.getX(), bestTarget.getY(), bestTarget.getZ());
                    } else if (bestTarget == null && targetBlockPos != null && lightning.getOnPos() != targetBlockPos) {
                        lightning.teleportTo(targetBlockPos.getX() + 0.5, targetBlockPos.getY() + 1, targetBlockPos.getZ() + 0.5);
                    }
                }
            }
        }
    }

    public static void onLightningJoinWorld(EntityJoinLevelEvent event) {
        if (!event.getEntity().level().isClientSide() && (event.getEntity() instanceof LightningBolt || event.getEntity() instanceof LightningEntity)) {
            Entity entity = event.getEntity();
            AABB aabb = new AABB(entity.getX() - 75, entity.getY() - 200, entity.getZ() - 75, entity.getX() + 75, entity.getY() + 75, entity.getZ() + 75);
            List<LivingEntity> nearbyEntities = entity.level().getEntitiesOfClass(LivingEntity.class, aabb);
            LivingEntity livingEntity = null;
            double closestDistance = Double.MAX_VALUE;
            for (LivingEntity living : nearbyEntities) {
                if (living.getPersistentData().contains("lightningRedirection") && living.getPersistentData().getBoolean("lightningRedirection")) {
                    double distance = entity.distanceToSqr(living);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        livingEntity = living;
                    }
                }
            }
            if (livingEntity != null) {
                final LivingEntity redirector = livingEntity;
                CompoundTag tag = redirector.getPersistentData();
                AABB searchBox = redirector.getBoundingBox().inflate((int) (float) BeyonderUtil.getDamage(redirector).get(ItemInit.LIGHTNING_REDIRECTION.get()));
                List<LivingEntity> possibleTargets = redirector.level().getEntitiesOfClass(LivingEntity.class, searchBox, targetEntity -> !targetEntity.isSpectator() && targetEntity.isPickable() && targetEntity != redirector);
                LivingEntity bestTarget = null;
                double bestDotProduct = 0.99;
                Vec3 eyePosition = redirector.getEyePosition();
                Vec3 lookVector = redirector.getLookAngle();
                for (LivingEntity target : possibleTargets) {
                    Vec3 toEntity = target.getEyePosition().subtract(eyePosition).normalize();
                    double dotProduct = toEntity.dot(lookVector);
                    if (dotProduct > bestDotProduct) {
                        BlockHitResult hitResult = redirector.level().clip(new ClipContext(eyePosition, target.getEyePosition(),
                                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, redirector));
                        if (hitResult.getType() == HitResult.Type.MISS && redirector.level().isEmptyBlock(target.blockPosition().above())) {
                            bestTarget = target;
                            bestDotProduct = dotProduct;
                        }
                    }
                }
                if (bestTarget != null && !BeyonderUtil.areAllies(bestTarget, redirector)) {
                    if (entity instanceof LightningEntity lightningEntity) {
                        lightningEntity.setTargetEntity(bestTarget);
                        if (lightningEntity.getNoUp()) {
                            lightningEntity.setNoUp(false);
                        }
                    } else if (entity instanceof LightningBolt lightningBolt) {
                        lightningBolt.teleportTo(bestTarget.getX(), bestTarget.getY(), bestTarget.getZ());
                    }
                }
            }
        }
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (!livingEntity.getPersistentData().getBoolean("lightningRedirection")) {
            return 100;
        }
        else {
            return 0;
        }
    }
}
