package net.swimmingtuna.lotm.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.swimmingtuna.lotm.item.OtherItems.Doll;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {
    @Inject(
            method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/item/ItemEntity;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void preventDollPickup(Container container, ItemEntity itemEntity, CallbackInfoReturnable<Boolean> cir) {
        if (itemEntity.getItem().getItem() instanceof Doll) {
            // Returning true here means the hopper *acts like it picked it up*, but the item stays.
            // Returning false means it ignores it and keeps trying later.
            cir.setReturnValue(false);
        }
    }
}
