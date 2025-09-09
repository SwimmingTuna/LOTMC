package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.UpdateItemInHandC2S;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class TrickWind extends LeftClickHandlerSkillP {


    public TrickWind(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 8, 40, 200);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        useSpirituality(player);
        addCooldown(player);
        if (player.isShiftKeyDown()) {
            pull(player);
        } else {
            push(player);
        }
        return InteractionResult.SUCCESS;
    }


    public static void pull(LivingEntity player) {
        if (!player.level().isClientSide()) {
            Vec3 playerLookVector = player.getViewVector(1.0F);
            double fovAngle = Math.toRadians(70.0);
            DimensionalSightTileEntity dimensionalSightTileEntity = BeyonderUtil.findNearbyDimensionalSight(player);
            if (dimensionalSightTileEntity != null && dimensionalSightTileEntity.getScryTarget() != null) {
                player.sendSystemMessage(Component.literal("You created a suction force around your Dimensional Sight Target").withStyle(ChatFormatting.AQUA));
                LivingEntity scry = dimensionalSightTileEntity.getScryTarget();
                for (LivingEntity livingEntity : BeyonderUtil.checkEntitiesInLocation(scry, BeyonderUtil.getDamage(player).get(ItemInit.TRICKWIND.get()), (float) scry.getX(), (float) scry.getY(), (float) scry.getZ())) {
                    if (livingEntity != player && !BeyonderUtil.areAllies(livingEntity, player)) {
                        Vec3 playerPos = dimensionalSightTileEntity.getScryTarget().position();
                        Vec3 entityPos = livingEntity.position();
                        Vec3 toEntityVector = entityPos.subtract(playerPos).normalize();
                        double dotProduct = playerLookVector.dot(toEntityVector);
                        double angle = Math.acos(dotProduct);
                        if (angle <= fovAngle) {
                            Vec3 direction = playerPos.subtract(entityPos).normalize();
                            double distance = playerPos.distanceTo(entityPos);
                            double force = 0.5 * distance;
                            direction = new Vec3(direction.x, 0, direction.z).normalize();
                            livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(direction.scale(force)));
                            livingEntity.hurtMarked = true;
                        }
                    }
                }
            } else {
                for (Entity entity : BeyonderUtil.getNonAlliesNearby(player, BeyonderUtil.getDamage(player).get(ItemInit.TRICKWIND.get()))) {
                    Vec3 playerPos = player.position();
                    Vec3 entityPos = entity.position();
                    Vec3 toEntityVector = entityPos.subtract(playerPos).normalize();
                    double dotProduct = playerLookVector.dot(toEntityVector);
                    double angle = Math.acos(dotProduct);
                    if (angle <= fovAngle) {
                        Vec3 direction = playerPos.subtract(entityPos).normalize();
                        double distance = playerPos.distanceTo(entityPos);
                        double force = 0.5 * distance;
                        direction = new Vec3(direction.x, 0, direction.z).normalize();
                        entity.setDeltaMovement(entity.getDeltaMovement().add(direction.scale(force)));
                        entity.hurtMarked = true;
                    }
                }
            }
        }
    }

    public static void push(LivingEntity player) {
        if (!player.level().isClientSide()) {
            Vec3 playerLookVector = player.getViewVector(1.0F);
            double fovAngle = Math.toRadians(70.0);

            DimensionalSightTileEntity dimensionalSightTileEntity = BeyonderUtil.findNearbyDimensionalSight(player);
            if (dimensionalSightTileEntity != null && dimensionalSightTileEntity.getScryTarget() != null) {
                player.sendSystemMessage(Component.literal("You created a pushing force around your Dimensional Sight Target").withStyle(ChatFormatting.AQUA));
                LivingEntity scry = dimensionalSightTileEntity.getScryTarget();
                for (LivingEntity entity : BeyonderUtil.checkEntitiesInLocation(scry, BeyonderUtil.getDamage(player).get(ItemInit.TRICKWIND.get()), (float) scry.getX(), (float) scry.getY(), (float) scry.getZ())) {
                    if (entity != player && !BeyonderUtil.areAllies(entity, player)) {
                        Vec3 playerPos = dimensionalSightTileEntity.getScryTarget().position();
                        Vec3 entityPos = entity.position();
                        Vec3 toEntityVector = entityPos.subtract(playerPos).normalize();
                        double dotProduct = playerLookVector.dot(toEntityVector);
                        double angle = Math.acos(dotProduct);
                        if (angle <= fovAngle) {
                            Vec3 direction = playerPos.subtract(entityPos).normalize();
                            int factor = 10 - BeyonderUtil.getSequence(player);
                            entity.setDeltaMovement(entity.getDeltaMovement().add(direction.scale(-factor)));
                            entity.hurtMarked = true;
                        }
                    }
                }
            } else {
                for (LivingEntity living : BeyonderUtil.getNonAlliesNearby(player, BeyonderUtil.getDamage(player).get(ItemInit.TRICKWIND.get()))) {
                    Vec3 playerPos = player.position();
                    Vec3 entityPos = living.position();
                    Vec3 toEntityVector = entityPos.subtract(playerPos).normalize();
                    double dotProduct = playerLookVector.dot(toEntityVector);
                    double angle = Math.acos(dotProduct);
                    if (angle <= fovAngle) {
                        Vec3 direction = playerPos.subtract(entityPos).normalize();
                        int factor = 10 - BeyonderUtil.getSequence(player);
                        living.setDeltaMovement(living.getDeltaMovement().add(direction.scale(-factor)));
                        living.hurtMarked = true;
                    }
                }
            }
        }
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

        //reach should be___
        attributeBuilder.putAll(super.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND));
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 12, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 12, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, manipulate the wind to push entities away from you, or pull them in if you're shifting."));
        tooltipComponents.add(Component.literal("Left click for Trick: Black Curtain"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("40").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("10 Seconds").withStyle(ChatFormatting.YELLOW)));
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
            if (livingEntity.getHealth() > target.getHealth()) {
                livingEntity.setShiftKeyDown(true);
                return 45;
            } else if (livingEntity.getHealth() < target.getHealth()) {
                return 45;
            }
        }
        return 0;
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.TRICKBLACKCURTAIN.get()));
    }
}