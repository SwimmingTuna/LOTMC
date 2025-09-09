package net.swimmingtuna.lotm.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

import static net.swimmingtuna.lotm.util.BeyonderUtil.shouldBypassSeal;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        CompoundTag tag = entity.getPersistentData();
        int timer = tag.getInt("twilightManifestationTimer");
        int cancelTickTimer = tag.getInt("cancelTick");
        if (!entity.level().isClientSide()) {
            if (timer > 1) {
                tag.putInt("twilightManifestationTimer", timer - 1);
                if (tag.getInt("unableToUseAbility") == 0) {
                    tag.putInt("unableToUseAbility", 1);
                }
            } else if (timer == 1) {
                tag.putInt("unableToUseAbility", 0);
                tag.putInt("twilightManifestationTimer", 0);
                tag.putDouble("twilightManifestationX", 0);
                tag.putDouble("twilightManifestationY", 0);
                tag.putDouble("twilightManifestationZ", 0);
            }
            if (cancelTickTimer >= 1) {
                tag.putInt("cancelTick", cancelTickTimer - 1);
            }
            double twilightX = tag.getDouble("twilightManifestationX");
            double twilightY = tag.getDouble("twilightManifestationY");
            double twilightZ = tag.getDouble("twilightManifestationZ");
            int inTwilight = tag.getInt("inTwilight");
            if (entity.isRemoved() || (entity instanceof LivingEntity living && living.isDeadOrDying())) {
                return;
            }
            if (twilightX != 0 || twilightY != 0 || twilightZ != 0 || inTwilight >= 1) {
                if (entity instanceof LivingEntity living) {
                    living.getDeltaMovement().multiply(0, 0, 0);
                    living.setDeltaMovement(0, 0, 0);
                    living.xo = living.getX();
                    living.yo = living.getY();
                    living.zo = living.getZ();
                    living.xOld = living.getX();
                    living.yOld = living.getY();
                    living.zOld = living.getZ();
                }
                ci.cancel();
            }
            if (cancelTickTimer >= 1) {
                if (entity instanceof LivingEntity living) {
                    living.getDeltaMovement().multiply(0, 0, 0);
                    living.setDeltaMovement(0, 0, 0);
                    living.xo = living.getX();
                    living.yo = living.getY();
                    living.zo = living.getZ();
                    living.xOld = living.getX();
                    living.yOld = living.getY();
                    living.zOld = living.getZ();
                }
                ci.cancel();
            }
        }
    }

    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z", at = @At("HEAD"), cancellable = true)
    private void checkDimensionalSightSealMain(ServerLevel pLevel, double pX, double pY, double pZ, Set<RelativeMovement> pRelativeMovements, float pYRot, float pXRot, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (shouldBypassSeal()) {
            return;
        }
        if (entity instanceof LivingEntity) {
            int sealValue = entity.getPersistentData().getInt("dimensionalSightSeal");
            if (sealValue > 1) {
                if (entity instanceof Player player) {
                    player.displayClientMessage(Component.literal("You can't teleport due to a seal you are in.").withStyle(ChatFormatting.RED), true);
                }
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }

    @Inject(method = "teleportTo(DDD)V", at = @At("HEAD"), cancellable = true)
    private void checkDimensionalSightSealSimple(double pX, double pY, double pZ, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (shouldBypassSeal()) {
            return;
        }
        if (entity instanceof LivingEntity) {
            int sealValue = entity.getPersistentData().getInt("dimensionalSightSeal");
            if (sealValue > 1) {
                if (entity instanceof Player player) {
                    player.displayClientMessage(Component.literal("You can't teleport due to a seal you are in.").withStyle(ChatFormatting.RED), true);
                }
                ci.cancel();
            }
        }
    }

    @Inject(method = "teleportToWithTicket(DDD)V", at = @At("HEAD"), cancellable = true)
    private void checkDimensionalSightSealWithTicket(double pX, double pY, double pZ, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (shouldBypassSeal()) {
            return;
        }
        if (entity instanceof LivingEntity) {
            int sealValue = entity.getPersistentData().getInt("dimensionalSightSeal");
            if (sealValue > 1) {
                if (entity instanceof Player player) {
                    player.displayClientMessage(Component.literal("You can't teleport due to a seal you are in.").withStyle(ChatFormatting.RED), true);
                    ci.cancel();
                }
            }
        }
    }


}