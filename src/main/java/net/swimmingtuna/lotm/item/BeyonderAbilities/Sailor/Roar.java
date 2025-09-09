package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.entity.RoarEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class Roar extends SimpleAbilityItem {

    public Roar(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 4, 500, 100);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        roar(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    public static void roar(LivingEntity player) {
        if (!player.level().isClientSide()) {
            RoarEntity roarEntity = new RoarEntity(EntityInit.ROAR_ENTITY.get(), player.level());
            roarEntity.teleportTo(player.getX(), player.getY(), player.getZ());
            Vec3 lookVec = player.getLookAngle();
            roarEntity.setOwner(player);
            float speed = BeyonderUtil.getDamage(player).get(ItemInit.ROAR.get()) / 1.5f;
            roarEntity.setDeltaMovement(lookVec.scale(speed).x, lookVec.scale(speed).y, lookVec.scale(speed).z);
            roarEntity.hurtMarked = true;
            player.level().addFreshEntity(roarEntity);
            Vec3 startPos = player.getEyePosition();
            Vec3 endPos = startPos.add(lookVec.scale(10));
            BeyonderUtil.setScale(roarEntity, speed);
            BlockPos.betweenClosed(new BlockPos((int) Math.min(startPos.x, endPos.x) - 2, (int) Math.min(startPos.y, endPos.y) - 2, (int) Math.min(startPos.z, endPos.z) - 2), new BlockPos((int) Math.max(startPos.x, endPos.x) + 2, (int) Math.max(startPos.y, endPos.y) + 2, (int) Math.max(startPos.z, endPos.z) + 2)).
                    forEach(pos -> {
                        if (isInCone(startPos, lookVec, new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), 0.5) && startPos.distanceTo(new Vec3(pos.getX(), pos.getY(), pos.getZ())) <= 10) {
                            BlockState state = player.level().getBlockState(pos);
                            if (!state.isAir() && state.getDestroySpeed(player.level(), pos) >= 0 && state.getBlock() != Blocks.BEDROCK) {
                                player.level().destroyBlock(pos, false);
                            }
                        }
                    });
        }
    }

    private static boolean isInCone(Vec3 origin, Vec3 direction, Vec3 point, double radius) {
        Vec3 toPoint = point.subtract(origin);
        double distance = toPoint.length();
        if (distance <= 0) return true;
        double angle = Math.acos(toPoint.dot(direction) / (distance * direction.length()));
        double maxAngle = Math.atan(radius / distance);
        return angle <= maxAngle;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, lets out a devastating roar which destroys blocks and hurts entities that it passes through, only exploding on strong entities."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("500").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("5 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
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
