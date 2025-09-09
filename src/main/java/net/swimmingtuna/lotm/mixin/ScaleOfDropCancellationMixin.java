package net.swimmingtuna.lotm.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ScaleUtils.class)
public class ScaleOfDropCancellationMixin
{
    @Inject(method = "setScaleOfDrop", at = @At("HEAD"), cancellable = true, remap = false)
    private static void pehkui$cancelSetScaleOfDrop(Entity entity, Entity source, CallbackInfoReturnable<Float> cir)
    {
        cir.setReturnValue(1.0F);
    }
}
