package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.entity.ApprenticeDoorEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class TravelersDoor extends SimpleAbilityItem {

    public TravelersDoor(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 5, 0, 0);
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("You can type coordinates or an ally's name in chat using the following pattern to travel to them."));
        tooltipComponents.add(Component.literal("\"X\", \"Y\", \"Z\", \"Dimension ID\" (optional)"));
        tooltipComponents.add(Component.literal("\"Ally Name\", instant (optional)"));
        tooltipComponents.add(Component.literal("\"X\", \"Y\" and \"Z\" are the coordinates. \"Dimension ID\" is the target dimension's ID (optional)."));
        tooltipComponents.add(Component.literal("\"Ally Name\" is the nickname of the ally you want to travel to. 'instant' determines if the travel is immediate."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("300").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("None").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static boolean hasDimensionId(String message) {
        message = message.replace(",", " ").trim();
        message = message.replaceAll("\\s+", " ");
        String[] parts = message.split(" ");

        if (parts.length >= 4) {
            return !parts[3].equalsIgnoreCase("instant");
        }

        return false;
    }

    public static String getDimensionId(String message) {
        message = message.replace(",", " ").trim();
        message = message.replaceAll("\\s+", " ");
        String[] parts = message.split(" ");

        if (parts.length >= 4) {
            return parts[3];
        }

        return "";
    }

    public static ServerLevel getLevelFromId(MinecraftServer server, String dimensionId, Level originalDestination) {
        String normalizedId = normalizeDimensionId(dimensionId);
        ResourceLocation location = new ResourceLocation(normalizedId);
        ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, location);
        ServerLevel level = server.getLevel(levelKey);
        return level != null ? level : (ServerLevel) originalDestination;
    }

    public static String normalizeDimensionId(String dimensionId){
        dimensionId = dimensionId.toLowerCase();
        if (dimensionId.equals("nether") || dimensionId.equals("minecraft:nether") || dimensionId.equals("the_nether")) return "minecraft:the_nether";
        if (dimensionId.equals("end") || dimensionId.equals("minecraft:end") || dimensionId.equals("the_end")) return "minecraft:the_end";
        if (dimensionId.equals("overworld")) return "minecraft:overworld";
        return dimensionId;
    }

    public static boolean coordsTravel(String message) {
        message = message.replace(",", " ").trim();
        message = message.replaceAll("\\s+", " ");
        try {
            String[] parts = message.split(" ");
            if (parts.length < 3 || parts.length > 5) return false;

            for (int i = 0; i < 3; i++) {
                Integer.parseInt(parts[i]);
            }

            for (int i = 3; i < parts.length; i++) {
                try {
                    Integer.parseInt(parts[i]);
                    return false;
                } catch (NumberFormatException e) {
                }
            }

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String getDimensionName(String input) {
        if (input == null || input.isEmpty()) return input;

        input = input.replace("_", " ").trim();

        if (input.toLowerCase().startsWith("the ")) {
            input = input.substring(4).trim();
        }

        StringBuilder result = new StringBuilder();
        for (String word : input.split("\\s+")) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
                result.append(" ");
            }
        }

        return result.toString().trim();
    }

    public static boolean canTeleportAcrossDimensions(LivingEntity entity, Level destination){
        return entity.level().dimension() == destination.dimension() || BeyonderUtil.getSequence(entity) <= 3;
    }

    public static boolean isInstant(String message) {
        message = message.replace(",", " ").trim();
        message = message.replaceAll("\\s+", " ");
        String[] parts = message.split(" ");

        if (parts.length >= 4 && parts[3].equalsIgnoreCase("instant")) {
            return true;
        }

        if (parts.length >= 5 && parts[4].equalsIgnoreCase("instant")) {
            return true;
        }

        return false;
    }

    public static boolean isInstantPlayer(String message) {
        message = message.replace(",", " ").trim();
        message = message.replaceAll("\\s+", " ");
        String[] parts = message.split(" ");

        return parts.length >= 2 && parts[1].equalsIgnoreCase("instant");
    }

    public static double[] getHorizontalLookCoordinates(Player player, double distance){
        float yaw = player.getYRot();
        double angleRadians = Math.toRadians(-yaw);
        double x = player.getX() + distance * Math.sin(angleRadians);
        double z = player.getZ() + distance * Math.cos(angleRadians);
        return new double[] {x, z};
    }

    public static void spawnDoor(Player player, int x, int y, int z, Level destination){
        float yaw = -player.getYRot() + 180;
        ApprenticeDoorEntity.DoorAnimationKind animationKind = ApprenticeDoorEntity.DoorAnimationKind.BELLOW;
        if(player.level().getBlockState(new BlockPos((int) Math.floor(getHorizontalLookCoordinates(player, 2)[0]),
                (int) Math.floor(player.getY() - 1),
                (int) Math.floor(getHorizontalLookCoordinates(player, 2)[1]))).isAir()){
            animationKind = ApprenticeDoorEntity.DoorAnimationKind.FADE_IN;
        }
        ApprenticeDoorEntity door = new ApprenticeDoorEntity(player.level(), player, BeyonderUtil.getSequence(player), 150, yaw, x, y, z, destination, animationKind);
        door.setPos(getHorizontalLookCoordinates(player, 2)[0], player.getY(), getHorizontalLookCoordinates(player, 2)[1]);
        player.level().addFreshEntity(door);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICE_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null && target.getHealth() < livingEntity.getHealth()) {
            livingEntity.teleportTo(target.getX(), target.getY(), target.getZ());
            return 0;
        }
        return 0;
    }
}
