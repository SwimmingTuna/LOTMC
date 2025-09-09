package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.entity.FlashEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.UpdateItemInHandC2S;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class TrickFlash extends LeftClickHandlerSkillP {

    public TrickFlash(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 8, 35, 300);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        useSpirituality(player);
        addCooldown(player);
        flash(player);
        return InteractionResult.SUCCESS;
    }

    public static void flash(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            FlashEntity flash = new FlashEntity(EntityInit.FLASH_ENTITY.get(), livingEntity.level());
            Vec3 lookVec = livingEntity.getLookAngle();
            flash.setDeltaMovement(lookVec.scale(1.5));
            flash.setOwner(livingEntity);
            flash.hurtMarked = true;
            DimensionalSightTileEntity dimensionalSightTileEntity = BeyonderUtil.findNearbyDimensionalSight(livingEntity);
            if (dimensionalSightTileEntity != null && dimensionalSightTileEntity.getScryTarget() != null) {
                livingEntity.sendSystemMessage(Component.literal("You created a flash around your Dimensional Sight Target").withStyle(ChatFormatting.AQUA));
                flash.teleportTo(dimensionalSightTileEntity.getScryTarget().getX(), dimensionalSightTileEntity.getScryTarget().getY(), dimensionalSightTileEntity.getScryTarget().getZ());
            } else {
                flash.teleportTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
            }
            livingEntity.level().addFreshEntity(flash);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, create and throw out a flash bomb, that on hit, will brighly flash and blind all those, apart from yourself, that look at it, causing their screen to go white. Causes all mobs to lose their current target as well."));
        tooltipComponents.add(Component.literal("Left click for Trick: Fog"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("35").withStyle(ChatFormatting.YELLOW)));
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
        if (target != null) {
            return (int) (100 - (target.distanceTo(livingEntity) * 10));
        }
        return 0;
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.TRICKFOG.get()));
    }
}