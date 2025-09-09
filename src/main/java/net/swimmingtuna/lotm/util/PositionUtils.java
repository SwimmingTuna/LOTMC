package net.swimmingtuna.lotm.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class PositionUtils {

    /**
     * Finds the nearest block of the specified types within the search radius
     *
     * @param level The world level
     * @param entity The entity to search from (usually a player)
     * @param searchBlocks List of block items to search for
     * @param searchRadius The radius to search within
     * @return The position of the nearest matching block, or null if none found
     */
    @Nullable
    public static BlockPos getNearestBlock(Level level, Entity entity, List<Item> searchBlocks, double searchRadius) {
        if (!(entity instanceof Player player) || searchBlocks == null || searchBlocks.isEmpty()) {
            return null;
        }

        BlockPos userPos = player.getOnPos();
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        AABB searchArea = new AABB(px, py, pz, px + 1, py + 1, pz + 1).inflate(searchRadius);

        return BlockPos.betweenClosedStream(searchArea)
                .map(BlockPos::immutable)
                .filter(pos -> {
                    Block block = level.getBlockState(pos).getBlock();
                    if (block instanceof AirBlock) {
                        return false;
                    }
                    return searchBlocks.contains(block.asItem());
                })
                .min(Comparator.comparingDouble(pos -> pos.distSqr(userPos)))
                .orElse(null);
    }

    /**
     * Finds the nearest living entity of the specified types within the search radius
     *
     * @param level The world level
     * @param entity The entity to search from (usually a player)
     * @param targetEntityTypes List of entity types to search for (as Items, typically spawn eggs)
     * @param searchRadius The radius to search within
     * @param excludePlayers Whether to exclude players from the search
     * @return The position of the nearest matching entity, or null if none found
     */
    @Nullable
    public static BlockPos getNearestEntity(Level level, Entity entity, List<Item> targetEntityTypes,
                                            double searchRadius, boolean excludePlayers) {
        if (!(entity instanceof Player player) || targetEntityTypes == null || targetEntityTypes.isEmpty()) {
            return null;
        }

        BlockPos userPos = player.getOnPos();
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        AABB searchArea = new AABB(px, py, pz, px + 1, py + 1, pz + 1).inflate(searchRadius);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, searchArea);

        return nearbyEntities.stream()
                .filter(livingEntity -> {
                    // Exclude the searching player
                    if (livingEntity.equals(player)) {
                        return false;
                    }
                    // Optionally exclude other players
                    if (excludePlayers && livingEntity instanceof Player) {
                        return false;
                    }
                    // Add your entity type matching logic here
                    // This is simplified - you'd need to implement proper entity type matching
                    // based on your specific requirements
                    return true;
                })
                .map(LivingEntity::getOnPos)
                .min(Comparator.comparingDouble(pos -> pos.distSqr(userPos)))
                .orElse(null);
    }

    /**
     * Finds the nearest position from a list of candidate positions
     *
     * @param positions List of positions to check
     * @param entity The entity to measure distance from
     * @return The closest position, or null if the list is empty
     */
    @Nullable
    public static BlockPos getClosestPosition(List<BlockPos> positions, Entity entity) {
        if (positions == null || positions.isEmpty()) {
            return null;
        }

        BlockPos entityPos = entity.blockPosition();

        return positions.stream()
                .filter(pos -> pos != null)
                .min(Comparator.comparingDouble(pos -> pos.distSqr(entityPos)))
                .orElse(null);
    }

    /**
     * Gets all entities of a specific type within a radius
     *
     * @param level The world level
     * @param center The center position to search from
     * @param entityClass The class of entities to find
     * @param searchRadius The radius to search within
     * @return List of entities found
     */
    public static <T extends Entity> List<T> getEntitiesInRadius(Level level, BlockPos center,
                                                                 Class<T> entityClass, double searchRadius) {
        AABB searchArea = new AABB(center).inflate(searchRadius);
        return level.getEntitiesOfClass(entityClass, searchArea);
    }

    /**
     * Gets all blocks of specific types within a radius
     *
     * @param level The world level
     * @param center The center position to search from
     * @param searchBlocks List of block items to search for
     * @param searchRadius The radius to search within
     * @return List of positions where matching blocks were found
     */
    public static List<BlockPos> getBlocksInRadius(Level level, BlockPos center,
                                                   List<Item> searchBlocks, double searchRadius) {
        AABB searchArea = new AABB(center).inflate(searchRadius);

        return BlockPos.betweenClosedStream(searchArea)
                .map(BlockPos::immutable)
                .filter(pos -> {
                    Block block = level.getBlockState(pos).getBlock();
                    return !(block instanceof AirBlock) && searchBlocks.contains(block.asItem());
                })
                .toList();
    }

    /**
     * Calculates the distance between two positions
     *
     * @param pos1 First position
     * @param pos2 Second position
     * @return The distance between the positions
     */
    public static double getDistance(BlockPos pos1, BlockPos pos2) {
        return Math.sqrt(pos1.distSqr(pos2));
    }

    /**
     * Simple data class to hold position information
     */
    public static class LocationData {
        private final BlockPos blockPos;
        private final String name;

        public LocationData(BlockPos blockPos, String name) {
            this.blockPos = blockPos;
            this.name = name;
        }

        public BlockPos getBlockPos() {
            return blockPos;
        }

        public String getName() {
            return name;
        }
    }
}
