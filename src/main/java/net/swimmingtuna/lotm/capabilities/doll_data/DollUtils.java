package net.swimmingtuna.lotm.capabilities.doll_data;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class DollUtils {



    public static Optional<IDollDataCapability> getDollData(Player player) {
        return player.getCapability(DollDataProvider.DOLL_DATA).resolve();
    }

    public static boolean isDoll(Player player){
        return getDollData(player).map(IDollDataCapability::isDoll).orElse(false);
    }

    public static boolean canUseAbilities(Player player){
        return true;
    }

    public static boolean markedForDeath(Player player){
        return getDollData(player).map(IDollDataCapability::markedForDeath).orElse(false);
    }

    public static float dollX(Player player){
        return getDollData(player).map(IDollDataCapability::dollX).orElse(0f);
    }

    public static float dollY(Player player){
        return getDollData(player).map(IDollDataCapability::dollY).orElse(0f);
    }

    public static float dollZ(Player player){
        return getDollData(player).map(IDollDataCapability::dollZ).orElse(0f);
    }
    public static Level dollDimension(Player player){
        MinecraftServer server = player.getServer();
        if(server == null) return null;
        return getDollData(player).map(IDollDataCapability::dollDimension).map(server::getLevel).orElse(null);
    }

    public static int dollTimer(Player player){
        return getDollData(player).map(IDollDataCapability::dollTimer).orElse(0);
    }

    public static int dollDetectTimer(Player player){
        return getDollData(player).map(IDollDataCapability::dollDetectTimer).orElse(5);
    }

    public static void setIsDoll(Player player, boolean isDoll){
        player.getCapability(DollDataProvider.DOLL_DATA).ifPresent(data -> {
            data.setIsDoll(isDoll);
        });
    }

    public static void setMarkedForDeath(Player player, boolean markedForDeath){
        player.getCapability(DollDataProvider.DOLL_DATA).ifPresent(data -> {
            data.setMarkedForDeath(markedForDeath);
        });
    }

    public static void setCoords(Player player, float x, float y, float z){
        player.getCapability(DollDataProvider.DOLL_DATA).ifPresent(data -> {
            data.setDollX(x);
            data.setDollY(y);
            data.setDollZ(z);
        });
    }

    public static void setDollDimension(Player player, Level level){
        player.getCapability(DollDataProvider.DOLL_DATA).ifPresent(data -> {
            data.setDollDimension(level.dimension());
        });
    }

    public static void setTimer(Player player, int timer){
        player.getCapability(DollDataProvider.DOLL_DATA).ifPresent(data -> {
            data.setDollTimer(timer);
        });
    }

    public static void setDetectTimer(Player player, int timer){
        player.getCapability(DollDataProvider.DOLL_DATA).ifPresent(data -> {
            data.setDetectDollTimer(timer);
        });
    }

    public static void dollPlayerTick(Player player){
        if(!player.level().isClientSide){
            if(isDoll(player)) {
                if(player instanceof ServerPlayer serverPlayer) {
                    Level targetLevel = dollDimension(serverPlayer);
                    if(targetLevel != null && serverPlayer.level().dimension() != targetLevel.dimension()){
                        ResourceKey<Level> destination = targetLevel.dimension();
                        ServerLevel destinationLevel = serverPlayer.server.getLevel(destination);
                        if(destinationLevel != null){
                            serverPlayer.teleportTo(destinationLevel, dollX(serverPlayer), dollY(serverPlayer), dollZ(serverPlayer), serverPlayer.getYRot(), serverPlayer.getXRot());
                        }
                    }else{
                        serverPlayer.connection.teleport(dollX(serverPlayer), dollY(serverPlayer), dollZ(serverPlayer), serverPlayer.getYRot(), serverPlayer.getXRot());
                    }
                }
                if (dollTimer(player) > 0 && dollDetectTimer(player) > 0) {
                    setTimer(player, Math.max(0, dollTimer(player) - 1));
                    setDetectTimer(player, Math.max(0, dollDetectTimer(player) - 1));
                } else {
                    setMarkedForDeath(player, true);
                }
            }else{
                setTimer(player, 0);
            }
            if(markedForDeath(player)){
                ((ServerPlayer) player).setGameMode(GameType.SURVIVAL);
                setIsDoll(player, false);
                setMarkedForDeath(player, false);
                player.kill();
            }
        }
    }
}