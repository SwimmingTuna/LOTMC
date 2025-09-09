package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.entity.CustomFallingBlockEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.ToggleDistanceC2S;
import net.swimmingtuna.lotm.networking.packet.UpdateEntityLocationS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkill;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import static net.swimmingtuna.lotm.util.BeyonderUtil.getCustomFallingBlockFromUUID;
import static net.swimmingtuna.lotm.util.BeyonderUtil.getLivingEntityFromUUID;

public class InvisibleHand extends LeftClickHandlerSkill {
    public InvisibleHand(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 5, 0, 0);
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
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 25, AttributeModifier.Operation.ADDITION)); // adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 25, AttributeModifier.Operation.ADDITION)); // adds a 12 block reach for interacting with blocks, pretty much useless for this item
        return attributeBuilder.build();
    }

    public static boolean isGrabbingSomething(LivingEntity entity) {
        if (!entity.getPersistentData().getBoolean("justUsedInvisibleHand")) {
            return entity.getPersistentData().contains("invisibleHandUUID") && !entity.getPersistentData().getUUID("invisibleHandUUID").equals(new UUID(0, 0));
        }
        entity.getPersistentData().putBoolean("justUsedInvisibleHand", false);
        return false;
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity livingEntity, InteractionHand hand) {
        if (!level.isClientSide()) {
            if (isGrabbingSomething(livingEntity)) {
                throwEntity(livingEntity);
            } else {
                tryToThrowDimensionalSight(livingEntity);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity livingEntity, LivingEntity interactionTarget, InteractionHand hand) {
        if (!livingEntity.level().isClientSide && !interactionTarget.level().isClientSide) {
            if (!checkAll(livingEntity)) {
                return InteractionResult.FAIL;
            }
            if (BeyonderUtil.getSequence(livingEntity) > 4 || (BeyonderUtil.isBeyonder(interactionTarget) && BeyonderUtil.getSequence(interactionTarget) < BeyonderUtil.getSequence(livingEntity))) {
                if (livingEntity instanceof Player player) {
                    player.displayClientMessage(Component.literal("Target is too strong to be picked up").withStyle(BeyonderUtil.getStyle(player)), true);
                }
                return InteractionResult.FAIL;
            }
            if (isGrabbingSomething(livingEntity)) {
                throwEntity(livingEntity);
                return InteractionResult.FAIL;
            }
            livingEntity.getPersistentData().putBoolean("justUsedInvisibleHand", true);
            if (!livingEntity.isShiftKeyDown()) {
                grabEntity(livingEntity, interactionTarget, 100);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbilityOnBlock(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState blockState = level.getBlockState(pos);
        if (level.isClientSide || player == null || !checkAll(player) || blockState.getBlock() instanceof EntityBlock || blockState.isAir())
            return InteractionResult.FAIL;
        if (isGrabbingSomething(player)) return InteractionResult.FAIL;
        player.getPersistentData().putBoolean("justUsedInvisibleHand", true);
        if (!player.isShiftKeyDown()) grabBlock(player, level, blockState, pos);
        return InteractionResult.SUCCESS;
    }

    public static void tryToThrowDimensionalSight(LivingEntity livingEntity) {
        DimensionalSightTileEntity dimensionalSightTileEntity = BeyonderUtil.findNearbyDimensionalSight(livingEntity);
        if (dimensionalSightTileEntity != null && dimensionalSightTileEntity.getScryTarget() != null) {
            livingEntity.sendSystemMessage(Component.literal("You grabbed your Dimensional Sight Target").withStyle(ChatFormatting.AQUA));
            livingEntity.getPersistentData().putUUID("invisibleHandUUID", dimensionalSightTileEntity.getScryTarget().getUUID());
            if (!livingEntity.getPersistentData().contains("invisibleHandDistance")) {
                livingEntity.getPersistentData().putDouble("invisibleHandDistance", 5);
            }
            dimensionalSightTileEntity.removeThis();
            livingEntity.getPersistentData().putInt("invisibleHandCounter", 100);
            int amount = livingEntity.getPersistentData().getInt("invisibleHandDistance");
            Vec3 lookVec = livingEntity.getLookAngle().scale(amount);
            dimensionalSightTileEntity.getScryTarget().teleportTo(lookVec.x(), lookVec.y(), lookVec.z());
        }
    }

    public static void grabBlock(LivingEntity livingEntity, Level level, BlockState target, BlockPos targetPos) {
        level.removeBlock(targetPos, false);
        CustomFallingBlockEntity fallingBlock = CustomFallingBlockEntity.fall(level, targetPos, target);
        fallingBlock.setInvulnerable(true);
        fallingBlock.setNoGravity(true);
        fallingBlock.time = 0;
        level.addFreshEntity(fallingBlock);
        int sequence = BeyonderUtil.getSequence(livingEntity);
        int time = 1200 - (1000 * (sequence / 5));
        grabEntity(livingEntity, fallingBlock, time);
    }

    public static void grabEntity(LivingEntity livingEntity, Entity target, int time) {
        livingEntity.getPersistentData().putUUID("invisibleHandUUID", target.getUUID());
        livingEntity.getPersistentData().putInt("invisibleHandCounter", time);
        if (!livingEntity.getPersistentData().contains("invisibleHandDistance")) {
            livingEntity.getPersistentData().putDouble("invisibleHandDistance", 5);
        }
    }


    public static void releaseEntity(LivingEntity livingEntity) {
        livingEntity.fallDistance = 0;
        livingEntity.getPersistentData().putInt("invisibleHandCounter", 0);
        livingEntity.getPersistentData().putDouble("invisibleHandDistance", 10);
        if (livingEntity.getPersistentData().contains("invisibleHandUUID")) {
            CustomFallingBlockEntity fallingBlock = BeyonderUtil.getCustomFallingBlockFromUUID(livingEntity.level(), livingEntity.getPersistentData().getUUID("invisibleHandUUID"));
            if (fallingBlock != null) fallingBlock.setNoGravity(false);
        }
        livingEntity.getPersistentData().putUUID("invisibleHandUUID", new UUID(0, 0));
    }

    public static void throwEntity(LivingEntity livingEntity) {
        if (!livingEntity.getPersistentData().contains("invisibleHandUUID")) {
            return;
        }

        CompoundTag tag = livingEntity.getPersistentData();
        UUID targetUUID = tag.getUUID("invisibleHandUUID");
        Entity target = getLivingEntityFromUUID(livingEntity.level(), targetUUID);
        if (target == null) target = getCustomFallingBlockFromUUID(livingEntity.level(), targetUUID);

        if (target != null) {
            double throwStrength = 5 - (3.5 * BeyonderUtil.getSequence(livingEntity) / 5);
            Vec3 lookAngle = livingEntity.getLookAngle().normalize();

            target.setDeltaMovement(lookAngle.scale(throwStrength));

            if (target instanceof CustomFallingBlockEntity fallingBlock) {
                fallingBlock.setNoGravity(false);
                fallingBlock.getPersistentData().putUUID("blockThrowerUUID", livingEntity.getUUID());
                fallingBlock.getPersistentData().putBoolean("hasBeenThrow", true);
                fallingBlock.getPersistentData().putInt("sequenceThrower", BeyonderUtil.getSequence(livingEntity));
            }

            tag.putUUID("invisibleHandUUID", new UUID(0, 0));
            tag.putInt("invisibleHandCounter", 0);

            if (target instanceof LivingEntity livingTarget) {
                livingTarget.fallDistance = 0;
            }
        }
    }

    public static void invisibleHandTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        if (!livingEntity.level().isClientSide() && livingEntity.getMainHandItem().getItem() instanceof InvisibleHand) {
            double distance = tag.getDouble("invisibleHandDistance");
            double minDistance = 3;
            float maxDistance = BeyonderUtil.getDamage(livingEntity).get(ItemInit.INVISIBLEHAND.get());
            if (livingEntity.isShiftKeyDown()) {
                if (tag.getBoolean("invisibleHandIncrease")) {
                    if (distance < maxDistance) {
                        tag.putDouble("invisibleHandDistance", distance + 0.5);
                    } else {
                        tag.putDouble("invisibleHandDistance", minDistance);
                    }
                } else {
                    if (distance > minDistance) {
                        tag.putDouble("invisibleHandDistance", distance - 0.5);
                    } else {
                        tag.putDouble("invisibleHandDistance", maxDistance);
                    }
                }
                if (livingEntity instanceof Player player) {
                    player.displayClientMessage(Component.literal("Distance: ").withStyle(BeyonderUtil.getStyle(player)).append(Component.literal(String.valueOf(tag.getDouble("invisibleHandDistance"))).withStyle(ChatFormatting.WHITE)), true);
                }
            }
        }
        if (!livingEntity.level().isClientSide() && tag.contains("invisibleHandUUID")) {
            int counter = tag.getInt("invisibleHandCounter");
            UUID targetUUID = tag.getUUID("invisibleHandUUID");
            double distance = tag.getDouble("invisibleHandDistance");
            float maxDistance = BeyonderUtil.getDamage(livingEntity).get(ItemInit.INVISIBLEHAND.get());
            double minDistance = 3;
            Entity target = getLivingEntityFromUUID(livingEntity.level(), targetUUID);
            if (target == null) target = getCustomFallingBlockFromUUID(livingEntity.level(), targetUUID);
            if (distance > maxDistance) {
                tag.putDouble("invisibleHandDistance", maxDistance);
            }
            if (distance < minDistance) {
                tag.putDouble("invisibleHandDistance", minDistance);
            }
            if (counter == 1) {
                releaseEntity(livingEntity);
            }
            if (counter >= 1) {
                tag.putDouble("invisibleHandCounter", counter - 1);
            }
            if (target != null) {
                if (counter >= 1 && !target.level().isClientSide()) {
                    HitResult hitResult = livingEntity.pick(distance, 0.0F, false);
                    if (hitResult instanceof BlockHitResult blockHit) {
                        double x = blockHit.getLocation().x();
                        double y = blockHit.getLocation().y();
                        double z = blockHit.getLocation().z();
                        target.setPos(x, y, z);
                        if (target instanceof ServerPlayer serverPlayer) {
                            serverPlayer.connection.teleport(x, y, z, serverPlayer.getYRot(), serverPlayer.getXRot());
                        }
                        LOTMNetworkHandler.sendToAllPlayers(new UpdateEntityLocationS2C(x, y, z, 0, 0, 0, target.getId()));
                        if (target instanceof LivingEntity living && BeyonderUtil.areAllies(livingEntity, living)) {
                            target.fallDistance = 0;
                        } else {
                            target.fallDistance = (float) (target.getY() - target.level().getHeight(Heightmap.Types.WORLD_SURFACE, (int) x, (int) y));
                        }
                        if (target.fallDistance >= maxDistance) {
                            target.fallDistance = maxDistance;
                        }
                    }
                }
                if (counter >= 1 && counter <= 3) {
                    target.fallDistance = 0;
                }
            }
        }
    }


    public static void setDistanceBoolean(Player player) {
        player.getPersistentData().putBoolean("invisibleHandIncrease", !player.getPersistentData().getBoolean("invisibleHandIncrease"));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.literal("Upon use, creates an invisible hand to manipulate an throw blocks."));
        tooltipComponents.add(Component.literal("When you grow stronger, you will also be able to manipulate weaker entities."));
        tooltipComponents.add(Component.literal("Left Click to switch between increasing/decreasing distance."));
        tooltipComponents.add(Component.literal("Shift to increase/decrease distance"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("None").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("None").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICE_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null) {
            return 80;
        }
        return 0;
    }
    @Override
    public LeftClickType getleftClickEmpty() {
        return new ToggleDistanceC2S();
    }
}