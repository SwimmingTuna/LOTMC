package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlinkAfterimage extends LeftClickHandlerSkillP {


    public BlinkAfterimage(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 5, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        useSpirituality(player);
        addCooldown(player);
        enableDisableTelekenesis(player);
        return InteractionResult.SUCCESS;
    }

    public static void enableDisableTelekenesis(LivingEntity player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            boolean afterimage = tag.getBoolean("travelerAfterimage");
            tag.putBoolean("travelerAfterimage", !afterimage);
            if (player instanceof Player pPlayer) {
                pPlayer.displayClientMessage(Component.literal("Blink Afterimage Turned " + (afterimage ? "Off" : "On")).withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE), true);
            }
        }
    }


    public static void travelerBlinkPassive(LivingAttackEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        Level level = livingEntity.level();
        if (!level.isClientSide() && !event.isCanceled()) {
            if (tag.getBoolean("travelerAfterimage")) {
                float amount = 40 + (event.getAmount() * BeyonderUtil.getDamage(livingEntity).get(ItemInit.BLINKAFTERIMAGE.get()));
                if (BeyonderUtil.getSpirituality(livingEntity) >= amount) {
                    BeyonderUtil.useSpirituality(livingEntity, (int) ((int) amount * 1.3f));
                    int teleportDistance = (int) Math.ceil(event.getAmount());
                    boolean teleported = tryTeleportToSafeLocation(livingEntity, level, teleportDistance);
                    event.setCanceled(true);
                    if (livingEntity instanceof Player player && !teleported) {
                        player.displayClientMessage(Component.literal("Failed to find safe location to blink to").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED), true);
                    }

                } else {
                    tag.putBoolean("travelerAfterimage", false);
                    SimpleAbilityItem.addCooldown(livingEntity, ItemInit.BLINKAFTERIMAGE.get(), 200);
                    if (livingEntity instanceof Player player) {
                        player.displayClientMessage(Component.literal("Blink Afterimage turned off due to lack of spirituality").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED), true);
                    }
                }
            }
        }
    }


    private static boolean tryTeleportToSafeLocation(Entity entity, Level level, int distance) {
        distance = Math.max(1, distance);
        List<Vec3> directions = new ArrayList<>();
        directions.add(new Vec3(1, 0, 0));   // East
        directions.add(new Vec3(-1, 0, 0));  // West
        directions.add(new Vec3(0, 0, 1));   // South
        directions.add(new Vec3(0, 0, -1));  // North
        directions.add(new Vec3(1, 0, 1));   // Southeast
        directions.add(new Vec3(-1, 0, 1));  // Southwest
        directions.add(new Vec3(1, 0, -1));  // Northeast
        directions.add(new Vec3(-1, 0, -1)); // Northwest
        directions.add(new Vec3(0, 1, 0));   // Up
        directions.add(new Vec3(0, -1, 0));  // Down
        directions.add(new Vec3(1, 1, 0));   // Up + East
        directions.add(new Vec3(-1, 1, 0));  // Up + West
        directions.add(new Vec3(0, 1, 1));   // Up + South
        directions.add(new Vec3(0, 1, -1));  // Up + North
        directions.add(new Vec3(1, -1, 0));  // Down + East
        directions.add(new Vec3(-1, -1, 0)); // Down + West
        directions.add(new Vec3(0, -1, 1));  // Down + South
        directions.add(new Vec3(0, -1, -1)); // Down + North

        // Shuffle the directions for randomness
        Collections.shuffle(directions);
        Vec3 currentPos = entity.position();
        for (Vec3 dir : directions) {
            Vec3 normalizedDir = dir.normalize().scale(distance);
            Vec3 targetVec = currentPos.add(normalizedDir);
            BlockPos targetPos = new BlockPos((int) Math.floor(targetVec.x), (int) Math.floor(targetVec.y), (int) Math.floor(targetVec.z));
            if (isSafeLocation(targetPos, level, entity)) {
                entity.teleportTo(targetVec.x, targetVec.y, targetVec.z);
                return true;
            }
        }

        for (int attempt = 0; attempt < 10; attempt++) {
            Vec3 dir = directions.get(attempt % directions.size());
            int adjustedDistance = distance + (attempt % 3 - 1);
            Vec3 normalizedDir = dir.normalize().scale(adjustedDistance);
            Vec3 targetVec = currentPos.add(normalizedDir);
            BlockPos targetPos = new BlockPos((int) Math.floor(targetVec.x), (int) Math.floor(targetVec.y), (int) Math.floor(targetVec.z));
            if (isSafeLocation(targetPos, level, entity)) {
                entity.teleportTo(targetVec.x, targetVec.y, targetVec.z);
                return true;
            }
        }

        return false;
    }

    private static boolean isSafeLocation(BlockPos pos, Level level, Entity entity) {
        float scale = 1.0f;
        if (entity instanceof LivingEntity livingEntity) {
            scale = BeyonderUtil.getScale(livingEntity);
        }
        int requiredSpace = Math.max(1, (int) Math.ceil(scale));
        for (int x = -requiredSpace; x <= requiredSpace; x++) {
            for (int y = 0; y <= requiredSpace * 2; y++) {
                for (int z = -requiredSpace; z <= requiredSpace; z++) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    if (!level.getBlockState(checkPos).isAir()) {
                        return false;
                    }
                }
            }
        }

        return true;
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
        tooltipComponents.add(Component.literal("Use in order to enable or disable your automatic blinking. If enabled, you will be automatically blink out of the way of any form of damage."));
        tooltipComponents.add(Component.literal("Left Click for Blink"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("While Active: 50 + (Amount of Damage * ~2)").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (PlayerMobEntity.isCopy(livingEntity)) {
            return 0;
        }
        if (target != null && BeyonderUtil.getSpirituality(livingEntity) < BeyonderUtil.getMaxSpirituality(livingEntity) / 3 && livingEntity.getPersistentData().getBoolean("travelerAfterimage")) {
            return 80;
        }
        if (!livingEntity.getPersistentData().getBoolean("travelerAfterimage") && target != null && BeyonderUtil.getSpirituality(livingEntity) > BeyonderUtil.getMaxSpirituality(livingEntity) / 2) {
            return 80;
        } else if (target == null && livingEntity.getPersistentData().getBoolean("travelerAfterimage")) {
            return 100;
        }
        return 0;
    }



    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICE_ABILITY", ChatFormatting.AQUA);
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.BLINK.get()));
    }
}