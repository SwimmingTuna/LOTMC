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

public class CalamityIncarnationTsunami extends LeftClickHandlerSkillP {

    public CalamityIncarnationTsunami(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 2,1000,1000);
    }
    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        calamityIncarnationTsunami(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    public void calamityIncarnationTsunami(LivingEntity player) {
        if (!player.level().isClientSide()) {
            int x = player.getPersistentData().getInt("calamityIncarnationTsunami");
            if (x == 0) {
                EventManager.addToRegularLoop(player, EFunctions.CALAMITYINCARNATIONTSUNAMI.get());
                player.getPersistentData().putInt("calamityIncarnationTsunami", (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.CALAMITY_INCARNATION_TSUNAMI.get()));
            } else {
                player.getPersistentData().putInt("calamityIncarnationTsunami", 0);
                if (player instanceof Player pPlayer) {
                    pPlayer.displayClientMessage(Component.literal("Tsunami Incarnation Cancelled").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE), true);
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, summon a ginormous bubble of water around you for 10 seconds"));
        tooltipComponents.add(Component.literal("Left Click for Calamity Incarnation (Tornado)"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("1000").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("50 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static void calamityIncarnationTsunamiTick(LivingEntity livingEntity) {
        //CALAMITY INCARNATION TSUNAMI
        CompoundTag tag = livingEntity.getPersistentData();
        int calamityIncarnationTsunami = tag.getInt("calamityIncarnationTsunami");
        if (calamityIncarnationTsunami < 1) {
            EventManager.removeFromRegularLoop(livingEntity, EFunctions.CALAMITYINCARNATIONTSUNAMI.get());
            return;
        }
        Level level = livingEntity.level();
        tag.putInt("calamityIncarnationTsunami", calamityIncarnationTsunami - 1);
        BlockPos playerPos = livingEntity.blockPosition();
        float damage = BeyonderUtil.getDamage(livingEntity).get(ItemInit.CALAMITY_INCARNATION_TORNADO.get());
        double radius = damage / 8;
        double minRemovalRadius = damage / 7;
        double maxRemovalRadius = damage / 6;

        // Create a sphere of water around the player
        for (int sphereX = (int) -radius; sphereX <= radius; sphereX++) {
            for (int sphereY = (int) -radius; sphereY <= radius; sphereY++) {
                for (int sphereZ = (int) -radius; sphereZ <= radius; sphereZ++) {
                    double distance = Math.sqrt(sphereX * sphereX + sphereY * sphereY + sphereZ * sphereZ);
                    if (distance <= radius) {
                        BlockPos blockPos = playerPos.offset(sphereX, sphereY, sphereZ);
                        if (level.getBlockState(blockPos).isAir() && !level.getBlockState(blockPos).is(Blocks.WATER)) {
                            level.setBlock(blockPos, Blocks.WATER.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
        for (int sphereX = (int) -maxRemovalRadius; sphereX <= maxRemovalRadius; sphereX++) {
            for (int sphereY = (int) -maxRemovalRadius; sphereY <= maxRemovalRadius; sphereY++) {
                for (int sphereZ = (int) -maxRemovalRadius; sphereZ <= maxRemovalRadius; sphereZ++) {
                    double distance = Math.sqrt(sphereX * sphereX + sphereY * sphereY + sphereZ * sphereZ);
                    if (distance <= maxRemovalRadius && distance >= minRemovalRadius) {
                        BlockPos blockPos = playerPos.offset(sphereX, sphereY, sphereZ);
                        if (level.getBlockState(blockPos).getBlock() == Blocks.WATER) {
                            level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
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
        if (target != null && livingEntity.getPersistentData().getInt("calamityIncarnationTornado") == 0) {
            return (int) (80 - livingEntity.getHealth());
        }
        return 0;
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.CALAMITY_INCARNATION_TORNADO.get()));
    }
}
