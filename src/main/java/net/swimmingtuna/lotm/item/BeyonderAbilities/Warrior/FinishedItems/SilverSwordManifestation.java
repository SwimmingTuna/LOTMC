package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class SilverSwordManifestation extends SimpleAbilityItem {


    public SilverSwordManifestation(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 3, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        manifestSword(player);
        return InteractionResult.SUCCESS;
    }

    public static void manifestSword(LivingEntity player) {
        if (!player.level().isClientSide()) {
            ItemStack sword = createSword(ItemInit.SWORDOFSILVER.get().getDefaultInstance());
            if (player instanceof Player pPlayer) {
                int selectedSlot = findClosestEmptySlot(pPlayer);
                if (selectedSlot != -1) {
                    Inventory inventory = pPlayer.getInventory();
                    inventory.setItem(selectedSlot, sword);
                }
            } else {
                player.getPersistentData().putInt("dawnWeaponryTick", 3);
                player.getPersistentData().putBoolean("dawnWeaponrySilverSword", true);
                player.setItemSlot(EquipmentSlot.MAINHAND, sword);
            }
            player.setHealth(player.getHealth() - 10.0f);
        }
    }

    public static ItemStack createSword(ItemStack armor) {
        armor.enchant(Enchantments.SHARPNESS, 5);
        armor.enchant(Enchantments.KNOCKBACK, 2);
        armor.enchant(Enchantments.UNBREAKING, 3);
        armor.enchant(Enchantments.FIRE_ASPECT, 2);
        return armor;
    }

    public static int findClosestEmptySlot(Player player) {
        Inventory inventory = player.getInventory();
        int selectedSlot = player.getInventory().selected;
        if (inventory.getItem(selectedSlot).isEmpty()) {
            return selectedSlot;
        }
        for (int distance = 1; distance < 9; distance++) {
            int rightSlot = (selectedSlot + distance) % 9;
            if (inventory.getItem(rightSlot).isEmpty()) {
                return rightSlot;
            }
            int leftSlot = (selectedSlot - distance + 9) % 9;
            if (inventory.getItem(leftSlot).isEmpty()) {
                return leftSlot;
            }
        }
        for (int i = 9; i < 36; i++) {
            if (inventory.getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, conjure a silver sword that can be used by anyone. This sword does immense damage, and can also be thrown like a spear"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("None. 10 Health.").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("0").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("WARRIOR_ABILITY", ChatFormatting.YELLOW);
    }
    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        return 0;
    }
}

