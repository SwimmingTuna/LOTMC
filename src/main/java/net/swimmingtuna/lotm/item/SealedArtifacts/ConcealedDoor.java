package net.swimmingtuna.lotm.item.SealedArtifacts;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.capabilities.concealed_data.ConcealedUtils;
import net.swimmingtuna.lotm.capabilities.is_concealed_data.IsConcealedUtils;
import net.swimmingtuna.lotm.entity.ApprenticeDoorEntity;
import net.swimmingtuna.lotm.world.worldgen.dimension.DimensionInit;

public class ConcealedDoor extends Item {
    public ConcealedDoor(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrCreateTag();
        if (!world.isClientSide) {
            if(tag.contains("concealedSpaceOwner") && tag.contains("concealedSpaceSequence") && tag.contains("concealedSpaceLocation")){
                if(!IsConcealedUtils.getIsConcealed(player)) {
                    MinecraftServer server = player.getServer();
                    if(server == null) return InteractionResultHolder.fail(stack);
                    ResourceKey<Level> dimensionKey = DimensionInit.CONCEALED_SPACE_LEVEL_KEY;
                    ServerLevel concealedDimension = server.getLevel(dimensionKey);
                    if(concealedDimension == null) return InteractionResultHolder.fail(stack);

                    BlockPos location = BlockPos.of(tag.getLong("concealedSpaceLocation"));
                    int x = location.getX();
                    int y = location.getY();
                    int z = location.getZ();

                    float yaw = -player.getYRot() + 180;
                    ApprenticeDoorEntity.DoorAnimationKind animationKind = ApprenticeDoorEntity.DoorAnimationKind.BELLOW;
                    if (player.level().getBlockState(new BlockPos((int) Math.floor(getHorizontalLookCoordinates(player, 2)[0]),
                            (int) Math.floor(player.getY() - 1),
                            (int) Math.floor(getHorizontalLookCoordinates(player, 2)[1]))).isAir()) {
                        animationKind = ApprenticeDoorEntity.DoorAnimationKind.FADE_IN;
                    }

                    ApprenticeDoorEntity enterDoor = new ApprenticeDoorEntity(player.level(), tag.getUUID("concealedSpaceOwner"), tag.getInt("concealedSpaceSequence"), 150, yaw, x, y, z, true, concealedDimension, animationKind);
                    enterDoor.setPos(getHorizontalLookCoordinates(player, 2)[0], player.getY(), getHorizontalLookCoordinates(player, 2)[1]);
                    player.level().addFreshEntity(enterDoor);
                }else{
                    Level level = ConcealedUtils.getConcealedSpaceExitDimension(player);
                    if (level == null) return InteractionResultHolder.fail(stack);

                    float yaw = -player.getYRot() + 180;
                    ApprenticeDoorEntity.DoorAnimationKind animationKind = ApprenticeDoorEntity.DoorAnimationKind.BELLOW;
                    if(player.level().getBlockState(new BlockPos((int) Math.floor(getHorizontalLookCoordinates(player, 2)[0]),
                            (int) Math.floor(player.getY() - 1),
                            (int) Math.floor(getHorizontalLookCoordinates(player, 2)[1]))).isAir()){
                        animationKind = ApprenticeDoorEntity.DoorAnimationKind.FADE_IN;
                    }

                    int x = ConcealedUtils.getConcealedSpaceExit(player).getX();
                    int y = ConcealedUtils.getConcealedSpaceExit(player).getY();
                    int z = ConcealedUtils.getConcealedSpaceExit(player).getZ();

                    ApprenticeDoorEntity leaveDoor = new ApprenticeDoorEntity(player.level(), tag.getUUID("concealedSpaceOwner"), tag.getInt("concealedSpaceSequence"), 150, yaw, x, y, z, false, level, animationKind);
                    leaveDoor.setPos(getHorizontalLookCoordinates(player, 2)[0], player.getY(), getHorizontalLookCoordinates(player, 2)[1]);
                    player.level().addFreshEntity(leaveDoor);
                }
                stack.hurtAndBreak(1, player, (p) -> {
                    p.broadcastBreakEvent(hand);
                });
            }else{
                delete(hand, player);
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), world.isClientSide());
    }

    public static double[] getHorizontalLookCoordinates(LivingEntity player, double distance){
        float yaw = player.getYRot();
        double angleRadians = Math.toRadians(-yaw);
        double x = player.getX() + distance * Math.sin(angleRadians);
        double z = player.getZ() + distance * Math.cos(angleRadians);
        return new double[] {x, z};
    }

    public void delete(InteractionHand hand, Player player){
        player.setItemInHand(hand, ItemStack.EMPTY);
        player.displayClientMessage(Component.literal("No Concealed Space saved"), false);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }
}
