package net.swimmingtuna.lotm.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Ability;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.ClearAbilitiesS2C;
import net.swimmingtuna.lotm.networking.packet.SyncAbilitiesS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.world.worlddata.BeyonderRecipeData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbilityRegisterCommand {

    public static final String REGISTERED_ABILITIES_KEY = "RegisteredAbilities";
    private static final Map<String, Integer> COMBINATION_MAP = new HashMap<>();
    private static final Map<String, String> abilitiesToSync = new HashMap<>();

    private static final DynamicCommandExceptionType NOT_ABILITY = new DynamicCommandExceptionType(o -> Component.literal("Not an ability: " + o));

    static {
        initializeCombinationMap();
    }

    private static void initializeCombinationMap() {
        String[] combinations = {
                "XXXXX", "XXXXO", "XXXOX", "XXXOO", "XXOXX", "XXOXO", "XXOOX", "XXOOO",
                "XOXXX", "XOXXO", "XOXOX", "XOXOO", "XOOXX", "XOOXO", "XOOOX", "XOOOO",
                "OXXXX", "OXXXO", "OXXOX", "OXXOO", "OXOXX", "OXOXO", "OXOOX", "OXOOO",
                "OOXXX", "OOXXO", "OOXOX", "OOXOO", "OOOXX", "OOOXO", "OOOOX", "OOOOO"
        };
        for (int i = 0; i < combinations.length; i++) {
            COMBINATION_MAP.put(combinations[i], i + 1);
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(Commands.literal("abilityput")
                .then(Commands.argument("combination", StringArgumentType.word())
                        .then(Commands.argument("item", ResourceArgument.resource(buildContext, Registries.ITEM))
                                .executes(context -> registerAbility(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "combination"),
                                        ResourceArgument.getResource(context, "item", Registries.ITEM)
                                )))));
        dispatcher.register(Commands.literal("abilityput")
                .then(Commands.literal("load")
                        .executes(AbilityRegisterCommand::loadBeyonderAbilities)));
        dispatcher.register(Commands.literal("abilityput")
                .then(Commands.literal("remove")
                        .then(Commands.argument("item", ResourceArgument.resource(buildContext, Registries.ITEM))
                                .executes(context -> removeAbility(
                                        context,
                                        ResourceArgument.getResource(context, "item", Registries.ITEM)
                                )))));
    }

    private static int registerAbility(CommandSourceStack source, String combination, Holder.Reference<Item> itemReference) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!COMBINATION_MAP.containsKey(combination)) {
            source.sendFailure(Component.literal("Invalid combination. Please use a valid 5-character combination of L and R.").withStyle(ChatFormatting.RED));
            return 0;
        }
        int combinationNumber = COMBINATION_MAP.get(combination);
        List<Item> availableAbilities = BeyonderUtil.getAbilities(player);

        Item item = itemReference.get();
        ResourceLocation resourceLocation = itemReference.key().location();
        if (!(item instanceof Ability)) {
            throw NOT_ABILITY.create(resourceLocation);
        }
        if (!availableAbilities.contains(item)) {
            source.sendFailure(Component.literal("Ability not available: " + itemReference).withStyle(ChatFormatting.RED));
            return 0;
        }

        CompoundTag tag = player.getPersistentData();
        CompoundTag registeredAbilities;
        if (tag.contains(REGISTERED_ABILITIES_KEY, Tag.TAG_COMPOUND)) {
            registeredAbilities = tag.getCompound(REGISTERED_ABILITIES_KEY);
        } else {
            registeredAbilities = new CompoundTag();
            tag.put(REGISTERED_ABILITIES_KEY, registeredAbilities);
        }

        registeredAbilities.putString(String.valueOf(combinationNumber), resourceLocation.toString());
        tag.put(REGISTERED_ABILITIES_KEY, registeredAbilities);
        source.sendSuccess(() -> Component.literal("Added ability: ").append(Component.translatable(item.getDescriptionId())).append(Component.literal(" for combination " + combination).withStyle(ChatFormatting.GREEN)), true);

        return 1;
    }

    private static int removeAbility(CommandContext<CommandSourceStack> context, Holder.Reference<Item> itemReference) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Item item = itemReference.get();
        ResourceLocation resourceLocation = itemReference.key().location();

        if (!(item instanceof Ability)) {
            throw NOT_ABILITY.create(resourceLocation);
        }

        CompoundTag tag = player.getPersistentData();
        if (!tag.contains(REGISTERED_ABILITIES_KEY, Tag.TAG_COMPOUND)) {
            context.getSource().sendFailure(Component.literal("No abilities registered.").withStyle(ChatFormatting.RED));
            return 0;
        }

        CompoundTag registeredAbilities = tag.getCompound(REGISTERED_ABILITIES_KEY);
        String itemResourceLocationStr = resourceLocation.toString();
        boolean found = false;
        for (String combinationNumber : registeredAbilities.getAllKeys()) {
            String registeredAbilityLocation = registeredAbilities.getString(combinationNumber);
            if (registeredAbilityLocation.equals(itemResourceLocationStr)) {
                registeredAbilities.remove(combinationNumber);
                String combination = findCombinationForNumber(Integer.parseInt(combinationNumber));
                context.getSource().sendSuccess(() -> Component.literal("Removed ability: ")
                        .append(Component.translatable(item.getDescriptionId()))
                        .append(Component.literal(" (combination: " + combination + ")").withStyle(ChatFormatting.GREEN)), true);
                found = true;
                break;
            }
        }

        if (!found) {
            context.getSource().sendFailure(Component.literal("Ability not found in registered abilities: " + resourceLocation).withStyle(ChatFormatting.RED));
            return 0;
        }

        // Update the tag if there are still abilities, or remove it if empty
        if (registeredAbilities.getAllKeys().isEmpty()) {
            tag.remove(REGISTERED_ABILITIES_KEY);
        } else {
            tag.put(REGISTERED_ABILITIES_KEY, registeredAbilities);
        }

        // Sync the changes to the client
        syncRegisteredAbilitiesToClient(player);

        return 1;
    }



    public static void syncRegisteredAbilitiesToClient(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (tag.contains(REGISTERED_ABILITIES_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag registeredAbilities = tag.getCompound(REGISTERED_ABILITIES_KEY);
            Map<String, String> abilitiesToSync = new HashMap<>();

            for (String combinationNumber : registeredAbilities.getAllKeys()) {
                String abilityResourceLocationString = registeredAbilities.getString(combinationNumber);
                ResourceLocation resourceLocation = new ResourceLocation(abilityResourceLocationString);
                Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
                if (item != null) {
                    String combination = findCombinationForNumber(Integer.parseInt(combinationNumber));
                    if (!combination.isEmpty()) {
                        String localizedName = Component.translatable(item.getDescriptionId()).getString();
                        abilitiesToSync.put(combination, localizedName);
                    }
                }
            }

            if (!abilitiesToSync.isEmpty()) {
                LOTMNetworkHandler.sendToPlayer(new SyncAbilitiesS2C(abilitiesToSync), player);
            }
            if (abilitiesToSync.isEmpty()) {
                LOTMNetworkHandler.sendToPlayer(new ClearAbilitiesS2C(), player);
                player.sendSystemMessage(Component.literal("Cleared Abilities").withStyle(ChatFormatting.GREEN));
            }
        }
    }


    public static void tickEvent(ServerPlayer player) {
        syncRegisteredAbilitiesToClient(player);
    }

    public static String findCombinationForNumber(int number) {
        for (Map.Entry<String, Integer> entry : COMBINATION_MAP.entrySet()) {
            if (entry.getValue() == number) {
                return entry.getKey();
            }
        }
        return "";
    }

    private static int loadBeyonderAbilities(CommandContext<CommandSourceStack> context) {
        try {
            ServerLevel level = context.getSource().getLevel();
            BeyonderRecipeData recipeData = BeyonderRecipeData.getInstance(level);
            recipeData.clearRecipes();
            loadAbilities(context);
            context.getSource().sendSuccess(() -> Component.literal("Successfully registered all available beyonder abilities!")
                    .withStyle(ChatFormatting.GREEN), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error loading beyonder ability: " + e.getMessage())
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static void excecuteAbilityCommand(CommandContext<CommandSourceStack> context, String command) {
        try {
            context.getSource().getServer().getCommands().performPrefixedCommand(
                    context.getSource(), command.substring(1)); // Remove the leading '/' from command
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to load abilities")
                    .withStyle(ChatFormatting.RED));
        }
    }

    private static void loadAbilities(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();
        if (player != null) {
            int sequence = BeyonderUtil.getSequence(player);
            if (BeyonderUtil.currentPathwayMatchesNoException(player, BeyonderClassInit.SPECTATOR.get())) {
                if (sequence == 9) {
                    player.sendSystemMessage(Component.literal("No abilities to register"));
                } else if (sequence >= 8) {
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:mindreading");
                } else if (sequence == 7) {
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:mindreading");
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:awe");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:frenzy");
                    excecuteAbilityCommand(context, "/abilityput OOOXO lotm:placate");
                } else if (sequence == 6) {
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:mindreading");
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:awe");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:frenzy");
                    excecuteAbilityCommand(context, "/abilityput OOOXO lotm:placate");
                    excecuteAbilityCommand(context, "/abilityput OOOXX lotm:psychologicalinvisibility");
                } else if (sequence == 5) {
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:mindreading");
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:awe");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:frenzy");
                    excecuteAbilityCommand(context, "/abilityput OOOXO lotm:placate");
                    excecuteAbilityCommand(context, "/abilityput OOOXX lotm:psychologicalinvisibility");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:dreamwalking");
                } else if (sequence == 4) {
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:mindreading");
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:awe");
                    excecuteAbilityCommand(context, "/abilityput OOOXO lotm:placate");
                    excecuteAbilityCommand(context, "/abilityput OOOXX lotm:psychologicalinvisibility");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:dreamwalking");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:dragonbreath");
                    excecuteAbilityCommand(context, "/abilityput OXXXX lotm:mindstorm");
                } else if (sequence == 3) {
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:mindreading");
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:awe");
                    excecuteAbilityCommand(context, "/abilityput OOOXO lotm:placate");
                    excecuteAbilityCommand(context, "/abilityput OOOXX lotm:psychologicalinvisibility");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:dreamwalking");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:dragonbreath");
                    excecuteAbilityCommand(context, "/abilityput OXXXX lotm:plaguestorm");
                    excecuteAbilityCommand(context, "/abilityput OXXOXO lotm:dreamweaving");
                } else if (sequence == 2) {
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:mindreading");
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:awe");
                    excecuteAbilityCommand(context, "/abilityput OOOXO lotm:placate");
                    excecuteAbilityCommand(context, "/abilityput OOOXX lotm:psychologicalinvisibility");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:dreamwalking");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:dragonbreath");
                    excecuteAbilityCommand(context, "/abilityput OXXXX lotm:plaguestorm");
                    excecuteAbilityCommand(context, "/abilityput OOXOO lotm:dreamintoreality");
                    excecuteAbilityCommand(context, "/abilityput XXXXO lotm:discern");
                    excecuteAbilityCommand(context, "/abilityput OXXOXO lotm:dreamweaving");
                } else if (sequence == 1) {
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:mindreading");
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:awe");
                    excecuteAbilityCommand(context, "/abilityput OOOXO lotm:placate");
                    excecuteAbilityCommand(context, "/abilityput OOOXX lotm:psychologicalinvisibility");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:dreamwalking");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:dragonbreath");
                    excecuteAbilityCommand(context, "/abilityput OXXXX lotm:plaguestorm");
                    excecuteAbilityCommand(context, "/abilityput OOXOO lotm:dreamintoreality");
                    excecuteAbilityCommand(context, "/abilityput XXXXO lotm:discern");
                    excecuteAbilityCommand(context, "/abilityput XXOOO lotm:prophecy");
                    excecuteAbilityCommand(context, "/abilityput OOXXX lotm:meteorshower");
                    excecuteAbilityCommand(context, "/abilityput OXXOXO lotm:dreamweaving");
                } else if (sequence == 0) {
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:mindreading");
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:awe");
                    excecuteAbilityCommand(context, "/abilityput OOOXO lotm:placate");
                    excecuteAbilityCommand(context, "/abilityput OOOXX lotm:psychologicalinvisibility");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:dreamwalking");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:dragonbreath");
                    excecuteAbilityCommand(context, "/abilityput OXXXX lotm:plaguestorm");
                    excecuteAbilityCommand(context, "/abilityput OOXOO lotm:dreamintoreality");
                    excecuteAbilityCommand(context, "/abilityput XXXXO lotm:discern");
                    excecuteAbilityCommand(context, "/abilityput XXOOO lotm:prophecy");
                    excecuteAbilityCommand(context, "/abilityput OOXXX lotm:meteorshower");
                    excecuteAbilityCommand(context, "/abilityput OXOOO lotm:envisionhealth");
                    excecuteAbilityCommand(context, "/abilityput XXOXX lotm:envisionlocation");
                    excecuteAbilityCommand(context, "/abilityput OXXOO lotm:envisionbarrier");
                    excecuteAbilityCommand(context, "/abilityput OXXOXO lotm:dreamweaving");
                }
            } else if (BeyonderUtil.currentPathwayMatchesNoException(player, BeyonderClassInit.MONSTER.get())) {
                if (sequence == 9) {
                    excecuteAbilityCommand(context, "/abilityput OXOOX lotm:monsterdangersense");
                } else if (sequence >= 8) {
                    excecuteAbilityCommand(context, "/abilityput OXOOX lotm:monsterdangersense");
                } else if (sequence == 7) {
                    excecuteAbilityCommand(context, "/abilityput OXOOX lotm:monsterdangersense");
                    excecuteAbilityCommand(context, "/abilityput OOXXO lotm:luckperception");
                } else if (sequence == 6) {
                    excecuteAbilityCommand(context, "/abilityput OXOOX lotm:monsterdangersense");
                    excecuteAbilityCommand(context, "/abilityput OOXXO lotm:luckperception");
                    excecuteAbilityCommand(context, "/abilityput XXXOO lotm:psychestorm");
                } else if (sequence == 5) {
                    excecuteAbilityCommand(context, "/abilityput OXOOX lotm:monsterdangersense");
                    excecuteAbilityCommand(context, "/abilityput OOXXO lotm:luckperception");
                    excecuteAbilityCommand(context, "/abilityput XXXOO lotm:psychestorm");
                    excecuteAbilityCommand(context, "/abilityput OOXXX lotm:luckfuturetelling");
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:misfortunebestowal");
                } else if (sequence == 4) {
                    excecuteAbilityCommand(context, "/abilityput OXOOX lotm:monsterdangersense");
                    excecuteAbilityCommand(context, "/abilityput OOXXO lotm:luckperception");
                    excecuteAbilityCommand(context, "/abilityput XXXOO lotm:psychestorm");
                    excecuteAbilityCommand(context, "/abilityput OOXXX lotm:luckfuturetelling");
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:misfortunebestowal");
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:providencedomain");
                    excecuteAbilityCommand(context, "/abilityput XOXXX lotm:misfortunedomain");
                } else if (sequence == 3) {
                    excecuteAbilityCommand(context, "/abilityput OXOOX lotm:monsterdangersense");
                    excecuteAbilityCommand(context, "/abilityput OOXXO lotm:luckperception");
                    excecuteAbilityCommand(context, "/abilityput XXXOO lotm:psychestorm");
                    excecuteAbilityCommand(context, "/abilityput OOXXX lotm:luckfuturetelling");
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:misfortunebestowal");
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:providencedomain");
                    excecuteAbilityCommand(context, "/abilityput XOXXX lotm:misfortunedomain");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:auraofchaos");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:chaoswalkercombat");
                    excecuteAbilityCommand(context, "/abilityput XOOOO lotm:enabledisableripple");
                } else if (sequence == 2) {
                    excecuteAbilityCommand(context, "/abilityput OXOOX lotm:monsterdangersense");
                    excecuteAbilityCommand(context, "/abilityput OOXXO lotm:luckperception");
                    excecuteAbilityCommand(context, "/abilityput XXXOO lotm:psychestorm");
                    excecuteAbilityCommand(context, "/abilityput OOXXX lotm:luckfuturetelling");
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:misfortunebestowal");
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:providencedomain");
                    excecuteAbilityCommand(context, "/abilityput XOXXX lotm:misfortunedomain");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:auraofchaos");
                    excecuteAbilityCommand(context, "/abilityput OOOXX lotm:chaoswalkercombat");
                    excecuteAbilityCommand(context, "/abilityput XOOOO lotm:enabledisableripple");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:whisperofcorruption");
                    excecuteAbilityCommand(context, "/abilityput XOOOO lotm:misfortuneimplosion");
                } else if (sequence == 1) {
                    excecuteAbilityCommand(context, "/abilityput OXOOX lotm:monsterdangersense");
                    excecuteAbilityCommand(context, "/abilityput OOXXO lotm:luckperception");
                    excecuteAbilityCommand(context, "/abilityput XXXOO lotm:psychestorm");
                    excecuteAbilityCommand(context, "/abilityput OOXXX lotm:luckfuturetelling");
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:misfortunebestowal");
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:providencedomain");
                    excecuteAbilityCommand(context, "/abilityput XOXXX lotm:misfortunedomain");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:auraofchaos");
                    excecuteAbilityCommand(context, "/abilityput OOOXX lotm:chaoswalkercombat");
                    excecuteAbilityCommand(context, "/abilityput XOOOO lotm:enabledisableripple");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:whisperofcorruption");
                    excecuteAbilityCommand(context, "/abilityput XOOOO lotm:misfortuneimplosion");
                    excecuteAbilityCommand(context, "/abilityput OXXXX lotm:rebootself");
                    excecuteAbilityCommand(context, "/abilityput OOOXO lotm:cycleoffate");
                    excecuteAbilityCommand(context, "/abilityput OOXOO lotm:fatereincarnation");
                } else if (sequence == 0) {
                    excecuteAbilityCommand(context, "/abilityput OXOOX lotm:monsterdangersense");
                    excecuteAbilityCommand(context, "/abilityput OOXXO lotm:luckperception");
                    excecuteAbilityCommand(context, "/abilityput XXXOO lotm:psychestorm");
                    excecuteAbilityCommand(context, "/abilityput OOXXX lotm:luckfuturetelling");
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:misfortunebestowal");
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:providencedomain");
                    excecuteAbilityCommand(context, "/abilityput XOXXX lotm:misfortunedomain");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:auraofchaos");
                    excecuteAbilityCommand(context, "/abilityput OOOXX lotm:chaoswalkercombat");
                    excecuteAbilityCommand(context, "/abilityput XOOOO lotm:enabledisableripple");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:whisperofcorruption");
                    excecuteAbilityCommand(context, "/abilityput XOOOO lotm:misfortuneimplosion");
                    excecuteAbilityCommand(context, "/abilityput OXXXX lotm:rebootself");
                    excecuteAbilityCommand(context, "/abilityput OOOXO lotm:cycleoffate");
                    excecuteAbilityCommand(context, "/abilityput OOXOO lotm:fatereincarnation");
                    excecuteAbilityCommand(context, "/abilityput XXXOX lotm:probabilityinfinitefortune");
                    excecuteAbilityCommand(context, "/abilityput OXOOO lotm:probabilityinfinitemisfortune");
                    excecuteAbilityCommand(context, "/abilityput XOXOX lotm:probabilityfortune");
                    excecuteAbilityCommand(context, "/abilityput OXXXO lotm:probabilitymisfortune");
                }
            } else if (BeyonderUtil.currentPathwayMatchesNoException(player, BeyonderClassInit.SAILOR.get())) {
                if (sequence == 9) {
                    player.sendSystemMessage(Component.literal("No abilities to register"));
                } else if (sequence >= 8) {
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:ragingblows");
                } else if (sequence == 7) {
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:ragingblows");
                } else if (sequence == 6) {
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:ragingblows");
                } else if (sequence == 5) {
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:ragingblows");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:sailorlightning");
                    excecuteAbilityCommand(context, "/abilityput XXOOX lotm:acidicrain");
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:watersphere");
                } else if (sequence == 4) {
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:ragingblows");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:sailorlightning");
                    excecuteAbilityCommand(context, "/abilityput XXOOX lotm:acidicrain");
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:watersphere");
                    excecuteAbilityCommand(context, "/abilityput XXXOO lotm:tornado");
                    excecuteAbilityCommand(context, "/abilityput XXXXO lotm:roar");
                    excecuteAbilityCommand(context, "/abilityput OOOXX lotm:earthquake");
                } else if (sequence == 3) {
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:ragingblows");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:sailorlightning");
                    excecuteAbilityCommand(context, "/abilityput XXOOX lotm:acidicrain");
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:watersphere");
                    excecuteAbilityCommand(context, "/abilityput XXXOO lotm:tornado");
                    excecuteAbilityCommand(context, "/abilityput XXXXO lotm:roar");
                    excecuteAbilityCommand(context, "/abilityput OOOXX lotm:earthquake");
                    excecuteAbilityCommand(context, "/abilityput XOOOO lotm:sonicboom");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:lightningbranch");
                    excecuteAbilityCommand(context, "/abilityput OOXXO lotm:thunderclap");
                } else if (sequence == 2) {
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:ragingblows");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:sailorlightning");
                    excecuteAbilityCommand(context, "/abilityput XXOOX lotm:acidicrain");
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:watersphere");
                    excecuteAbilityCommand(context, "/abilityput XXXOO lotm:tornado");
                    excecuteAbilityCommand(context, "/abilityput XXXXO lotm:roar");
                    excecuteAbilityCommand(context, "/abilityput OOOXX lotm:earthquake");
                    excecuteAbilityCommand(context, "/abilityput XOOOO lotm:sonicboom");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:lightningbranch");
                    excecuteAbilityCommand(context, "/abilityput OOXXO lotm:thunderclap");
                    excecuteAbilityCommand(context, "/abilityput XOXOX lotm:lightningball");
                    excecuteAbilityCommand(context, "/abilityput OOXXX lotm:extremecoldness");
                    excecuteAbilityCommand(context, "/abilityput XOOXO lotm:raineyes");
                    excecuteAbilityCommand(context, "/abilityput OXOOX lotm:volcaniceruption");
                } else if (sequence == 1) {

                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:ragingblows");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:sailorlightning");
                    excecuteAbilityCommand(context, "/abilityput XXOOX lotm:acidicrain");
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:watersphere");
                    excecuteAbilityCommand(context, "/abilityput XXXOO lotm:tornado");
                    excecuteAbilityCommand(context, "/abilityput XXXXO lotm:roar");
                    excecuteAbilityCommand(context, "/abilityput OOOXX lotm:earthquake");
                    excecuteAbilityCommand(context, "/abilityput XOOOO lotm:sonicboom");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:lightningbranch");
                    excecuteAbilityCommand(context, "/abilityput OOXXO lotm:thunderclap");
                    excecuteAbilityCommand(context, "/abilityput OOXXX lotm:extremecoldness");
                    excecuteAbilityCommand(context, "/abilityput XOOXO lotm:raineyes");
                    excecuteAbilityCommand(context, "/abilityput OXOOX lotm:volcaniceruption");
                    excecuteAbilityCommand(context, "/abilityput XOXOX lotm:lightningballabsorb");
                    excecuteAbilityCommand(context, "/abilityput OOOXO lotm:sailorlightningtravel");
                    excecuteAbilityCommand(context, "/abilityput XXOOO lotm:staroflightning");
                    excecuteAbilityCommand(context, "/abilityput XOXXO lotm:lightningredirection");

                } else if (sequence == 0) {
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:ragingblows");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:sailorlightning");
                    excecuteAbilityCommand(context, "/abilityput XXOOX lotm:acidicrain");
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:watersphere");
                    excecuteAbilityCommand(context, "/abilityput XXXOO lotm:tornado");
                    excecuteAbilityCommand(context, "/abilityput XXXXO lotm:roar");
                    excecuteAbilityCommand(context, "/abilityput OOOXX lotm:earthquake");
                    excecuteAbilityCommand(context, "/abilityput XOOOO lotm:sonicboom");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:lightningbranch");
                    excecuteAbilityCommand(context, "/abilityput OOXXO lotm:thunderclap");
                    excecuteAbilityCommand(context, "/abilityput XOXOX lotm:lightningball");
                    excecuteAbilityCommand(context, "/abilityput OOXXX lotm:extremecoldness");
                    excecuteAbilityCommand(context, "/abilityput XOOXO lotm:raineyes");
                    excecuteAbilityCommand(context, "/abilityput OXOOX lotm:volcaniceruption");
                    excecuteAbilityCommand(context, "/abilityput XOXOX lotm:lightningballabsorb");
                    excecuteAbilityCommand(context, "/abilityput OOOXO lotm:sailorlightningtravel");
                    excecuteAbilityCommand(context, "/abilityput XXOOO lotm:staroflightning");
                    excecuteAbilityCommand(context, "/abilityput XOXXO lotm:lightningredirection");
                    excecuteAbilityCommand(context, "/abilityput OXXXX lotm:tyranny");
                    excecuteAbilityCommand(context, "/abilityput OXXXO lotm:stormseal");
                }
            }else if (BeyonderUtil.currentPathwayMatchesNoException(player, BeyonderClassInit.WARRIOR.get())) {
                if (sequence == 9) {
                    player.sendSystemMessage(Component.literal("No abilities to register"));
                } else if (sequence == 8) {
                    player.sendSystemMessage(Component.literal("No abilities to register"));
                } else if (sequence == 7) {
                    player.sendSystemMessage(Component.literal("No abilities to register"));
                } else if (sequence == 6) {
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:gigantification");
                    excecuteAbilityCommand(context, "/abilityput XXXXO lotm:lightofdawn");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:dawnarmory");
                } else if (sequence == 5) {
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:gigantification");
                    excecuteAbilityCommand(context, "/abilityput XXXXO lotm:lightofdawn");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:dawnarmory");
                    excecuteAbilityCommand(context, "/abilityput OOXXX lotm:enabledisableprotection");
                } else if (sequence == 4) {
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:gigantification");
                    excecuteAbilityCommand(context, "/abilityput XXXXO lotm:lightofdawn");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:dawnarmory");
                    excecuteAbilityCommand(context, "/abilityput OOXXX lotm:enabledisableprotection");
                    excecuteAbilityCommand(context, "/abilityput XOXOX lotm:eyeofdemonhunting");
                    excecuteAbilityCommand(context, "/abilityput XOOXO lotm:warriordangersense");
                }else if (sequence == 3) {
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:gigantification");
                    excecuteAbilityCommand(context, "/abilityput XXXXO lotm:lightofdawn");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:dawnarmory");
                    excecuteAbilityCommand(context, "/abilityput OOXXX lotm:enabledisableprotection");
                    excecuteAbilityCommand(context, "/abilityput XOXOX lotm:eyeofdemonhunting");
                    excecuteAbilityCommand(context, "/abilityput XOOXO lotm:warriordangersense");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:mercuryliquefication");
                    excecuteAbilityCommand(context, "/abilityput XOXXO lotm:silverswordmanifestation");
                    excecuteAbilityCommand(context, "/abilityput OOOXX lotm:silverrapier");
                    excecuteAbilityCommand(context, "/abilityput OXOOX lotm:silverarmory");
                    excecuteAbilityCommand(context, "/abilityput XXOOO lotm:lightconcealment");
                } else if (sequence == 2) {
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:gigantification");
                    excecuteAbilityCommand(context, "/abilityput XXXXO lotm:lightofdawn");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:dawnarmory");
                    excecuteAbilityCommand(context, "/abilityput OOXXX lotm:enabledisableprotection");
                    excecuteAbilityCommand(context, "/abilityput XOXOX lotm:eyeofdemonhunting");
                    excecuteAbilityCommand(context, "/abilityput XOOXO lotm:warriordangersense");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:mercuryliquefication");
                    excecuteAbilityCommand(context, "/abilityput XOXXO lotm:silverswordmanifestation");
                    excecuteAbilityCommand(context, "/abilityput OOOXX lotm:silverrapier");
                    excecuteAbilityCommand(context, "/abilityput OXOOX lotm:silverarmory");
                    excecuteAbilityCommand(context, "/abilityput XXOOO lotm:lightconcealment");
                    excecuteAbilityCommand(context, "/abilityput OXXXX lotm:beamofglory");
                    excecuteAbilityCommand(context, "/abilityput XOXXX lotm:auraofglory");
                    excecuteAbilityCommand(context, "/abilityput OXOXX lotm:twilightsword");
                    excecuteAbilityCommand(context, "/abilityput OXOXO lotm:mercurycage");
                } else if (sequence == 1) {
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:gigantification");
                    excecuteAbilityCommand(context, "/abilityput XXXXO lotm:lightofdawn");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:dawnarmory");
                    excecuteAbilityCommand(context, "/abilityput OOXXX lotm:enabledisableprotection");
                    excecuteAbilityCommand(context, "/abilityput XOXOX lotm:eyeofdemonhunting");
                    excecuteAbilityCommand(context, "/abilityput XOOXO lotm:warriordangersense");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:mercuryliquefication");
                    excecuteAbilityCommand(context, "/abilityput XOXXO lotm:silverswordmanifestation");
                    excecuteAbilityCommand(context, "/abilityput OOOXX lotm:silverrapier");
                    excecuteAbilityCommand(context, "/abilityput OXOOX lotm:silverarmory");
                    excecuteAbilityCommand(context, "/abilityput XXOOO lotm:lightconcealment");
                    excecuteAbilityCommand(context, "/abilityput OXXXX lotm:beamofglory");
                    excecuteAbilityCommand(context, "/abilityput XOXXX lotm:auraofglory");
                    excecuteAbilityCommand(context, "/abilityput OXOXX lotm:twilightsword");
                    excecuteAbilityCommand(context, "/abilityput OXOXO lotm:mercurycage");
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:divinehandright");
                    excecuteAbilityCommand(context, "/abilityput OXXOO lotm:divinehandleft");
                    excecuteAbilityCommand(context, "/abilityput XXOXO lotm:twilightmanifestation");
                } else if (sequence == 0) {
                    excecuteAbilityCommand(context, "/abilityput XXXXX lotm:gigantification");
                    excecuteAbilityCommand(context, "/abilityput XXXXO lotm:lightofdawn");
                    excecuteAbilityCommand(context, "/abilityput OOOOX lotm:dawnarmory");
                    excecuteAbilityCommand(context, "/abilityput OOXXX lotm:enabledisableprotection");
                    excecuteAbilityCommand(context, "/abilityput XOXOX lotm:eyeofdemonhunting");
                    excecuteAbilityCommand(context, "/abilityput XOOXO lotm:warriordangersense");
                    excecuteAbilityCommand(context, "/abilityput OOOOO lotm:mercuryliquefication");
                    excecuteAbilityCommand(context, "/abilityput XOXXO lotm:silverswordmanifestation");
                    excecuteAbilityCommand(context, "/abilityput OOOXX lotm:silverrapier");
                    excecuteAbilityCommand(context, "/abilityput OXOOX lotm:silverarmory");
                    excecuteAbilityCommand(context, "/abilityput XXOOO lotm:lightconcealment");
                    excecuteAbilityCommand(context, "/abilityput OXXXX lotm:beamoftwilight");
                    excecuteAbilityCommand(context, "/abilityput XOXXX lotm:auraoftwilight");
                    excecuteAbilityCommand(context, "/abilityput OXOXX lotm:twilightsword");
                    excecuteAbilityCommand(context, "/abilityput OXOXO lotm:mercurycage");
                    excecuteAbilityCommand(context, "/abilityput XOOXX lotm:divinehandright");
                    excecuteAbilityCommand(context, "/abilityput OXXOO lotm:divinehandleft");
                    excecuteAbilityCommand(context, "/abilityput XXOXO lotm:twilightmanifestation");

                    excecuteAbilityCommand(context, "/abilityput OXOOO lotm:twilightfreeze");
                    excecuteAbilityCommand(context, "/abilityput OXXXX lotm:twilightlight");
                    excecuteAbilityCommand(context, "/abilityput OOXOO lotm:twilightaccelerate");
                    excecuteAbilityCommand(context, "/abilityput XOOOO lotm:globeoftwilight");
                }
            }
        }
    }
}