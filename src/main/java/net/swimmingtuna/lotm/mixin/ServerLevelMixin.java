package net.swimmingtuna.lotm.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(method = "unload", at = @At("HEAD"), cancellable = true)
    private void onUnloadChunk(LevelChunk chunk, CallbackInfo ci) {
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof DimensionalSightTileEntity) {
                ci.cancel();
                return;
            }
        }
    }

    @Inject(method = "shouldDiscardEntity", at = @At("HEAD"), cancellable = true)
    private void onShouldDiscardEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity.getPersistentData().getInt("ignoreShouldntRender") >= 1) {
            cir.setReturnValue(false);
        }
        if (entity.getPersistentData().getBoolean("shouldFlicker") && entity instanceof PlayerMobEntity) {
            cir.setReturnValue(false);
        }
    }
}
