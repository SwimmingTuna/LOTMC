package net.swimmingtuna.lotm.item.OtherItems;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.init.BlockInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.world.worldgen.dimension.DimensionInit;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DollStructure extends Item {
    private static final Set<Block> BLOCKED_BLOCKS = new HashSet<>();

    static {
        BLOCKED_BLOCKS.add(Blocks.BEDROCK);
        BLOCKED_BLOCKS.add(BlockInit.VOID_BLOCK.get());
        BLOCKED_BLOCKS.add(BlockInit.REAL_VOID_BLOCK.get());
    }

    public DollStructure(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if(!level.isClientSide) {
            ItemStack doll = player.getItemInHand(hand);
            deMiniaturize(doll, player);
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    public static ItemStack createWithCapturedStructure(LivingEntity user, int radius) {
        ItemStack stack = new ItemStack(ItemInit.DOLL_STRUCTURE.get());

        CompoundTag tag = new CompoundTag();
        ListTag blocksTag = new ListTag();

        Level level = user.level();
        BlockPos origin = user.blockPosition();

        double radiusSquared = radius * radius;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distanceSquared = x * x + y * y + z * z;
                    if (distanceSquared > radiusSquared) continue;

                    BlockPos pos = origin.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);

                    if (state.isAir()) continue;

                    CompoundTag blockTag = new CompoundTag();
                    blockTag.putInt("x", x);
                    blockTag.putInt("y", y);
                    blockTag.putInt("z", z);

                    ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                    if (id == null) continue;

                    blockTag.putString("block", id.toString());
                    blockTag.putInt("meta", Block.getId(state));

                    blocksTag.add(blockTag);
                }
            }
        }

        tag.put("StructureBlocks", blocksTag);
        stack.setTag(tag);
        return stack;
    }

    public static void deMiniaturize(ItemStack stack, LivingEntity user) {
        MinecraftServer server = user.getServer();

        if (server == null) return;

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        ResourceKey<Level> dimensionKey = DimensionInit.DOLL_SPACE_LEVEL_KEY;
        ServerLevel dollLevel = server.getLevel(dimensionKey);

        Level level = user.level();

        CompoundTag tag = stack.getOrCreateTag();
        if(tag.contains("radius")){
            int centerX = tag.getInt("centerX");
            int centerY = tag.getInt("centerY");
            int centerZ = tag.getInt("centerZ");
            int radius = tag.getInt("radius");
            BlockPos center = new BlockPos(centerX, centerY, centerZ);

            //de-miniaturize
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        double distanceSquared = x * x + y * y + z * z;
                        if (distanceSquared > radius * radius) continue;

                        BlockPos sourcePos = center.offset(x, y, z);
                        BlockState sourceState = dollLevel.getBlockState(sourcePos);

                        if (sourceState.isAir() || BLOCKED_BLOCKS.contains(sourceState.getBlock())) continue;

                        BlockPos destPos = user.blockPosition().offset(x, y, z);

                        CompoundTag beTag = null;
                        BlockEntity sourceBE = dollLevel.getBlockEntity(sourcePos);
                        if (sourceBE != null) {
                            beTag = sourceBE.saveWithFullMetadata();
                        }

                        level.setBlock(destPos, sourceState, 3);

                        if (beTag != null) {
                            BlockEntity destBE = level.getBlockEntity(destPos);
                            if (destBE != null) {
                                beTag.putInt("x", destPos.getX());
                                beTag.putInt("y", destPos.getY());
                                beTag.putInt("z", destPos.getZ());
                                destBE.load(beTag);
                            }
                        }
                    }
                }
            }

            //clean space
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = - radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        int x = center.getX() + dx;
                        int y = center.getY() + dy;
                        int z = center.getZ() + dz;
                        mutablePos.set(x, y, z);
                        dollLevel.setBlock(mutablePos, Blocks.GOLD_BLOCK.defaultBlockState(), 3);
                    }
                }
            }
        }else if(user instanceof Player player){
            player.displayClientMessage(Component.literal("Couldnt find the stored structure"), false);
        }
    }

    public static ListTag getStructureBlocks(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("Blocks", Tag.TAG_LIST) ? tag.getList("Blocks", Tag.TAG_COMPOUND) : new ListTag();
    }

    public static int getStructureSize(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt("Size") : 0;
    }
}