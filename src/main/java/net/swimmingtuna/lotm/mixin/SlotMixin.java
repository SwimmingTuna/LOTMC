package net.swimmingtuna.lotm.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.LOTM;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin {
    @Inject(method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private void getMaxStackSize(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        ResourceLocation wormOfStarId = new ResourceLocation("lotm", "wormofstar");
        if (ForgeRegistries.ITEMS.getKey(stack.getItem()).equals(wormOfStarId)) {
            cir.setReturnValue(LOTM.getMaxStackCount());
        }
    }
}