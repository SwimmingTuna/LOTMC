package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.capabilities.concealed_data.ConcealedUtils;
import net.swimmingtuna.lotm.capabilities.is_concealed_data.IsConcealedUtils;
import net.swimmingtuna.lotm.entity.ApprenticeDoorEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.BlockInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.world.worldgen.dimension.DimensionInit;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CreateConcealedSpace extends SimpleAbilityItem {

    public CreateConcealedSpace(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 4, 400, 300);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        useSpirituality(player);
        addCooldown(player, this, 300 * (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.CREATE_CONCEALED_SPACE.get()));
        concealedSpace(player);
        return InteractionResult.SUCCESS;
    }

    private static void concealedSpace(LivingEntity entity) {
        if (entity.level().isClientSide()) return;
        if (!ConcealedUtils.hasConcealedSpace(entity)) createConcealedSpace(entity);
        else {
            if (IsConcealedUtils.getIsConcealed(entity)) {
                if (entity.isShiftKeyDown()) {
                    changeConcealedSpaceSpawn(entity);
                }
                else {
                    createDoorLeaveConcealedSpace(entity);
                }
            } else {
                if (entity.isShiftKeyDown()) {
                    createDoorItem(entity);
                } else {
                    DimensionalSightTileEntity dimensionalSightTileEntity = BeyonderUtil.findNearbyDimensionalSight(entity);
                    if (dimensionalSightTileEntity != null && dimensionalSightTileEntity.getScryTarget() != null) {
                        entity.sendSystemMessage(Component.literal("You brought your Dimensional Sight Target to your concealed space").withStyle(ChatFormatting.AQUA));
                        dimensionalSightTileEntity.getScryTarget().teleportTo(entity.getX(), entity.getY(), entity.getZ());
                    }
                    createDoorEnterConcealedSpace(entity);
                }
                upgradeConcealedSpace(entity);
            }
        }
    }

    private static void upgradeConcealedSpace(LivingEntity entity) {
        int sequence = BeyonderUtil.getSequence(entity);
        MinecraftServer server = entity.getServer();
        if (server == null) return;
        ResourceKey<Level> dimensionKey = DimensionInit.CONCEALED_SPACE_LEVEL_KEY;
        ServerLevel level = server.getLevel(dimensionKey);

        if (sequence == ConcealedUtils.getConcealedSpaceSequence(entity)) return;

        int diameter = 43 - 4 * sequence;
        int radius = (diameter - 1) / 2;

        BlockPos destination = ConcealedUtils.getConcealedSpaceCenter(entity);
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 0; dy <= diameter; dy++) {
                    int x = destination.getX() + dx;
                    int y = destination.getY() + dy;
                    int z = destination.getZ() + dz;
                    mutablePos.set(x, y, z);
                    if (level.getBlockState(mutablePos).is(BlockInit.VOID_BLOCK.get()))
                        level.destroyBlock(mutablePos, false);
                }
            }
        }
        ConcealedUtils.setConcealedSpaceSequence(entity, sequence);
    }

    private static void changeConcealedSpaceSpawn(LivingEntity entity) {
        ConcealedUtils.setConcealedSpaceSpawn(entity, new BlockPos(entity.getBlockX(), entity.getBlockY(), entity.getBlockZ()));
        if (entity instanceof Player player) {
            player.displayClientMessage(Component.literal("Spawn changed").withStyle(BeyonderUtil.getStyle(player)), true);
        }
    }

    private static void createDoorItem(LivingEntity entity) {
        if (entity.getOffhandItem().isEmpty()) {
            ItemStack stack = new ItemStack(ItemInit.CONCEALED_DOOR.get());
            CompoundTag tag = stack.getOrCreateTag();
            entity.setItemInHand(InteractionHand.OFF_HAND, stack);
            tag.putInt("concealedSpaceSequence", BeyonderUtil.getSequence(entity));
            tag.putUUID("concealedSpaceOwner", entity.getUUID());
            tag.putLong("concealedSpaceLocation", ConcealedUtils.getConcealedSpaceSpawn(entity).asLong());
        }
    }

    private static void createConcealedSpace(LivingEntity entity) {
        int sequence = BeyonderUtil.getSequence(entity);
        MinecraftServer server = entity.getServer();
        if (server == null) return;
        ResourceKey<Level> dimensionKey = DimensionInit.CONCEALED_SPACE_LEVEL_KEY;
        ServerLevel level = server.getLevel(dimensionKey);

        int diameter = 43 - 4 * sequence;
        int radius = (diameter - 1) / 2;

        BlockPos destination;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int attempts = 0;

        outer:
        while (true) {
            BlockPos center = new BlockPos(
                    ThreadLocalRandom.current().nextInt(-100000, 100001),
                    2,
                    ThreadLocalRandom.current().nextInt(-100000, 100001)
            );

            for (int dx = -radius * 2; dx <= radius * 2; dx++) {
                for (int dz = -radius * 2; dz <= radius * 2; dz++) {
                    for (int dy = 0; dy <= diameter * 2; dy++) {
                        int x = center.getX() + dx;
                        int y = center.getY() + dy;
                        int z = center.getZ() + dz;

                        ChunkAccess chunk = level.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, true);
                        if (chunk == null) continue;

                        mutablePos.set(x, y, z);
                        BlockState state = chunk.getBlockState(mutablePos);

                        if (!state.is(BlockInit.VOID_BLOCK.get())) {
                            attempts++;
                            if (attempts >= 1000) {
                                if (entity instanceof Player player)
                                    player.displayClientMessage(Component.literal("It wasn't possible to find any safe space to build your concealed space"), false);
                                return;
                            }
                            continue outer;
                        }
                    }
                }
            }

            destination = center;
            break;
        }

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 0; dy <= diameter; dy++) {
                    int x = destination.getX() + dx;
                    int y = destination.getY() + dy;
                    int z = destination.getZ() + dz;
                    mutablePos.set(x, y, z);
                    level.destroyBlock(mutablePos, false);
                }
            }
        }

        ConcealedUtils.setConcealedSpaceOwnership(entity, true);
        ConcealedUtils.setConcealedSpaceSequence(entity, sequence);
        ConcealedUtils.setConcealedSpaceCenter(entity, destination);
        ConcealedUtils.setConcealedSpaceSpawn(entity, destination);
        createDoorEnterConcealedSpace(entity);
    }

    private static void createDoorEnterConcealedSpace(LivingEntity entity) {
        MinecraftServer server = entity.getServer();
        if (server == null) return;
        ResourceKey<Level> dimensionKey = DimensionInit.CONCEALED_SPACE_LEVEL_KEY;
        ServerLevel concealedDimension = server.getLevel(dimensionKey);

        ConcealedUtils.setConcealedSpaceExit(entity, new BlockPos(entity.getBlockX(), entity.getBlockY(), entity.getBlockZ()));
        ConcealedUtils.setConcealedSpaceExitDimension(entity, entity.level());

        int x = ConcealedUtils.getConcealedSpaceSpawn(entity).getX();
        int y = ConcealedUtils.getConcealedSpaceSpawn(entity).getY();
        int z = ConcealedUtils.getConcealedSpaceSpawn(entity).getZ();

        float yaw = -entity.getYRot() + 180;
        ApprenticeDoorEntity.DoorAnimationKind animationKind = ApprenticeDoorEntity.DoorAnimationKind.BELLOW;
        if (entity.level().getBlockState(new BlockPos((int) Math.floor(getHorizontalLookCoordinates(entity, 2)[0]),
                (int) Math.floor(entity.getY() - 1),
                (int) Math.floor(getHorizontalLookCoordinates(entity, 2)[1]))).isAir()) {
            animationKind = ApprenticeDoorEntity.DoorAnimationKind.FADE_IN;
        }
        ApprenticeDoorEntity enterDoor = new ApprenticeDoorEntity(entity.level(), entity.getUUID(), BeyonderUtil.getSequence(entity), 150, yaw, x, y, z, true, concealedDimension, animationKind);
        enterDoor.setPos(getHorizontalLookCoordinates(entity, 2)[0], entity.getY(), getHorizontalLookCoordinates(entity, 2)[1]);
        entity.level().addFreshEntity(enterDoor);
    }

    public static double[] getHorizontalLookCoordinates(LivingEntity player, double distance) {
        float yaw = player.getYRot();
        double angleRadians = Math.toRadians(-yaw);
        double x = player.getX() + distance * Math.sin(angleRadians);
        double z = player.getZ() + distance * Math.cos(angleRadians);
        return new double[]{x, z};
    }

    private static void createDoorLeaveConcealedSpace(LivingEntity entity) {
        Level level = ConcealedUtils.getConcealedSpaceExitDimension(entity);
        if (level == null) return;

        float yaw = -entity.getYRot() + 180;
        ApprenticeDoorEntity.DoorAnimationKind animationKind = ApprenticeDoorEntity.DoorAnimationKind.BELLOW;
        if (entity.level().getBlockState(new BlockPos((int) Math.floor(getHorizontalLookCoordinates(entity, 2)[0]),
                (int) Math.floor(entity.getY() - 1),
                (int) Math.floor(getHorizontalLookCoordinates(entity, 2)[1]))).isAir()) {
            animationKind = ApprenticeDoorEntity.DoorAnimationKind.FADE_IN;
        }

        int x = ConcealedUtils.getConcealedSpaceExit(entity).getX();
        int y = ConcealedUtils.getConcealedSpaceExit(entity).getY();
        int z = ConcealedUtils.getConcealedSpaceExit(entity).getZ();

        ApprenticeDoorEntity leaveDoor = new ApprenticeDoorEntity(entity.level(), entity.getUUID(), BeyonderUtil.getSequence(entity), 150, yaw, x, y, z, false, level, animationKind);
        leaveDoor.setPos(getHorizontalLookCoordinates(entity, 2)[0], entity.getY(), getHorizontalLookCoordinates(entity, 2)[1]);
        entity.level().addFreshEntity(leaveDoor);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, Conceal a part of the Spirit World, to be used at you will."));
        tooltipComponents.add(Component.literal("If used while sneaking, in your off hand, you will receive a special door that leads to the users Concealed Space."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("400").withStyle(ChatFormatting.YELLOW)));
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
        if (livingEntity.getHealth() < livingEntity.getMaxHealth() / 8 && !IsConcealedUtils.getIsConcealed(livingEntity)) {
            return 80;
        } else if (IsConcealedUtils.getIsConcealed(livingEntity) && livingEntity.getHealth() > livingEntity.getMaxHealth() - 5) {
            return 100;
        }
        return 0;
    }
}