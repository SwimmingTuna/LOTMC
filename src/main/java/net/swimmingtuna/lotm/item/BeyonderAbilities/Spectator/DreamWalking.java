package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class DreamWalking extends SimpleAbilityItem {
    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    public DreamWalking(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 5, 40, 40,300,300);
    }
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
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 300, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 300, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, connect your and the target's minds, travelling into their dreams before appearing in reality at their location."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("70").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("2 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!player.level().isClientSide()) {
            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }
            addCooldown(player, this, 40 / BeyonderUtil.getDreamIntoReality(player));
            useSpirituality(player);
            dreamWalkingNew(player, interactionTarget);
        }
        return InteractionResult.SUCCESS;
    }

    public static void dreamWalk(LivingEntity interactionTarget, LivingEntity player) {
        if (!player.level().isClientSide()) {
            double x = interactionTarget.getX();
            double y = interactionTarget.getY();
            double z = interactionTarget.getZ();
            player.teleportTo(x, y, z);
            player.getPersistentData().putInt("dreamWalkingDeaggro", 5);
        }
    }

    public static void dreamWalkingNew(LivingEntity player, LivingEntity interactionTarget) {
        if (!player.level().isClientSide()) {
            if (player instanceof Player pPlayer) {
                player.getPersistentData().putInt("dreamWalkingDeaggro", 5);
                player.getPersistentData().putUUID("dreamWalkingTargetUUID", interactionTarget.getUUID());
                if (pPlayer.isCreative()) {
                    pPlayer.getPersistentData().putString("dreamWalkingGamemode", "creative");
                } else if (pPlayer.isSpectator()) {
                    pPlayer.getPersistentData().putString("dreamWalkingGamemode", "spectator");
                } else {
                    pPlayer.getPersistentData().putString("dreamWalkingGamemode", "survival");
                }
            } else {
                double x = interactionTarget.getX();
                double y = interactionTarget.getY();
                double z = interactionTarget.getZ();
                player.teleportTo(x, y, z);
                player.getPersistentData().putInt("dreamWalkingDeaggro", 5);
            }
        }
    }

    public static void dreamWalkingTick(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            if (event.getEntity().getPersistentData().getInt("dreamWalkingDeaggro") >= 1) {
                event.getEntity().getPersistentData().putInt("dreamWalkingDeaggro", event.getEntity().getPersistentData().getInt("dreamWalkingDeaggro") - 1);
                for (Mob mob : event.getEntity().level().getEntitiesOfClass(Mob.class, event.getEntity().getBoundingBox().inflate(5))) {
                    if (mob.getTarget() == event.getEntity()) {
                        mob.setTarget(null);
                    }
                }
            }
            LivingEntity player = event.getEntity();
            if (player.getPersistentData().contains("dreamWalkingTargetUUID")) {
                LivingEntity dreamWalkingTarget = BeyonderUtil.getLivingEntityFromUUID(player.level(), player.getPersistentData().getUUID("dreamWalkingTargetUUID"));
                if (dreamWalkingTarget != null) {
                    if (dreamWalkingTarget.level().dimension() == player.level().dimension()) {
                        player.getPersistentData().putInt("dreamWalkingMode", 7);
                    } else {
                        if (player instanceof ServerPlayer serverPlayer) {
                            player.getPersistentData().putInt("dreamWalkingMode", 0);
                            String string = serverPlayer.getPersistentData().getString("dreamWalkingGamemode");
                            if (string.equalsIgnoreCase("creative")) {
                                serverPlayer.setGameMode(GameType.CREATIVE);
                            } else if (string.equalsIgnoreCase("spectator")) {
                                serverPlayer.setGameMode(GameType.SPECTATOR);
                            } else {
                                serverPlayer.setGameMode(GameType.SURVIVAL);
                            }
                        }
                    }
                    if (player.distanceTo(dreamWalkingTarget) <= 5) {
                        player.getPersistentData().remove("dreamWalkingTargetUUID");
                        if (player instanceof ServerPlayer serverPlayer) {
                            player.getPersistentData().putInt("dreamWalkingMode", 0);
                            String string = serverPlayer.getPersistentData().getString("dreamWalkingGamemode");
                            if (string.equalsIgnoreCase("creative")) {
                                serverPlayer.setGameMode(GameType.CREATIVE);
                            } else if (string.equalsIgnoreCase("spectator")) {
                                serverPlayer.setGameMode(GameType.SPECTATOR);
                            } else {
                                serverPlayer.setGameMode(GameType.SURVIVAL);
                            }
                        }
                        player.teleportTo(dreamWalkingTarget.getX(), dreamWalkingTarget.getY(), dreamWalkingTarget.getZ());
                    }
                    player.hurtMarked = true;
                    player.setDeltaMovement(dreamWalkingTarget.getX() - player.getX(), dreamWalkingTarget.getY() - player.getY(), dreamWalkingTarget.getZ() - player.getZ());
                }
            }
            if (player.getPersistentData().getInt("dreamWalkingMode") >= 1 && player instanceof ServerPlayer serverPlayer) {
                player.getPersistentData().putInt("dreamWalkingMode", player.getPersistentData().getInt("dreamWalkingMode") - 1);
                serverPlayer.setGameMode(GameType.SPECTATOR);
                if (player.getPersistentData().getInt("dreamWalkingMode") == 1) {
                    player.getPersistentData().putInt("dreamWalkingMode", 0);
                    String string = serverPlayer.getPersistentData().getString("dreamWalkingGamemode");
                    if (string.equalsIgnoreCase("creative")) {
                        serverPlayer.setGameMode(GameType.CREATIVE);
                    } else if (string.equalsIgnoreCase("spectator")) {
                        serverPlayer.setGameMode(GameType.SPECTATOR);
                    } else {
                        serverPlayer.setGameMode(GameType.SURVIVAL);
                    }
                }
            }
        }
    }
    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SPECTATOR_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null && target.getHealth() < livingEntity.getHealth()) {
            return 80;
        }
        return 0;
    }
}
