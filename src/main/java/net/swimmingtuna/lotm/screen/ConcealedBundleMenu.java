package net.swimmingtuna.lotm.screen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.init.MenuInit;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ConcealedBundleMenu extends AbstractContainerMenu {
    private final int rows;
    private final int slotCount;
    private final ItemStackHandler bagInventory;
    private final ItemStack bagStack;
    private final int originalSlot;

    private static final Set<Item> BLOCKED_ITEMS = new HashSet<>();

    static {
        // Add items you want to block here
        BLOCKED_ITEMS.add(Items.SHULKER_BOX);
        BLOCKED_ITEMS.add(Items.WHITE_SHULKER_BOX);
        BLOCKED_ITEMS.add(Items.ORANGE_SHULKER_BOX);
        BLOCKED_ITEMS.add(Items.MAGENTA_SHULKER_BOX);
        BLOCKED_ITEMS.add(Items.LIGHT_BLUE_SHULKER_BOX);
        BLOCKED_ITEMS.add(Items.YELLOW_SHULKER_BOX);
        BLOCKED_ITEMS.add(Items.LIME_SHULKER_BOX);
        BLOCKED_ITEMS.add(Items.PINK_SHULKER_BOX);
        BLOCKED_ITEMS.add(Items.GRAY_SHULKER_BOX);
        BLOCKED_ITEMS.add(Items.LIGHT_GRAY_SHULKER_BOX);
        BLOCKED_ITEMS.add(Items.CYAN_SHULKER_BOX);
        BLOCKED_ITEMS.add(Items.PURPLE_SHULKER_BOX);
        BLOCKED_ITEMS.add(Items.BLUE_SHULKER_BOX);
        BLOCKED_ITEMS.add(Items.BROWN_SHULKER_BOX);
        BLOCKED_ITEMS.add(Items.GREEN_SHULKER_BOX);
        BLOCKED_ITEMS.add(Items.RED_SHULKER_BOX);
        BLOCKED_ITEMS.add(Items.BLACK_SHULKER_BOX);
        BLOCKED_ITEMS.add(Items.BUNDLE);
        BLOCKED_ITEMS.add(ItemInit.CONCEALED_BUNDLE.get());
    }

    public static class FilteredSlotItemHandler extends SlotItemHandler {
        private final ConcealedBundleMenu menu;

        public FilteredSlotItemHandler(ConcealedBundleMenu menu, ItemStackHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !BLOCKED_ITEMS.contains(stack.getItem()) && super.mayPlace(stack);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            // Save the inventory immediately
            saveInventory(menu.bagStack, menu.bagInventory);
        }
    }

    public ConcealedBundleMenu(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readItem(), extraData.readInt(), extraData.readInt());
    }

    public ConcealedBundleMenu(int windowId, Inventory playerInventory, ItemStack bagStack, int slot, int rows) {
        super(MenuInit.CONCEALED_BUNDLE_MENU.get(), windowId);
        this.bagStack = bagStack;
        this.rows = rows;
        this.slotCount = rows * 9;
        this.bagInventory = getInventory(bagStack, slotCount);
        this.originalSlot = slot;

        CompoundTag tag = bagStack.getOrCreateTag();
        if (!tag.contains("BagUUID")) {
            tag.putUUID("BagUUID", UUID.randomUUID());
        }

        // Use filtered slots for the bag inventory
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new FilteredSlotItemHandler(this, bagInventory, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // Player inventory
        int inventoryY = 18 + rows * 18 + 14; // 18: bag slot top offset, rows*18: bag slot height, 14: spacing
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, inventoryY + row * 18));
            }
        }

        // Hotbar
        int hotbarY = inventoryY + 58; // 3 rows * 18 + 4 spacing
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, hotbarY));
        }
    }

    public static ItemStackHandler getInventory(ItemStack stack, int slotCount) {
        CompoundTag tag = stack.getOrCreateTag();
        ItemStackHandler handler = new ItemStackHandler(slotCount);

        if (tag.contains("Inventory")) {
            handler.deserializeNBT(tag.getCompound("Inventory"));
        }

        return handler;
    }

    public static void saveInventory(ItemStack stack, ItemStackHandler handler) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put("Inventory", handler.serializeNBT());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack originalStack = slot.getItem();
        ItemStack copyStack = originalStack.copy();

        int bagStart = 0;
        int bagEnd = slotCount;
        int playerInvStart = slotCount;
        int playerInvEnd = playerInvStart + 27;
        int hotbarStart = playerInvEnd;
        int hotbarEnd = hotbarStart + 9;

        if (index < bagEnd) {
            // Moving from bag to player inventory
            if (!this.moveItemStackTo(originalStack, playerInvStart, hotbarEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Moving from player inventory to bag - check if item is allowed
            if (BLOCKED_ITEMS.contains(originalStack.getItem())) {
                return ItemStack.EMPTY; // Block the transfer
            }
            if (!this.moveItemStackTo(originalStack, bagStart, bagEnd, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (originalStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return copyStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            saveInventory(bagStack, bagInventory);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (!player.isAlive()) return false;

        ItemStack currentItem = player.getInventory().getItem(originalSlot);

        CompoundTag originalTag = bagStack.getTag();
        CompoundTag currentTag = currentItem.getTag();

        if (originalTag == null || currentTag == null) return false;
        if (!originalTag.contains("BagUUID") || !currentTag.contains("BagUUID")) return false;

        return originalTag.getUUID("BagUUID").equals(currentTag.getUUID("BagUUID"));
    }

    public int getRows() {
        return this.rows;
    }
}
