package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.init.SoundInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.UpdateItemInHandC2S;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class TrickLoudNoise extends LeftClickHandlerSkillP {
    public TrickLoudNoise(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 8, 40, 300);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity livingEntity, InteractionHand hand) {
        if (!checkAll(livingEntity)) {
            return InteractionResult.FAIL;
        }
        bang(livingEntity);
        addCooldown(livingEntity);
        useSpirituality(livingEntity);
        return InteractionResult.SUCCESS;
    }

    public static void bang(LivingEntity entity) {
        if (!entity.level().isClientSide) {
            int damage = (int) (float) BeyonderUtil.getDamage(entity).get(ItemInit.TRICKFOG.get());
            int duration = damage * 20;
            AABB area;
            DimensionalSightTileEntity dimensionalSightTileEntity = BeyonderUtil.findNearbyDimensionalSight(entity);
            if (dimensionalSightTileEntity != null && dimensionalSightTileEntity.getScryTarget() != null) {
                entity.sendSystemMessage(Component.literal("You made a loud noise around your Dimensional Sight Target").withStyle(ChatFormatting.AQUA));
                area = dimensionalSightTileEntity.getScryTarget().getBoundingBox().inflate(damage);
            } else {
                area = entity.getBoundingBox().inflate(damage);
            }
            List<Entity> players = entity.level().getEntities(entity, area, e -> e instanceof Player && e != entity);
            for (Entity list : players) {
                if (list instanceof Player player && !BeyonderUtil.areAllies(entity, player)) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundInit.BANG.get(), SoundSource.PLAYERS, 1f, 1f);
                    player.addEffect(new MobEffectInstance(ModEffects.DEAFNESS.get(), duration, 0, false, false, true));
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, generates a deafening sound, deafening any players around."));
        tooltipComponents.add(Component.literal("Left click for Trick: Telekenisis"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("40").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("15 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICE_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target instanceof Player) {
            int damage = (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TRICKFOG.get());
            if (target.distanceTo(livingEntity) < damage) {
                return 30;
            }
            return 0;
        }
        return 0;
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.TRICKTELEKENISIS.get()));
    }
}