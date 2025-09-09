package net.swimmingtuna.lotm.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.LOTM;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ItemStack.class})
public class ItemStackMixin {
    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void getMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        ResourceLocation wormOfStarId = new ResourceLocation("lotm", "wormofstar");
        if (ForgeRegistries.ITEMS.getKey(self.getItem()).equals(wormOfStarId)) {
            cir.setReturnValue(LOTM.getMaxStackCount());
        }
    }
    @Inject(method = "grow", at = @At("HEAD"), cancellable = true)
    private void grow(int increment, CallbackInfo ci) {
        ItemStack self = (ItemStack) (Object) this;
        ResourceLocation wormOfStarId = new ResourceLocation("lotm", "wormofstar");
        if (ForgeRegistries.ITEMS.getKey(self.getItem()).equals(wormOfStarId)) {
            int newCount = Math.min(self.getCount() + increment, LOTM.getMaxStackCount());
            self.setCount(newCount);
            ci.cancel();
        }
    }
}