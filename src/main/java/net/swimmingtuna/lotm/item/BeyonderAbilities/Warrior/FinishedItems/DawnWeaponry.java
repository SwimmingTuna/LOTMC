package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.entity.HurricaneOfLightEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.OtherItems.SpearOfDawn;
import net.swimmingtuna.lotm.networking.packet.DawnWeaponryLeftClickC2S;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkill;
import net.swimmingtuna.lotm.util.ModArmorMaterials;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class DawnWeaponry extends LeftClickHandlerSkill {


    public DawnWeaponry(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        if (player instanceof Player) {
            addCooldown(player);
        }
        useSpirituality(player);
        dawnWeaponry(player);
        return InteractionResult.SUCCESS;
    }

    public void dawnWeaponry(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            CompoundTag tag = livingEntity.getPersistentData();
            ItemStack sword = createSword(ItemInit.SWORDOFDAWN.get().getDefaultInstance());
            ItemStack axe = createPickaxe(ItemInit.PICKAXEOFDAWN.get().getDefaultInstance());
            ItemStack spear = createSpear(ItemInit.SPEAROFDAWN.get().getDefaultInstance());
            if (livingEntity instanceof Player pPlayer) {
                int selectedSlot = findClosestEmptySlot(pPlayer);
                int x = tag.getInt("dawnWeaponry");
                if (selectedSlot != -1) {
                    Inventory inventory = pPlayer.getInventory();
                    switch (x) {
                        case 1 -> {
                            inventory.setItem(selectedSlot, sword);
                        }
                        case 2 -> {
                            inventory.setItem(selectedSlot, axe);
                        }
                        case 3 -> {
                            inventory.setItem(selectedSlot, spear);
                        }
                    }
                }
            } else if (livingEntity instanceof Mob mob) {
                float random = BeyonderUtil.getPositiveRandomInRange(2);
                if (random >= 2) {
                    SpearOfDawn.throwSpearMob(mob.level(), mob);
                } else {
                    HurricaneOfLightEntity.summonHurricaneOfLightDawnMob(mob);
                }
                addCooldown(livingEntity,this, 400);
            }
        }
    }

    private static ItemStack createSword(ItemStack armor) {
        armor.enchant(Enchantments.SHARPNESS, 5);
        armor.enchant(Enchantments.KNOCKBACK, 2);
        armor.enchant(Enchantments.UNBREAKING, 3);
        armor.enchant(Enchantments.FIRE_ASPECT, 2);
        return armor;
    }

    private static ItemStack createSpear(ItemStack armor) {
        armor.enchant(Enchantments.SHARPNESS, 3);
        armor.enchant(Enchantments.UNBREAKING, 3);
        armor.enchant(Enchantments.LOYALTY, 1);
        return armor;
    }

    private static ItemStack createPickaxe(ItemStack armor) {
        armor.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        armor.enchant(Enchantments.BLOCK_FORTUNE, 2);
        armor.enchant(Enchantments.UNBREAKING, 3);
        return armor;
    }

    private static int findClosestEmptySlot(Player player) {
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

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof Player player) {
            if (player.tickCount % 2 == 0 && !level.isClientSide()) {
                if (player.getMainHandItem().getItem() instanceof DawnWeaponry) {
                    player.displayClientMessage(Component.literal("Dawn Weaponry Choice is: " + dawnWeaponryString(player)).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.YELLOW), true);
                }
            }
        }
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    public static String dawnWeaponryString(Player pPlayer) {
        CompoundTag tag = pPlayer.getPersistentData();
        int dawnWeaponry = tag.getInt("dawnWeaponry");
        if (dawnWeaponry == 1) {
            return "Sword of Dawn";
        } else if (dawnWeaponry == 2) {
            return "Pickaxe of Dawn";
        } else if (dawnWeaponry == 3) {
            return "Spear of Dawn";
        }
        return "None";
    }

    public static boolean hasFullDawnArmor(LivingEntity entity) {
        if (entity == null) return false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack itemStack = entity.getItemBySlot(slot);
                if (!(itemStack.getItem() instanceof ArmorItem armor) || armor.getMaterial() != ModArmorMaterials.DAWN) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void removeDawnArmor(LivingEntity livingEntity) {
        if (BeyonderUtil.currentPathwayAndSequenceMatches(livingEntity, BeyonderClassInit.WARRIOR.get(), 6)) return;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack itemStack = livingEntity.getItemBySlot(slot);
                if (itemStack.getItem() instanceof ArmorItem armor && armor.getMaterial() == ModArmorMaterials.DAWN) {
                    livingEntity.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
        }
    }

    public static boolean hasFullSilverArmor(LivingEntity entity) {
        if (entity == null) return false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack itemStack = entity.getItemBySlot(slot);
                if (!(itemStack.getItem() instanceof ArmorItem armor) || armor.getMaterial() != ModArmorMaterials.DAWN) {
                    return false; // If any slot is not diamond armor, return false
                }
            }
        }
        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, create a weapon or tool of dawn depending on your selection."));
        tooltipComponents.add(Component.literal("Left click to cycle."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("Dependent on choice").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Seconds").withStyle(ChatFormatting.YELLOW)));
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
        if (target != null) {
            return 60;
        }
        return 0;
    }

    @Override
    public DawnWeaponryLeftClickC2S getleftClickEmpty() {
        return new DawnWeaponryLeftClickC2S();
    }
}

