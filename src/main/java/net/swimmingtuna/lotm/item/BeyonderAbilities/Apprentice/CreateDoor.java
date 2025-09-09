package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.entity.ApprenticeDoorEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class CreateDoor extends SimpleAbilityItem {
    public CreateDoor(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 9, 70, 200);
    }

    @Override
    public InteractionResult useAbilityOnBlock(UseOnContext context) {
        if (context.getPlayer() == null) {
            Entity entity = context.getItemInHand().getEntityRepresentation();
            if (entity instanceof LivingEntity user) {
                Level level = context.getLevel();
                BlockPos targetPos = context.getClickedPos();
                BlockPos posRelativeTo = context.getClickedPos().relative(context.getClickedFace());
                Direction direction = context.getClickedFace().getOpposite();

                if (!checkAll(user)) {
                    return InteractionResult.FAIL;
                }
                if (!canCreateDoor(user, targetPos, posRelativeTo)) {
                    return InteractionResult.FAIL;
                }
                createDoor(user, level, targetPos, posRelativeTo, direction);
                return InteractionResult.SUCCESS;
            }
        } else {
            Player player = context.getPlayer();
            Level level = context.getLevel();
            BlockPos targetPos = context.getClickedPos();
            BlockPos posRelativeTo = context.getClickedPos().relative(context.getClickedFace());
            Direction direction = context.getClickedFace().getOpposite();

            if (!checkAll(player, BeyonderClassInit.APPRENTICE.get(), 9, 70 * (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.CREATEDOOR.get()), false)) {
                return InteractionResult.FAIL;
            }
            if (!canCreateDoor(player, targetPos, posRelativeTo)) {
                return InteractionResult.FAIL;
            }
            createDoor(player, level, targetPos, posRelativeTo, direction);
            addCooldown(player);
            useSpirituality(player, 70 * (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.CREATEDOOR.get()));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity livingEntity, InteractionHand hand){
        if(!checkAll(livingEntity)){
            return InteractionResult.FAIL;
        }
        checkAmount(livingEntity);
        return InteractionResult.SUCCESS;
    }

    public static boolean canCreateDoor(LivingEntity player, BlockPos pos, BlockPos posRelativeTo){
        Level level = player.level();
        if(!level.isEmptyBlock(pos) && !level.isEmptyBlock(BlockPos.containing(pos.getX(), pos.getY() + 1, pos.getZ()))){
            if(level.isEmptyBlock(posRelativeTo) && level.isEmptyBlock(BlockPos.containing(posRelativeTo.getX(), posRelativeTo.getY() + 1, posRelativeTo.getZ()))){
                if(!level.isEmptyBlock(BlockPos.containing(posRelativeTo.getX(), posRelativeTo.getY() - 1, posRelativeTo.getZ()))){
                    return true;
                }
            }
        }
        if(!level.isEmptyBlock(pos) && !level.isEmptyBlock(BlockPos.containing(pos.getX(), pos.getY() - 1, pos.getZ()))){
            if(level.isEmptyBlock(posRelativeTo) && level.isEmptyBlock(BlockPos.containing(posRelativeTo.getX(), posRelativeTo.getY() - 1, posRelativeTo.getZ()))){
                if(!level.isEmptyBlock(BlockPos.containing(posRelativeTo.getX(), posRelativeTo.getY() - 2, posRelativeTo.getZ()))){
                    return true;
                }
            }
        }
        return false;
    }

    public static void createDoor(LivingEntity player, Level level, BlockPos pos, BlockPos posRelativeTo, Direction direction) {
        if (level.isClientSide) return;
        int sequence = BeyonderUtil.getSequence(player);
        int maxBlocks = 100 - (97 * sequence / 9);

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        int cordModifier = (direction == Direction.WEST || direction == Direction.NORTH) ? -1 : 1;
        int yModifier = 0;

        if (!player.isShiftKeyDown()) {
            BlockPos posUp = pos.above();
            BlockPos posDown = pos.below();
            BlockPos posRelativeUp = posRelativeTo.above();
            BlockPos posRelativeDown = posRelativeTo.below();

            if (!level.isEmptyBlock(pos) && !level.isEmptyBlock(posUp)) {
                if (level.isEmptyBlock(posRelativeTo) && level.isEmptyBlock(posRelativeUp)) {
                    yModifier = 0;
                }
            }

            if (!level.isEmptyBlock(pos) && !level.isEmptyBlock(posDown)) {
                if (level.isEmptyBlock(posRelativeTo) && level.isEmptyBlock(posRelativeDown)) {
                    yModifier = -1;
                }
            }

            BlockPos adjustedPos = pos.offset(0, yModifier, 0);
            BlockPos adjustedPosUp = adjustedPos.above();
            BlockPos adjustedRelativePos = posRelativeTo.offset(0, yModifier, 0);
            BlockPos adjustedRelativePosUp = adjustedRelativePos.above();

            if (!level.isEmptyBlock(adjustedRelativePos) ||
                    !level.isEmptyBlock(adjustedRelativePosUp) ||
                    level.isEmptyBlock(adjustedPos) ||
                    level.isEmptyBlock(adjustedPosUp)) {
                return;
            }

            boolean found = false;
            int targetX = x;
            int targetY = y + yModifier;
            int targetZ = z;
            int loop = 0;

            if (direction.getAxis() == Direction.Axis.Z) {
                while (loop < maxBlocks) {
                    loop++;
                    int newZ = z + loop * cordModifier;
                    BlockPos checkPos = new BlockPos(x, targetY, newZ);
                    BlockPos checkPosUp = checkPos.above();

                    if (level.isEmptyBlock(checkPos) && level.isEmptyBlock(checkPosUp)) {
                        targetZ = newZ;
                        found = true;
                        break;
                    }
                }

                if (!found && loop > 0) {
                    targetZ = z + loop * cordModifier;
                    found = true;
                }

                if (found) {
                    double offsetZ = targetZ + (0.5 * cordModifier - 0.2) * cordModifier;
                    spawnDoor(
                            level,
                            adjustedRelativePos,
                            x + 0.5,
                            targetY,
                            offsetZ,
                            direction,
                            120,
                            player
                    );
                }
            } else {
                while (loop < maxBlocks) {
                    loop++;
                    int newX = x + loop * cordModifier;
                    BlockPos checkPos = new BlockPos(newX, targetY, z);
                    BlockPos checkPosUp = checkPos.above();

                    if (level.isEmptyBlock(checkPos) && level.isEmptyBlock(checkPosUp)) {
                        targetX = newX;
                        found = true;
                        break;
                    }
                }

                if (!found && loop > 0) {
                    targetX = x + loop * cordModifier;
                    found = true;
                }
                DimensionalSightTileEntity dimensionalSightTileEntity = BeyonderUtil.findNearbyDimensionalSight(player);
                if (dimensionalSightTileEntity != null) {
                    if (dimensionalSightTileEntity.getScryTarget() != null) {
                        player.sendSystemMessage(Component.literal("You created a door to your Dimensional Sight Target").withStyle(ChatFormatting.AQUA));
                        spawnDoor(level, adjustedRelativePos, dimensionalSightTileEntity.getScryTarget().getX(), dimensionalSightTileEntity.getScryTarget().getY(), dimensionalSightTileEntity.getScryTarget().getZ(), direction, 120, player);
                    }
                } else {
                    if (found) {
                        double offsetX = targetX + (0.5 * cordModifier - 0.2) * cordModifier;
                        spawnDoor(
                                level,
                                adjustedRelativePos,
                                offsetX,
                                targetY,
                                z + 0.5,
                                direction,
                                120,
                                player
                        );
                    }
                }
            }
        }
    }

    public static void spawnDoor(Level level, BlockPos pos, double X, double Y, double Z, Direction direction, int life, LivingEntity user) {
        int sequence = BeyonderUtil.getSequence(user);
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        float yaw;

        switch(direction) {
            case NORTH:
                x += 0.5;
                z += 0.25;
                yaw = 0F;
                break;
            case WEST:
                x += 0.25;
                z += 0.5;
                yaw = 90F;
                break;
            case SOUTH:
                x += 0.5;
                z += 0.75;
                yaw = 180F;
                break;
            case EAST:
            default:
                x += 0.75;
                z += 0.5;
                yaw = 270F;
                break;
        }

        ApprenticeDoorEntity door = new ApprenticeDoorEntity(level, user, sequence, life, yaw, (float)X, (float)Y, (float)Z, level, ApprenticeDoorEntity.DoorAnimationKind.BEHIND);
        door.setPos(x, y, z);
        level.addFreshEntity(door);
    }

    public static void checkAmount(LivingEntity living) {
        if(living.isShiftKeyDown() && living instanceof Player player) {
            player.displayClientMessage(Component.literal("Amount of blocks that can be opened: ").withStyle(ChatFormatting.WHITE).append(Component.literal(String.valueOf(100 - (97 * BeyonderUtil.getSequence(living) / 9))).withStyle(ChatFormatting.BLUE)), true);
            //player.displayClientMessage(Component.literal("Angle: ").withStyle(ChatFormatting.WHITE).append(Component.literal(String.valueOf(player.getYRot())).withStyle(ChatFormatting.BLUE)), true);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.literal("Upon use on a block, creates a conceptual door that can be used to pass trough a few blocks. Shift in order to go through any doors created."));
        tooltipComponents.add(Component.literal("Use while sneaking to see how many blocks can be passed."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("70").withStyle(ChatFormatting.YELLOW)));
        Component.literal("Cooldown: ").append(Component.literal("10 Seconds").withStyle(ChatFormatting.YELLOW));
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
        return 0;
    }
}