package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.swimmingtuna.lotm.entity.StoneEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class Earthquake extends SimpleAbilityItem {

    public Earthquake(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 4, 600, 500);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        earthquakeAbility(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    public void earthquakeAbility(LivingEntity player) {
        if (!player.level().isClientSide()) {
            EventManager.addToRegularLoop(player, EFunctions.EARTHQUAKE.get());
            player.getPersistentData().putInt("sailorEarthquake", 200);
        }
    }

    public static void earthquake(LivingEntity livingEntity) {
        int sailorEarthquake = livingEntity.getPersistentData().getInt("sailorEarthquake");
        if (sailorEarthquake >= 1) {
            int sequence = BeyonderUtil.getSequence(livingEntity);
            int radius = (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.EARTHQUAKE.get());
            if (sailorEarthquake % 20 == 0) {
                for (LivingEntity entity : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate((radius)))) {
                    if (entity != livingEntity && !BeyonderUtil.areAllies(livingEntity, entity)) {
                        if (entity.onGround()) {
                            entity.hurt(BeyonderUtil.fallSource(livingEntity, entity), 35 - (sequence * 5));
                            entity.hurt(livingEntity.damageSources().fall(), 35 - (sequence * 5));
                        }
                    }
                }
            }
            if (sailorEarthquake % 2 == 0) {
                AABB checkArea = livingEntity.getBoundingBox().inflate(radius);
                Random random = new Random();
                for (int x = (int) checkArea.minX; x <= (int) checkArea.maxX; x++) {
                    for (int z = (int) checkArea.minZ; z <= (int) checkArea.maxZ; z++) {
                        int effectiveY = findHighestSolidBlock(livingEntity.level(), x, z);
                        if (effectiveY != -1) {
                            BlockPos blockPos = new BlockPos(x, effectiveY, z);
                            if (random.nextInt(20) == 1) {
                                BlockState blockState = livingEntity.level().getBlockState(blockPos);
                                if (livingEntity.level() instanceof ServerLevel serverLevel) {
                                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState), blockPos.getX(), blockPos.getY() + 1, blockPos.getZ(), 0, 0.0, 0.0, 0, 0);
                                }
                            }
                            if (random.nextInt(6000) == 1) {
                                livingEntity.level().destroyBlock(blockPos, false);
                            } else if (random.nextInt(18000) == 2) {
                                StoneEntity stoneEntity = new StoneEntity(livingEntity.level(), livingEntity);
                                ScaleData scaleData = ScaleTypes.BASE.getScaleData(stoneEntity);
                                stoneEntity.teleportTo(blockPos.getX(), blockPos.getY() + 3, blockPos.getZ());
                                stoneEntity.setDeltaMovement(0, (3 + (Math.random() * (6 - 3))), 0);
                                stoneEntity.setStoneYRot((int) (Math.random() * 18));
                                stoneEntity.setDamage((int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.EARTHQUAKE.get()) / 2);
                                stoneEntity.setStoneXRot((int) (Math.random() * 18));
                                scaleData.setScale((float) (1 + (Math.random()) * 2.0f));
                                livingEntity.level().addFreshEntity(stoneEntity);
                            }
                        }
                    }
                }
            }
            livingEntity.getPersistentData().putInt("sailorEarthquake", sailorEarthquake - 1);
        }
    }

    private static int findHighestSolidBlock(Level level, int x, int z) {
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        for (int y = surfaceY; y >= surfaceY - 3; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getBlockState(pos).isAir() && level.getBlockState(pos).isSolid()) {
                return y;
            }
        }
        for (int y = 200; y >= level.getMinBuildHeight(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && state.isSolid()) {
                return y;
            }
        }
        return -1;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, summon an earthquake."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("600").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("25 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static boolean isOnSurface(Level level, BlockPos pos) {
        return level.canSeeSky(pos.above()) || !level.getBlockState(pos.above()).isSolid();
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null) {
            return 50;
        }
        return 0;
    }
}