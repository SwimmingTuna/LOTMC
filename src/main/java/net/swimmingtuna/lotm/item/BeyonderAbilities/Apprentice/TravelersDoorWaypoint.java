package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.entity.ApprenticeDoorEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.TravelerWaypointC2S;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkill;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import net.swimmingtuna.lotm.world.worldgen.dimension.DimensionInit;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class TravelersDoorWaypoint extends LeftClickHandlerSkill {

    public TravelersDoorWaypoint(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 5, 300, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        CompoundTag tag = player.getPersistentData();
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        if(!tag.contains("doorWaypoint")){
            tag.putInt("doorWaypoint", 1);
        }
        if (player.isShiftKeyDown()) {
            setWaypoint(player, tag);
        } else {
            useSpirituality(player, 300 *  (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.TRAVELERSDOORHOME.get()));
            teleportToWaypoint(player, tag);
        }
        addCooldown(player);
        return InteractionResult.SUCCESS;
    }

    public static boolean isValidDimension(Level level){
        return level.dimension() != DimensionInit.CONCEALED_SPACE_LEVEL_KEY;
    }

    private void setWaypoint(LivingEntity entity, CompoundTag tag) {
        if (!entity.level().isClientSide) {
            if(!isValidDimension(entity.level())){
                if(entity instanceof Player player){
                    tag.putInt("waypointMessageCooldown", 30);
                    player.displayClientMessage(Component.literal("You cant set a waypoint on this dimension").withStyle(ChatFormatting.RED), true);
                    return;
                }
            }
            int waypoint = tag.getInt("doorWaypoint");
            tag.putDouble("x" + waypoint, entity.getX());
            tag.putDouble("y" + waypoint, entity.getY());
            tag.putDouble("z" + waypoint, entity.getZ());
            tag.putString("dimension" + waypoint, entity.level().dimension().location().toString());
            String coords = String.format("Waypoint %d set at: %.1f, %.1f, %.1f, in The %s Dimension", waypoint, entity.getX(), entity.getY(), entity.getZ(), getDimensionName(entity.level().dimension().location().getPath()));
            if (entity instanceof Player player) {
                tag.putInt("waypointMessageCooldown", 30);
                player.displayClientMessage(Component.literal(coords).withStyle(BeyonderUtil.getStyle(player)), true);
            }
        }
    }

    public static ServerLevel getLevelFromId(MinecraftServer server, String dimensionId) {
        ResourceLocation location = new ResourceLocation(dimensionId);
        ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, location);
        return server.getLevel(levelKey);
    }

    public static void toggleInstant(LivingEntity entity){
        CompoundTag tag = entity.getPersistentData();
        tag.putBoolean("doorWaypointIsInstant", !tag.getBoolean("doorWaypointIsInstant"));
    }

    public static boolean canTeleportAcrossDimensions(LivingEntity entity, Level destination){
        return entity.level().dimension() == destination.dimension() || BeyonderUtil.getSequence(entity) <= 3;
    }

    private void teleportToWaypoint(LivingEntity livingEntity, CompoundTag tag) {
        if (!livingEntity.level().isClientSide) {
            int waypoint = tag.getInt("doorWaypoint");
            double x = tag.getDouble("x" + waypoint);
            double y = tag.getDouble("y" + waypoint);
            double z = tag.getDouble("z" + waypoint);
            String dimensionName = tag.getString("dimension" + waypoint);
            String waypointName = tag.getString("waypointName" + waypoint);
            if (x != 0 && y != 0 && z != 0) {
                Level destination = getLevelFromId(Objects.requireNonNull(livingEntity.getServer()), dimensionName);
                if(!canTeleportAcrossDimensions(livingEntity, destination)){
                    if(livingEntity instanceof Player player){
                        tag.putInt("waypointMessageCooldown", 30);
                        player.displayClientMessage(Component.literal("Waypoint is in an inaccessible dimension").withStyle(ChatFormatting.RED), true);
                        return;
                    }
                }

                String coords;
                boolean isInstant = tag.getBoolean("doorWaypointIsInstant");
                if(!isInstant){
                    coords = String.format("Door created leading to %.1f, %.1f, %.1f, in The %s Dimension", x, y, z, getDimensionName(destination.dimension().location().getPath()));
                    if(!waypointName.isEmpty()) coords = String.format("Door created leading to Waypoint %s", waypointName);
                    spawnDoor(livingEntity, x, y, z, destination);
                }else{
                    coords = String.format("Teleported to %.1f, %.1f, %.1f, in The %s Dimension", x, y, z, getDimensionName(destination.dimension().location().getPath()));
                    if(!waypointName.isEmpty()) coords = String.format("Teleported to Waypoint %s", waypointName);
                    BeyonderUtil.teleportEntity(livingEntity, destination, x, y, z);
                }
                if (livingEntity instanceof Player pPlayer) {
                    tag.putInt("waypointMessageCooldown", 30);
                    pPlayer.displayClientMessage(Component.literal(coords).withStyle(BeyonderUtil.getStyle(pPlayer)), true);
                }
            } else if (livingEntity instanceof Player pPlayer) {
                tag.putInt("waypointMessageCooldown", 30);
                pPlayer.displayClientMessage(Component.literal("No waypoint found").withStyle(ChatFormatting.RED), true);
            }
        }
    }

    public static void setWaypointName(LivingEntity entity, String name){
        CompoundTag tag = entity.getPersistentData();
        if(!tag.contains("doorWaypoint")) tag.putInt("doorWaypoint", 1);

        int waypoint =  tag.getInt("doorWaypoint");
        tag.putString("waypointName" + waypoint, name);

        if (entity instanceof Player player) {
            tag.putInt("waypointMessageCooldown", 30);
            player.displayClientMessage(Component.literal("Waypoint " + waypoint + " name set to " + name).withStyle(BeyonderUtil.getStyle(player)), true);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Right-click to teleport to selected waypoint"));
        tooltipComponents.add(Component.literal("Shift + Right-click to set waypoint at current position"));
        tooltipComponents.add(Component.literal("Left-click to cycle between waypoints"));
        tooltipComponents.add(Component.literal("Shift + Left-click to toggle instant teleportation"));
        tooltipComponents.add(Component.literal("Type a name in chat to set it as your selected waypoint name"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("300").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("None").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof Player player && !level.isClientSide && player.getMainHandItem().getItem() == ItemInit.TRAVELERSDOORHOME.get()) {
            if (isSelected) {
                CompoundTag tag = player.getPersistentData();
                int currentWaypoint = tag.getInt("doorWaypoint");
                double x = tag.getDouble("x" + currentWaypoint);
                double y = tag.getDouble("y" + currentWaypoint);
                double z = tag.getDouble("z" + currentWaypoint);
                String dimensionName = tag.getString("dimension" + currentWaypoint);
                String waypointName = tag.getString("waypointName" + currentWaypoint);

                if(tag.getInt("waypointMessageCooldown") > 0){
                    tag.putInt("waypointMessageCooldown", tag.getInt("waypointMessageCooldown") - 1);
                }else{
                    if (tag.contains("x" + currentWaypoint)) {
                        String coords = String.format("Waypoint %d: %.1f, %.1f, %.1f, in The %s Dimension", currentWaypoint, x, y, z, getDimensionName(getLevelFromId(Objects.requireNonNull(player.getServer()), dimensionName).dimension().location().getPath()));
                        if(!waypointName.isEmpty()) coords = String.format("Waypoint %d: %s", currentWaypoint, waypointName);
                        player.displayClientMessage(Component.literal(coords)
                                .withStyle(BeyonderUtil.getStyle(player)), true);
                    }
                }

                if(tag.getInt("waypointNetworkCooldown") > 0){
                    tag.putInt("waypointNetworkCooldown", tag.getInt("waypointNetworkCooldown") - 1);
                }
            }
        }
    }

    public static double[] getHorizontalLookCoordinates(LivingEntity player, double distance){
        float yaw = player.getYRot();
        double angleRadians = Math.toRadians(-yaw);
        double x = player.getX() + distance * Math.sin(angleRadians);
        double z = player.getZ() + distance * Math.cos(angleRadians);
        return new double[] {x, z};
    }

    public static void spawnDoor(LivingEntity player, double x, double y, double z, Level destination){
        float yaw = -player.getYRot() + 180;
        ApprenticeDoorEntity.DoorAnimationKind animationKind = ApprenticeDoorEntity.DoorAnimationKind.BELLOW;
        if(player.level().getBlockState(new BlockPos((int) Math.floor(getHorizontalLookCoordinates(player, 2)[0]),
                (int) Math.floor(player.getY() - 1),
                (int) Math.floor(getHorizontalLookCoordinates(player, 2)[1]))).isAir()){
            animationKind = ApprenticeDoorEntity.DoorAnimationKind.FADE_IN;
        }
        ApprenticeDoorEntity door = new ApprenticeDoorEntity(player.level(), player, BeyonderUtil.getSequence(player), 150, yaw, (float) x, (float) y, (float) z, destination, animationKind);
        door.setPos(getHorizontalLookCoordinates(player, 2)[0], player.getY(), getHorizontalLookCoordinates(player, 2)[1]);
        player.level().addFreshEntity(door);
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

    public static void clearAllWaypoints(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide) {
            CompoundTag tag = livingEntity.getPersistentData();
            for (int i = 0; i < 100; i++) {
                tag.remove("x" + i);
                tag.remove("y" + i);
                tag.remove("z" + i);
                tag.remove("dimension" + i);
                tag.remove("waypointName" + i);
            }
        }
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICE_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public LeftClickType getleftClickEmpty() {
        return new TravelerWaypointC2S();
    }
}