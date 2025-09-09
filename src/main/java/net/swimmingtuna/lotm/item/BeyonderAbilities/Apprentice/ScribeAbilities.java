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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.capabilities.scribed_abilities.ScribedUtils;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.ScribeCopyAbilityC2S;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkill;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import net.swimmingtuna.lotm.util.ScribeRecording.ScribeMenu;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ScribeAbilities extends LeftClickHandlerSkill {


    public ScribeAbilities(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 6, 0, 0);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity livingEntity, InteractionHand hand) {
        if (!checkAll(livingEntity)) {
            return InteractionResult.FAIL;
        }
        openOrCheck(livingEntity);
        return InteractionResult.SUCCESS;
    }

    public static void openOrCheck(LivingEntity livingEntity) {
        if (livingEntity.getOffhandItem().getItem() instanceof SimpleAbilityItem) {
            if (!livingEntity.isShiftKeyDown()) {
                checkRemainingUses(livingEntity);
            } else {
                deleteAbility(livingEntity);
            }
        } else {
            openMenu(livingEntity);
        }
    }

    public static void checkRemainingUses(LivingEntity livingEntity) {
        Item offHand = livingEntity.getOffhandItem().getItem();
        if (offHand instanceof SimpleAbilityItem ability) {
            if(livingEntity instanceof Player player){
                if(ScribedUtils.hasAbility(player, ability)){
                    player.displayClientMessage(Component.literal("Scribed copies: ").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN).append(Component.literal(String.valueOf(ScribedUtils.getRemainingUses(player, ability))).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.BOLD)), true);
                }else{
                    player.displayClientMessage(Component.literal("Haven`t scribed this ability yet.").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.WHITE), true);
                }
            }
        }
    }

    public static void deleteAbility(LivingEntity livingEntity) {
        Item offHand = livingEntity.getOffhandItem().getItem();
        if (offHand instanceof SimpleAbilityItem ability) {
            if(ScribedUtils.hasAbility(livingEntity, ability)){
                ScribedUtils.useScribedAbility(livingEntity, ability);
                if (livingEntity instanceof Player player) {
                    player.displayClientMessage(Component.literal("1 copy deleted.").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN), true);
                }
            }else if(livingEntity instanceof Player player){
                player.displayClientMessage(Component.literal("All copies have been deleted, or haven`t scribed this ability yet.").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.WHITE), true);
            }
        }
    }

    public static void openMenu(LivingEntity living) { //marked
        if (living instanceof Player player) {
            player.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    int max = player.getPersistentData().getInt("maxScribedAbilities");
                    int amount = ScribedUtils.getAbilitiesCount(player);
                    return Component.literal(amount + "/" + max).withStyle(ChatFormatting.BOLD);
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                    return new ScribeMenu(containerId, playerInventory, ScribedUtils.getScribedAbilities(player));
                }
            });
        }
    }

    public static void acceptCopiedAbilities(Player player) {
        BeyonderUtil.confirmCopyAbility(player);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, opens a menu that you can use to get your scribed abilities. In order to scribe an ability, look at someone using an ability and you'll have a chance, depending on how strong it is relative to you, to gain the ability to use it once, or one more time depending on if you already had it recorded or not."));
        tooltipComponents.add(Component.literal("Left click while not shifting accept the copy or shift left click to deny it"));
        tooltipComponents.add(Component.literal("Use while sneaking with an ability in your off-hand to get how many copies of that ability you have scribed so far."));
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

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (BeyonderUtil.pendingAbilityCopies.containsKey(livingEntity.getUUID())) {
            livingEntity.getPersistentData().putBoolean("acceptCopiedAbility", true);
        }
        return 0;
    }
    @Override
    public LeftClickType getleftClickEmpty() {
        return new ScribeCopyAbilityC2S();
    }
}