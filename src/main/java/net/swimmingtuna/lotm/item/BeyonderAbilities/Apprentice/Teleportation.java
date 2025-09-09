package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class Teleportation extends SimpleAbilityItem {

    public Teleportation(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 2, 1500, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }
            addCooldown(player);
            teleporation(player);
            useSpirituality(player);
        } else {
            removeCopies(player);
            if (player instanceof Player pPlayer) {
                pPlayer.displayClientMessage(Component.literal("You are no longer flickering between locations").withStyle(ChatFormatting.AQUA), true);
            }
            addCooldown(player);
        }
        return InteractionResult.SUCCESS;
    }

    public static void removeCopies(LivingEntity player){
        for (PlayerMobEntity playerMobEntity : BeyonderUtil.getAllPlayerMobEntities(player.level())) {
            if (playerMobEntity.getCreator().is(player) && playerMobEntity.getPersistentData().getBoolean("shouldFlicker")) {
                playerMobEntity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    public void teleporation(LivingEntity player) {
        if (!player.level().isClientSide()) {
            PlayerMobEntity playerMobEntity = flickeringCopy(player);
            Vec3 lookVec = player.getLookAngle().scale(20);
            Vec3 location = new Vec3(player.getX() + lookVec.x(), player.getY() + lookVec.y(), player.getZ() + lookVec.z());
            int surfaceY = playerMobEntity.level().getHeight(Heightmap.Types.WORLD_SURFACE, (int) location.x, (int) location.z) + 2;
            DimensionalSightTileEntity dimensionalSightTileEntity = BeyonderUtil.findNearbyDimensionalSight(player);
            if (dimensionalSightTileEntity != null && dimensionalSightTileEntity.getScryTarget() != null) {
                playerMobEntity.teleportTo(dimensionalSightTileEntity.getScryTarget().getX(), dimensionalSightTileEntity.getScryTarget().getY(), dimensionalSightTileEntity.getScryTarget().getZ());
            } else {
                playerMobEntity.teleportTo(location.x(), surfaceY, location.z());
            }
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                playerMobEntity.setItemSlot(slot, player.getItemBySlot(slot).copy());
            }
            CompoundTag playerData = player.getPersistentData();
            CompoundTag cloneData = playerMobEntity.getPersistentData();
            cloneData.merge(playerData.copy());
            for (Attribute attribute : ForgeRegistries.ATTRIBUTES.getValues()) {
                AttributeInstance playerAttribute = player.getAttribute(attribute);
                AttributeInstance cloneAttribute = playerMobEntity.getAttribute(attribute);
                if (playerAttribute != null && cloneAttribute != null) {
                    cloneAttribute.setBaseValue(playerAttribute.getBaseValue());
                    for (AttributeModifier modifier : playerAttribute.getModifiers()) {
                        if (!cloneAttribute.hasModifier(modifier)) {
                            cloneAttribute.addPermanentModifier(modifier);
                        }
                    }
                }
            }
            BeyonderUtil.setScale(playerMobEntity, BeyonderUtil.getScale(player));
            playerMobEntity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(player.getMaxHealth());
            playerMobEntity.setHealth(player.getHealth());
            for (MobEffectInstance effect : player.getActiveEffects()) {
                playerMobEntity.addEffect(new MobEffectInstance(effect));
            }

            Set<String> playerTags = player.getTags();
            for (String tag : playerTags) {
                playerMobEntity.addTag(tag);
            }


            if (BeyonderUtil.canFly(player)) {
                BeyonderUtil.startFlying(playerMobEntity, 0.12f * BeyonderUtil.getDamage(player).get(ItemInit.TELEPORTATION.get()), 5000);
            }

            playerMobEntity.setCreator(player);
            playerMobEntity.setUsername(player.getScoreboardName());
            playerMobEntity.setIsClone(true);
            playerMobEntity.setIdealDistanceFromTarget(10);
            playerMobEntity.setAttackChance(100);
            playerMobEntity.setMaxSpirituality(BeyonderClassInit.APPRENTICE.get().spiritualityLevels().get(BeyonderUtil.getSequence(player)));
            playerMobEntity.setSpirituality(BeyonderClassInit.APPRENTICE.get().spiritualityLevels().get(BeyonderUtil.getSequence(player)));
            playerMobEntity.setRegenSpirituality(false);
            if (player.hasCustomName()) {
                playerMobEntity.setCustomName(player.getCustomName());
            }

            player.level().addFreshEntity(playerMobEntity);
        }
    }

    public static PlayerMobEntity flickeringCopy(LivingEntity player) {
        PlayerMobEntity playerMobEntity = new PlayerMobEntity(EntityInit.PLAYER_MOB_ENTITY.get(), player.level());
        playerMobEntity.setSequence(BeyonderUtil.getSequence(player));
        playerMobEntity.setPathway(BeyonderUtil.getPathway(player));
        CompoundTag tag = playerMobEntity.getPersistentData();
        tag.putBoolean("shouldFlicker", true);
        return playerMobEntity;
    }

    public static void teleportationHurtEvent(LivingHurtEvent event) {
        if (event.getEntity() instanceof PlayerMobEntity playerMobEntity) {
            if (!playerMobEntity.level().isClientSide() && playerMobEntity.getPersistentData().getBoolean("shouldFlicker")) {
                float amount = event.getAmount();
                if (playerMobEntity.getCreator() != null) {
                    LivingEntity creator = playerMobEntity.getCreator();
                    boolean x = !(creator instanceof Player player) || (!player.isCreative() && !player.isSpectator());
                    if (creator.isAlive()) {
                        if (event.getAmount() > creator.getHealth() + 10) {
                            playerMobEntity.remove(Entity.RemovalReason.DISCARDED);
                            creator.sendSystemMessage(Component.empty().append(Component.literal("Your copy flickering at ").withStyle(ChatFormatting.AQUA)).append(Component.literal(String.format("%.1f, %.1f, %.1f", playerMobEntity.getX(), playerMobEntity.getY(), playerMobEntity.getZ())).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD)).append(Component.literal(" was removed due to too much damage").withStyle(ChatFormatting.AQUA)));
                        }
                        if (creator.getHealth() > 10) {
                            event.setCanceled(true);
                            if (x) {
                                creator.hurt(event.getSource(), amount);
                                creator.sendSystemMessage(Component.empty().append(Component.literal("Your copy flickering at ").withStyle(ChatFormatting.AQUA)).append(Component.literal(String.format("%.1f, %.1f, %.1f", playerMobEntity.getX(), playerMobEntity.getY(), playerMobEntity.getZ())).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD)).append(Component.literal(" hurt you.").withStyle(ChatFormatting.AQUA)));
                            }
                        } else {
                            creator.sendSystemMessage(Component.empty().append(Component.literal("Your copy flickering at ").withStyle(ChatFormatting.AQUA)).append(Component.literal(String.format("%.1f, %.1f, %.1f", playerMobEntity.getX(), playerMobEntity.getY(), playerMobEntity.getZ())).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD)).append(Component.literal(" was removed due to your low health.").withStyle(ChatFormatting.AQUA)));
                            playerMobEntity.remove(Entity.RemovalReason.DISCARDED);
                        }
                    } else {
                        playerMobEntity.remove(Entity.RemovalReason.DISCARDED);
                    }
                } else {
                    playerMobEntity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, create a copy of yourself that is flickering in front of you, which will keep any active abilities you choose."));
        tooltipComponents.add(Component.literal("These flickering copies will try to attach anything nearby, and cause you to take any damage they take (despawning if the damage will put you near death), and use your spirituality."));
        tooltipComponents.add(Component.literal("Shift right click to remove all your copies"));
        tooltipComponents.add(Component.literal("Type in a player's name or any coordinates in order to cause a copy to appear at that location."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("1500").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Second").withStyle(ChatFormatting.YELLOW)));
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
        if (PlayerMobEntity.isCopy(livingEntity)) {
            return 0;
        }
        return 0;
    }
}
