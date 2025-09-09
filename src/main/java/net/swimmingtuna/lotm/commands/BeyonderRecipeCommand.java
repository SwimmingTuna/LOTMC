package net.swimmingtuna.lotm.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.swimmingtuna.lotm.world.worlddata.BeyonderRecipeData;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.swimmingtuna.lotm.util.BeyonderUtil.registerAllRecipes;

public class BeyonderRecipeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
                literal("beyonderrecipe")
                        .requires(source -> source.hasPermission(2))
                        .then(literal("add")
                                .then(argument("craftedItem", ItemArgument.item(buildContext))
                                        .then(literal("ingredients")
                                                .then(argument("mainCount", IntegerArgumentType.integer(1, 5))
                                                        .then(argument("ingredient1", ItemArgument.item(buildContext))
                                                                .executes(ctx -> addRecipeWithFlexibleIngredients(ctx, 1))
                                                                .then(argument("ingredient2", ItemArgument.item(buildContext))
                                                                        .executes(ctx -> addRecipeWithFlexibleIngredients(ctx, 2))
                                                                        .then(argument("ingredient3", ItemArgument.item(buildContext))
                                                                                .executes(ctx -> addRecipeWithFlexibleIngredients(ctx, 3))
                                                                                .then(argument("ingredient4", ItemArgument.item(buildContext))
                                                                                        .executes(ctx -> addRecipeWithFlexibleIngredients(ctx, 4))
                                                                                        .then(argument("ingredient5", ItemArgument.item(buildContext))
                                                                                                .executes(ctx -> addRecipeWithFlexibleIngredients(ctx, 5))
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(literal("remove")
                                .then(argument("craftedItem", ItemArgument.item(buildContext))
                                        .executes(BeyonderRecipeCommand::removeRecipe)
                                )
                                .then(literal("all")
                                        .executes(BeyonderRecipeCommand::removeAllRecipes)
                                )
                        )
                        .then(literal("load")
                                .executes(BeyonderRecipeCommand::loadModpackRecipes)
                        )
        );
    }

    private static int removeRecipe(CommandContext<CommandSourceStack> context) {
        try {
            ItemStack craftedItem = ItemArgument.getItem(context, "craftedItem").createItemStack(1, false);
            ServerLevel level = context.getSource().getLevel();
            BeyonderRecipeData recipeData = BeyonderRecipeData.getInstance(level);

            boolean recipeRemoved = recipeData.removeRecipe(craftedItem);
            if (recipeRemoved) {
                context.getSource().sendSuccess(() -> Component.literal("Successfully removed recipe for " +
                        craftedItem.getHoverName().getString()).withStyle(ChatFormatting.GREEN), true);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("No recipe found for this item!").withStyle(ChatFormatting.YELLOW));
                return 0;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error removing recipe: " + e.getMessage()).withStyle(ChatFormatting.DARK_RED));
            return 0;
        }
    }

    private static int removeAllRecipes(CommandContext<CommandSourceStack> context) {
        try {
            ServerLevel level = context.getSource().getLevel();
            BeyonderRecipeData recipeData = BeyonderRecipeData.getInstance(level);
            recipeData.clearRecipes();
            context.getSource().sendSystemMessage(Component.literal("Cleared all recipes").withStyle(ChatFormatting.GREEN));
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error removing recipe: " + e.getMessage()).withStyle(ChatFormatting.DARK_RED));
            return 0;
        }
    }


    private static int addRecipeWithFlexibleIngredients(CommandContext<CommandSourceStack> context, int ingredientCount) {
        try {
            // Get the crafted item
            ItemStack craftedItem = ItemArgument.getItem(context, "craftedItem").createItemStack(1, false);

            // Get the specified main ingredient count
            int mainIngredientCount = context.getArgument("mainCount", Integer.class);

            // Prepare lists for ingredients
            List<ItemStack> mainIngredients = new ArrayList<>();
            List<ItemStack> supplementaryIngredients = new ArrayList<>();

            // Validate main ingredient count
            if (mainIngredientCount > ingredientCount) {
                context.getSource().sendFailure(Component.literal("Main ingredient count cannot exceed total ingredient count.")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }

            // Collect ingredients
            for (int i = 1; i <= ingredientCount; i++) {
                ItemStack ingredient = ItemArgument.getItem(context, "ingredient" + i).createItemStack(1, false);

                // Categorize ingredients
                if (mainIngredients.size() < mainIngredientCount) {
                    mainIngredients.add(ingredient);
                } else {
                    supplementaryIngredients.add(ingredient);
                }
            }

            // Ensure at least one main ingredient
            if (mainIngredients.isEmpty()) {
                context.getSource().sendFailure(Component.literal("At least one main ingredient is required!")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }

            // Get recipe data and add recipe
            ServerLevel level = context.getSource().getLevel();
            BeyonderRecipeData recipeData = BeyonderRecipeData.getInstance(level);
            boolean recipeAdded = recipeData.setRecipe(craftedItem, mainIngredients, supplementaryIngredients);

            if (recipeAdded) {
                // Construct success message
                StringBuilder message = new StringBuilder("Successfully added recipe for ")
                        .append(craftedItem.getHoverName().getString())
                        .append(" - Main Ingredients: ");

                for (ItemStack ingredient : mainIngredients) {
                    message.append(ingredient.getHoverName().getString()).append(", ");
                }

                message.append(" - Supplementary Ingredients: ");
                for (ItemStack ingredient : supplementaryIngredients) {
                    message.append(ingredient.getHoverName().getString()).append(", ");
                }

                context.getSource().sendSuccess(() -> Component.literal(message.toString())
                        .withStyle(ChatFormatting.GREEN), true);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("A recipe for this item already exists!")
                        .withStyle(ChatFormatting.YELLOW));
                return 0;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error adding recipe: " + e.getMessage())
                    .withStyle(ChatFormatting.DARK_RED));
            return 0;
        }
    }


    private static int loadModpackRecipes(CommandContext<CommandSourceStack> context) {
        try {
            ServerLevel level = context.getSource().getLevel();
            BeyonderRecipeData recipeData = BeyonderRecipeData.getInstance(level);
            recipeData.clearRecipes();
            registerAllRecipes(context);
            context.getSource().sendSuccess(() -> Component.literal("Successfully loaded all modpack recipes!")
                    .withStyle(ChatFormatting.GREEN), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error loading modpack recipes: " + e.getMessage())
                    .withStyle(ChatFormatting.DARK_RED));
            return 0;
        }
    }



    public static void executeRecipeCommand(CommandContext<CommandSourceStack> context, String command) {
        try {
            context.getSource().getServer().getCommands().performPrefixedCommand(
                    context.getSource(), command.substring(1));
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to execute recipe: " + command)
                    .withStyle(ChatFormatting.RED));
        }
    }

}

