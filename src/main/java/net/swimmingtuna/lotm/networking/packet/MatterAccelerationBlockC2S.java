package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.entity.EndStoneEntity;
import net.swimmingtuna.lotm.entity.NetherrackEntity;
import net.swimmingtuna.lotm.entity.StoneEntity;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.MatterAccelerationBlocks;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;

import java.util.Comparator;
import java.util.function.Supplier;

public class MatterAccelerationBlockC2S implements LeftClickType {
    public MatterAccelerationBlockC2S() {

    }

    public MatterAccelerationBlockC2S(FriendlyByteBuf buf) {

    }

    public void toByte(FriendlyByteBuf buf) {

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        context.enqueueWork(() -> {
            ServerLevel serverLevel = (ServerLevel) player.level();
            int x = player.getPersistentData().getInt("matterAccelerationBlockTimer");
            if (x >= 1) {
                Vec3 lookDirection = player.getLookAngle().normalize();
                Vec3 playerPosition = player.position();
                Vec3 targetPosition = playerPosition.add(lookDirection.scale(35));
                Vec3 eyePosition = player.getEyePosition();
                Vec3 lookVector = player.getLookAngle();
                Vec3 reachVector = eyePosition.add(lookVector.scale(70));
                AABB searchBox = player.getBoundingBox().inflate(70);
                EntityHitResult targetEntity = ProjectileUtil.getEntityHitResult(player.level(), player, eyePosition, reachVector, searchBox, entity -> !entity.isSpectator() && entity.isPickable(), 0.1f);
                if (targetEntity != null) {
                    targetPosition = targetEntity.getEntity().position();
                }

                if (player.level().dimension() == Level.OVERWORLD) {
                    StoneEntity stoneEntity = player.level().getEntitiesOfClass(StoneEntity.class, player.getBoundingBox().inflate(10))
                            .stream()
                            .min(Comparator.comparingDouble(zombie -> zombie.distanceTo(player)))
                            .orElse(null);
                    if (stoneEntity != null) {
                        serverLevel.playSound(null, player.getOnPos(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 5, 5 );
                        Vec3 stoneToTarget = targetPosition.subtract(stoneEntity.position()).normalize();
                        stoneEntity.setDeltaMovement(stoneToTarget.scale(20.0));
                        stoneEntity.setBB(15);
                        stoneEntity.setSent(true);
                        stoneEntity.setDamage((int) (float) BeyonderUtil.getDamage(player).get(ItemInit.MATTER_ACCELERATION_BLOCKS.get()) * 12);
                        stoneEntity.setOwner(player);
                        stoneEntity.setNoGravity(true);
                        stoneEntity.setShouldntDamage(false);
                        stoneEntity.setTickCount(120);
                    }
                    if (stoneEntity == null) {
                        player.getPersistentData().putInt("matterAccelerationBlockTimer", 0);
                    }
                }
                if (player.level().dimension() == Level.NETHER) {
                    NetherrackEntity netherrackEntity = player.level().getEntitiesOfClass(NetherrackEntity.class, player.getBoundingBox().inflate(10))
                            .stream()
                            .min(Comparator.comparingDouble(zombie -> zombie.distanceTo(player)))
                            .orElse(null);
                    if (netherrackEntity != null) {
                        serverLevel.playSound(null, player.getOnPos(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 5, 5 );
                        Vec3 netherrackToTarget = targetPosition.subtract(netherrackEntity.position()).normalize();
                        netherrackEntity.setDeltaMovement(netherrackToTarget.scale(20.0));
                        netherrackEntity.setSent(true);
                        netherrackEntity.setBB(15);
                        netherrackEntity.setShouldDamage(true);
                        netherrackEntity.setTickCount(120);
                        netherrackEntity.setNoGravity(true);
                        netherrackEntity.setDamage((int) (float) BeyonderUtil.getDamage(player).get(ItemInit.MATTER_ACCELERATION_BLOCKS.get()) * 12);
                        netherrackEntity.setOwner(player);
                    }
                    if (netherrackEntity == null) {
                        player.getPersistentData().putInt("matterAccelerationBlockTimer", 0);
                    }
                }
                if (player.level().dimension() == Level.END) {
                    EndStoneEntity endStoneEntity = player.level().getEntitiesOfClass(EndStoneEntity.class, player.getBoundingBox().inflate(10))
                            .stream()
                            .min(Comparator.comparingDouble(zombie -> zombie.distanceTo(player)))
                            .orElse(null);
                    if (endStoneEntity != null) {
                        serverLevel.playSound(null, player.getOnPos(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 5, 5 );
                        Vec3 endStoneToTarget = targetPosition.subtract(endStoneEntity.position()).normalize();
                        endStoneEntity.setDeltaMovement(endStoneToTarget.scale(20.0)); // Adjust speed as needed
                        endStoneEntity.setSent(true);
                        endStoneEntity.setBB(15);
                        endStoneEntity.setShouldntDamage(false);
                        endStoneEntity.setTickCount(120);
                        endStoneEntity.setDamage((int) (float) BeyonderUtil.getDamage(player).get(ItemInit.MATTER_ACCELERATION_BLOCKS.get()) * 12);
                        endStoneEntity.setNoGravity(true);
                        endStoneEntity.setOwner(player);
                    }
                    if (endStoneEntity == null) {
                        player.getPersistentData().putInt("matterAccelerationBlockTimer", 0);
                    }
                }
            } else {
                int activeSlot = player.getInventory().selected;
                ItemStack heldItem = player.getMainHandItem();
                if (!heldItem.isEmpty() && heldItem.getItem() instanceof MatterAccelerationBlocks) {
                    heldItem.shrink(1);
                    player.getInventory().setItem(activeSlot, new ItemStack(ItemInit.MATTER_ACCELERATION_ENTITIES.get()));
                }
            }
        });
        return true;
    }
}