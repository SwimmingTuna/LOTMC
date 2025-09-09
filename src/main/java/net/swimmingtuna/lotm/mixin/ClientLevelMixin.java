package net.swimmingtuna.lotm.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.util.ClientData.ClientIgnoreShouldntRenderData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Inject(method = "removeEntity", at = @At("HEAD"), cancellable = true)
    private void onRemoveEntity(int entityId, Entity.RemovalReason reason, CallbackInfo ci) {
        ClientLevel level = (ClientLevel) (Object) this;
        Entity entity = level.getEntity(entityId);

        if (entity != null && ClientIgnoreShouldntRenderData.getIgnoreData(entity.getUUID()) >= 1) {
            ci.cancel();
        }
    }

    @Inject(method = "unload", at = @At("HEAD"), cancellable = true)
    private void onUnloadChunk(LevelChunk chunk, CallbackInfo ci) {
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof DimensionalSightTileEntity) {
                ci.cancel();
                return;
            }
        }
    }
}
