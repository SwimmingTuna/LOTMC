package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;


import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.Blink;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.LuckGifting;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.EnvisionLocation;
import net.swimmingtuna.lotm.item.OtherItems.WormOfStar;
import net.swimmingtuna.lotm.networking.packet.UpdateItemInHandC2S;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MatterAccelerationSelf extends LeftClickHandlerSkillP {

    public MatterAccelerationSelf(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 0, 0, 300);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        int matterAccelerationDistance = player.getPersistentData().getInt("tyrantSelfAcceleration");
        if (!checkAll(player, BeyonderClassInit.SAILOR.get(), 0, matterAccelerationDistance * 10, true)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player, matterAccelerationDistance * 10);
        matterAccelerationSelfAbility(player);
        return InteractionResult.SUCCESS;
    }

    public void matterAccelerationSelfAbility(LivingEntity player) {
        Level level = player.level();
        int blinkDistance = player.getPersistentData().getInt("tyrantSelfAcceleration");
        Vec3 lookVector = player.getLookAngle();
        BlockPos startPos = player.blockPosition();
        BlockPos endPos = new BlockPos((int) (player.getX() + blinkDistance * lookVector.x()), (int) (player.getY() + 1 + blinkDistance * lookVector.y()), (int) (player.getZ() + blinkDistance * lookVector.z()));
        BlockPos blockPos = new BlockPos(endPos.getX(), endPos.getY(), endPos.getZ());
        double distance = startPos.getCenter().distanceTo(blockPos.getCenter());
        Vec3 direction = new Vec3(endPos.getX() - startPos.getX(), endPos.getY() - startPos.getY(), endPos.getZ() - startPos.getZ()).normalize();
        Set<BlockPos> visitedPositions = new HashSet<>();
        for (double i = 0; i <= distance; i += 0.5) {
            BlockPos pos = new BlockPos(
                    (int) (startPos.getX() + i * direction.x),
                    (int) (startPos.getY() + i * direction.y),
                    (int) (startPos.getZ() + i * direction.z)
            );
            List<BlockPos> blockPositions = new ArrayList<>();
            for (BlockPos offsetedPos : BlockPos.betweenClosed(pos.offset(-5, -5, -5), pos.offset(5, 5, 5))) {
                if (visitedPositions.contains(offsetedPos)) continue;
                visitedPositions.add(offsetedPos);
                BlockState blockState = level.getBlockState(offsetedPos);
                blockPositions.add(offsetedPos);

                if (!blockState.isAir() && blockState.getBlock().defaultDestroyTime() != -1.0F) {
                    level.setBlock(offsetedPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }

            for (BlockPos blockPosToUpdate : blockPositions) {
                Block block = level.getBlockState(blockPosToUpdate).getBlock();
                level.blockUpdated(blockPosToUpdate, block);
            }

            AABB boundingBox = new AABB(pos).inflate(6);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, boundingBox);
            for (LivingEntity entity : entities) {
                if (entity != player && !BeyonderUtil.areAllies(player, entity)) {
                    if (!(entity instanceof Player)) {
                        entity.hurt(BeyonderUtil.lightningSource(player, entity), BeyonderUtil.getDamage(player).get(ItemInit.MATTER_ACCELERATION_SELF.get()) * 2.5f); // Adjust damage amount as needed
                    } else {
                        entity.hurt(BeyonderUtil.lightningSource(player, entity), BeyonderUtil.getDamage(player).get(ItemInit.MATTER_ACCELERATION_SELF.get())); // Adjust damage amount as needed
                    }
                }
            }
        }
        // Teleport the player
        BlockHitResult blockHitResult = level.clip(new ClipContext(player.getEyePosition(), new Vec3(endPos.getX() + 0.5, endPos.getY(), endPos.getZ() + 0.5), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        BlockPos teleportLocation = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
        player.teleportTo(teleportLocation.getX() + 0.5, teleportLocation.getY(), teleportLocation.getZ() + 0.5);
    }

    public static void matterAccelerationSelf(LivingEntity livingEntity) {
        //MATTER ACCELERATION SELF
        if (livingEntity.isSpectator()) return;
        if (!livingEntity.level().isClientSide()) {
            int matterAccelerationDistance = livingEntity.getPersistentData().getInt("tyrantSelfAcceleration");
            int blinkDistance = livingEntity.getPersistentData().getInt("BlinkDistance");
            int luckGiftingAmount = livingEntity.getPersistentData().getInt("monsterLuckGifting");
            int doorBlinkDistance = livingEntity.getPersistentData().getInt("trickmasterBlinkDistance");
            if (livingEntity.isShiftKeyDown() && livingEntity.getMainHandItem().getItem() instanceof MatterAccelerationSelf && BeyonderUtil.currentPathwayMatches(livingEntity, BeyonderClassInit.SAILOR.get())) {
                livingEntity.getPersistentData().putInt("tyrantSelfAcceleration", matterAccelerationDistance + 50);
                if (livingEntity instanceof Player player) {
                    player.displayClientMessage(Component.literal("Matter Acceleration Distance is " + matterAccelerationDistance).withStyle(BeyonderUtil.getStyle(livingEntity)), true);
                }
                if (matterAccelerationDistance >= 1001) {
                    if (livingEntity instanceof Player player) {
                        player.displayClientMessage(Component.literal("Matter Acceleration Distance is 0").withStyle(BeyonderUtil.getStyle(livingEntity)), true);
                    }
                    livingEntity.getPersistentData().putInt("tyrantSelfAcceleration", 0);
                }
            }
            if (livingEntity.isShiftKeyDown() && livingEntity.getMainHandItem().getItem() instanceof EnvisionLocation && BeyonderUtil.currentPathwayMatches(livingEntity, BeyonderClassInit.SPECTATOR.get())) {
                livingEntity.getPersistentData().putInt("BlinkDistance", blinkDistance + 5);
                if (livingEntity instanceof Player player) {
                    player.displayClientMessage(Component.literal("Blink Distance is " + blinkDistance).withStyle(BeyonderUtil.getStyle(livingEntity)), true);
                }
                if (blinkDistance >= 201) {
                    if (livingEntity instanceof Player player) {
                        player.displayClientMessage(Component.literal("Blink Distance is 0").withStyle(BeyonderUtil.getStyle(livingEntity)), true);
                    }
                    livingEntity.getPersistentData().putInt("BlinkDistance", 0);
                }
            }
            //LUCK GIFTING
            if (livingEntity.isShiftKeyDown() && livingEntity.getMainHandItem().getItem() instanceof LuckGifting && BeyonderUtil.currentPathwayMatches(livingEntity, BeyonderClassInit.MONSTER.get())) {
                livingEntity.getPersistentData().putInt("monsterLuckGifting", luckGiftingAmount + 1);
                if (livingEntity instanceof Player player) {
                    player.displayClientMessage(Component.literal("Luck Gifting Amount is " + luckGiftingAmount).withStyle(BeyonderUtil.getStyle(livingEntity)), true);
                }
                if (luckGiftingAmount >= BeyonderUtil.getDamage(livingEntity).get(ItemInit.LUCKGIFTING.get())) {
                    if (livingEntity instanceof Player player) {
                        player.displayClientMessage(Component.literal("Luck Gifting Amount is 0").withStyle(BeyonderUtil.getStyle(livingEntity)), true);
                    }
                    livingEntity.getPersistentData().putInt("monsterLuckGifting", 0);
                }
            }
            if (livingEntity.isShiftKeyDown() && livingEntity.getMainHandItem().getItem() instanceof Blink && BeyonderUtil.currentPathwayAndSequenceMatches(livingEntity, BeyonderClassInit.APPRENTICE.get(), 5)) {
                int maxBlinkDistance = 50;
                int blinkIncrement = 2;
                int sequence = BeyonderUtil.getSequence(livingEntity);
                if (sequence == 5) {
                    livingEntity.sendSystemMessage(Component.literal("SEQUENCE IS " + sequence));
                    maxBlinkDistance = 30;
                } else if (sequence == 4) {
                    maxBlinkDistance = 100;
                    blinkIncrement = 4;
                } else if (sequence == 3) {
                    maxBlinkDistance = 200;
                    blinkIncrement = 10;
                } else if (sequence == 2) {
                    maxBlinkDistance = 450;
                    blinkIncrement = 20;
                } else if (sequence == 1) {
                    maxBlinkDistance = 900;
                    blinkIncrement = 20;
                } else if (sequence == 0) {
                    maxBlinkDistance = 2000;
                    blinkIncrement = 30;
                }
                if (livingEntity.getPersistentData().getInt("trickmasterBlinkDistance") < maxBlinkDistance) {
                    livingEntity.getPersistentData().putInt("trickmasterBlinkDistance", livingEntity.getPersistentData().getInt("trickMasterBlinkDistance") + blinkIncrement);
                } else {
                    if (livingEntity instanceof Player player) {
                        player.displayClientMessage(Component.literal("Blink Distance is 0").withStyle(BeyonderUtil.getStyle(livingEntity)), true);
                    }
                    livingEntity.getPersistentData().putInt("trickmasterBlinkDistance", 0);
                }
                if (livingEntity instanceof Player player) {
                    player.displayClientMessage(Component.literal("Blink Distance is " + doorBlinkDistance).withStyle(BeyonderUtil.getStyle(livingEntity)), true);
                }
            }
            if (livingEntity.isShiftKeyDown() && livingEntity.getMainHandItem().getItem() instanceof WormOfStar && BeyonderUtil.currentPathwayAndSequenceMatches(livingEntity, BeyonderClassInit.APPRENTICE.get(), 4)) {
                if (livingEntity.getPersistentData().getInt("wormOfStarSpiritualityAmount") < BeyonderUtil.getMaxSpirituality(livingEntity)) {
                    livingEntity.getPersistentData().putInt("wormOfStarSpiritualityAmount", livingEntity.getPersistentData().getInt("wormOfStarSpiritualityAmount") + 10);
                } else {
                    if (livingEntity instanceof Player player) {
                        player.displayClientMessage(Component.literal("Worm of Star Spirituality amount is 0").withStyle(BeyonderUtil.getStyle(livingEntity)), true);
                    }
                    livingEntity.getPersistentData().putInt("wormOfStarSpiritualityAmount", 0);
                }
                if (livingEntity instanceof Player player) {
                    player.displayClientMessage(Component.literal("Worm of Star Spirituality amount is " + livingEntity.getPersistentData().getInt("wormOfStarSpiritualityAmount")).withStyle(BeyonderUtil.getStyle(livingEntity)), true);
                }
            }
            if (BeyonderUtil.currentPathwayAndSequenceMatches(livingEntity, BeyonderClassInit.SAILOR.get(), 3) && livingEntity.getMainHandItem().getItem() instanceof LightningStorm) {
                CompoundTag tag = livingEntity.getPersistentData();
                int stormVec = tag.getInt("sailorStormVec");
                if (livingEntity.isShiftKeyDown()) {
                    tag.putInt("sailorStormVec", stormVec + 10);
                    if (livingEntity instanceof Player player) {
                        player.displayClientMessage(Component.literal("Sailor Storm Spawn Distance is " + stormVec).withStyle(BeyonderUtil.getStyle(livingEntity)), true);
                    }
                }
                if (stormVec >= 301) {
                    if (livingEntity instanceof Player player) {
                        player.displayClientMessage(Component.literal("Sailor Storm Spawn Distance is 0").withStyle(BeyonderUtil.getStyle(player)), true);
                    }
                    tag.putInt("sailorStormVec", 0);
                }
            }
        }
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, accelerates you to the speed of light, instantly getting you to your destination and leaving behind destruction in your path"));
        tooltipComponents.add(Component.literal("Shift to increase distance"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("5 x Distance Traveled").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("15 seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(BeyonderClassInit.SAILOR.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(0, BeyonderClassInit.SAILOR.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null) {
            livingEntity.getPersistentData().putInt("tyrantSelfAcceleration", (int) (livingEntity.distanceTo(target) + 10));
            return 70;
        }
        return 0;
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.MATTER_ACCELERATION_BLOCKS.get()));
    }
}
