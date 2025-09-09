package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.EndStoneEntity;
import net.swimmingtuna.lotm.entity.NetherrackEntity;
import net.swimmingtuna.lotm.entity.StoneEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.MatterAccelerationBlockC2S;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkill;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class MatterAccelerationBlocks extends LeftClickHandlerSkill {

    public MatterAccelerationBlocks(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 0, 2000, 1200);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        matterAccelerationBlocks(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, accelerates ten blocks to your side. While they're on your side, left click to shoot them out, dealing massive damage to any "));
        tooltipComponents.add(Component.literal("Left Click for Matter Acceleration (Entities)"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("2000").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Minute").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static void matterAccelerationBlocksMobTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();

        if (!living.level().isClientSide() && living instanceof Mob mob) {
            int x = mob.getPersistentData().getInt("matterAccelerationMobShootTimer");
            if (x >= 1) {
                mob.getPersistentData().putInt("matterAccelerationMobShootTimer", x - 1);
                if (x % 10 == 0) {
                    Vec3 lookDirection = mob.getLookAngle().normalize().scale(20);
                    if (mob.level().dimension() == Level.OVERWORLD) {
                        StoneEntity stoneEntity = mob.level().getEntitiesOfClass(StoneEntity.class, mob.getBoundingBox().inflate(10))
                                .stream()
                                .min(Comparator.comparingDouble(zombie -> zombie.distanceTo(mob)))
                                .orElse(null);
                        if (stoneEntity != null) {
                            stoneEntity.setDeltaMovement(lookDirection);
                            stoneEntity.setSent(true);
                            stoneEntity.setDamage((int) (float) BeyonderUtil.getDamage(mob).get(ItemInit.MATTER_ACCELERATION_BLOCKS.get()) * 12);
                            stoneEntity.setShouldntDamage(false);
                            stoneEntity.setTickCount(440);
                        }
                        if (stoneEntity == null) {
                            mob.getPersistentData().putInt("matterAccelerationBlockTimer", 0);
                        }
                    }
                    else if (mob.level().dimension() == Level.NETHER) {
                        NetherrackEntity netherrackEntity = mob.level().getEntitiesOfClass(NetherrackEntity.class, mob.getBoundingBox().inflate(10))
                                .stream()
                                .min(Comparator.comparingDouble(zombie -> zombie.distanceTo(mob)))
                                .orElse(null);

                        if (netherrackEntity != null) {
                            netherrackEntity.setDeltaMovement(lookDirection);
                            netherrackEntity.setSent(true);
                            netherrackEntity.setDamage((int) (float) BeyonderUtil.getDamage(mob).get(ItemInit.MATTER_ACCELERATION_BLOCKS.get()) * 12);
                            netherrackEntity.setShouldDamage(true);
                            netherrackEntity.setTickCount(440);
                        }

                        if (netherrackEntity == null) {
                            mob.getPersistentData().putInt("matterAccelerationBlockTimer", 0);
                        }
                    }
                    else if (mob.level().dimension() == Level.END) {
                        EndStoneEntity endStoneEntity = mob.level().getEntitiesOfClass(EndStoneEntity.class, mob.getBoundingBox().inflate(10))
                                .stream()
                                .min(Comparator.comparingDouble(zombie -> zombie.distanceTo(mob)))
                                .orElse(null);

                        if (endStoneEntity != null) {
                            endStoneEntity.setDeltaMovement(lookDirection);
                            endStoneEntity.setSent(true);
                            endStoneEntity.setDamage((int) (float) BeyonderUtil.getDamage(mob).get(ItemInit.MATTER_ACCELERATION_BLOCKS.get()) * 12);
                            endStoneEntity.setShouldntDamage(false);
                            endStoneEntity.setTickCount(440);
                        }

                        if (endStoneEntity == null) {
                            mob.getPersistentData().putInt("matterAccelerationBlockTimer", 0);
                        }
                    }
                }
            }
        }
    }

    public static void matterAccelerationBlocks(LivingEntity player) {
        if (!player.level().isClientSide()) {
            EventManager.addToRegularLoop(player, EFunctions.NIGHTMARE_TICK.get());
            if (player instanceof Mob mob) {
                mob.getPersistentData().putInt("matterAccelerationMobShootTimer", (int) (float) BeyonderUtil.getDamage(mob).get(ItemInit.MATTER_ACCELERATION_BLOCKS.get()) / 10);
            }
            player.getPersistentData().putInt("matterAccelerationBlockTimer", 480);
            Level level = player.level();
            BlockPos playerPos = player.blockPosition();
            BlockPos surfacePos = findSurfaceBelow(level, playerPos);

            if (surfacePos != null) {
                for (int i = 0; i < BeyonderUtil.getDamage(player).get(ItemInit.MATTER_ACCELERATION_BLOCKS.get()); i++) {
                    BlockPos posToRemove = surfacePos.below(i);
                    level.destroyBlock(posToRemove, false);
                    if (level.dimension() == Level.OVERWORLD) {
                        StoneEntity stoneEntity = new StoneEntity(EntityInit.STONE_ENTITY.get(), player.level());
                        float randomStayX;
                        do {
                            randomStayX = (float) ((Math.random() * 6) - 3);
                        } while (randomStayX > -0.5 && randomStayX < 0.5);
                        float randomStayY = (float) ((Math.random() * 6) - 3);
                        float randomStayZ = (float) ((Math.random() * 6) - 3);
                        int randomXRot = (int) ((Math.random() * 10) - 5);
                        int randomYRot = (int) ((Math.random() * 10) - 5);
                        stoneEntity.setStoneYRot(randomYRot);
                        stoneEntity.setStoneXRot(randomXRot);
                        stoneEntity.setStoneStayAtX(randomStayX);
                        stoneEntity.setStoneStayAtY(randomStayY);
                        stoneEntity.setStoneStayAtZ(randomStayZ);
                        stoneEntity.setOwner(player);
                        stoneEntity.setRemoveAndHurt(true);
                        stoneEntity.setDamage(30);
                        stoneEntity.setSent(false);
                        stoneEntity.teleportTo(surfacePos.getX() + 0.5, surfacePos.getY() + 1, surfacePos.getZ() + 0.5);
                        stoneEntity.setShouldntDamage(true);
                        player.level().addFreshEntity(stoneEntity);
                    }
                    if (level.dimension() == Level.NETHER) {
                        NetherrackEntity netherrackEntity = new NetherrackEntity(EntityInit.NETHERRACK_ENTITY.get(), player.level());
                        float randomStayX;
                        do {
                            randomStayX = (float) ((Math.random() * 6) - 3);
                        } while (randomStayX > -0.5 && randomStayX < 0.5);
                        float randomStayY = (float) ((Math.random() * 6) - 3);
                        float randomStayZ = (float) ((Math.random() * 6) - 3);
                        int randomXRot = (int) ((Math.random() * 10) - 5);
                        int randomYRot = (int) ((Math.random() * 10) - 5);
                        netherrackEntity.setNetherrackStayAtX(randomStayX);
                        netherrackEntity.setNetherrackStayAtY(randomStayY);
                        netherrackEntity.setNetherrackStayAtZ(randomStayZ);
                        netherrackEntity.setOwner(player);
                        netherrackEntity.setRemoveAndHurt(true);
                        netherrackEntity.setSent(false);
                        netherrackEntity.setPos(surfacePos.getX() + 0.5, surfacePos.getY() + 1, surfacePos.getZ() + 0.5);
                        netherrackEntity.setShouldDamage(false);
                        netherrackEntity.setDamage(30);
                        netherrackEntity.setNetherrackXRot(randomXRot);
                        netherrackEntity.setNetherrackYRot(randomYRot);

                        player.level().addFreshEntity(netherrackEntity);
                    }
                    if (level.dimension() == Level.END) {
                        EndStoneEntity endstoneEntity = new EndStoneEntity(EntityInit.ENDSTONE_ENTITY.get(), player.level());
                        float randomStayX;
                        do {
                            randomStayX = (float) ((Math.random() * 6) - 3);
                        } while (randomStayX > -0.5 && randomStayX < 0.5);
                        float randomStayY = (float) ((Math.random() * 6) - 3);
                        float randomStayZ = (float) ((Math.random() * 6) - 3);
                        int randomXRot = (int) ((Math.random() * 10) - 5);
                        int randomYRot = (int) ((Math.random() * 10) - 5);
                        endstoneEntity.setEndstoneStayAtX(randomStayX);
                        endstoneEntity.setEndstoneStayAtY(randomStayY);
                        endstoneEntity.setEndstoneStayAtZ(randomStayZ);
                        endstoneEntity.setOwner(player);
                        endstoneEntity.setRemoveAndHurt(true);
                        endstoneEntity.setSent(false);
                        endstoneEntity.setDamage(30);
                        endstoneEntity.setPos(surfacePos.getX() + 0.5, surfacePos.getY() + 1, surfacePos.getZ() + 0.5);
                        endstoneEntity.setShouldntDamage(true);
                        endstoneEntity.setEndstoneXRot(randomXRot);
                        endstoneEntity.setEndstoneYRot(randomYRot);
                        player.level().addFreshEntity(endstoneEntity);
                    }
                    if (level.dimension() != Level.OVERWORLD && level.dimension() != Level.NETHER && level.dimension() != Level.END) {
                        StoneEntity stoneEntity = new StoneEntity(EntityInit.STONE_ENTITY.get(), player.level());
                        float randomStayX;
                        do {
                            randomStayX = (float) ((Math.random() * 6) - 3);
                        } while (randomStayX > -0.5 && randomStayX < 0.5);
                        float randomStayY = (float) ((Math.random() * 6) - 3);
                        float randomStayZ = (float) ((Math.random() * 6) - 3);
                        int randomXRot = (int) ((Math.random() * 10) - 5);
                        int randomYRot = (int) ((Math.random() * 10) - 5);
                        stoneEntity.setStoneStayAtX(randomStayX);
                        stoneEntity.setStoneStayAtY(randomStayY);
                        stoneEntity.setStoneStayAtZ(randomStayZ);
                        stoneEntity.setOwner(player);
                        stoneEntity.setDamage(30);
                        stoneEntity.setRemoveAndHurt(true);
                        stoneEntity.setSent(false);
                        stoneEntity.setPos(surfacePos.getX() + 0.5, surfacePos.getY() + 1, surfacePos.getZ() + 0.5);
                        stoneEntity.setShouldntDamage(true);
                        stoneEntity.setStoneXRot(randomXRot);
                        stoneEntity.setStoneYRot(randomYRot);
                        player.level().addFreshEntity(stoneEntity);
                    }
                }
            }
        }
    }

    private static BlockPos findSurfaceBelow(Level level, BlockPos startPos) {
        for (int y = startPos.getY(); y >= level.getMinBuildHeight(); y--) {
            BlockPos checkPos = new BlockPos(startPos.getX(), y, startPos.getZ());
            if (isOnSurface(level, checkPos)) {
                return checkPos;
            }
        }
        return null; // No surface found
    }

    private static boolean isOnSurface(Level level, BlockPos pos) {
        return level.canSeeSky(pos.above()) || !level.getBlockState(pos.above()).isSolid();
    }

    public static void leftClick(Player player) {
        int x = player.getPersistentData().getInt("matterAccelerationBlockTimer");
        if (x >= 1) {
            Vec3 lookDirection = player.getLookAngle().normalize().scale(20);
            if (player.level().dimension() == Level.OVERWORLD) {
                StoneEntity stoneEntity = player.level().getEntitiesOfClass(StoneEntity.class, player.getBoundingBox().inflate(10))
                        .stream()
                        .min(Comparator.comparingDouble(zombie -> zombie.distanceTo(player)))
                        .orElse(null);
                if (stoneEntity != null) {
                    stoneEntity.setDeltaMovement(lookDirection);
                    stoneEntity.setSent(true);
                    stoneEntity.setShouldntDamage(false);
                    stoneEntity.setTickCount(440);
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
                    netherrackEntity.setDeltaMovement(lookDirection);
                    netherrackEntity.setSent(true);
                    netherrackEntity.setShouldDamage(true);
                    netherrackEntity.setTickCount(440);
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
                    endStoneEntity.setDeltaMovement(lookDirection);
                    endStoneEntity.setSent(true);
                    endStoneEntity.setShouldntDamage(false);
                    endStoneEntity.setTickCount(440);
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
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null) {
            return 100;
        }
        return 0;
    }

    @Override
    public LeftClickType getleftClickEmpty() {
        return new MatterAccelerationBlockC2S();
    }
}
