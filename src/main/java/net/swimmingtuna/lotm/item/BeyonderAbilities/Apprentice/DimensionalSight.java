package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.entity.DimensionalSightSealEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.BlockInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class


DimensionalSight extends SimpleAbilityItem {

    public DimensionalSight(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 2, 1000, 6000);
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack pStack, LivingEntity player, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        if (!player.level().isClientSide()) {
            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }
            addCooldown(player, this, 400);
            useSpirituality(player);
            dimensionalSight(player, pInteractionTarget);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        dimensionalSight(player);
        return InteractionResult.SUCCESS;
    }


    public static void dimensionalSight(LivingEntity livingEntity, LivingEntity interactionTarget) {
        dimensionalSightDelayed(livingEntity, interactionTarget, 2);
        if (livingEntity instanceof Player player && player.isCreative()) {
            player.getCooldowns().addCooldown(ItemInit.DIMENSIONAL_SIGHT.get(), 10);
        }
    }

    private static void dimensionalSightDelayed(LivingEntity livingEntity, LivingEntity interactionTarget, int ticksToWait) {
        Level level = livingEntity.level();
        if (!level.isClientSide()) {
            MinecraftServer server = level.getServer();
            if (server != null) {
                if (ticksToWait <= 0) {
                    server.execute(() -> {
                        BlockPos playerPos = livingEntity.blockPosition();
                        Vec3 lookPos = livingEntity.getLookAngle().scale(5);
                        BlockPos targetPos = new BlockPos(playerPos.offset((int) lookPos.x(), -2, (int) lookPos.z()));
                        BlockState dimensionalSightState = BlockInit.DIMENSIONAL_SIGHT.get().defaultBlockState();
                        level.setBlock(targetPos, dimensionalSightState, 3);
                        BlockEntity blockEntity = level.getBlockEntity(targetPos);
                        if (blockEntity instanceof DimensionalSightTileEntity sightEntity) {
                            sightEntity.setCaster(livingEntity);
                            if (interactionTarget != null) {
                                sightEntity.viewTarget = interactionTarget.getName().getString();
                                sightEntity.scryUniqueID = interactionTarget.getUUID();
                                sightEntity.setCaster(livingEntity);
                                sightEntity.setChanged();
                                sightEntity.sendUpdates();
                            }
                            if (interactionTarget instanceof Player player) {
                                player.getPersistentData().putUUID("dimensionalSightPlayerUUID", livingEntity.getUUID());
                                player.getPersistentData().putInt("ignoreShouldntRender", 10);
                            }
                        }
                    });
                } else {
                    server.execute(() -> dimensionalSightDelayed(livingEntity, interactionTarget, ticksToWait - 1));
                }
            }
        }
    }

    public void dimensionalSight(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            if (livingEntity.isShiftKeyDown()) {
                int amount = 0;
                for (DimensionalSightSealEntity dimensionalSightSealEntity : livingEntity.level().getEntitiesOfClass(DimensionalSightSealEntity.class, livingEntity.getBoundingBox().inflate(10))) {
                    if (dimensionalSightSealEntity.getOwner() == livingEntity) {
                        amount++;
                        livingEntity.getPersistentData().putInt("dimensionalSightSealBackX", (int) livingEntity.getX());
                        livingEntity.getPersistentData().putInt("dimensionalSightSealBackY", (int) livingEntity.getY());
                        livingEntity.getPersistentData().putInt("dimensionalSightSealBackZ", (int) livingEntity.getZ());
                        livingEntity.getPersistentData().putInt("dimensionalSightSealX", (int) dimensionalSightSealEntity.getSealX());
                        livingEntity.getPersistentData().putInt("dimensionalSightSealY", (int) dimensionalSightSealEntity.getSealY());
                        livingEntity.getPersistentData().putInt("dimensionalSightSealZ", (int) dimensionalSightSealEntity.getSealZ());
                        livingEntity.getPersistentData().putInt("dimensionalSightSealTeleportTimer", 1);
                        dimensionalSightSealEntity.setShouldMessage(false);
                        dimensionalSightSealEntity.tickCount = dimensionalSightSealEntity.getMaxLife() - 1;
                        BlockPos sealPos = new BlockPos((int) dimensionalSightSealEntity.getSealX(), (int) dimensionalSightSealEntity.getSealY(), (int) dimensionalSightSealEntity.getSealZ());
                        int radius = 20;
                        for (int x = -radius; x <= radius; x++) {
                            for (int y = -radius; y <= radius; y++) {
                                for (int z = -radius; z <= radius; z++) {
                                    double distance = Math.sqrt(x * x + y * y + z * z);
                                    if (distance >= radius - 0.5 && distance <= radius + 0.5) {
                                        BlockPos blockPos = sealPos.offset(x, y, z);
                                        if (livingEntity.level().getBlockState(blockPos) == BlockInit.VOID_BLOCK.get().defaultBlockState()) {
                                            livingEntity.level().setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (amount >= 1) {
                    this.addCooldown(livingEntity);
                }
            }
            DimensionalSightTileEntity dimensionalSightTileEntity = BeyonderUtil.findNearbyDimensionalSight(livingEntity);
            if (dimensionalSightTileEntity != null && dimensionalSightTileEntity.scryUniqueID != null && dimensionalSightTileEntity.getCasterUUID() != null && dimensionalSightTileEntity.getCasterUUID().equals(livingEntity.getUUID())) {
                DimensionalSightSealEntity sightSealEntity = new DimensionalSightSealEntity(EntityInit.DIMENSIONAL_SIGHT_SEAL_ENTITY.get(), livingEntity.level());
                sightSealEntity.setSealX((float) dimensionalSightTileEntity.getScryTarget().getX());
                sightSealEntity.setSealY((float) dimensionalSightTileEntity.getScryTarget().getY());
                sightSealEntity.setSealZ((float) dimensionalSightTileEntity.getScryTarget().getZ());
                sightSealEntity.setOwner(livingEntity);
                Vec3 lookVec = livingEntity.getLookAngle().scale(-10);
                BlockPos pos = livingEntity.getOnPos();
                sightSealEntity.setMaxLife((int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.DIMENSIONAL_SIGHT.get()));
                sightSealEntity.teleportTo(pos.getX() + lookVec.z(), pos.getY() + lookVec.y(), pos.getZ() + lookVec.z());
                livingEntity.level().addFreshEntity(sightSealEntity);
                dimensionalSightTileEntity.removeThis();
                this.addCooldown(livingEntity);
            }

        }
    }

    public static BlockPos findSuitableBlockPos(Level level, BlockPos playerPos) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = playerPos.offset(x, -1, z);
                if (level.getBlockState(checkPos).isAir() && !level.getBlockState(checkPos.below()).isAir()) {
                    return checkPos;
                }
            }
        }
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                BlockPos checkPos = playerPos.offset(x, 0, z);
                if (level.getBlockState(checkPos).isAir()) {
                    return checkPos;
                }
            }
        }
        return null;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use on an entity, mark it with a dimensional sight in front of you, able to see all their surrounding blocks and themselves. You can also type the name of a player into chat to view them from anywhere."));
        tooltipComponents.add(Component.literal("You can use MOST Door pathway abilities while near and looking at a dimensional sight in order to have your abilities be cast at it's location."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("1000").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("5 Minutes").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    @SuppressWarnings("deprecation")
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.lazyAttributeMap.get();
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    private Multimap<Attribute, AttributeModifier> createAttributeMap() {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> attributeBuilder = ImmutableMultimap.builder();
        attributeBuilder.putAll(super.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND));
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 500, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 500, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
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