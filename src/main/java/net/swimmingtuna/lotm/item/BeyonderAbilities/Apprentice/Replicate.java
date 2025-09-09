package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.capabilities.replicated_entity.ReplicatedEntityUtils;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.Replicating.ReplicatedEntityMenu;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class Replicate extends SimpleAbilityItem {

    public Replicate(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 2, 50, 300);
    }


    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        openMenu(player);
        return InteractionResult.SUCCESS;
    }


    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity livingEntity, LivingEntity interactionTarget, InteractionHand hand) {
        if (!livingEntity.level().isClientSide && !interactionTarget.level().isClientSide) {
            if (!checkAll(livingEntity)) {
                return InteractionResult.FAIL;
            }
            replicatePlayer(livingEntity, interactionTarget);
        }
        return InteractionResult.SUCCESS;

    }

    public void replicatePlayer(LivingEntity userEntity, LivingEntity targetEntity) {
        if (userEntity instanceof Player player && targetEntity instanceof Player target) {
            ReplicatedEntityUtils.addReplicatedEntities(player, target);
        }
    }

    public static void openMenu(LivingEntity living) {
        if (living instanceof Player player) {
            player.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    int max = ReplicatedEntityUtils.getMaxEntities(player);
                    int amount = ReplicatedEntityUtils.getEntities(player).size();
                    return Component.literal(amount + "/" + max).withStyle(ChatFormatting.BOLD);
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                    return new ReplicatedEntityMenu(containerId, playerInventory, ReplicatedEntityUtils.getEntities(player));
                }
            });
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("WIP, DOES NOTHING RIGHT NOW, UPDATE SOON!!!!").withStyle(ChatFormatting.RED));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("50").withStyle(ChatFormatting.YELLOW)));
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
        if (target != null) {
            return 0;
        }
        return 0;
    }

}