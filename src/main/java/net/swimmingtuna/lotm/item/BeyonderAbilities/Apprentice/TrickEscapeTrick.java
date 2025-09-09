package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.UpdateItemInHandC2S;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TrickEscapeTrick extends LeftClickHandlerSkillP {
    private static final int MIN_TELEPORT_Y = -60;

    public TrickEscapeTrick(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 8, 150, 200);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity livingEntity, InteractionHand hand){
        if(!checkAll(livingEntity)){
            return InteractionResult.FAIL;
        }
        escape(livingEntity);
        if( livingEntity instanceof Player player) {
            if (!player.isShiftKeyDown()) {
                addCooldown(player);
                useSpirituality(player);
            }
        } else {
            addCooldown(livingEntity);
            useSpirituality(livingEntity);
        }
        return InteractionResult.SUCCESS;
    }

    public static void escape(LivingEntity entity){
        if(!entity.level().isClientSide()){
            int maxEscapes = (int) (float) BeyonderUtil.getDamage(entity).get(ItemInit.TRICKESCAPETRICK.get());
            CompoundTag tag = entity.getPersistentData();
            if(entity instanceof Player player) {
                if(player.isShiftKeyDown()) {
                    player.displayClientMessage(Component.literal("Escape Tricks prepared: ").withStyle(BeyonderUtil.getStyle(player)).append(Component.literal("" + tag.getInt("escapeTrickCount")).withStyle(ChatFormatting.WHITE)), true);
                    return;
                }
            } if (tag.getInt("escapeTrickCount") < maxEscapes) {
                tag.putInt("escapeTrickCount", tag.getInt("escapeTrickCount") + 1);
                if (entity instanceof Player player ) {
                    player.displayClientMessage(Component.literal("Trick prepared!").withStyle(BeyonderUtil.getStyle(player)), true);
                }
            } else {
                if(entity instanceof Player player) {
                    player.displayClientMessage(Component.literal("Can't prepare anymore Escape Tricks.").withStyle(BeyonderUtil.getStyle(player)), true);
                }
            }
        }
    }

    public static void escapeTrickAttackEvent(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide() && entity.getPersistentData().getInt("escapeTrickCount") >= 1 && BeyonderUtil.getSequence(entity) <= 4) {
            int range = (int) (float) BeyonderUtil.getDamage(entity).get(ItemInit.TRICKESCAPETRICK.get()) * 10;
            boolean teleported = tryTeleportToSafeLocation(entity, entity.level(), range);
            if (teleported) {
                entity.getPersistentData().putInt("escapeTrickCount", entity.getPersistentData().getInt("escapeTrickCount") - 1);
                event.setCanceled(true);
                if (entity instanceof Player player) {
                    player.displayClientMessage(Component.literal("Remaining Escape Tricks: ").withStyle(BeyonderUtil.getStyle(player)).append(Component.literal("" + player.getPersistentData().getInt("escapeTrickCount")).withStyle(ChatFormatting.WHITE)), true);
                }
            } else {
                if (entity instanceof Player player) {
                    player.displayClientMessage(Component.literal("No safe spaces found").withStyle(BeyonderUtil.getStyle(player)), true);
                }
            }
        }
    }

    private static boolean tryTeleportToSafeLocation(LivingEntity entity, Level level, int range) {
        Vec3 currentPos = entity.position();
        int currentY = (int) currentPos.y;
        int worldSurface = level.getHeight();
        int distanceFromSurface = worldSurface - currentY;

        // If within 10 blocks of surface, try to teleport to surface first
        if (distanceFromSurface <= 10 && distanceFromSurface >= 0) {
            if (tryTeleportToSurface(entity, level, range)) {
                return true;
            }
        }

        // If more than 10 blocks above surface or underground (more than 10 blocks below surface),
        // use random air teleportation
        if (currentY > worldSurface + 10 || currentY < worldSurface - 10) {
            return tryRandomAirTeleportation(entity, level, range);
        }

        // Fallback to original directional teleportation for edge cases
        return tryDirectionalTeleportation(entity, level, range);
    }

    private static boolean tryTeleportToSurface(LivingEntity entity, Level level, int range) {
        Vec3 currentPos = entity.position();
        Random random = new Random();

        // Try multiple surface locations
        for (int attempts = 0; attempts < 20; attempts++) {
            int xOffset = random.nextInt(range * 2) - range;
            int zOffset = random.nextInt(range * 2) - range;

            BlockPos surfacePos = new BlockPos((int) currentPos.x + xOffset, level.getHeight(), (int) currentPos.z + zOffset);

            // Find the actual surface (highest solid block)
            for (int y = level.getHeight(); y >= Math.max(level.getMinBuildHeight(), MIN_TELEPORT_Y); y--) {
                BlockPos checkPos = new BlockPos(surfacePos.getX(), y, surfacePos.getZ());
                if (!level.getBlockState(checkPos).isAir() && level.getBlockState(checkPos.above()).isAir()) {
                    BlockPos teleportPos = checkPos.above();
                    if (teleportPos.getY() >= MIN_TELEPORT_Y && isSafeLocation(teleportPos, level, entity)) {
                        performTeleport(entity, new Vec3(teleportPos.getX() + 0.5, teleportPos.getY(), teleportPos.getZ() + 0.5));
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }

    private static boolean tryRandomAirTeleportation(LivingEntity entity, Level level, int range) {
        Vec3 currentPos = entity.position();
        Random random = new Random();
        int maxAttempts = range * 3;

        for (int i = 0; i < maxAttempts; i++) {
            int xOffset = random.nextInt(range * 2) - range;
            int yOffset = random.nextInt(range) - range/2; // Allow both up and down movement
            int zOffset = random.nextInt(range * 2) - range;

            double distanceSq = xOffset*xOffset + yOffset*yOffset + zOffset*zOffset;
            if (distanceSq <= range*range) {
                Vec3 targetVec = currentPos.add(xOffset, yOffset, zOffset);

                // Ensure the target Y position is not below the minimum teleport level
                if (targetVec.y < MIN_TELEPORT_Y) {
                    targetVec = new Vec3(targetVec.x, MIN_TELEPORT_Y, targetVec.z);
                }

                BlockPos targetPos = new BlockPos((int) Math.floor(targetVec.x), (int) Math.floor(targetVec.y), (int) Math.floor(targetVec.z));

                // For air teleportation, we don't need a solid ground, just clear space
                if (isAirSafeLocation(targetPos, level, entity)) {
                    performTeleport(entity, targetVec);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean tryDirectionalTeleportation(LivingEntity entity, Level level, int range) {
        List<Vec3> directions = new ArrayList<>();
        directions.add(new Vec3(1, 0, 0));   // East
        directions.add(new Vec3(-1, 0, 0));  // West
        directions.add(new Vec3(0, 0, 1));   // South
        directions.add(new Vec3(0, 0, -1));  // North
        directions.add(new Vec3(1, 0, 1));   // Southeast
        directions.add(new Vec3(-1, 0, 1));  // Southwest
        directions.add(new Vec3(1, 0, -1));  // Northeast
        directions.add(new Vec3(-1, 0, -1)); // Northwest
        directions.add(new Vec3(0, 1, 0));   // Up
        directions.add(new Vec3(0, -1, 0));  // Down
        directions.add(new Vec3(1, 1, 0));   // Up + East
        directions.add(new Vec3(-1, 1, 0));  // Up + West
        directions.add(new Vec3(0, 1, 1));   // Up + South
        directions.add(new Vec3(0, 1, -1));  // Up + North
        directions.add(new Vec3(1, -1, 0));  // Down + East
        directions.add(new Vec3(-1, -1, 0)); // Down + West
        directions.add(new Vec3(0, -1, 1));  // Down + South
        directions.add(new Vec3(0, -1, -1)); // Down + North
        Collections.shuffle(directions);

        Vec3 currentPos = entity.position();
        for (Vec3 dir : directions) {
            for (int distance = range; distance > range / 2; distance -= 5) {
                Vec3 normalizedDir = dir.normalize().scale(distance);
                Vec3 targetVec = currentPos.add(normalizedDir);

                // Ensure the target Y position is not below the minimum teleport level
                if (targetVec.y < MIN_TELEPORT_Y) {
                    targetVec = new Vec3(targetVec.x, MIN_TELEPORT_Y, targetVec.z);
                }

                BlockPos targetPos = new BlockPos((int) Math.floor(targetVec.x), (int) Math.floor(targetVec.y), (int) Math.floor(targetVec.z));

                if (isSafeLocation(targetPos, level, entity)) {
                    performTeleport(entity, targetVec);
                    return true;
                }
            }
        }
        return false;
    }

    private static void performTeleport(LivingEntity entity, Vec3 targetVec) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            for (int p = 0; p < 10 * BeyonderUtil.getScale(entity); p++) {
                float randomInt = BeyonderUtil.getRandomInRange(BeyonderUtil.getScale(entity));
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, entity.getX() + randomInt, entity.getY() + randomInt, entity.getZ() + randomInt, 0, 0, 0, 0, 0f);
            }
            float randomInt = BeyonderUtil.getRandomInRange(BeyonderUtil.getScale(entity));
            serverLevel.sendParticles(ParticleTypes.FLASH, entity.getX() + randomInt, entity.getY() + randomInt, entity.getZ() + randomInt, 0, 0, 0, 0, 0f);
        }
        entity.teleportTo(targetVec.x, targetVec.y, targetVec.z);
    }

    private static boolean isSafeLocation(BlockPos pos, Level level, LivingEntity entity) {
        if (pos.getY() < MIN_TELEPORT_Y) {
            return false;
        }

        float scale = BeyonderUtil.getScale(entity);
        int requiredSpace = Math.max(1, (int) Math.ceil(scale));
        for (int x = -requiredSpace; x <= requiredSpace; x++) {
            for (int y = 0; y <= requiredSpace * 2; y++) {
                for (int z = -requiredSpace; z <= requiredSpace; z++) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    if (!level.getBlockState(checkPos).isAir()) {
                        return false;
                    }
                }
            }
        }
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    private static boolean isAirSafeLocation(BlockPos pos, Level level, LivingEntity entity) {
        // Check if the position is above the minimum teleport level
        if (pos.getY() < MIN_TELEPORT_Y) {
            return false;
        }

        float scale = BeyonderUtil.getScale(entity);
        int requiredSpace = Math.max(1, (int) Math.ceil(scale));
        for (int x = -requiredSpace; x <= requiredSpace; x++) {
            for (int y = 0; y <= requiredSpace * 2; y++) {
                for (int z = -requiredSpace; z <= requiredSpace; z++) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    if (!level.getBlockState(checkPos).isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static void escapeTrickHurtEvent(LivingHurtEvent event) {
        LivingEntity attacked = event.getEntity();
        if(attacked.getPersistentData().getInt("escapeTrickCount") > 0) {
            if (BeyonderUtil.currentPathwayMatches(attacked, BeyonderClassInit.APPRENTICE.get()) && BeyonderUtil.getSequence(attacked) > 4) {
                int range = (int) (float) BeyonderUtil.getDamage(attacked).get(ItemInit.TRICKESCAPETRICK.get()) * 2;
                boolean teleported = tryTeleportToSafeLocation(attacked, attacked.level(), range);

                if (teleported) {
                    attacked.getPersistentData().putInt("escapeTrickCount", attacked.getPersistentData().getInt("escapeTrickCount") - 1);
                    if (BeyonderUtil.getSequence(attacked) > 4) {
                        event.setAmount(event.getAmount() / 2);
                    }
                    if (attacked instanceof Player player) {
                        player.displayClientMessage(Component.literal("Remaining Escape Tricks: ").withStyle(BeyonderUtil.getStyle(player)).append(Component.literal("" + player.getPersistentData().getInt("escapeTrickCount")).withStyle(ChatFormatting.WHITE)), true);
                    }
                } else {
                    if (attacked instanceof Player player) {
                        player.displayClientMessage(Component.literal("No safe spaces found").withStyle(BeyonderUtil.getStyle(player)), true);
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, saves an Escape Trick, which causes the next time you take damage to be canceled, turning you into smoke and teleporting a small distance away."));
        tooltipComponents.add(Component.literal("Left click for Trick: Flash"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("150").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("10 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICE_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (PlayerMobEntity.isCopy(livingEntity)) {
            return 0;
        }
        int maxEscapes = (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TRICKESCAPETRICK.get());
        int escapeTrickCount = livingEntity.getPersistentData().getInt("escapeTrickCount");
        if (target == null && escapeTrickCount < maxEscapes) {
            return 30;
        } else if (escapeTrickCount >= maxEscapes) {
            return 0;
        }
        return 0;
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.TRICKFLASH.get()));
    }
}