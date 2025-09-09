package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.swimmingtuna.lotm.entity.ApprenticeDoorEntity;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class DoorMirage extends SimpleAbilityItem {
    private static final Random RANDOM = new Random();

    public DoorMirage(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 4, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity livingEntity, InteractionHand hand) {
        if (!checkAll(livingEntity)) {
            return InteractionResult.FAIL;
        }
        mirage(livingEntity);
        addCooldown(livingEntity);
        useSpirituality(livingEntity);
        return InteractionResult.SUCCESS;
    }

    public static void mirage(LivingEntity entity) {
        CompoundTag tag = entity.getPersistentData();
        boolean mirage = tag.getBoolean("doorMirageIsActive");
        tag.putBoolean("doorMirageIsActive", !mirage);

        if (mirage) {
            EventManager.removeFromRegularLoop(entity, EFunctions.DOOR_MIRAGE.get());
        } else {
            EventManager.addToRegularLoop(entity, EFunctions.DOOR_MIRAGE.get());
        }

        if (entity instanceof Player pPlayer) {
            pPlayer.displayClientMessage(Component.literal("Door Mirage Turned " + (mirage ? "Off" : "On"))
                    .withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE), true);
        }

        entity.getPersistentData().putInt("doorMirageDodgeCounter", 0);
    }

    public static void mirageTick(LivingEntity entity) {
        if (entity.level().isClientSide) return;

        CompoundTag tag = entity.getPersistentData();
        boolean doorMirage = tag.getBoolean("doorMirageIsActive");
        int counter = tag.getInt("doorMirageDodgeCounter");
        int invincibilityCounter = tag.getInt("doorMirageInvincibilityCounter");

        if (doorMirage) {
            if (counter < 100) {
                entity.getPersistentData().putInt("doorMirageDodgeCounter", counter + 1);
            }

            BeyonderUtil.setInvisible(entity, true, 20);

            if (entity.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleInit.DOOR.get(), entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), counter / 10, 0.2, 0.25, 0.2, 0.01);
            }
        }

        if (invincibilityCounter > 0) {
            tag.putInt("doorMirageInvincibilityCounter", invincibilityCounter - 1);
        }

        if (entity.getPersistentData().contains("xDoorMirageStuck") && entity.getPersistentData().contains("yDoorMirageStuck") && entity.getPersistentData().contains("zDoorMirageStuck")) {
            entity.teleportTo(entity.getPersistentData().getDouble("xDoorMirageStuck"), entity.getPersistentData().getDouble("yDoorMirageStuck"), entity.getPersistentData().getDouble("zDoorMirageStuck"));
        }

        if (!entity.level().isClientSide() && entity.getPersistentData().getBoolean("doorMirageIsActive")) {
            if (BeyonderUtil.getSpirituality(entity) > 3) {
                BeyonderUtil.useSpirituality(entity, 3);
            }
            else {
                entity.getPersistentData().putBoolean("doorMirageIsActive", false);
                entity.sendSystemMessage(Component.literal("Door Mirage turned off due to lack of spirituality").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
            }
        }
    }

    public static void doorMirageAttackEvent(LivingAttackEvent event) {
        LivingEntity attacked = event.getEntity();
        CompoundTag tag = attacked.getPersistentData();
        boolean doorMirage = tag.getBoolean("doorMirageIsActive");
        int invincibilityCounter = tag.getInt("doorMirageInvincibilityCounter");
        int counter = tag.getInt("doorMirageDodgeCounter");
        Entity attacker = event.getSource().getEntity();
        if (attacker != null) {
            if (doorMirage) {
                if (invincibilityCounter > 0) {
                    event.setCanceled(true);
                    return;
                }
                int requiredCounter = (int) (float) BeyonderUtil.getDamage(attacked).get(ItemInit.DOOR_MIRAGE.get());
                if (counter >= requiredCounter) {
                    event.setCanceled(true);
                    if (attacked instanceof Player player) {
                        player.displayClientMessage(Component.literal("Successfully dodged an attack").withStyle(BeyonderUtil.getStyle(player)), true);
                    }
                    tag.putInt("doorMirageInvincibilityCounter", 15);
                    if (attacker instanceof LivingEntity livingAttacker) {
                        DoorMirage.summonDoorOnAttacker(attacked, livingAttacker);
                    }
                    attacked.getPersistentData().putInt("doorMirageDodgeCounter", 0);
                } else {
                    if (attacked instanceof Player player) {
                        int timeRemaining = Math.max(1, (requiredCounter - counter) / 20);
                        player.displayClientMessage(Component.literal("Dodge not ready yet. Time until next dodge: " + timeRemaining + " seconds").withStyle(BeyonderUtil.getStyle(player)), true);
                    }
                }
            }
        }
    }

    public static void summonDoorOnAttacker(LivingEntity attacked, LivingEntity attacker) {
        float yaw = -attacked.getYRot() + 180;
        int x = getSafeSpaceCoordinates(attacker)[0];
        int y = getSafeSpaceCoordinates(attacker)[1];
        int z = getSafeSpaceCoordinates(attacker)[2];
        ApprenticeDoorEntity door = new ApprenticeDoorEntity(attacker.level(), attacker, BeyonderUtil.getSequence(attacked), yaw, x, y, z);
        door.setPos(attacker.getX(), attacker.getY(), attacker.getZ());
        attacker.level().addFreshEntity(door);
    }

    public static int[] getSafeSpaceCoordinates(LivingEntity entity) {
        Random random = new Random();
        float range = 10;
        int maxAttempts = 100;
        int x = entity.getBlockX();
        int y = entity.getBlockY();
        int z = entity.getBlockZ();
        for (int i = 0; i < maxAttempts; i++) {
            int xOffSet = random.nextInt((int) range * 2) - (int) range;
            int yOffSet = random.nextInt((int) range * 2) - (int) range;
            int zOffSet = random.nextInt((int) range * 2) - (int) range;
            double distanceSq = xOffSet * xOffSet + yOffSet * yOffSet + zOffSet * zOffSet;
            if (distanceSq <= range * range) {
                BlockPos pos = entity.blockPosition().offset(xOffSet, yOffSet, zOffSet);
                if (entity.level().getBlockState(pos).isAir() && entity.level().getBlockState(pos.below()).entityCanStandOn(entity.level(), pos.below(), entity)) {
                    return new int[]{pos.getX(), pos.getY(), pos.getZ()};
                }
            }
        }
        return new int[]{x, y, z};
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, starts to transform in a series of doors, causing an attacker once every ~3 seconds to be teleported away from you and have their attack nullified"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("60 per second").withStyle(ChatFormatting.YELLOW)));
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
        if (target != null && BeyonderUtil.getSpirituality(livingEntity) < BeyonderUtil.getMaxSpirituality(livingEntity) / 2 && livingEntity.getPersistentData().getBoolean("doorMirageIsActive")) {
            return 80;
        }
        if (target != null && !livingEntity.getPersistentData().getBoolean("doorMirageIsActive") && BeyonderUtil.getSpirituality(livingEntity) > BeyonderUtil.getMaxSpirituality(livingEntity) / 2) {
            return 80;
        }
        if (target == null && livingEntity.getPersistentData().getBoolean("doorMirageIsActive")) {
            return 100;
        }
        return 0;
    }
}
