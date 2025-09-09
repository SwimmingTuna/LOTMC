package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.swimmingtuna.lotm.entity.LightningEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class StarOfLightning extends SimpleAbilityItem {

    public StarOfLightning(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 1, 3000, 800);
    }


    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        starOfLightningAbility(player);
        return InteractionResult.SUCCESS;
    }

    private static void starOfLightningAbility(LivingEntity player) {
        if (!player.level().isClientSide()) {
            EventManager.addToRegularLoop(player, EFunctions.SIRENSONG.get());
            if (BeyonderUtil.getSequence(player) == 0) {
                player.getPersistentData().putInt("sailorLightningStar", 50);
            } else {
                player.getPersistentData().putInt("sailorLightningStar", 80);
            }
        }
    }

    public static void starOfLightning(LivingEntity livingEntity) {
        //STAR OF LIGHTNING
        CompoundTag tag = livingEntity.getPersistentData();
        int sailorLightningStar = tag.getInt("sailorLightningStar");
        if (sailorLightningStar >= 2) {
            StarOfLightning.summonLightningParticles(livingEntity);
            livingEntity.level().playSound(livingEntity, livingEntity.getOnPos(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 10, 1);
            tag.putInt("sailorLightningStar", sailorLightningStar - 1);
        }
        if (sailorLightningStar == 1) {
            StarOfLightning.starOfLightningExplode(livingEntity, livingEntity.getOnPos(), 15);
            tag.putInt("sailorLightningStar", 0);
            tag.putInt("sailorLightningStarLightning", 20);
        }
        if (tag.getInt("sailorLightningStarLightning") >= 1) {
            Level level = livingEntity.level();
            tag.putInt("sailorLightningStarLightning", tag.getInt("sailorLightningStarLightning") - 1);
            float searchRadius = Math.min(300, BeyonderUtil.getDamage(livingEntity).get(ItemInit.STAR_OF_LIGHTNING.get()));
            AABB searchArea = new AABB(livingEntity.getX() - searchRadius, livingEntity.getY() - searchRadius, livingEntity.getZ() - searchRadius, livingEntity.getX() + searchRadius, livingEntity.getY() + searchRadius, livingEntity.getZ() + searchRadius);
            List<LivingEntity> potentialTargets = level.getEntitiesOfClass(LivingEntity.class, searchArea, target -> target != livingEntity && target.isAlive() && !BeyonderUtil.areAllies(livingEntity, target));
            for (int i = 0; i < BeyonderUtil.getDamage(livingEntity).get(ItemInit.STAR_OF_LIGHTNING.get()) / 75; i++) {
                LightningEntity lightningEntity = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), livingEntity.level());
                lightningEntity.setSpeed(50);
                lightningEntity.setDamage((int) (BeyonderUtil.getDamage(livingEntity).get(ItemInit.STAR_OF_LIGHTNING.get()) / 1.5f));
                if (!potentialTargets.isEmpty()) {
                    LivingEntity randomTarget = potentialTargets.get(level.random.nextInt(potentialTargets.size()));
                    lightningEntity.setTargetEntity(randomTarget);
                    double dirX = randomTarget.getX() - livingEntity.getX();
                    double dirY = randomTarget.getY() - livingEntity.getY();
                    double dirZ = randomTarget.getZ() - livingEntity.getZ();
                    double length = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
                    if (length > 0) {
                        dirX /= length;
                        dirY /= length;
                        dirZ /= length;
                    }
                    lightningEntity.setDeltaMovement(dirX, dirY, dirZ);
                } else {
                    double sailorStarX = (Math.random() * 2 - 1);
                    double sailorStarY = (Math.random() * 2 - 1);
                    double sailorStarZ = (Math.random() * 2 - 1);
                    lightningEntity.setDeltaMovement(sailorStarX, sailorStarY, sailorStarZ);
                }

                lightningEntity.setMaxLength(10);
                lightningEntity.setOwner(livingEntity);
                lightningEntity.setMentalDamage(lightningEntity.getMentalDamage());
                lightningEntity.teleportTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
                livingEntity.level().addFreshEntity(lightningEntity);
            }
        }
    }


    public static void summonLightningParticles(LivingEntity player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 500; i++) {
                double offsetX = (Math.random() * 5) - 2.5;
                double offsetY = (Math.random() * 5) - 2.5;
                double offsetZ = (Math.random() * 5) - 2.5;
                if (Math.sqrt(offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ) <= 2.5) {
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX() + offsetX, player.getY() + offsetY, player.getZ() + offsetZ,0, 0.0, 0.0, 0.0,0);
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, gathers hundreds of lightning bolts in your bodies before letting them out in every direction"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("3000").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("40 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

    public static void starOfLightningExplode(LivingEntity livingEntity, BlockPos hitPos, double radius) {
        for (BlockPos pos : BlockPos.betweenClosed(
                hitPos.offset((int) -radius, (int) -radius, (int) -radius),
                hitPos.offset((int) radius, (int) radius, (int) radius))) {
            if (pos.distSqr(hitPos) <= radius * radius) {
                if (livingEntity.level().getBlockState(pos).getDestroySpeed(livingEntity.level(), pos) >= 0) {
                    livingEntity.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        List<Entity> entities = livingEntity.level().getEntities(livingEntity,
                new AABB(hitPos.offset((int) -radius, (int) -radius, (int) -radius),
                        hitPos.offset((int) radius, (int) radius, (int) radius)));

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity pEntity) {
                pEntity.hurt(BeyonderUtil.lightningSource(livingEntity, pEntity), 50);
            }
        }
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null) {
            return 10;
        }
        return 0;
    }
}
