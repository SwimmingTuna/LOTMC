package net.swimmingtuna.lotm.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.RelativeMovement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;


@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "teleportTo(DDD)V", at = @At("HEAD"), cancellable = true)
    private void checkDimensionalSightSealSimpleServer(double pX, double pY, double pZ, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        int sealValue = player.getPersistentData().getInt("dimensionalSightSeal");
        if (sealValue > 1) {
            player.displayClientMessage(Component.literal("You can't teleport due to a seal you are in.").withStyle(ChatFormatting.RED), true);
            ci.cancel();
        }
    }

    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z", at = @At("HEAD"), cancellable = true)
    private void checkDimensionalSightSealMainServer(ServerLevel pLevel, double pX, double pY, double pZ, Set<RelativeMovement> pRelativeMovements, float pYRot, float pXRot, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        int sealValue = player.getPersistentData().getInt("dimensionalSightSeal");
        if (sealValue > 1) {
            player.displayClientMessage(Component.literal("You can't teleport due to a seal you are in.").withStyle(ChatFormatting.RED), true);
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V", at = @At("HEAD"), cancellable = true)
    private void checkDimensionalSightSealDimensionalServer(ServerLevel pNewLevel, double pX, double pY, double pZ, float pYaw, float pPitch, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        int sealValue = player.getPersistentData().getInt("dimensionalSightSeal");
        if (sealValue > 1) {
            player.displayClientMessage(Component.literal("You can't teleport due to a seal you are in.").withStyle(ChatFormatting.RED), true);
            ci.cancel();
        }
    }
}
