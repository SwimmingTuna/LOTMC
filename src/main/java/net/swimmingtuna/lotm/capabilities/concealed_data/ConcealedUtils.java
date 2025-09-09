package net.swimmingtuna.lotm.capabilities.concealed_data;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class ConcealedUtils {

    public static Optional<IConcealedDataCapability> getConcealedData(LivingEntity entity) {
        return entity.getCapability(ConcealedDataProvider.CONCEALED_DATA).resolve();
    }

    public static boolean hasConcealedSpace(LivingEntity entity) {
        return getConcealedData(entity).map(IConcealedDataCapability::ownsConcealedSpace).orElse(false);
    }

    public static void setConcealedSpaceOwnership(LivingEntity entity, boolean owns) {
        entity.getCapability(ConcealedDataProvider.CONCEALED_DATA).ifPresent(data -> {
            data.setOwnsConcealedSpace(owns);
        });
    }

    public static int getConcealedSpaceSequence(LivingEntity entity){
        return getConcealedData(entity).map(IConcealedDataCapability::sequenceConcealedSpace).orElse(9);
    }

    public static void setConcealedSpaceSequence(LivingEntity entity, int sequence){
        entity.getCapability(ConcealedDataProvider.CONCEALED_DATA).ifPresent(data ->{
            data.setSequenceConcealedSpace(sequence);
        });
    }

    public static BlockPos getConcealedSpaceCenter(LivingEntity entity){
        return getConcealedData(entity).map(IConcealedDataCapability::centerConcealedSpace).orElse(BlockPos.ZERO);
    }

    public static void setConcealedSpaceCenter(LivingEntity entity, BlockPos center){
        entity.getCapability(ConcealedDataProvider.CONCEALED_DATA).ifPresent(data ->{
            data.setCenterConcealedSpace(center);
        });
    }

    public static BlockPos getConcealedSpaceSpawn(LivingEntity entity){
        return getConcealedData(entity).map(IConcealedDataCapability::spawnConcealedSpace).orElse(BlockPos.ZERO);
    }

    public static void setConcealedSpaceSpawn(LivingEntity entity, BlockPos spawn){
        entity.getCapability(ConcealedDataProvider.CONCEALED_DATA).ifPresent(data ->{
            data.setSpawnConcealedSpace(spawn);
        });
    }

    public static BlockPos getConcealedSpaceExit(LivingEntity entity){
        return getConcealedData(entity).map(IConcealedDataCapability::exitConcealedSpace).orElse(BlockPos.ZERO);
    }

    public static void setConcealedSpaceExit(LivingEntity entity, BlockPos exit){
        entity.getCapability(ConcealedDataProvider.CONCEALED_DATA).ifPresent(data ->{
            data.setExitConcealedSpace(exit);
        });
    }

    public static Level getConcealedSpaceExitDimension(LivingEntity entity) {
        MinecraftServer server = entity.getServer();
        if (server == null) return null;
        return getConcealedData(entity).map(IConcealedDataCapability::exitDimensionConcealedSpace).map(server::getLevel).orElse(null);
    }

    public static void setConcealedSpaceExitDimension(LivingEntity entity, Level exit){
        entity.getCapability(ConcealedDataProvider.CONCEALED_DATA).ifPresent(data ->{
            data.setExitDimensionConcealedSpace(exit.dimension());
        });
    }
}
