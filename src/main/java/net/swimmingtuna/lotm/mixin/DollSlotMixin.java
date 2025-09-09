package net.swimmingtuna.lotm.mixin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.swimmingtuna.lotm.item.OtherItems.Doll;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class DollSlotMixin {
    @Inject(
            method = "mayPlace",
            at = @At("HEAD"),
            cancellable = true
    )
    private void disallowSpecificItemOutsideMainInventory(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof Doll) {
            Slot self = (Slot)(Object)this;

            // Only check further if this is player inventory
            if (self.container instanceof Inventory) {
                int slotIndex = self.getSlotIndex();
                // Allow slots 0–35 (hotbar and main inventory)
                if ((slotIndex >= 0 && slotIndex <= 35) || slotIndex == 40) {
                    return;
                } else {
                    cir.setReturnValue(false); // Disallow armor, offhand, crafting
                }
            } else {
                // Not player inventory: always disallow
                cir.setReturnValue(false);
            }
        }
    }
}