package net.swimmingtuna.lotm.item.BeyonderAbilities.Monster;


import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.world.worlddata.CalamityEnhancementData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class MisfortuneImplosion extends SimpleAbilityItem {

    public MisfortuneImplosion(Properties properties) {
        super(properties, BeyonderClassInit.MONSTER, 2, 1000, 200);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        useSpirituality(player);
        addCooldown(player);
        misfortuneImplosion(player);
        return InteractionResult.SUCCESS;
    }

    public static void misfortuneImplosion(LivingEntity player) {
        if (!player.level().isClientSide()) {
            Level level = player.level();
            int enhancement = 1;
            if (level instanceof ServerLevel serverLevel) {
                enhancement = CalamityEnhancementData.getInstance(serverLevel).getCalamityEnhancement();
            }
            double radius = BeyonderUtil.getDamage(player).get(ItemInit.MISFORTUNEIMPLOSION.get());
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius))) {
                if (entity != player && !BeyonderUtil.areAllies(player, entity)) {
                    CompoundTag tag = entity.getPersistentData();
                    double misfortune = tag.getDouble("misfortune");
                    Random random = new Random();
                    int randomInt = random.nextInt(3);
                    if (randomInt == 0) {
                        float explosionRadius = (float) (Math.max(3, misfortune / 8) + (enhancement * 3));
                        float damage = (float) ((2 * misfortune) + (enhancement * 10));
                        explodeMeteorBlock(entity, entity.getOnPos(), explosionRadius, damage);
                        tag.putDouble("misfortune", 0);
                    } else if (randomInt == 1) {
                        float duration = (float) (100 + (misfortune * 5) * enhancement);
                        entity.addEffect(new MobEffectInstance(MobEffects.WITHER, (int) duration, 4, false, false));
                        BeyonderUtil.applyNoRegeneration(entity, (int) (duration * 0.75));
                        entity.hurt(BeyonderUtil.mentalSource(player.level(), player, entity), (float) misfortune / 2);
                    } else {
                        int duration = (int) ((5) + (misfortune / 2) + (enhancement * 3));
                        entity.getPersistentData().putInt("monsterImplosionLightning", duration);
                        EventManager.addToRegularLoop(entity, EFunctions.MISFORTUNEIMPLOSIONLIGHTNING.get());
                        tag.putDouble("misfortune", 0);
                    }
                }
            }
        }
    }

    public static void misfortuneImplosionLightning(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity().getPersistentData().getInt("monsterImplosionLightning") >= 1) {
            CompoundTag tag = event.getEntity().getPersistentData();
            LivingEntity livingEntity = event.getEntity();

            tag.putInt("monsterImplosionLightning", tag.getInt("monsterImplosionLightning") - 1);

            LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, livingEntity.level());
            lightningBolt.setDamage(25);
            lightningBolt.teleportTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
            livingEntity.level().addFreshEntity(lightningBolt);
        }
    }

    public static void misfortuneCurse(Player player) {
        if (!player.level().isClientSide()) {
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            int sequence = holder.getSequence();
            Level level = player.level();
            int enhancement = 1;
            if (level instanceof ServerLevel serverLevel) {
                enhancement = CalamityEnhancementData.getInstance(serverLevel).getCalamityEnhancement();
            }
            double radius = (250 - (sequence * 100) + (enhancement * 50));
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius))) {
                if (entity != player) {
                    CompoundTag tag = entity.getPersistentData();
                    double misfortune = tag.getDouble("misfortune");
                    float duration = (float) (100 + (misfortune * 5) * enhancement);
                    entity.addEffect(new MobEffectInstance(MobEffects.WITHER, (int) duration, 4, false, false));
                    entity.hurt(BeyonderUtil.genericSource(player, entity), (float) misfortune / 2);
                    tag.putDouble("misfortune", 0);
                }
            }
        }
    }

    public static void explodeMeteorBlock(Entity entity, BlockPos hitPos, double radius, float damage) {
        for (BlockPos pos : BlockPos.betweenClosed(
                hitPos.offset((int) -radius, (int) -radius, (int) -radius),
                hitPos.offset((int) radius, (int) radius, (int) radius))) {
            if (pos.distSqr(hitPos) <= radius * radius) {
                if (entity.level().getBlockState(pos).getDestroySpeed(entity.level(), pos) >= 0) {
                    entity.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        List<Entity> entities = entity.level().getEntities(entity, new AABB(hitPos.offset((int) -radius, (int) -radius, (int) -radius), hitPos.offset((int) radius, (int) radius, (int) radius)));
        for (Entity pEntity : entities) {
            if (pEntity instanceof LivingEntity livingEntity) {
                livingEntity.hurt(BeyonderUtil.genericSource(entity, livingEntity), damage); // problem w/ damage sources
            }
        }
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, causes all entities misfortune around you to implode, causing them to deal with either an explosion which scales off their misfortune, wither and the inability to regenerate for a time dependent on their misfortunem, or lightning to be attracted to them for a time dependent on their misfortune"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("1000").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("10 Seconds").withStyle(ChatFormatting.YELLOW)));
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
            return (int) Math.min(100, target.getPersistentData().getDouble("misfortune") * 2);
        }
        return 0;
    }

}
