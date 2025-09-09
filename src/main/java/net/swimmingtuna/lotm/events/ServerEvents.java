package net.swimmingtuna.lotm.events;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.beyonder.SpectatorClass;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.BlockInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.ProbabilityManipulationFortune;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.ProbabilityManipulationInfiniteFortune;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.ProbabilityManipulationInfiniteMisfortune;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.ProbabilityManipulationMisfortune;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.*;
import net.swimmingtuna.lotm.item.OtherItems.Astrolabe;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.ClientWormOfStarDataS2C;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.world.worlddata.PlayerMobTracker;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static net.swimmingtuna.lotm.beyonder.SpectatorClass.EVENT_TO_TAG;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.Teleportation.flickeringCopy;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.TravelersDoor.*;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.EnvisionLife.spawnMob;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.EnvisionLocation.isThreeIntegers;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.EnvisionWeather.*;

@Mod.EventBusSubscriber(modid = LOTM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {


    @SubscribeEvent
    public static void onChatMessage(ServerChatEvent event) {
        Level level = event.getPlayer().serverLevel();
        ServerPlayer player = event.getPlayer();
        Style style = BeyonderUtil.getStyle(player);
        Astrolabe.astrolabeChatMessage(event);
        if (!player.level().isClientSide() && player.getMainHandItem().getItem() instanceof EnvisionWeather) {
            if (!BeyonderUtil.currentPathwayMatches(player, BeyonderClassInit.SPECTATOR.get())) {
                player.displayClientMessage(Component.literal("You are not of the Spectator pathway").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
            }
            if (BeyonderUtil.getSpirituality(player) < (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_WEATHER.get())) {
                player.displayClientMessage(Component.literal("You need " + ((int) 500 / BeyonderUtil.getDreamIntoReality(player) + " spirituality in order to use this")).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
            }
            String message = event.getMessage().getString().toLowerCase();
            if (BeyonderUtil.currentPathwayMatches(player, BeyonderClassInit.SPECTATOR.get())) {
                if (message.equals("clear") && BeyonderUtil.getSpirituality(player) > (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_WEATHER.get())) {
                    setWeatherClear(level);
                    event.getPlayer().displayClientMessage(Component.literal("Set Weather to Clear").withStyle(style), true);
                    BeyonderUtil.useSpirituality(player, (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_WEATHER.get()));
                    event.setCanceled(true);
                }
                if (message.equals("rain") && BeyonderUtil.getSpirituality(player) > (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_WEATHER.get())) {
                    event.getPlayer().displayClientMessage(Component.literal("Set Weather to Rain").withStyle(style), true);
                    setWeatherRain(level);
                    BeyonderUtil.useSpirituality(player, (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_WEATHER.get()));
                    event.setCanceled(true);
                }
                if (message.equals("thunder") && BeyonderUtil.getSpirituality(player) > (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_WEATHER.get())) {
                    event.getPlayer().displayClientMessage(Component.literal("Set Weather to Thunder").withStyle(style), true);
                    setWeatherThunder(level);
                    BeyonderUtil.useSpirituality(player, (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_WEATHER.get()));
                    event.setCanceled(true);
                }
            }
        }
        if (!player.level().isClientSide()) {
            String message = event.getMessage().getString().toLowerCase();
            for (Player otherPlayer : level.players()) {
                if (message.contains(otherPlayer.getName().getString().toLowerCase()) && !event.isCanceled()) {
                    BeyonderHolder otherHolder = BeyonderHolderAttacher.getHolderUnwrap(otherPlayer);
                    if (otherHolder.currentClassMatches(BeyonderClassInit.SPECTATOR) && otherHolder.getSequence() <= 1 && !otherPlayer.level().isClientSide()) {
                        otherPlayer.sendSystemMessage(Component.literal(player.getName().getString() + " mentioned you in chat. Their coordinates are: " + (int) player.getX() + " ," + (int) player.getY() + " ," + (int) player.getZ()).withStyle(style));
                    }
                    if (otherHolder.currentClassMatches(BeyonderClassInit.SAILOR) && otherHolder.getSequence() <= 1 && !otherPlayer.level().isClientSide()) {
                        otherPlayer.getPersistentData().putInt("tyrantMentionedInChat", 200);
                        EventManager.addToRegularLoop(otherPlayer, EFunctions.LIGHTNING_STORM.get());
                        otherPlayer.sendSystemMessage(Component.literal(player.getName().getString() + " mentioned you in chat. Do you want to summon a lightning storm on them? Type Yes if so, you have 10 seconds").withStyle(style));
                        otherPlayer.getPersistentData().putInt("sailorStormVecX1", (int) player.getX());
                        otherPlayer.getPersistentData().putInt("sailorStormVecY1", (int) player.getY());
                        otherPlayer.getPersistentData().putInt("sailorStormVecZ1", (int) player.getZ());
                    }
                }
            }
            if (player.getPersistentData().getInt("tyrantMentionedInChat") >= 1 && message.toLowerCase().contains("yes")) {
                if (BeyonderUtil.getSpirituality(player) >= 800) {
                    EventManager.addToRegularLoop(player, EFunctions.LIGHTNING_STORM.get());
                    BeyonderUtil.useSpirituality(player, 800);
                    player.getPersistentData().putInt("sailorLightningStorm1", 300);
                    player.getPersistentData().putInt("sailorStormVecX1", (int) player.getX());
                    player.getPersistentData().putInt("sailorStormVecY1", (int) player.getY());
                    player.getPersistentData().putInt("sailorStormVecZ1", (int) player.getZ());
                    event.setCanceled(true);
                } else {
                    player.sendSystemMessage(Component.literal("Not enough spirituality").withStyle(BeyonderUtil.getStyle(player)));
                }
            }
        }
        if (!player.level().isClientSide()) {
            String message = event.getMessage().getString().toLowerCase();
            if (player.getMainHandItem().getItem() instanceof ProbabilityManipulationFortune && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.MONSTER.get(), 0)) {
                for (Player onlinePlayer : player.level().players()) {
                    if (message.equals(onlinePlayer.getName().getString().toLowerCase())) {
                        player.sendSystemMessage(Component.literal("Successfully gave " + onlinePlayer.getName().getString() + " all fortunate events").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD));
                        ProbabilityManipulationFortune.giveFortuneEvents(onlinePlayer);
                        BeyonderUtil.useSpirituality(player, 500);
                    }
                }
            }
            if (player.getMainHandItem().getItem() instanceof ProbabilityManipulationMisfortune && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.MONSTER.get(), 0)) {
                for (Player onlinePlayer : player.level().players()) {
                    if (message.equals(onlinePlayer.getName().getString().toLowerCase())) {
                        player.sendSystemMessage(Component.literal("Successfully gave " + onlinePlayer.getName().getString() + " all misfortunate events").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD));
                        ProbabilityManipulationMisfortune.giveMisfortuneEvents(onlinePlayer);
                        BeyonderUtil.useSpirituality(player, 500);
                    }
                }
            }
            if (player.getMainHandItem().getItem() instanceof ProbabilityManipulationInfiniteMisfortune && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.MONSTER.get(), 0)) {
                for (Player onlinePlayer : player.level().players()) {
                    if (message.equals(onlinePlayer.getName().getString().toLowerCase())) {
                        player.sendSystemMessage(Component.literal("Successfully gave " + onlinePlayer.getName().getString() + " infinite misfortune").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD));
                        ProbabilityManipulationInfiniteMisfortune.giveInfiniteMisfortune(onlinePlayer);
                        BeyonderUtil.useSpirituality(player, 2000);
                    }
                }
            }
            if (player.getMainHandItem().getItem() instanceof ProbabilityManipulationInfiniteFortune && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.MONSTER.get(), 0)) {
                for (Player onlinePlayer : player.level().players()) {
                    if (message.equals(onlinePlayer.getName().getString().toLowerCase())) {
                        player.sendSystemMessage(Component.literal("Successfully gave " + onlinePlayer.getName().getString() + " infinite fortune").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD));
                        ProbabilityManipulationInfiniteFortune.giveInfiniteFortune(onlinePlayer);
                        BeyonderUtil.useSpirituality(player, 2000);
                    }
                }
            }
        }
        if (!player.level().isClientSide() && player.getMainHandItem().getItem() instanceof ConsciousnessStroll && !player.getCooldowns().isOnCooldown(ItemInit.CONSCIOUSNESS_STROLL.get()) && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.SPECTATOR.get(), 3)) {
            String message = event.getMessage().getString();
            for (ServerPlayer onlinePlayer : player.getServer().getPlayerList().getPlayers()) {
                if (message.equalsIgnoreCase(onlinePlayer.getName().getString())) {
                    if (!BeyonderUtil.currentPathwayMatches(player, BeyonderClassInit.SPECTATOR.get())) {
                        player.displayClientMessage(Component.literal("You are not of the Spectator pathway").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
                    } else if (BeyonderUtil.getSpirituality(player) < 300) {
                        player.displayClientMessage(Component.literal("You need 300 spirituality in order to use this").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
                    } else {
                        EventManager.addToRegularLoop(player, EFunctions.CONSCIOUSNESS_STROLL.get());
                        player.getCooldowns().addCooldown(ItemInit.CONSCIOUSNESS_STROLL.get(), 400);
                        player.getPersistentData().putInt("consciousnessStrollActivatedX", (int) player.getX());
                        player.getPersistentData().putInt("consciousnessStrollActivatedY", (int) player.getY());
                        player.getPersistentData().putInt("consciousnessStrollActivatedZ", (int) player.getZ());
                        player.getPersistentData().putString("consciousnessStrollDimension", player.level().dimension().toString());
                        if (player.isCreative()) {
                            player.getPersistentData().putString("consciousnessStrollGamemode", "creative");
                        } else if (player.isSpectator()) {
                            player.getPersistentData().putString("consciousnessStrollGamemode", "spectator");
                        } else {
                            player.getPersistentData().putString("consciousnessStrollGamemode", "survival");
                        }
                        player.setGameMode(GameType.SPECTATOR);
                        BeyonderUtil.useSpirituality(player, 300);
                        if (player.level().dimension() != onlinePlayer.level().dimension()) {
                            ResourceLocation dimensionLocation = onlinePlayer.level().dimension().location();
                            String dimensionName;
                            if (dimensionLocation.equals(Level.OVERWORLD.location())) {
                                dimensionName = "Overworld";
                            } else if (dimensionLocation.equals(Level.NETHER.location())) {
                                dimensionName = "The Nether";
                            } else if (dimensionLocation.equals(Level.END.location())) {
                                dimensionName = "The End";
                            } else {
                                String path = dimensionLocation.getPath();
                                dimensionName = Arrays.stream(path.split("_")).map(word -> word.substring(0, 1).toUpperCase() + word.substring(1)).collect(Collectors.joining(" "));
                            }
                            player.displayClientMessage(Component.literal("You can't view from the target's consciousness as they are in another dimension. Try going to the " + dimensionName + " dimension").withStyle(ChatFormatting.RED), true);                        } else {
                            player.teleportTo(onlinePlayer.getX(), onlinePlayer.getY(), onlinePlayer.getZ());
                            player.getPersistentData().putInt("consciousnessStrollActivated", 120);
                        }

                        event.setCanceled(true);
                    }
                }
            }
        }
        if (player.getMainHandItem().getItem() instanceof EnvisionLife && !player.level().isClientSide() && !player.getCooldowns().isOnCooldown(ItemInit.ENVISION_LIFE.get())) {
            if (!BeyonderUtil.currentPathwayMatches(player, BeyonderClassInit.SPECTATOR.get())) {
                player.displayClientMessage(Component.literal("You are not of the Spectator pathway").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
            }

            if (BeyonderUtil.getSpirituality(player) < 1500) {
                player.displayClientMessage(Component.literal("You need " + (int) (1500 / BeyonderUtil.getDreamIntoReality(player)) + " spirituality in order to use this").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
            }
        }
        if (!player.level().isClientSide() && player.getMainHandItem().getItem() instanceof EnvisionLife && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.SPECTATOR.get(), 0)) {
            String message = event.getMessage().getString().toLowerCase();
            spawnMob(player, message);
            BeyonderUtil.useSpirituality(player, 1500 / BeyonderUtil.getDreamIntoReality(player));
            event.setCanceled(true);
        }
        String message = event.getMessage().getString();
        if (!player.level().isClientSide() && player.getMainHandItem().getItem() instanceof EnvisionLocation && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.SPECTATOR.get(), 0)) {
            if (!BeyonderUtil.currentPathwayMatches(player, BeyonderClassInit.SPECTATOR.get())) {
                player.displayClientMessage(Component.literal("You are not of the Spectator pathway").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
                event.setCanceled(true);
                return;
            }
            if (BeyonderUtil.getSpirituality(player) < BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_LOCATION.get())) {
                player.displayClientMessage(Component.literal("You need " + BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_LOCATION.get()) + " spirituality in order to use this").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
                event.setCanceled(true);
                return;
            }
            if (isThreeIntegers(message)) {
                String[] coordinates = message.replace(",", " ").trim().split("\\s+");
                int x = Integer.parseInt(coordinates[0]);
                int y = Integer.parseInt(coordinates[1]);
                int z = Integer.parseInt(coordinates[2]);
                EnvisionLocation.envisionLocationTeleport(player, x, y, z);
                event.getPlayer().displayClientMessage(Component.literal("Teleported to " + x + ", " + y + ", " + z).withStyle(BeyonderUtil.getStyle(player)), true);
                BeyonderUtil.useSpirituality(player, (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_LOCATION.get()));
                event.setCanceled(true);
                return;
            }
            Player targetPlayer = null;
            for (Player serverPlayer : level.players()) {
                if (serverPlayer.getName().getString().toLowerCase().equals(message.toLowerCase())) {
                    targetPlayer = serverPlayer;
                    break;
                }
            }
            if (targetPlayer != null) {
                int x = (int) targetPlayer.getX();
                int y = (int) targetPlayer.getY();
                int z = (int) targetPlayer.getZ();
                EnvisionLocation.envisionLocationTeleport(player, targetPlayer.level(), x, y, z);
                BeyonderUtil.useSpirituality(player, (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_LOCATION.get()));
            } else {
                event.getPlayer().displayClientMessage(Component.literal("Invalid coordinates or player name: " + message).withStyle(BeyonderUtil.getStyle(player)), true);
            }
            event.setCanceled(true);
        }
        if (!player.level().isClientSide && player.getMainHandItem().getItem() instanceof TravelersDoor && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.APPRENTICE.get(), 5)) {
            if (!BeyonderUtil.currentPathwayMatches(player, BeyonderClassInit.APPRENTICE.get())) {
                player.displayClientMessage(Component.literal("You are not of the Apprentice pathway").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE), true);
                event.setCanceled(true);
                return;
            }
            if (BeyonderUtil.getSpirituality(player) < 300 * (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.TRAVELERSDOOR.get())) {
                player.displayClientMessage(Component.literal("You need " + 300 * (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.TRAVELERSDOOR.get()) + " spirituality in order to use this").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE), true);
                event.setCanceled(true);
                return;
            }
            if (coordsTravel(message)) {
                String[] coordinates = message.replace(",", " ").trim().split("\\s+");
                int x = Integer.parseInt(coordinates[0]);
                int y = Integer.parseInt(coordinates[1]);
                int z = Integer.parseInt(coordinates[2]);

                Level destination = player.level();
                if (hasDimensionId(message)) {
                    destination = getLevelFromId(Objects.requireNonNull(player.getServer()), getDimensionId(message), destination);
                }

                if (!canTeleportAcrossDimensions(player, destination)) {
                    event.getPlayer().displayClientMessage(Component.literal("Target is in an inaccessible dimension").withStyle(ChatFormatting.RED), true);
                    event.setCanceled(true);
                    return;
                }

                String dimensionName = getDimensionName(destination.dimension().location().getPath());

                boolean isInstant = isInstant(message);
                if (!isInstant) {
                    spawnDoor(player, x, y, z, destination);
                    event.getPlayer().displayClientMessage(Component.literal("Door created leading to " + x + ", " + y + ", " + z + ", in The " + dimensionName + " Dimension").withStyle(BeyonderUtil.getStyle(player)), true);
                } else {
                    BeyonderUtil.teleportEntity(player, destination, x, y, z);
                    event.getPlayer().displayClientMessage(Component.literal("Teleported to " + x + ", " + y + ", " + z + ", in The " + dimensionName + " Dimension").withStyle(BeyonderUtil.getStyle(player)), true);
                }
                BeyonderUtil.useSpirituality(player, 300 * (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.TRAVELERSDOOR.get()));
                event.setCanceled(true);
                return;
            }
            Player targetPlayer = null;
            for (Player serverPlayer : level.players()) {
                if (serverPlayer.getName().getString().toLowerCase().equals(message.toLowerCase())) {
                    targetPlayer = serverPlayer;
                    break;
                }
            }
            if (targetPlayer != null) {
                if (BeyonderUtil.areAllies(targetPlayer, event.getPlayer())) {
                    int x = (int) targetPlayer.getX();
                    int y = (int) targetPlayer.getY();
                    int z = (int) targetPlayer.getZ();
                    Level destination = targetPlayer.level();

                    if (!canTeleportAcrossDimensions(player, destination)) {
                        event.getPlayer().displayClientMessage(Component.literal("Target is in an inaccessible dimension").withStyle(ChatFormatting.RED), true);
                        event.setCanceled(true);
                        return;
                    }

                    String dimensionName = getDimensionName(destination.dimension().location().getPath());

                    boolean isInstant = isInstantPlayer(message);
                    if (!isInstant) {
                        spawnDoor(player, x, y, z, destination);
                        event.getPlayer().displayClientMessage(Component.literal("Door created leading to " + targetPlayer.getName() + " in The " + dimensionName + " Dimension").withStyle(BeyonderUtil.getStyle(player)), true);
                    } else {
                        BeyonderUtil.teleportEntity(player, targetPlayer.level(), x, y, z);
                        event.getPlayer().displayClientMessage(Component.literal("Teleported to " + targetPlayer.getName() + " in The " + dimensionName + " Dimension").withStyle(BeyonderUtil.getStyle(player)), true);
                    }
                    BeyonderUtil.useSpirituality(player, 300 * (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.TRAVELERSDOOR.get()));
                    event.getPlayer().displayClientMessage(Component.literal("Teleported to " + targetPlayer.getName().getString()).withStyle(BeyonderUtil.getStyle(player)), true);
                } else {
                    event.getPlayer().displayClientMessage(Component.literal("Player is not your ally").withStyle(BeyonderUtil.getStyle(player)), true);
                }
            } else {
                event.getPlayer().displayClientMessage(Component.literal("Invalid coordinates or player name: " + message).withStyle(BeyonderUtil.getStyle(player)), true);
            }
            event.setCanceled(true);
        }
        if (!player.level().isClientSide && player.getMainHandItem().getItem() instanceof TravelersDoorWaypoint && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.APPRENTICE.get(), 5)) {
            TravelersDoorWaypoint.setWaypointName(player, message);
        }
        ItemStack heldItem = player.getMainHandItem();
        if (!heldItem.isEmpty() && heldItem.getItem() instanceof Prophecy && !player.getCooldowns().isOnCooldown(ItemInit.PROPHECY.get())) {
            if (BeyonderUtil.getSpirituality(player) >= 1500) {
                Matcher matcher = SpectatorClass.PROPHECY_PATTERN.matcher(message);
                if (matcher.matches()) {
                    String targetPlayerName = matcher.group(1);
                    String eventDescription = matcher.group(2);
                    int timeValue = Integer.parseInt(matcher.group(3));
                    String timeUnit = matcher.group(4).toLowerCase();
                    int ticksPerSecond = 20;
                    int ticks;
                    if (timeUnit.startsWith("second")) {
                        ticks = timeValue * ticksPerSecond;
                    } else {
                        ticks = timeValue * 60 * ticksPerSecond;
                    }
                    String tagKey = null;
                    for (Map.Entry<String, String> entry : EVENT_TO_TAG.entrySet()) {
                        if (eventDescription.equals(entry.getKey())) {
                            tagKey = entry.getValue();
                            break;
                        }
                    }

                    if (tagKey != null) {
                        Optional<ServerPlayer> targetPlayer = level.getServer().getPlayerList().getPlayers().stream().filter(p -> p.getName().getString().equals(targetPlayerName)).findFirst();
                        if (targetPlayer.isPresent()) {
                            player.getCooldowns().addCooldown(ItemInit.PROPHECY.get(), 1200);
                            CompoundTag tag = targetPlayer.get().getPersistentData();
                            tag.putInt(tagKey, ticks);
                            EventManager.removeFromRegularLoop(targetPlayer.get(), EFunctions.SPECTATORPROPHECY.get());
                            player.sendSystemMessage(Component.literal("Prophecy has been set for " + targetPlayerName).withStyle(ChatFormatting.GREEN));
                            BeyonderUtil.useSpirituality(player, 1500);
                        } else {
                            player.sendSystemMessage(Component.literal("Could not find player: " + targetPlayerName).withStyle(ChatFormatting.RED));
                        }
                    } else {
                        for (String description : EVENT_TO_TAG.keySet()) {
                            player.sendSystemMessage(Component.literal("• " + description).withStyle(ChatFormatting.YELLOW));
                        }
                        player.sendSystemMessage(Component.literal("Unknown prophecy. Known prophecy types are above").withStyle(ChatFormatting.RED));

                    }
                } else {
                    player.sendSystemMessage(Component.literal("Prophecy written incorrectly. Should be put in the format of (Player) will (event) in (number) (minutes/seconds).").withStyle(ChatFormatting.RED));
                }
                event.setCanceled(true);
            } else {
                player.displayClientMessage(Component.literal("You require 1500 Spirituality").withStyle(ChatFormatting.RED), true);
            }
        }
        if (!heldItem.isEmpty() && heldItem.getItem() instanceof SeparateWormOfStar && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.APPRENTICE.get(), 4)) {
            int wormOfStarSeparationAmount = Integer.parseInt(message.trim());
            if (wormOfStarSeparationAmount <= 0) {
                player.displayClientMessage(Component.literal("Please enter a positive number!").withStyle(ChatFormatting.RED), true);
                event.setCanceled(true);
                return;
            }
            CompoundTag tag = player.getPersistentData();
            int wormOfStarAmount = tag.getInt("wormOfStar");
            if (wormOfStarSeparationAmount > wormOfStarAmount) {
                player.displayClientMessage(Component.literal("You don't have enough Worms of Star! You have: " + wormOfStarAmount).withStyle(ChatFormatting.BLUE), true);
                event.setCanceled(true); // Cancel the chat message
                return;
            }
            tag.putInt("wormOfStar", wormOfStarAmount - wormOfStarSeparationAmount);
            LOTMNetworkHandler.sendToPlayer(new ClientWormOfStarDataS2C(tag.getInt("wormOfStar")), player);
            DimensionalSightTileEntity dimensionalSightTileEntity = BeyonderUtil.findNearbyDimensionalSight(player);
            if (dimensionalSightTileEntity != null && dimensionalSightTileEntity.getScryTarget() != null && dimensionalSightTileEntity.getScryTarget() instanceof Player pPlayer) {
                ItemStack wormStack = new ItemStack(ItemInit.WORM_OF_STAR.get(), wormOfStarSeparationAmount);
                pPlayer.getInventory().add(wormStack);
                player.sendSystemMessage(Component.literal("You gave " + wormOfStarSeparationAmount +  "Worm of Stars to your Dimensional Sight Target").withStyle(ChatFormatting.AQUA));
                if (!wormStack.isEmpty()) {
                    player.drop(wormStack, false);
                }
            } else {
                ItemStack wormStack = new ItemStack(ItemInit.WORM_OF_STAR.get(), wormOfStarSeparationAmount);
                player.getInventory().add(wormStack);
                if (!wormStack.isEmpty()) {
                    player.drop(wormStack, false);
                }
            }

            player.displayClientMessage(Component.literal("Successfully separated " + wormOfStarSeparationAmount + " Worms of Star!").withStyle(ChatFormatting.BLUE), true);
            event.setCanceled(true);
        }
        if (!player.level().isClientSide() && player.getMainHandItem().getItem() instanceof DimensionalSight && !player.getCooldowns().isOnCooldown(ItemInit.DIMENSIONAL_SIGHT.get()) && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.APPRENTICE.get(), 3)) {
            for (ServerPlayer onlinePlayer : player.getServer().getPlayerList().getPlayers()) {
                if (message.equalsIgnoreCase(onlinePlayer.getName().getString())) {
                    BlockPos playerPos = player.blockPosition();
                    Vec3 lookPos = player.getLookAngle().scale(5);
                    BlockPos targetPos = new BlockPos(playerPos.offset((int) lookPos.x(), (int) lookPos.y() - 2, (int) lookPos.z()));
                    BlockState dimensionalSightState = BlockInit.DIMENSIONAL_SIGHT.get().defaultBlockState();
                    level.setBlock(targetPos, dimensionalSightState, 3);
                    level.getServer().execute(() -> {
                        BlockEntity blockEntity = level.getBlockEntity(targetPos);
                        if (blockEntity instanceof DimensionalSightTileEntity sightEntity) {
                            sightEntity.setCaster(player.getUUID());
                            sightEntity.viewTarget = onlinePlayer.getName().getString();
                            sightEntity.scryUniqueID = onlinePlayer.getUUID();
                            sightEntity.setChanged();
                            sightEntity.sendUpdates();
                            onlinePlayer.getPersistentData().putUUID("dimensionalSightPlayerUUID", player.getUUID());
                            onlinePlayer.getPersistentData().putInt("ignoreShouldntRender", 10);
                            player.displayClientMessage(Component.literal("Successfully created a Dimensional Sight for " + onlinePlayer.getName().getString()).withStyle(ChatFormatting.GREEN), true);
                        }
                    });
                    player.getCooldowns().addCooldown(ItemInit.DIMENSIONAL_SIGHT.get(), 6000);
                }
            }
            event.setCanceled(true);
        }
        if (!player.level().isClientSide() && player.getMainHandItem().getItem() instanceof DimensionalSight && !player.getCooldowns().isOnCooldown(ItemInit.DIMENSIONAL_SIGHT.get()) && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.APPRENTICE.get(), 3)) {
            for (ServerPlayer onlinePlayer : player.getServer().getPlayerList().getPlayers()) {
                if (message.equalsIgnoreCase(onlinePlayer.getName().getString())) {
                    BlockPos playerPos = player.blockPosition();
                    Vec3 lookPos = player.getLookAngle().scale(5);
                    BlockPos targetPos = new BlockPos(playerPos.offset((int) lookPos.x(), -2, (int) lookPos.z()));
                    BlockState dimensionalSightState = BlockInit.DIMENSIONAL_SIGHT.get().defaultBlockState();
                    level.setBlock(targetPos, dimensionalSightState, 3);
                    level.getServer().execute(() -> {
                        BlockEntity blockEntity = level.getBlockEntity(targetPos);
                        if (blockEntity instanceof DimensionalSightTileEntity sightEntity) {
                            sightEntity.setCaster(player.getUUID());
                            sightEntity.viewTarget = onlinePlayer.getName().getString();
                            sightEntity.scryUniqueID = onlinePlayer.getUUID();
                            sightEntity.setChanged();
                            sightEntity.sendUpdates();
                            onlinePlayer.getPersistentData().putUUID("dimensionalSightPlayerUUID", player.getUUID());
                            onlinePlayer.getPersistentData().putInt("ignoreShouldntRender", 10);
                            player.displayClientMessage(Component.literal("Successfully created a Dimensional Sight for " + onlinePlayer.getName().getString()).withStyle(ChatFormatting.GREEN), true);
                        }
                    });
                }
            }
            event.setCanceled(true);
        }
        if (!player.level().isClientSide() && player.getMainHandItem().getItem() instanceof Teleportation && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.APPRENTICE.get(), 2)) {
            if (!BeyonderUtil.currentPathwayMatches(player, BeyonderClassInit.APPRENTICE.get())) {
                player.displayClientMessage(Component.literal("You are not of the Apprentice pathway").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
                event.setCanceled(true);
                return;
            }
            if (BeyonderUtil.getSpirituality(player) < BeyonderUtil.getDamage(player).get(ItemInit.TELEPORTATION.get())) {
                player.displayClientMessage(Component.literal("You need " + BeyonderUtil.getDamage(player).get(ItemInit.TELEPORTATION.get()) + " spirituality in order to use this").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
                event.setCanceled(true);
                return;
            }
            if (isThreeIntegers(message)) {
                String[] coordinates = message.replace(",", " ").trim().split("\\s+");
                int x = Integer.parseInt(coordinates[0]);
                int y = Integer.parseInt(coordinates[1]);
                int z = Integer.parseInt(coordinates[2]);

                PlayerMobEntity playerMobEntity = flickeringCopy(player);
                DimensionalSightTileEntity dimensionalSightTileEntity = BeyonderUtil.findNearbyDimensionalSight(player);
                if (dimensionalSightTileEntity != null && dimensionalSightTileEntity.getScryTarget() != null) {
                    playerMobEntity.teleportTo(dimensionalSightTileEntity.getScryTarget().getX(), dimensionalSightTileEntity.getScryTarget().getY(), dimensionalSightTileEntity.getScryTarget().getZ());
                } else {
                    playerMobEntity.teleportTo(x,y,z);
                }
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    playerMobEntity.setItemSlot(slot, player.getItemBySlot(slot).copy());
                }
                if (player.getPersistentData().getInt("LOTMinCombat") >= 1) {
                    playerMobEntity.setTarget(player.getLastHurtMob());
                }
                playerMobEntity.setCreator(player);
                playerMobEntity.setUsername(player.getScoreboardName());
                playerMobEntity.setIsClone(true);
                playerMobEntity.setMaxSpirituality(BeyonderClassInit.APPRENTICE.get().spiritualityLevels().get(BeyonderUtil.getSequence(player)));
                playerMobEntity.setSpirituality(BeyonderClassInit.APPRENTICE.get().spiritualityLevels().get(BeyonderUtil.getSequence(player)));
                playerMobEntity.setRegenSpirituality(false);
                playerMobEntity.setAttackChance(100);
                if (playerMobEntity.distanceTo(player) < 50 && player.getPersistentData().getInt("LOTMinCombat") >= 1) {
                    playerMobEntity.setTarget(player.getLastHurtMob());
                } else {
                    for (LivingEntity living : playerMobEntity.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(30))) {
                        if (BeyonderUtil.areAllies(living, player) && living.getPersistentData().getInt("LOTMinCombat") >= 1) {
                            playerMobEntity.setTarget(player.getLastHurtMob());
                        }
                    }
                }
                player.level().addFreshEntity(playerMobEntity);
                event.getPlayer().displayClientMessage(Component.literal("You're currently flickering at " + x + ", " + y + ", " + z).withStyle(BeyonderUtil.getStyle(player)), true);
                BeyonderUtil.useSpirituality(player, (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.TELEPORTATION.get()));
                event.setCanceled(true);
                return;
            }
            Player targetPlayer = null;
            for (Player serverPlayer : level.players()) {
                if (serverPlayer.getName().getString().toLowerCase().equals(message.toLowerCase())) {
                    targetPlayer = serverPlayer;
                    break;
                }
            }
            if (targetPlayer != null) {
                int x = (int) targetPlayer.getX();
                int y = (int) targetPlayer.getY();
                int z = (int) targetPlayer.getZ();
                PlayerMobEntity playerMobEntity = flickeringCopy(player);
                DimensionalSightTileEntity dimensionalSightTileEntity = BeyonderUtil.findNearbyDimensionalSight(player);
                if (dimensionalSightTileEntity != null && dimensionalSightTileEntity.getScryTarget() != null) {
                    playerMobEntity.teleportTo(dimensionalSightTileEntity.getScryTarget().getX(), dimensionalSightTileEntity.getScryTarget().getY(), dimensionalSightTileEntity.getScryTarget().getZ());
                } else {
                    playerMobEntity.teleportTo(x,y,z);
                }
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    playerMobEntity.setItemSlot(slot, player.getItemBySlot(slot).copy());
                }
                if (player.getPersistentData().getInt("LOTMinCombat") >= 1) {
                    playerMobEntity.setTarget(player.getLastHurtMob());
                }
                playerMobEntity.setCreator(player);
                playerMobEntity.setUsername(player.getScoreboardName());
                playerMobEntity.setIsClone(true);
                playerMobEntity.setMaxSpirituality(BeyonderClassInit.APPRENTICE.get().spiritualityLevels().get(BeyonderUtil.getSequence(player)));
                playerMobEntity.setSpirituality(BeyonderClassInit.APPRENTICE.get().spiritualityLevels().get(BeyonderUtil.getSequence(player)));
                playerMobEntity.setRegenSpirituality(false);
                playerMobEntity.setAttackChance(100);
                player.level().addFreshEntity(playerMobEntity);
                event.getPlayer().displayClientMessage(Component.literal("You're currently flickering at " + x + ", " + y + ", " + z).withStyle(BeyonderUtil.getStyle(player)), true);
                BeyonderUtil.useSpirituality(player, (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.TELEPORTATION.get()));
            } else {
                event.getPlayer().displayClientMessage(Component.literal("Invalid coordinates or player name: " + message).withStyle(BeyonderUtil.getStyle(player)), true);
            }
            event.setCanceled(true);
        }
    }
    private static int tickCounter = 0;
    private static final int CLEANUP_INTERVAL = 2400;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;
            if (tickCounter >= CLEANUP_INTERVAL) {
                tickCounter = 0;
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                if (server != null) {
                    for (ServerLevel level : server.getAllLevels()) {
                        PlayerMobTracker tracker = PlayerMobTracker.get(level);
                        tracker.cleanupMissingEntities(level);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof PlayerMobEntity playerMob) {
            PlayerMobTracker tracker = PlayerMobTracker.get((ServerLevel) event.getLevel());
            tracker.addPlayerMob(playerMob);
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof PlayerMobEntity playerMob) {
            if (!playerMob.isAlive() || playerMob.isRemoved()) {
                PlayerMobTracker tracker = PlayerMobTracker.get((ServerLevel) event.getLevel());
                tracker.removePlayerMob(playerMob.getUUID(), (ServerLevel) event.getLevel());
            }
        }
    }
}
