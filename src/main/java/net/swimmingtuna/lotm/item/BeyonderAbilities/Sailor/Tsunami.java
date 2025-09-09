package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.UpdateItemInHandC2S;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class Tsunami extends LeftClickHandlerSkillP {

    public Tsunami(Properties properties) { //fix cooldown and spirituality
        super(properties, BeyonderClassInit.SAILOR, 4, 500, 900);
    }


    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        if (player instanceof Player pPlayer) {
            pPlayer.getCooldowns().addCooldown(ItemInit.TSUNAMI_SEAL.get(), 2400);
        }
        addCooldown(player);
        useSpirituality(player);
        startTsunami(player);
        return InteractionResult.SUCCESS;
    }

    public static void startTsunami(LivingEntity player) {
        if (!player.level().isClientSide()) {
            EventManager.addToRegularLoop(player, EFunctions.TSUNAMI.get());
            player.getPersistentData().putInt("sailorTsunami", (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.TSUNAMI.get()));
            float yaw = player.getYRot();
            String direction = getDirectionFromYaw(yaw);
            player.getPersistentData().putString("sailorTsunamiDirection", direction);
            player.getPersistentData().putInt("sailorTsunamiX", (int) player.getX());
            player.getPersistentData().putInt("sailorTsunamiY", (int) player.getY());
            player.getPersistentData().putInt("sailorTsunamiZ", (int) player.getZ());
        }
    }

    public static void tsunami(LivingEntity livingEntity) {
        //TSUNAMI
        CompoundTag tag = livingEntity.getPersistentData();
        int tsunami = tag.getInt("sailorTsunami");
        if (tsunami >= 1) {
            tag.putInt("sailorTsunami", tsunami - 5);
            Tsunami.summonTsunami(livingEntity);
        } else {
            tag.remove("sailorTsunamiDirection");
            tag.remove("sailorTsunamiX");
            tag.remove("sailorTsunamiY");
            tag.remove("sailorTsunamiZ");
        }

        //TSUNAMI SEAL
        int tsunamiSeal = tag.getInt("sailorTsunamiSeal");
        if (tsunamiSeal >= 1) {
            tag.putInt("sailorTsunamiSeal", tsunamiSeal - 5);
            TsunamiSeal.summonTsunami(livingEntity);
        } else {
            tag.remove("sailorTsunamiSealDirection");
            tag.remove("sailorTsunamiSealX");
            tag.remove("sailorTsunamiSealY");
            tag.remove("sailorTsunamiSealZ");
        }
        if (tsunami == 0 && tsunamiSeal == 0) {
            EventManager.removeFromRegularLoop(livingEntity, EFunctions.TSUNAMI.get());
        }
    }
    public static String getDirectionFromYaw(float yaw) {
        if (yaw < 0) {
            yaw += 360;
        }
        if (yaw >= 315 || yaw < 45) {
            return "N";
        } else if (yaw >= 45 && yaw < 135) {
            return "E";
        } else if (yaw >= 135 && yaw < 225) {
            return "S";
        } else if (yaw >= 225 && yaw < 315) {
            return "W";
        }
        return "N";
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, summons a colossal wave in the direction you're looking"));
        tooltipComponents.add(Component.literal("Left Click for Tsunami (Seal)"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("500").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("45 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static void summonTsunami(LivingEntity player) {
        CompoundTag tag = player.getPersistentData();
        int playerX = tag.getInt("sailorTsunamiX");
        int playerY = tag.getInt("sailorTsunamiY");
        int playerZ = tag.getInt("sailorTsunamiZ");
        int tsunami = tag.getInt("sailorTsunami");
        String direction = tag.getString("sailorTsunamiDirection");

        int offsetX = 0;
        int offsetZ = 0;

        switch (direction) {
            case "N":
                offsetZ = 1;
                break;
            case "E":
                offsetX = -1;
                break;
            case "S":
                offsetZ = -1;
                break;
            case "W":
                offsetX = 1;
                break;
        }

        int waveWidth = 80;
        int waveHeight = 10;
        int startDistance = 15;

        for (int w = -waveWidth / 2; w < waveWidth / 2; w++) {
            for (int h = 0; h < waveHeight; h++) {
                int x = playerX + (offsetX * startDistance) + (offsetX * (200 - tsunami) / 5);
                int y = playerY + h;
                int z = playerZ + (offsetZ * startDistance) + (offsetZ * (200 - tsunami) / 5);

                if (offsetX == 0) {
                    x += w;
                } else {
                    z += w;
                }

                BlockPos blockPos = new BlockPos(x, y, z);
                if (player.level().getBlockState(blockPos).isAir()) {
                    player.level().setBlock(blockPos, Blocks.WATER.defaultBlockState(), 3);
                }
            }
        }
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null) {
            return (int) (100 - (livingEntity.getHealth() * 2));
        }
        return 0;
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.TSUNAMI_SEAL.get()));
    }
}
