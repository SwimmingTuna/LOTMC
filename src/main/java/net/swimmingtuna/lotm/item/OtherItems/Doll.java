package net.swimmingtuna.lotm.item.OtherItems;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.capabilities.doll_data.DollUtils;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.Renderer.DollRenderer;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ItemNBTHelper;

import javax.annotation.Nullable;
import java.util.*;

public class Doll extends Item {
    private static Map<String, Entity> renderEntityMap = new HashMap<>();
    private static Map<String, ResourceLocation> rlCache = new WeakHashMap<>();

    public Doll() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getOrCreateTag();
        InteractionHand hand = context.getHand();
        if (!level.isClientSide) {
            if (!tag.contains("EntityPlayerName")) {
                releaseEntity(stack, level, pos, player, hand);
                return InteractionResult.SUCCESS;
            }
            releasePlayer(stack, level, pos, player, hand);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("EntityPlayerUUID")) {
                if (entity.getServer() != null) {
                    Player trapped = BeyonderUtil.getPlayerFromUUID(entity.getServer(), tag.getUUID("EntityPlayerUUID"));
                    if (trapped != null) {
                        DollUtils.setCoords(trapped, (float) entity.getX(), (float) entity.getY(), (float) entity.getZ());
                        DollUtils.setDollDimension(trapped, level);
                        if (DollUtils.dollTimer(trapped) == 0) DollUtils.setTimer(trapped, 12000);
                        DollUtils.setDetectTimer(trapped, 20);
                        ((ServerPlayer) trapped).setGameMode(GameType.SPECTATOR);
                        DollUtils.setIsDoll(trapped, true);
                    } else {
                        stack.setCount(0);
                    }
                }
            }
        }
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!entity.level().isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("EntityPlayerUUID")) {
                if (entity.getServer() == null) {
                } else {
                    Player trapped = BeyonderUtil.getPlayerFromUUID(entity.getServer(), tag.getUUID("EntityPlayerUUID"));
                    if (trapped != null) {
                        DollUtils.setCoords(trapped, (float) entity.getX(), (float) entity.getY(), (float) entity.getZ());
                        DollUtils.setDollDimension(trapped, entity.level());
                        if (DollUtils.dollTimer(trapped) == 0) DollUtils.setTimer(trapped, 12000);
                        DollUtils.setDetectTimer(trapped, 20);
                        ((ServerPlayer) trapped).setGameMode(GameType.SPECTATOR);
                        DollUtils.setIsDoll(trapped, true);
                    } else {
                        entity.discard();
                    }
                }
            }
        }
        return false;
    }

    public void releasePlayer(ItemStack stack, Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("EntityPlayerUUID")) {
                if (level.getServer() != null) {
                    Player trapped = BeyonderUtil.getPlayerFromUUID(level.getServer(), tag.getUUID("EntityPlayerUUID"));
                    if (trapped != null) {
                        ((ServerPlayer) trapped).setGameMode(GameType.SURVIVAL);
                        DollUtils.setIsDoll(trapped, false);
                        trapped.setPos(pos.getX(), pos.getY(), pos.getZ());
                        player.setItemInHand(hand, ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    public void releaseEntity(ItemStack stack, Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();
            if (!tag.contains("EntityPlayerName")) {
                String entityTypeId = tag.getString("EntityID");
                EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityTypeId));
                if (entityType != null) {
                    Entity reconstructedEntity = entityType.create(level);
                    if (reconstructedEntity != null) {
                        reconstructedEntity.load(tag);
                        reconstructedEntity.setPos(pos.getX(), pos.getY(), pos.getZ());
                        level.addFreshEntity(reconstructedEntity);
                        player.setItemInHand(hand, ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("EntityPlayerName")) {
            String playerName = stack.getTag().getString("EntityPlayerName");
            return Component.literal(playerName + "'s ").append(super.getName(stack));
        }

        String eName = getEntityString(stack);
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(getCachedRegName(eName));
        if (type == null) {
            return super.getName(stack);
        }
        return Component.translatable(type.getDescriptionId()).append("`s ").append(super.getName(stack));
    }

    public String getEntityString(ItemStack stack) {
        return ItemNBTHelper.getString(stack, "EntityName", "pig");
    }

    public void setEntity(ResourceLocation entityName, ItemStack stack) {
        ItemNBTHelper.setString(stack, "EntityName", String.valueOf(entityName));
    }

    @Nullable
    public CompoundTag getEntityData(ItemStack stack) {
        CompoundTag compound = ItemNBTHelper.getCompound(stack);
        if (compound.contains("EntityData")) {
            return compound.getCompound("EntityData");
        }
        return null;
    }

    public void setEntityData(CompoundTag compound, ItemStack stack) {
        compound.remove("UUID");
        compound.remove("Motion");
        ItemNBTHelper.getCompound(stack).put("EntityData", compound);
    }

    public ItemStack getDollFromEntity(LivingEntity user, Entity entity, boolean saveEntityData) {
        ItemStack doll = new ItemStack(ItemInit.DOLL.get());
        Entity target = entity;
        CompoundTag tag = doll.getOrCreateTag();
        boolean shouldRemove = false;

        if (target instanceof Player player) {
            PlayerMobEntity clone = new PlayerMobEntity(EntityInit.PLAYER_MOB_ENTITY.get(), user.level());
            target = clone;
            ItemNBTHelper.setString(doll, "EntityPlayerName", player.getGameProfile().getName());
            ItemNBTHelper.setUUID(doll, "EntityPlayerUUID", player.getUUID());
            ItemNBTHelper.setInteger(doll, "EntityPlayerUserSequence", BeyonderUtil.getSequence(user));
            ItemNBTHelper.setInteger(doll, "EntityPlayerTrappedSequence", BeyonderUtil.getSequence(player));
            List<ItemStack> itemStacks = Arrays.asList(
                    player.getItemBySlot(EquipmentSlot.MAINHAND),
                    player.getItemBySlot(EquipmentSlot.OFFHAND),
                    player.getItemBySlot(EquipmentSlot.HEAD),
                    player.getItemBySlot(EquipmentSlot.CHEST),
                    player.getItemBySlot(EquipmentSlot.LEGS),
                    player.getItemBySlot(EquipmentSlot.FEET)
            );
            ListTag itemListTag = new ListTag();

            for (ItemStack stack : itemStacks) {
                CompoundTag stackTag = new CompoundTag();
                stack.save(stackTag);
                itemListTag.add(stackTag);
            }
            tag.put("EntityPlayerEquipment", itemListTag);
            if (user.level().isClientSide) {
                if(DollRenderer.playerRendererBuffer.containsKey(tag.getUUID("EntityPlayerUUID"))) {
                    DollRenderer.playerRendererBuffer.remove(tag.getUUID("EntityPlayerUUID"));
                }
            }
        } else {
            if (target != null) {
                entity.saveWithoutId(tag);
                tag.putString("EntityID", ForgeRegistries.ENTITY_TYPES.getKey(target.getType()).toString());
                shouldRemove = true;
            }
        }

        String registryName = ForgeRegistries.ENTITY_TYPES.getKey(target.getType()).toString();
        ItemNBTHelper.setString(doll, "EntityName", registryName);

        if (saveEntityData) {
            CompoundTag compound = new CompoundTag();
            target.saveWithoutId(compound);
            setEntityData(compound, doll);
        }
        if (shouldRemove) target.remove(Entity.RemovalReason.DISCARDED);
        return doll;
    }

    public Entity getRenderEntity(ItemStack stack) {
        return getRenderEntity(getEntityString(stack));
    }

    public static Entity getRenderEntityStatic(ItemStack stack) {
        return ((Doll) ItemInit.DOLL.get()).getRenderEntity(stack);
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public Entity getRenderEntity(String name) {
        if (!renderEntityMap.containsKey(name)) {
            Level level = Minecraft.getInstance().level;
            Entity entity;
            try {
                EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(getCachedRegName(name));
                if (type != null) {
                    entity = type.create(level);
                    if (entity == null) {
                        entity = EntityType.PIG.create(level);
                    }
                } else {
                    entity = EntityType.PIG.create(level);
                }
            } catch (Throwable e) {
                entity = EntityType.PIG.create(level);
            }
            renderEntityMap.put(name, entity);
        }

        return renderEntityMap.get(name);
    }

    public static ResourceLocation getCachedRegName(String name) {
        return rlCache.computeIfAbsent(name, ResourceLocation::new);
    }
}