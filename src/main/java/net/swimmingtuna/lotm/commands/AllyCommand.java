package net.swimmingtuna.lotm.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.swimmingtuna.lotm.util.AllyInformation.PlayerAllyData;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class AllyCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ally")
                .then(Commands.literal("add")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(AllyCommand::addAlly)))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(AllyCommand::removeAlly)))
                .then(Commands.literal("list")
                        .executes(AllyCommand::listAllies))
        );
    }

    private static int addAlly(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer source = context.getSource().getPlayerOrException();
            ServerPlayer target = EntityArgument.getPlayer(context, "player");
            if (source.getUUID().equals(target.getUUID())) {
                context.getSource().sendFailure(Component.literal("You cannot add yourself as an ally!").withStyle(ChatFormatting.RED));
                return 0;
            }
            ServerLevel level = source.serverLevel();
            PlayerAllyData allyData = level.getDataStorage().computeIfAbsent(PlayerAllyData::load, PlayerAllyData::create, "player_allies");
            if (allyData.areAllies(source.getUUID(), target.getUUID())) {
                context.getSource().sendFailure(Component.literal("You are already allies with " + target.getScoreboardName()).withStyle(ChatFormatting.YELLOW));
                return 0;
            }
            allyData.addAlly(source.getUUID(), target.getUUID());
            source.sendSystemMessage(Component.literal(("Added " + target.getScoreboardName() + " as an ally")).withStyle(ChatFormatting.GREEN), true);
            target.sendSystemMessage(Component.literal(source.getScoreboardName() + " has added you as an ally").withStyle(ChatFormatting.GREEN));
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to add ally: " + e.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int removeAlly(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer source = context.getSource().getPlayerOrException();
            ServerPlayer target = EntityArgument.getPlayer(context, "player");
            ServerLevel level = source.serverLevel();
            PlayerAllyData allyData = level.getDataStorage().computeIfAbsent(PlayerAllyData::load, PlayerAllyData::create, "player_allies");
            if (!allyData.areAllies(source.getUUID(), target.getUUID())) {
                context.getSource().sendFailure(Component.literal("You are not allies with " + target.getScoreboardName()).withStyle(ChatFormatting.YELLOW));
                return 0;
            }
            allyData.removeAlly(source.getUUID(), target.getUUID());
            source.sendSystemMessage(Component.literal("Removed " + target.getScoreboardName() + " from allies").withStyle(ChatFormatting.GREEN));
            target.sendSystemMessage(Component.literal(source.getScoreboardName() + " has removed you as an ally").withStyle(ChatFormatting.RED));
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to remove ally: " + e.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int listAllies(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer source = context.getSource().getPlayerOrException();
            ServerLevel level = source.serverLevel();
            MinecraftServer server = level.getServer();
            PlayerAllyData allyData = level.getDataStorage().computeIfAbsent(PlayerAllyData::load, PlayerAllyData::create, "player_allies");
            Set<UUID> allies = allyData.getAllies(source.getUUID());
            if (allies.isEmpty()) {
                source.sendSystemMessage(Component.literal("You have no allies").withStyle(ChatFormatting.RED));
                return 1;
            }
            StringBuilder allyList = new StringBuilder("Your allies: ");
            boolean first = true;
            GameProfileCache profileCache = server.getProfileCache();
            for (UUID allyUUID : allies) {
                String allyName;
                ServerPlayer allyPlayer = server.getPlayerList().getPlayer(allyUUID);
                if (allyPlayer != null) {
                    allyName = allyPlayer.getScoreboardName();
                    allyName = ChatFormatting.GREEN + allyName + ChatFormatting.WHITE;
                } else {
                    Optional<GameProfile> profile = profileCache != null ?
                            profileCache.get(allyUUID) : Optional.empty();

                    if (profile.isPresent()) {
                        allyName = profile.get().getName();
                        allyName = ChatFormatting.GRAY + allyName + ChatFormatting.WHITE; // Show offline players in gray
                    } else {
                        allyName = ChatFormatting.GRAY + allyUUID.toString().substring(0, 8) + "..." + ChatFormatting.WHITE;
                    }
                }

                if (!first) {
                    allyList.append(", ");
                }
                allyList.append(allyName);
                first = false;
            }

            context.getSource().sendSuccess(() ->
                    Component.literal(allyList.toString()), false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to list allies: " + e.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }
}