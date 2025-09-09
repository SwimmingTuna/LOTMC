package net.swimmingtuna.lotm.item.OtherItems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.entity.SpaceRiftEntity;
import net.swimmingtuna.lotm.entity.SpatialCageEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.PositionUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Astrolabe extends Item {

    public Astrolabe(Properties pProperties) {
        super(pProperties.durability(2000));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        if (!pLevel.isClientSide()) {
            for (LivingEntity living : BeyonderUtil.getNonAlliesNearby(pPlayer, 30)) {
                int totalDamage = 10 - Math.min(10, BeyonderUtil.getSequence(living));
                if (totalDamage > 0) {
                    stack.hurtAndBreak(totalDamage, pPlayer, (player) -> {
                        player.broadcastBreakEvent(pHand);
                    });
                }
            }
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return pStack.isDamaged();
    }

    private static Registry<Structure> getStructureRegistry(ServerLevel level) {
        return level.registryAccess().registryOrThrow(Registries.STRUCTURE);
    }

    public static void astrolabeChatMessage(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getMessage().getString();

        // Check if player is holding astrolabe OR message starts with "Position: "
        boolean holdingAstrolabe = !player.level().isClientSide() && player.getMainHandItem().getItem() == ItemInit.ASTROLABE.get();
        boolean hasPositionPrefix = message.startsWith("Position: ");

        if (holdingAstrolabe || hasPositionPrefix) {
            String searchQuery;

            if (hasPositionPrefix) {
                searchQuery = message.substring(10).toLowerCase();
            } else {
                searchQuery = message.toLowerCase();
            }
            CompoundTag tag = player.getPersistentData();
            boolean foundResource = false;
            String resourceKey = searchQuery.replace(' ', '_');
            int maxDistance = (int) (BeyonderUtil.getDivination(player) * 30.0);
            ResourceLocation resourceLocation;

            try {
                resourceLocation = new ResourceLocation(resourceKey);
                if (ForgeRegistries.BLOCKS.containsKey(resourceLocation)) {
                    foundResource = true;
                    Block targetBlock = ForgeRegistries.BLOCKS.getValue(resourceLocation);
                    ServerLevel level = (ServerLevel) player.level();
                    BlockPos nearestBlockPos = PositionUtils.getNearestBlock(level, player, Collections.singletonList(targetBlock.asItem()), maxDistance / 40.0
                    );

                    if (nearestBlockPos != null) {
                        int nearestX = nearestBlockPos.getX();
                        int nearestY = nearestBlockPos.getY();
                        int nearestZ = nearestBlockPos.getZ();
                        player.sendSystemMessage(Component.literal(targetBlock.getName().getString() + " is at: ")
                                .withStyle(ChatFormatting.LIGHT_PURPLE)
                                .append(Component.literal(nearestX + ", " + nearestY + ", " + nearestZ)
                                        .withStyle(ChatFormatting.GREEN)));
                        BeyonderUtil.useSpirituality(player, maxDistance * 3);
                    } else {
                        player.sendSystemMessage(Component.literal("No " + targetBlock.getName().getString() + " found").withStyle(ChatFormatting.RED));
                    }
                }

                // Handle entity searching using PositionUtils
                if (ForgeRegistries.ENTITY_TYPES.containsKey(resourceLocation)) {
                    foundResource = true;
                    EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(resourceLocation);
                    ServerLevel level = (ServerLevel) player.level();

                    // Get all entities in radius using PositionUtils
                    List<Entity> nearbyEntities = PositionUtils.getEntitiesInRadius(
                            level,
                            player.blockPosition(),
                            Entity.class,
                            maxDistance
                    );

                    // Find the nearest entity of the specified type
                    Entity nearestEntity = nearbyEntities.stream()
                            .filter(entity -> entity.getType() == entityType)
                            .filter(entity -> !entity.equals(player)) // Exclude the searching player
                            .min((e1, e2) -> Double.compare(
                                    player.distanceToSqr(e1),
                                    player.distanceToSqr(e2)
                            ))
                            .orElse(null);

                    if (nearestEntity != null) {
                        int nearestX = (int) nearestEntity.getX();
                        int nearestY = (int) nearestEntity.getY();
                        int nearestZ = (int) nearestEntity.getZ();
                        BeyonderUtil.useSpirituality(player, maxDistance * 4);
                        player.sendSystemMessage(Component.literal(entityType.getDescription().getString() + " is at: ")
                                .withStyle(ChatFormatting.LIGHT_PURPLE)
                                .append(Component.literal(nearestX + ", " + nearestY + ", " + nearestZ)
                                        .withStyle(ChatFormatting.GREEN)));
                    } else {
                        player.sendSystemMessage(Component.literal("No " + entityType.getDescription().getString() + " found within range").withStyle(ChatFormatting.RED));
                    }
                }

                // Handle player divination
                MinecraftServer server = player.getServer();
                if (server != null) {
                    List<ServerPlayer> players = server.getPlayerList().getPlayers();
                    for (ServerPlayer targetPlayer : players) {
                        if (searchQuery.contains(targetPlayer.getGameProfile().getName().toLowerCase())) {
                            foundResource = handlePlayerDivination(player, targetPlayer, searchQuery, tag, maxDistance, hasPositionPrefix);
                            if (foundResource) break;
                        }
                    }
                }

                // Handle biome searching
                if (!foundResource) {
                    foundResource = handleBiomeSearch(player, searchQuery, maxDistance);
                }

                // Handle structure searching
                if (!foundResource && player.level() instanceof ServerLevel serverLevel) {
                    foundResource = handleStructureSearch(player, serverLevel, resourceLocation, maxDistance);
                }

                if (!foundResource) {
                    player.sendSystemMessage(Component.literal("No divination target found with " + searchQuery)
                            .withStyle(ChatFormatting.RED));
                } else {
                    BeyonderUtil.useSpirituality(player, maxDistance * 2);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            event.setCanceled(true);
        }
    }

    private static boolean handlePlayerDivination(ServerPlayer player, ServerPlayer targetPlayer, String message, CompoundTag tag, int maxDistance, boolean position) {
        boolean spatialIntegration = true;
        if (BeyonderUtil.currentPathwayAndSequenceMatchesNoException(targetPlayer, BeyonderClassInit.APPRENTICE.get(), 3)) {
            if (BeyonderUtil.getSequence(player) != 0 && BeyonderUtil.getSequence(player) != 1) {
                spatialIntegration = false;
            }
        }

        if (!spatialIntegration) {
            player.sendSystemMessage(Component.literal("The target can't have divination used against them at your sequence")
                    .withStyle(ChatFormatting.RED));
            return true;
        }

        // Use PositionUtils to calculate distance
        double distance = PositionUtils.getDistance(player.blockPosition(), targetPlayer.blockPosition());

        if (distance > maxDistance * 10 || BeyonderUtil.getAntiDivination(targetPlayer) > BeyonderUtil.getDivination(player)) {
            player.sendSystemMessage(Component.literal(targetPlayer.getName().getString() +
                    " is too far away or has anti-divination too high for you"));
            return true;
        }
        int amountToCorrupt = (BeyonderUtil.getSequence(player) - BeyonderUtil.getSequence(targetPlayer) * 25);
        if (message.contains("sequence")) {
            handleSequenceDivination(player, targetPlayer, tag, amountToCorrupt);
        } else if (message.contains("location") || message.contains("coordinates")) {
            handleLocationDivination(player, targetPlayer, tag, amountToCorrupt);
        } else if (message.contains("inventory") || message.contains("item")) {
            handleInventoryDivination(player, targetPlayer, tag, amountToCorrupt);
        } else if (message.contains("health")) {
            handleHealthDivination(player, targetPlayer, tag, amountToCorrupt);
        } else if (message.contains("pathway")) {
            handlePathwayDivination(player, targetPlayer, tag, amountToCorrupt);
        } else if (message.contains("luck") || message.contains("fortune") ||
                message.contains("unluck") || message.contains("misfortune")) {
            handleLuckDivination(player, targetPlayer, tag, amountToCorrupt);
        }
        if (position) {
            handleLocationDivination(player, targetPlayer, tag, 0);
            if (message.toLowerCase().contains("tear")) {
                SpaceRiftEntity rift = new SpaceRiftEntity(EntityInit.SPACE_RIFT_ENTITY.get(), targetPlayer.level());
                rift.setOwner(player);
                rift.teleportTo(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());
                BeyonderUtil.setScale(rift, 6 - BeyonderUtil.getSequence(player));
                rift.setMaxLife((int) (float) BeyonderUtil.getDamage(player).get(ItemInit.SPATIAL_TEARING.get()));
                targetPlayer.level().addFreshEntity(rift);
            } else if (message.toLowerCase().contains("to me")) {
                Level destination = player.level();
                BeyonderUtil.teleportEntity(targetPlayer, destination, player.getX(), player.getY(), player.getZ());
            } else if (message.toLowerCase().contains("to them")) {
                Level destination = targetPlayer.level();
                BeyonderUtil.teleportEntity(player, destination, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());
            } else if (message.toLowerCase().contains("seal")) {
                SpatialCageEntity.setSealed(targetPlayer, player, BeyonderUtil.getSequence(player) - 1, (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.SPATIAL_CAGE.get()));
            }
        }
        BeyonderUtil.useSpirituality(player, maxDistance * 5);
        return true;
    }

    private static void handleSequenceDivination(ServerPlayer player, ServerPlayer targetPlayer, CompoundTag tag, int amountToCorrupt) {
        if (shouldApplyCorruption(player, targetPlayer)) {
            tag.putDouble("corruption", tag.getDouble("corruption") + amountToCorrupt);
            targetPlayer.sendSystemMessage(Component.literal(player.getName().getString() + " divined about you")
                    .withStyle(ChatFormatting.RED));
        }
        player.sendSystemMessage(Component.literal(targetPlayer.getName().getString() + "'s sequence is " +
                BeyonderUtil.getSequence(targetPlayer)));
    }

    private static void handleLocationDivination(ServerPlayer player, ServerPlayer targetPlayer, CompoundTag tag, int amountToCorrupt) {
        if (shouldApplyCorruption(player, targetPlayer)) {
            tag.putDouble("corruption", tag.getDouble("corruption") + amountToCorrupt);
            targetPlayer.sendSystemMessage(Component.literal(player.getName().getString() + " divined about you")
                    .withStyle(ChatFormatting.RED));
        }
        int targetX = (int) targetPlayer.getX();
        int targetY = (int) targetPlayer.getY();
        int targetZ = (int) targetPlayer.getZ();
    }

    private static void handleInventoryDivination(ServerPlayer player, ServerPlayer targetPlayer, CompoundTag tag, int amountToCorrupt) {
        if (shouldApplyCorruption(player, targetPlayer)) {
            tag.putDouble("corruption", tag.getDouble("corruption") + amountToCorrupt * 2);
            targetPlayer.sendSystemMessage(Component.literal(player.getName().getString() + " divined about you")
                    .withStyle(ChatFormatting.RED));
        }

        StringBuilder inventoryMessage = new StringBuilder();
        boolean hasItems = false;
        for (int i = 0; i < targetPlayer.getInventory().getContainerSize(); i++) {
            ItemStack itemStack = targetPlayer.getInventory().getItem(i);
            if (!itemStack.isEmpty()) {
                hasItems = true;
                inventoryMessage.append("\n- ").append(itemStack.getDisplayName().getString());
            }
        }

        if (hasItems) {
            String playerName = targetPlayer.getName().getString();
            player.sendSystemMessage(Component.literal(playerName + "'s inventory contains:")
                    .withStyle(ChatFormatting.BOLD)
                    .append(Component.literal(inventoryMessage.toString()).withStyle(ChatFormatting.AQUA)));
        } else {
            player.sendSystemMessage(Component.literal("The target player's inventory is empty.")
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    private static void handleHealthDivination(ServerPlayer player, ServerPlayer targetPlayer, CompoundTag tag, int amountToCorrupt) {
        if (shouldApplyCorruption(player, targetPlayer)) {
            tag.putDouble("corruption", tag.getDouble("corruption") + amountToCorrupt);
        }
        player.sendSystemMessage(Component.literal(targetPlayer.getName().getString() + "'s health is " +
                targetPlayer.getHealth()));
    }

    private static void handlePathwayDivination(ServerPlayer player, ServerPlayer targetPlayer, CompoundTag tag, int amountToCorrupt) {
        if (BeyonderUtil.getPathway(targetPlayer) != null) {
            if (shouldApplyCorruption(player, targetPlayer)) {
                tag.putDouble("corruption", tag.getDouble("corruption") + amountToCorrupt * 0.75);
                targetPlayer.sendSystemMessage(Component.literal(player.getName().getString() + " divined about you")
                        .withStyle(ChatFormatting.RED));
            }
            player.sendSystemMessage(Component.literal(targetPlayer.getName().getString() + "'s pathway is " +
                    BeyonderUtil.getPathway(targetPlayer).toString()));
        } else {
            player.sendSystemMessage(Component.literal(targetPlayer.getName().getString() + " has no pathway"));
        }
    }

    private static void handleLuckDivination(ServerPlayer player, ServerPlayer targetPlayer, CompoundTag tag, int amountToCorrupt) {
        if (shouldApplyCorruption(player, targetPlayer)) {
            tag.putDouble("corruption", tag.getDouble("corruption") + amountToCorrupt * 0.75);
            targetPlayer.sendSystemMessage(Component.literal(player.getName().getString() + " divined about you")
                    .withStyle(ChatFormatting.RED));
        }
        player.sendSystemMessage(Component.literal(targetPlayer.getName().getString() +
                "'s luck and misfortune is " + targetPlayer.getPersistentData().getDouble("luck") +
                " luck and " + targetPlayer.getPersistentData().getDouble("misfortune") + " misfortune"));
    }

    private static boolean shouldApplyCorruption(ServerPlayer player, ServerPlayer targetPlayer) {
        return (BeyonderUtil.getSequence(player) > 4 && BeyonderUtil.getSequence(targetPlayer) <= 4) ||
                (BeyonderUtil.getSequence(targetPlayer) == 0 && BeyonderUtil.getSequence(player) > 1);
    }

    private static boolean handleBiomeSearch(ServerPlayer player, String message, int maxDistance) {
        Registry<Biome> biomeRegistry = player.level().registryAccess().registryOrThrow(Registries.BIOME);
        ResourceKey<Biome> targetBiomeKey = null;

        for (Map.Entry<ResourceKey<Biome>, Biome> entry : biomeRegistry.entrySet()) {
            ResourceKey<Biome> biomeKey = entry.getKey();
            ResourceLocation biomeId = biomeKey.location();
            String path = biomeId.getPath().toLowerCase(Locale.ROOT);
            String namespaceAndPath = biomeId.toString().toLowerCase(Locale.ROOT);

            if (path.equals(message) || namespaceAndPath.equals(message)) {
                targetBiomeKey = biomeKey;
                break;
            }
        }

        if (targetBiomeKey != null) {
            BlockPos origin = player.blockPosition();
            int radius = maxDistance;
            int step = 16; // Chunk step to make it not too heavy
            BlockPos closest = null;
            double closestDist = Double.MAX_VALUE;

            for (int dx = -radius; dx <= radius; dx += step) {
                for (int dz = -radius; dz <= radius; dz += step) {
                    BlockPos checkPos = origin.offset(dx, 0, dz);
                    ResourceKey<Biome> biomeAtPosKey = player.level().getBiome(checkPos).unwrapKey().orElse(null);
                    if (biomeAtPosKey != null && biomeAtPosKey.equals(targetBiomeKey)) {
                        double distSq = checkPos.distSqr(origin);
                        if (distSq < closestDist) {
                            closest = checkPos;
                            closestDist = distSq;
                        }
                    }
                }
            }

            if (closest != null) {
                String biomeName = Component.translatable("biome." + targetBiomeKey.location().getNamespace() +
                        "." + targetBiomeKey.location().getPath()).getString();
                BeyonderUtil.useSpirituality(player, maxDistance * 4);
                player.sendSystemMessage(Component.literal("Nearest " + biomeName + " found at: ")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                        .append(closest.toShortString()).withStyle(ChatFormatting.GREEN));
            } else {
                player.sendSystemMessage(Component.literal("Biome found but no nearby instance within 500 blocks.")
                        .withStyle(ChatFormatting.RED));
            }
            return true;
        }
        return false;
    }

    private static boolean handleStructureSearch(ServerPlayer player, ServerLevel serverLevel, ResourceLocation resourceLocation, int maxDistance) {
        Registry<Structure> structureRegistry = getStructureRegistry(serverLevel);
        ResourceKey<Structure> structureResourceKey = ResourceKey.create(Registries.STRUCTURE, resourceLocation);
        TagKey<Structure> structureTagKey = TagKey.create(Registries.STRUCTURE, resourceLocation);

        boolean isTag = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE).containsKey(structureTagKey.location());
        boolean isResource = structureRegistry.containsKey(structureResourceKey);

        if (isResource) {
            return handleExactStructureSearch(player, serverLevel, structureResourceKey, maxDistance);
        } else if (isTag) {
            return handleStructureTagSearch(player, serverLevel, structureTagKey, maxDistance);
        } else {
            return handlePartialStructureSearch(player, serverLevel, structureRegistry, resourceLocation, maxDistance);
        }
    }

    private static boolean handleExactStructureSearch(ServerPlayer player, ServerLevel serverLevel, ResourceKey<Structure> structureResourceKey, int maxDistance) {
        int searchRadius = maxDistance * 2;
        BlockPos playerPos = player.blockPosition();
        BlockPos nearestStructurePos = serverLevel.findNearestMapStructure(
                TagKey.create(Registries.STRUCTURE, structureResourceKey.location()),
                playerPos,
                searchRadius,
                false
        );

        if (nearestStructurePos != null) {
            double distance = PositionUtils.getDistance(playerPos, nearestStructurePos);
            String structureName = structureResourceKey.location().getPath().replace('_', ' ');

            if (distance <= searchRadius) {
                BeyonderUtil.useSpirituality(player, maxDistance * 4);
                player.sendSystemMessage(Component.literal("Found " + structureName + " near: ")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                        .append(Component.literal("X: " + nearestStructurePos.getX() + " Z: " + nearestStructurePos.getZ())
                                .withStyle(ChatFormatting.GREEN)));
            } else {
                player.sendSystemMessage(Component.literal("No " + structureName + " found")
                        .withStyle(ChatFormatting.RED));
            }
        } else {
            player.sendSystemMessage(Component.literal("No " + structureResourceKey.location().getPath().replace('_', ' ') + " found")
                    .withStyle(ChatFormatting.RED));
        }
        return true;
    }

    private static boolean handleStructureTagSearch(ServerPlayer player, ServerLevel serverLevel, TagKey<Structure> structureTagKey, int maxDistance) {
        int searchRadius = maxDistance * 2;
        BlockPos playerPos = player.blockPosition();
        HolderSet<Structure> structuresInTag = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE)
                .getTag(structureTagKey).orElse(null);

        if (structuresInTag != null) {
            boolean foundAny = false;
            for (Holder<Structure> structureHolder : structuresInTag) {
                if (structureHolder.unwrapKey().isPresent()) {
                    ResourceKey<Structure> key = structureHolder.unwrapKey().get();
                    TagKey<Structure> structureTypeTag = TagKey.create(Registries.STRUCTURE, key.location());
                    BlockPos nearestPos = serverLevel.findNearestMapStructure(structureTypeTag, playerPos, searchRadius, false);

                    if (nearestPos != null) {
                        foundAny = true;
                        String structureName = key.location().getPath().replace('_', ' ');
                        double distance = PositionUtils.getDistance(playerPos, nearestPos);

                        if (distance <= searchRadius) {
                            BeyonderUtil.useSpirituality(player, maxDistance * 4);
                            player.sendSystemMessage(Component.literal("Found " + structureName + " near: ")
                                    .withStyle(ChatFormatting.LIGHT_PURPLE)
                                    .append(Component.literal("X: " + nearestPos.getX() + " Z: " + nearestPos.getZ())
                                            .withStyle(ChatFormatting.GREEN)));
                        } else {
                            player.sendSystemMessage(Component.literal("No " + structureName + " found")
                                    .withStyle(ChatFormatting.RED));
                        }
                        break;
                    }
                }
            }
            if (!foundAny) {
                player.sendSystemMessage(Component.literal("No structures from tag " +
                        structureTagKey.location().getPath().replace('_', ' ') +
                        " found within " + searchRadius + " blocks").withStyle(ChatFormatting.RED));
            }
        }
        return true;
    }

    private static boolean handlePartialStructureSearch(ServerPlayer player, ServerLevel serverLevel, Registry<Structure> structureRegistry, ResourceLocation resourceLocation, int maxDistance) {
        String searchName = resourceLocation.getPath().toLowerCase(Locale.ROOT);
        boolean structureFound = false;

        for (Map.Entry<ResourceKey<Structure>, Structure> entry : structureRegistry.entrySet()) {
            ResourceKey<Structure> key = entry.getKey();
            String structurePath = key.location().getPath().toLowerCase(Locale.ROOT);

            if (structurePath.contains(searchName)) {
                structureFound = true;
                BlockPos playerPos = player.blockPosition();
                BlockPos nearestStructurePos = serverLevel.findNearestMapStructure(
                        TagKey.create(Registries.STRUCTURE, key.location()),
                        playerPos,
                        maxDistance * 2,
                        false
                );

                if (nearestStructurePos != null) {
                    String structureName = key.location().getPath().replace('_', ' ');
                    double distance = PositionUtils.getDistance(playerPos, nearestStructurePos);
                    int searchRadius = maxDistance * 2;

                    if (distance <= searchRadius) {
                        BeyonderUtil.useSpirituality(player, maxDistance * 4);
                        player.sendSystemMessage(Component.literal("Found " + structureName + " near: ")
                                .withStyle(ChatFormatting.LIGHT_PURPLE)
                                .append(Component.literal("X: " + nearestStructurePos.getX() + " Z: " + nearestStructurePos.getZ())
                                        .withStyle(ChatFormatting.GREEN)));
                    } else {
                        player.sendSystemMessage(Component.literal("No " + structureName + " found")
                                .withStyle(ChatFormatting.RED));
                    }
                    break;
                }
            }
        }
        return structureFound;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Use in order to gauge the danger around you, with the more damage this item takes, the higher the danger level.")
                .withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.literal("Type in a biome, structure, entity name, or block to get it's location.")
                .withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD));
        tooltipComponents.add(Component.literal("For modded structures, blocks, biomes, or entities, you need to type the untranslated name. For example, lotm:corpse_cathedral")
                .withStyle(ChatFormatting.AQUA).withStyle(ChatFormatting.BOLD));
        tooltipComponents.add(Component.literal("You can also type in a player's name followed by either (sequence, location, inventory, luck, misfortune, health, or pathway) to get that data")
                .withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD));
        tooltipComponents.add(Component.literal("Be warned, if you try to divine information about a player who is many sequences above you, they might know.")
                .withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}