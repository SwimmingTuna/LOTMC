package net.swimmingtuna.lotm.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.util.LangKeys;
import net.swimmingtuna.lotm.util.PlayerMobs.NameManager;

import javax.annotation.Nullable;

public class PlayerMobsCommand {

    private static final SimpleCommandExceptionType SUMMON_FAILED = new SimpleCommandExceptionType(Component.translatable(LangKeys.COMMANDS_SPAWN_FAILED.key()));
    private static final SimpleCommandExceptionType DUPLICATE_UUID = new SimpleCommandExceptionType(Component.translatable(LangKeys.COMMANDS_SPAWN_UUID.key()));
    private static final SimpleCommandExceptionType INVALID_POS = new SimpleCommandExceptionType(Component.translatable(LangKeys.COMMANDS_SPAWN_INVALID_POS.key()));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands
                .literal("playermobs")
                .requires(commandSource -> commandSource.hasPermission(2))
                .then(Commands.literal("reload").executes(context -> {
                            context.getSource().sendSuccess(() -> Component.translatable(LangKeys.COMMANDS_RELOAD_START.key()), true);
                            NameManager.INSTANCE.reloadRemoteLinks().thenAccept(change ->
                                    context.getSource().sendSuccess(() -> Component.translatable(LangKeys.COMMANDS_RELOAD_DONE.key(), change), true));
                            return 1;
                        })
                ).then(Commands.literal("spawn")
                        .executes(context -> spawnPlayerMob(context.getSource(), null, context.getSource().getPosition(), null, -1, 1))
                        .then(Commands.argument("username", StringArgumentType.string())
                                .executes(context -> spawnPlayerMob(context.getSource(), StringArgumentType.getString(context, "username"), context.getSource().getPosition(), null, -1, 1))
                                .then(Commands.argument("pos", Vec3Argument.vec3())
                                        .executes(context -> spawnPlayerMob(context.getSource(), StringArgumentType.getString(context, "username"), Vec3Argument.getVec3(context, "pos"), null, -1, 1))
                                        .then(Commands.argument("pathway", BeyonderClassArgument.beyonderClass())
                                                .executes(context -> spawnPlayerMob(context.getSource(), StringArgumentType.getString(context, "username"), Vec3Argument.getVec3(context, "pos"), BeyonderClassArgument.getBeyonderClass(context, "pathway"), -1, 1))
                                                .then(Commands.argument("sequence", IntegerArgumentType.integer(0, 9))
                                                        .executes(context -> spawnPlayerMob(context.getSource(), StringArgumentType.getString(context, "username"), Vec3Argument.getVec3(context, "pos"), BeyonderClassArgument.getBeyonderClass(context, "pathway"), IntegerArgumentType.getInteger(context, "sequence"), 1))
                                                        .then(Commands.argument("attack_chance", IntegerArgumentType.integer(0, 100))
                                                                .executes(context -> spawnPlayerMob(context.getSource(), StringArgumentType.getString(context, "username"), Vec3Argument.getVec3(context, "pos"), BeyonderClassArgument.getBeyonderClass(context, "pathway"), IntegerArgumentType.getInteger(context, "sequence"), IntegerArgumentType.getInteger(context, "attack_chance"))))))))
                ));
    }


    private static int spawnPlayerMob(CommandSourceStack source, @Nullable String username, Vec3 pos, @Nullable BeyonderClass pathway, int sequence, int attackChance) throws CommandSyntaxException {
        BlockPos blockpos = BlockPos.containing(pos);
        if (!Level.isInSpawnableBounds(blockpos)) {
            throw INVALID_POS.create();
        } else {
            PlayerMobEntity entity = EntityInit.PLAYER_MOB_ENTITY.get().create(source.getLevel());
            if (entity == null) {
                throw SUMMON_FAILED.create();
            } else {
                entity.moveTo(pos.x, pos.y, pos.z, entity.getYRot(), entity.getXRot());

                // Set username
                if (username != null)
                    entity.setUsername(username);
                else
                    entity.setUsername(NameManager.INSTANCE.getRandomName());
                if (pathway != null) {
                    entity.setPathway(pathway);
                    if (sequence != -1) {
                        entity.setSequence(sequence);
                    }
                }
                entity.setAttackChance(attackChance);

                ForgeEventFactory.onFinalizeSpawn(entity, source.getLevel(), source.getLevel().getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.COMMAND, null, null);

                if (!source.getLevel().tryAddFreshEntityWithPassengers(entity)) {
                    throw DUPLICATE_UUID.create();
                }

                MutableComponent name = MutableComponent.create(entity.getDisplayName().getContents())
                        .withStyle(Style.EMPTY
                                .withColor(ChatFormatting.YELLOW)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, entity.getUUID().toString()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new HoverEvent.EntityTooltipInfo(entity.getType(), entity.getUUID(), entity.getName()))));
                return 1;
            }
        }
    }
}