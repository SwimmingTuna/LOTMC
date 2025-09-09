package net.swimmingtuna.lotm.util.Replicating;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.swimmingtuna.lotm.capabilities.replicated_entity.ReplicatedEntityUtils;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;

import java.util.List;

public class ReplicatedEntityMenu extends AbstractContainerMenu {
    private final Container container;

    public ReplicatedEntityMenu(int containerId, Inventory playerInventory, List<ReplicatedEntityDataHolder> replicatedEntities) {
        super(MenuType.GENERIC_9x5, containerId);
        this.container = createContainer(replicatedEntities);
        for (int i = 0; i < this.container.getContainerSize(); i++) {
            this.addSlot(new Slot(this.container, i, 8 + (i % 9) * 18, 18 + (i / 9) * 18) {
                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 86 + i * 18));
            }
        }
        for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 144));
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < this.container.getContainerSize()) {
            Slot slot = this.slots.get(slotId);
            if (slot != null && slot.hasItem()) {
                ItemStack clickedItem = slot.getItem();
                if (clickedItem.hasTag() && clickedItem.getTag().contains("ReplicatedData")) {
                    CompoundTag dataTag = clickedItem.getTag().getCompound("ReplicatedData");
                    ReplicatedEntityDataHolder holder = ReplicatedEntityDataHolder.deserialize(dataTag);

                    PlayerMobEntity replicatedEntity = ReplicatedEntityUtils.getEntity(player, holder);
                    replicatedEntity.setPos(player.getX(), player.getY(), player.getZ());
                    player.level().addFreshEntity(replicatedEntity);
                }
            }
        }
    }

    private SimpleContainer createContainer(List<ReplicatedEntityDataHolder> entities) {
        SimpleContainer container = new SimpleContainer(45); // 9x5 grid
        int index = 0;
        for (ReplicatedEntityDataHolder entity : entities) {
            if (index >= container.getContainerSize()) break;

            ItemStack head = new ItemStack(Items.PLAYER_HEAD);

            CompoundTag skullOwnerTag = new CompoundTag();
            NbtUtils.writeGameProfile(skullOwnerTag, entity.getGameProfile());
            head.getOrCreateTag().put("SkullOwner", skullOwnerTag);

            CompoundTag replicatedData = entity.serialize();
            head.getOrCreateTag().put("ReplicatedData", replicatedData);

            container.setItem(index++, head);
        }
        return container;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}