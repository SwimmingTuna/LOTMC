package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.networking.packet.UpdateItemInHandC2S;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class TrickFreezing extends LeftClickHandlerSkillP {
    public TrickFreezing(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 8, 70, 300);
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity livingEntity, LivingEntity interactionTarget, InteractionHand hand) {
        if (!livingEntity.level().isClientSide && !interactionTarget.level().isClientSide) {
            if (!checkAll(livingEntity)) {
                return InteractionResult.FAIL;
            }
            addCooldown(livingEntity);
            useSpirituality(livingEntity);
            freezeEntity(livingEntity, interactionTarget);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity livingEntity, InteractionHand hand) {
        if (!checkAll(livingEntity)) {
            return InteractionResult.FAIL;
        }
        freezeAura(livingEntity);
        return InteractionResult.SUCCESS;
    }

    public static void freezeEntity(LivingEntity livingEntity, LivingEntity target) {
        if (!livingEntity.level().isClientSide() && !target.level().isClientSide()) {
            BeyonderUtil.applyParalysis(target, (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TRICKFREEZING.get()));
            if (target.level() instanceof ServerLevel serverLevel) {
                Vec3 sourcePos = livingEntity.position().add(0, livingEntity.getBbHeight() * 0.5, 0);
                Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
                double distance = sourcePos.distanceTo(targetPos);
                Vec3 direction = targetPos.subtract(sourcePos).normalize();
                int particleCount = (int) (distance * 5);
                for (int i = 0; i < particleCount; i++) {
                    double progress = i / (double) particleCount;
                    Vec3 pos = sourcePos.add(direction.scale(distance * progress));
                    double offsetX = livingEntity.getRandom().nextGaussian() * 0.02;
                    double offsetY = livingEntity.getRandom().nextGaussian() * 0.02;
                    double offsetZ = livingEntity.getRandom().nextGaussian() * 0.02;
                    serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, pos.x, pos.y, pos.z, 1, offsetX, offsetY, offsetZ, 0.01);
                }
            }
        }
    }

    public static void freezeAura(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            int damage = (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TRICKFREEZING.get());
            DimensionalSightTileEntity dimensionalSightTileEntity = BeyonderUtil.findNearbyDimensionalSight(livingEntity);
            if (dimensionalSightTileEntity != null && dimensionalSightTileEntity.getScryTarget() != null) {
                for (LivingEntity living : BeyonderUtil.checkEntitiesInLocation(livingEntity, (float) (damage ), (float) dimensionalSightTileEntity.getScryTarget().getX(), (float) dimensionalSightTileEntity.getScryTarget().getY(), (float) dimensionalSightTileEntity.getScryTarget().getZ())) {
                    BeyonderUtil.applyParalysis(living, damage / 3);
                }
                Level level = dimensionalSightTileEntity.getScryTarget().level();
                BlockPos centerPos = dimensionalSightTileEntity.getScryTarget().blockPosition();
                for (int x = -damage; x <= damage; x++) {
                    for (int z = -damage; z <= damage; z++) {
                        for (int y = -damage; y <= damage; y++) {
                            BlockPos pos = centerPos.offset(x, y, z);
                            if (isOnSurface(level, pos) && level.getBlockEntity(pos) == null) {
                                freezeBlock(level, pos);
                            }
                        }
                    }
                }
                livingEntity.sendSystemMessage(Component.literal("You froze your Dimensional Sight Target").withStyle(ChatFormatting.AQUA));
            }
            for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(damage))) {
                if (living != livingEntity && !BeyonderUtil.areAllies(livingEntity, living)) {
                    BeyonderUtil.applyParalysis(living,(int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TRICKFREEZING.get()));
                }
            }
            Level level = livingEntity.level();
            BlockPos centerPos = livingEntity.blockPosition();
            for (int x = -damage; x <= damage; x++) {
                for (int z = -damage; z <= damage; z++) {
                    for (int y = -damage; y <= damage; y++) {
                        BlockPos pos = centerPos.offset(x, y, z);
                        if (isOnSurface(level, pos)) {
                            freezeBlock(level, pos);
                        }
                    }
                }
            }
        }
    }

    public static void freezeBlock(Level level, BlockPos pos) {
        BlockState currentState = level.getBlockState(pos);
        if (isOnSurface(level, pos)) {
            if (!currentState.isAir()) {
                level.setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.literal("Upon use, freezes the target for a small amount of time, or if none is selected, freeze all entities/blocks around you."));
        tooltipComponents.add(Component.literal("Left click for Trick: Loud Noise"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("70").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("15 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, isAdvanced);
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
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 15, AttributeModifier.Operation.ADDITION)); // adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 15, AttributeModifier.Operation.ADDITION)); // adds a 12 block reach for interacting with blocks, pretty much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICE_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null) {
            return 60;
        }
        return 0;
    }

    public static boolean isOnSurface(Level level, BlockPos pos) {
        return level.canSeeSky(pos.above()) || !level.getBlockState(pos.above()).isSolid();
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.TRICKLOUDNOISE.get()));
    }
}