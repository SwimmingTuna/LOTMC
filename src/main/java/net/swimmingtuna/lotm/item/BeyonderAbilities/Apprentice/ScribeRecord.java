package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.network.NetworkHooks;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderAbilitiesItemMenu;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ScribeRecord extends SimpleAbilityItem {


    public ScribeRecord(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 6, 0, 0);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity living, InteractionHand hand) {
        if (!checkAll(living)) {
            return InteractionResult.FAIL;
        }
        openMenu(level, living);
        addCooldown(living);
        useSpirituality(living);
        return InteractionResult.SUCCESS;
    }

    public void openMenu(Level level, LivingEntity livingEntity) {
        if(!level.isClientSide){
            NetworkHooks.openScreen((ServerPlayer) livingEntity, new MenuProvider() {
                @Override
                public Component getDisplayName() {return Component.translatable("container.lotm.abilities");}

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                    BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                    return new BeyonderAbilitiesItemMenu(containerId, playerInventory, holder.getCurrentClass().getAbilityItemsContainer(holder.getSequence()));
                }
            });
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Use in order to open up a menu with all your saved abilities. Left click in order to attempt to copy an ability after viewing it, the difficulty of copying an ability will increase the higher sequence it is compared to your own."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("0").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICE_ABILITY", ChatFormatting.AQUA);
    }
}