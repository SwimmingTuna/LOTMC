
package net.swimmingtuna.lotm.blocks.DimensionalSight;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.init.BlockEntityInit;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.ClientShouldntRenderS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class DimensionalSightTileEntity extends DimensionalTileEntity implements BlockEntityTicker<DimensionalSightTileEntity> {
    public CompoundTag scryNBT = null;
    public float yaw = 0.0F;
    public float headYaw = 0.0F;
    public float renderYaw = 0.0F;
    public float pitch = 0.0F;
    public float prevYaw = 0.0F;
    public float prevHeadYaw = 0.0F;
    public float prevRenderYaw = 0.0F;
    public float prevPitch = 0.0F;
    public double velX = 0.0;
    public double velY = 0.0;
    public double velZ = 0.0;
    public Vec3 targetPos = null;
    public float prevSwingProgress = 0.0F;
    public float swingProgress = 0.0F;
    public UUID scryUniqueID = null;
    public float limbSwingAmount = 0.0F;
    public float prevLimbSwingAmount = 0.0F;
    public float limbSwing = 0.0F;
    public ArrayList<BlockPosInfo> blockList = null;
    public List<SynchedEntityData.DataValue<?>> scryDataManager = null;
    public int tickCounter = 0;
    public int scryTimer = 0;
    public ArrayList<BlockPosInfo> lst = null;
    public int scryMaxTimer = 0;
    public String viewTarget = "";
    public static final float RENDER_SCALE = 0.5F;
    public static final float RENDER_HEIGHT_OFFSET = 2.0F;
    public Vec3 displayOffset = Vec3.ZERO;
    public final String VIEW_TARGET = "MAHOUTSUKAI_VIEW_TARGET";
    public final String SCRY_RENDER_YAW = "MAHOUTSUKAI_RENDER_YAW";
    public final String SCRY_YAW = "MAHOUTSUKAI_YAW";
    public final String SCRY_HEAD_YAW = "MAHOUTSUKAI_HYAW";
    public final String SCRY_PITCH = "MAHOUTSUKAI_PITCH";
    public final String SCRY_VEL_X = "MAHOUTSUKAI_VEL_X";
    public final String SCRY_VEL_Y = "MAHOUTSUKAI_VEL_Y";
    public final String SCRY_VEL_Z = "MAHOUTSUKAI_VEL_Z";
    public final String SCRY_UNIQ = "MAHOUTSUKAI_SCRY_UNIQ";
    public final String LIMB_SWING = "MAHOUTSUKAI_LS";
    public final String LIMB_SWING_AMOUNT = "MAHOUTSUKAI_LSA";
    public final String LIMB_SWING_PROGRESS = "MAHOUTSUKAI_LSP";
    public final String SCRY_LOC_X = "MAHOUTSUKAI_LOC_X";
    public final String SCRY_LOC_Y = "MAHOUTSUKAI_LOC_Y";
    public final String SCRY_LOC_Z = "MAHOUTSUKAI_LOC_Z";
    public final String SCRY_BLOCK = "MAHOUTSUKAI_BLOCK_";

    public boolean doRead = false;

    public DimensionalSightTileEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityInit.DIMENSIONAL_SIGHT_ENTITY.get(), pos, blockState);
    }

    public DimensionalSightTileEntity(BlockPos pos, BlockState blockState, int maxScry) {
        super(BlockEntityInit.DIMENSIONAL_SIGHT_ENTITY.get(), pos, blockState);
        scryMaxTimer = maxScry;
    }

    public LivingEntity getScryTarget() {
        return BeyonderUtil.getLivingEntityFromUUID(this.level, this.scryUniqueID);
    }

    public ArrayList<BlockPosInfo> getScryBlocks() {
        return this.blockList != null ? this.blockList : new ArrayList<>();
    }


    public Vec3 getDisplayCenter() {
        return new Vec3(
                this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + RENDER_HEIGHT_OFFSET,
                this.worldPosition.getZ() + 0.5
        ).add(this.displayOffset);
    }

    public Vec3 getBlockDisplayPos(int relativeX, int relativeY, int relativeZ) {
        Vec3 center = getDisplayCenter();
        return new Vec3(
                center.x + (relativeX * RENDER_SCALE),
                center.y + (relativeY * RENDER_SCALE),
                center.z + (relativeZ * RENDER_SCALE)
        );
    }


    @Override
    public AABB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }



    public Vec3 getEntityDisplayPos() {
        if (this.targetPos == null) return getDisplayCenter();
        Vec3 center = getDisplayCenter();
        int centerX = (int) Math.floor(this.targetPos.x);
        int centerZ = (int) Math.floor(this.targetPos.z);
        int centerY = (int) Math.floor(this.targetPos.y);

        double relativeX = this.targetPos.x - centerX;
        double relativeY = this.targetPos.y - centerY;
        double relativeZ = this.targetPos.z - centerZ;

        return new Vec3(
                center.x + (relativeX * RENDER_SCALE),
                center.y + (relativeY * RENDER_SCALE),
                center.z + (relativeZ * RENDER_SCALE)
        );
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        compound.putString(this.VIEW_TARGET, this.viewTarget);
        if (this.getScryTarget() != null) {
            compound.putFloat(this.SCRY_PITCH, this.getScryTarget().getXRot());
            compound.putFloat(this.SCRY_YAW, this.getScryTarget().getYRot());
            compound.putFloat(this.SCRY_HEAD_YAW, this.getScryTarget().yHeadRot);
            compound.putFloat(this.SCRY_RENDER_YAW, this.getScryTarget().yBodyRot);
            compound.putFloat(this.LIMB_SWING, this.getScryTarget().walkAnimation.position());
            compound.putFloat(this.LIMB_SWING_AMOUNT, this.getScryTarget().walkAnimation.speed());
            compound.putFloat(this.LIMB_SWING_PROGRESS, this.getScryTarget().attackAnim);
            Vec3 entityPos = this.getScryTarget().position();
            Vec3 entityMotion = this.getScryTarget().getDeltaMovement();
            compound.putDouble(this.SCRY_VEL_X, entityMotion.x);
            compound.putDouble(this.SCRY_VEL_Y, entityMotion.y);
            compound.putDouble(this.SCRY_VEL_Z, entityMotion.z);
            compound.putDouble(this.SCRY_LOC_X, entityPos.x);
            compound.putDouble(this.SCRY_LOC_Y, entityPos.y);
            compound.putDouble(this.SCRY_LOC_Z, entityPos.z);
            compound.putUUID(this.SCRY_UNIQ, this.getScryTarget().getUUID());
            if (this.level != null) {
                this.blockList = new ArrayList<>();
                int index = 0;
                for (int i = (int) Math.floor(entityPos.x - 5.0); (double) i <= entityPos.x + 5.0; ++i) {
                    for (int j = (int) Math.floor(entityPos.z - 5.0); (double) j <= entityPos.z + 5.0; ++j) {
                        for (int k = (int) Math.floor(entityPos.y - 5.0); (double) k <= entityPos.y + 5.0; ++k) {
                            BlockPos pos = new BlockPos(i, k, j);
                            BlockEntity blockEntity1 = level.getBlockEntity(pos);
                            if (blockEntity1 instanceof DimensionalSightTileEntity) {
                                continue;
                            }
                            BlockPosInfo info = new BlockPosInfo(pos, this.level);
                            info.relativeX = i - (int) Math.floor(entityPos.x);
                            info.relativeY = k - (int) Math.floor(entityPos.y);
                            info.relativeZ = j - (int) Math.floor(entityPos.z);
                            CompoundTag infoNBT = info.write();
                            compound.put(this.SCRY_BLOCK + index, infoNBT);
                            this.blockList.add(info);
                            ++index;
                        }
                    }
                }
            }
        }

        super.saveAdditional(compound);
    }

    @Override
    public void load(CompoundTag compound) {
        this.viewTarget = compound.getString(this.VIEW_TARGET);

        if (compound.contains(this.SCRY_YAW)) {
            this.yaw = compound.getFloat(this.SCRY_YAW);
        }
        if (compound.contains(this.SCRY_PITCH)) {
            this.pitch = compound.getFloat(this.SCRY_PITCH);
        }
        if (compound.contains(this.SCRY_HEAD_YAW)) {
            this.headYaw = compound.getFloat(this.SCRY_HEAD_YAW);
        }
        if (compound.contains(this.SCRY_RENDER_YAW)) {
            this.renderYaw = compound.getFloat(this.SCRY_RENDER_YAW);
        }
        if (compound.contains(this.LIMB_SWING)) {
            this.limbSwing = compound.getFloat(this.LIMB_SWING);
        }
        if (compound.contains(this.LIMB_SWING_AMOUNT)) {
            this.limbSwingAmount = compound.getFloat(this.LIMB_SWING_AMOUNT);
        }
        if (compound.contains(this.LIMB_SWING_PROGRESS)) {
            this.swingProgress = compound.getFloat(this.LIMB_SWING_PROGRESS);
        }
        if (compound.contains(this.SCRY_VEL_X)) {
            this.velX = compound.getDouble(this.SCRY_VEL_X);
        }
        if (compound.contains(this.SCRY_VEL_Y)) {
            this.velY = compound.getDouble(this.SCRY_VEL_Y);
        }
        if (compound.contains(this.SCRY_VEL_Z)) {
            this.velZ = compound.getDouble(this.SCRY_VEL_Z);
        }
        if (compound.hasUUID(this.SCRY_UNIQ)) {
            this.scryUniqueID = compound.getUUID(this.SCRY_UNIQ);
        }
        this.blockList = new ArrayList<>();
        int index = 0;
        while (compound.contains(this.SCRY_BLOCK + index)) {
            BlockPosInfo info = new BlockPosInfo();
            info.read(compound.getCompound(this.SCRY_BLOCK + index), this.level);
            this.blockList.add(info);
            ++index;
        }

        if (compound.contains(this.SCRY_LOC_X) && compound.contains(this.SCRY_LOC_Y) && compound.contains(this.SCRY_LOC_Z)) {
            this.targetPos = new Vec3(
                    compound.getDouble(this.SCRY_LOC_X),
                    compound.getDouble(this.SCRY_LOC_Y),
                    compound.getDouble(this.SCRY_LOC_Z)
            );
        }

        this.doRead = true;
        super.load(compound);
    }

    @Override
    public void tick(Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @NotNull DimensionalSightTileEntity blockEntity) {
        if (!level.isClientSide) {
            int maxLife = 250;
            if (this.getCasterUUID() != null) {
                if (this.getScryTarget() != null) {
                    if (!this.getScryTarget().isAlive() || this.getScryTarget().isRemoved()) {
                        this.removeThis();
                    }
                    this.getScryTarget().getPersistentData().putUUID("dimensionalSightPlayerUUID", this.getCasterUUID());
                }
                LivingEntity livingEntity = BeyonderUtil.getLivingEntityFromUUID(level, this.getCasterUUID());
                AABB detectionBox = new AABB(blockPos.getX() - 1, blockPos.getY() - 1, blockPos.getZ() - 1, blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1);
                List<Entity> entitiesInBox = level.getEntitiesOfClass(Entity.class, detectionBox);
                for (Entity entity : entitiesInBox) {
                    if (entity == livingEntity || BeyonderUtil.isEntityAlly(livingEntity, entity)) {
                        if (this.getScryTarget() != null && tickCounter >= 50) {
                            BeyonderUtil.teleportEntity(livingEntity, this.getScryTarget().level().dimension().location(), this.getScryTarget().getX(), this.getScryTarget().getY(), this.getScryTarget().getZ());
                        }
                    }
                }
                int sequence = BeyonderUtil.getSequence(livingEntity);
                if (sequence != -1 && sequence != 0) {
                    maxLife = 2500 / (sequence);
                } else if (sequence == 0) {
                    maxLife = 4000;
                }
                if (this.getScryTarget() != null) {
                    if (this.level instanceof ServerLevel serverLevel) {
                        int chunkRadius = 5;
                        ChunkPos centerChunk = new ChunkPos(new BlockPos((int) this.getScryTarget().getX(), (int) this.getScryTarget().getY(), (int) this.getScryTarget().getX()));
                        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                                ChunkPos chunkPos = new ChunkPos(centerChunk.x + dx, centerChunk.z + dz);
                                serverLevel.getChunkSource().addRegionTicket(TicketType.PLAYER, chunkPos, 3, chunkPos);
                            }
                        }
                        ChunkPos newCenterChunk = new ChunkPos(this.getBlockPos());
                        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                                ChunkPos chunkPos = new ChunkPos(newCenterChunk.x + dx, newCenterChunk.z + dz);
                                serverLevel.getChunkSource().addRegionTicket(TicketType.PLAYER, chunkPos, 3, chunkPos);
                            }
                        }
                    }
                    LOTMNetworkHandler.sendToAllPlayers(new ClientShouldntRenderS2C(this.getScryTarget().getUUID(), 10));
                    this.getScryTarget().getPersistentData().putInt("ignoreShouldntRender", 10);
                }
            }
            if (this.tickCounter >= 5) {
                if (this.tickCounter > maxLife) {
                    level.setBlock(this.worldPosition, Blocks.AIR.defaultBlockState(), 3);
                }
                if (this.getCasterUUID() == null) {
                    level.setBlock(this.worldPosition, Blocks.AIR.defaultBlockState(), 3);
                } else {
                    LivingEntity livingEntity = BeyonderUtil.getLivingEntityFromUUID(level, this.getCasterUUID());
                    if (livingEntity == null) {
                        level.setBlock(this.worldPosition, Blocks.AIR.defaultBlockState(), 3);
                        return;
                    }
                    if (!livingEntity.isAlive() || this.getScryTarget() == null || !this.getScryTarget().isAlive()) {
                        level.setBlock(this.worldPosition, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
            if (this.tickCounter % 4 == 0) {
                Player caster;
                if (this.getScryTarget() == null || !this.getScryTarget().isAlive() || this.doRead) {
                    if (this.viewTarget != null && !this.viewTarget.isEmpty()) {
                        caster = getPlayerByName(this.viewTarget, level);
                        if (caster == null) {
                            HashSet<Entity> loaded = getAllEntities((ServerLevel) level);
                            this.scryUniqueID = null;
                            for (Entity entity : loaded) {
                                if (entity.getCustomName() != null && entity.getCustomName().getString().equals(this.viewTarget) && entity instanceof LivingEntity livingEntity) {
                                    this.sendUpdates();
                                    break;
                                }
                            }
                            if (this.getScryTarget() == null) {
                                for (Entity entity : loaded) {
                                    if (entity.getDisplayName().getString().equals(this.viewTarget) && entity instanceof LivingEntity livingEntity) {
                                        this.sendUpdates();
                                        break;
                                    }
                                }
                            }
                        } else {
                            this.sendUpdates();
                        }
                    }

                    if (this.getScryTarget() != null && !this.getScryTarget().isAlive()) {
                        this.sendUpdates();
                    }

                    this.doRead = false;
                }

                // Sync entity data
                if (this.getScryTarget() != null && this.getScryTarget().isAlive()) {
                    boolean checkDirty = this.scryDataManager != null;
                    List<SynchedEntityData.DataValue<?>> entries = this.getAll(this.getScryTarget().getEntityData(), checkDirty);
                    if (entries != null) {
                        List<SynchedEntityData.DataValue<?>> entriesCopy = new ArrayList<>(entries);
                        List<SynchedEntityData.DataValue<?>> toSend = new ArrayList<>(entriesCopy);
                        this.scryDataManager = entriesCopy;
                        if (!toSend.isEmpty()) {
                            int radius = 32;
                            AABB aabb = new AABB(this.worldPosition.getX() - radius, this.worldPosition.getY() - radius, this.worldPosition.getZ() - radius, this.worldPosition.getX() + radius, this.worldPosition.getY() + radius, this.worldPosition.getZ() + radius);
                            List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, aabb);
                            for (ServerPlayer player : players) {
                                LOTMNetworkHandler.sendToPlayer(new DimensionalSightPacketS2C(this.worldPosition, toSend), player);
                            }
                        }
                    }

                    this.scryNBT = new CompoundTag();
                    this.getScryTarget().saveWithoutId(this.scryNBT);
                    this.sendUpdates();
                }
            }
            if (this.tickCounter % 15 == 0 && this.getScryTarget() != null && this.getScryTarget().isAlive()) {
                Vec3 entityPos = this.getScryTarget().position();
                this.blockList = new ArrayList<>();

                for (int i = (int) Math.floor(entityPos.x - 5.0); (double) i <= entityPos.x + 5.0; ++i) {
                    for (int j = (int) Math.floor(entityPos.z - 5.0); (double) j <= entityPos.z + 5.0; ++j) {
                        for (int k = (int) Math.floor(entityPos.y - 5.0); (double) k <= entityPos.y + 5.0; ++k) {
                            BlockPos pos = new BlockPos(i, k, j);
                            BlockEntity blockEntity1 = level.getBlockEntity(pos);
                            if (blockEntity1 instanceof DimensionalSightTileEntity) {
                                continue;
                            }
                            BlockPosInfo info = new BlockPosInfo(pos, level);
                            info.relativeX = i - (int) Math.floor(entityPos.x);
                            info.relativeY = k - (int) Math.floor(entityPos.y);
                            info.relativeZ = j - (int) Math.floor(entityPos.z);
                            this.blockList.add(info);
                        }
                    }
                }
                this.setChanged();
            }


            ++this.scryTimer;
            ++this.tickCounter;
        }

        if (this.doRead && level.isClientSide && this.scryNBT != null) {
            LivingEntity scryTarget = BeyonderUtil.getLivingEntityFromUUID(this.level, this.scryUniqueID);
            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(this.scryNBT.getString("id")));

            if (this.scryNBT != null && entityType != null && entityType == EntityType.PLAYER) {
                scryTarget = getPlayerForScry(level, this.viewTarget, this.scryNBT, this.scryUniqueID);
            } else if (this.scryNBT != null && !this.scryNBT.isEmpty()) {
                try {
                    Optional<Entity> entityOptional = EntityType.create(this.scryNBT, level);
                    if (entityOptional.isPresent()) {
                        scryTarget = (LivingEntity) entityOptional.get();
                    }
                } catch (Exception e) {
                    // Handle entity creation failure
                }
            }

            this.doRead = false;
        }

        if (level.isClientSide && this.getScryTarget() != null) {
            CompoundTag renderData = new CompoundTag();
            renderData.putDouble("displayX", getEntityDisplayPos().x);
            renderData.putDouble("displayY", getEntityDisplayPos().y);
            renderData.putDouble("displayZ", getEntityDisplayPos().z);
            renderData.putFloat("displayYaw", this.yaw);
            renderData.putFloat("displayPitch", this.pitch);
            renderData.putFloat("displayHeadYaw", this.headYaw);
            renderData.putFloat("displayBodyYaw", this.renderYaw);
            renderData.putDouble("displayVelX", this.velX);
            renderData.putDouble("displayVelY", this.velY);
            renderData.putDouble("displayVelZ", this.velZ);
            renderData.putFloat("displayLimbSwing", this.limbSwing);
            renderData.putFloat("displayLimbSwingAmount", this.limbSwingAmount);
            renderData.putFloat("displaySwingProgress", this.swingProgress);
            renderData.putBoolean("displayOnGround", true);
            renderData.putFloat("displayFallDistance", 0.0f);
            this.getScryTarget().getPersistentData().put("dimensionalSightRenderData", renderData);
            this.prevSwingProgress = this.swingProgress;
            this.prevYaw = this.yaw;
            this.prevRenderYaw = this.renderYaw;
            this.prevPitch = this.pitch;
            this.prevHeadYaw = this.headYaw;
            if (this.scryDataManager != null) {
                try {
                    this.getScryTarget().getEntityData().assignValues(this.scryDataManager);
                } catch (Exception ignored) {
                }
            }
        }

    }

    public Player getPlayerForScry(Level world, String viewTarget, CompoundTag scryNBT, UUID uuid) {
        return DimensionalSightTileEntity.getPlayerByName(viewTarget, world);
    }

    public static HashSet<Entity> getAllEntities(ServerLevel world) {
        HashSet<Entity> ret = new HashSet<>();
        Iterable<Entity> entity = world.getAllEntities();
        Objects.requireNonNull(ret);
        entity.forEach(ret::add);
        return ret;
    }

    /**
     * Gets all entity data values, optionally filtering for dirty values only
     */
    public List<SynchedEntityData.DataValue<?>> getAll(SynchedEntityData data, boolean checkDirty) {
        List<SynchedEntityData.DataValue<?>> list = null;

        if (data.isDirty() || !checkDirty) {
            data.lock.readLock().lock();
            ObjectIterator<?> iterator = data.itemsById.values().iterator();

            try {
                while (iterator.hasNext()) {
                    SynchedEntityData.DataItem<?> dataItem = (SynchedEntityData.DataItem<?>) iterator.next();

                    if (dataItem.isDirty() || !checkDirty) {
                        dataItem.setDirty(false);
                        if (list == null) {
                            list = new ArrayList<>();
                        }
                        list.add(dataItem.value());
                    }
                }
            } finally {
                data.lock.readLock().unlock();
            }
        }

        return list;
    }

    /**
     * Finds a player by name in the world
     */
    public static Player getPlayerByName(String name, Level world) {
        Player result = null;
        if (world != null) {
            for (Player player : world.players()) {
                if (player.getName().getString().equals(name)) {
                    result = player;
                    break;
                }
            }
        }
        return result;
    }


    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        if (this.getScryTarget() != null) {
            CompoundTag nbt = new CompoundTag();
            EntityType<?> entityType = this.getScryTarget().getType();
            ResourceLocation resourceLocation = EntityType.getKey(entityType);
            nbt.putString("id", resourceLocation.toString());

            Set<String> keysToRemove = new HashSet<>();
            this.getScryTarget().saveWithoutId(nbt);

            // Remove oversized NBT data and problematic keys
            for (String key : nbt.getAllKeys()) {
                Tag tag = nbt.get(key);
                if (tag != null && tag.toString().length() > 15000) {
                    keysToRemove.add(key);
                }
                if (key.equals("ForgeCaps")) {
                    keysToRemove.add(key);
                }
            }

            for (String key : keysToRemove) {
                nbt.remove(key);
            }

            LOTMNetworkHandler.sendTrackingBlock(this.worldPosition, this.level, new ScryingEntityPacketS2C(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), nbt));
        } else {
            LOTMNetworkHandler.sendTrackingBlock(this.worldPosition, this.level, new ScryingEntityPacketS2C(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), null));
        }

        return super.getUpdatePacket();
    }

    @Override
    public void setRemoved() {
        if (level != null && level.isClientSide && this.getScryTarget() != null) {
            this.getScryTarget().getPersistentData().remove("dimensionalSightRenderData");
        }
        super.setRemoved();
    }

    public static class BlockPosInfo {
        public BlockState state;
        public BlockEntity blockEntity;
        public int relativeX = 0;
        public int relativeY = 0;
        public int relativeZ = 0;

        public BlockPosInfo(BlockPos pos, Level world) {
            this.state = world.getBlockState(pos);
            this.blockEntity = world.getBlockEntity(pos);
            if (this.blockEntity != null) {
                // Store block entity data for rendering
                try {
                    CompoundTag beTag = new CompoundTag();
                    this.blockEntity.saveAdditional(beTag);
                } catch (Exception e) {
                    this.blockEntity = null;
                }
            }
        }

        public BlockPosInfo() {
            this.state = null;
            this.blockEntity = null;
        }

        public CompoundTag write() {
            if (this.state == null || this.state.getBlock() == Blocks.AIR) {
                return new CompoundTag();
            }

            CompoundTag nbt = new CompoundTag();
            if (this.state != null) {
                CompoundTag stateNbt = NbtUtils.writeBlockState(this.state);
                nbt.put("SCRY_STATE", stateNbt);
            }

            nbt.putInt("REL_X", this.relativeX);
            nbt.putInt("REL_Y", this.relativeY);
            nbt.putInt("REL_Z", this.relativeZ);

            return nbt;
        }

        public void read(CompoundTag nbt, Level level) {
            if (level != null) {
                if (nbt.contains("SCRY_STATE")) {
                    this.state = NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), nbt.getCompound("SCRY_STATE"));
                }

                this.relativeX = nbt.getInt("REL_X");
                this.relativeY = nbt.getInt("REL_Y");
                this.relativeZ = nbt.getInt("REL_Z");
            }
        }
    }

    public static void updateTE(DimensionalSightPacketS2C message) {
        Minecraft mc = Minecraft.getInstance();
        mc.tell(() -> {
            if (mc.level != null) {
                BlockEntity te = mc.level.getBlockEntity(message.pos);
                if (te instanceof DimensionalSightTileEntity) {
                    ((DimensionalSightTileEntity) te).scryDataManager = message.dataManagerEntries;
                }
            }
        });
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getHeadYaw() {
        return this.headYaw;
    }

    public float getRenderYaw() {
        return this.renderYaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getPrevYaw() {
        return this.prevYaw;
    }

    public float getPrevHeadYaw() {
        return this.prevHeadYaw;
    }

    public float getPrevRenderYaw() {
        return this.prevRenderYaw;
    }

    public float getPrevPitch() {
        return this.prevPitch;
    }

    public double getVelX() {
        return this.velX;
    }

    public double getVelY() {
        return this.velY;
    }

    public double getVelZ() {
        return this.velZ;
    }

    public UUID getScryUniqueID() {
        return this.scryUniqueID;
    }

    public int getScryTimer() {
        return this.scryTimer;
    }

    public int getScryMaxTimer() {
        return this.scryMaxTimer;
    }

    public CompoundTag getScryNBT() {
        return this.scryNBT;
    }

    public List<SynchedEntityData.DataValue<?>> getScryDataManager() {
        return this.scryDataManager;
    }

    public void setScryTarget(LivingEntity target) {
        if (target != null) {
            this.scryUniqueID = target.getUUID();
            this.viewTarget = target.getName().getString();
            this.targetPos = target.position();
        }
    }
    private Map<UUID, EntityRenderData> originalEntityData = new HashMap<>();

    private static class EntityRenderData {
        public Vec3 originalPos;
        public float originalYaw, originalPitch, originalHeadYaw, originalBodyYaw;
        public float originalYawO, originalPitchO, originalHeadYawO, originalBodyYawO;
        public Vec3 originalDeltaMovement;
        public float originalLimbSwing, originalLimbSwingAmount, originalAttackAnim, originalOAttackAnim;

        public EntityRenderData(LivingEntity entity) {
            this.originalPos = entity.position();
            this.originalYaw = entity.getYRot();
            this.originalPitch = entity.getXRot();
            this.originalHeadYaw = entity.yHeadRot;
            this.originalBodyYaw = entity.yBodyRot;
            this.originalYawO = entity.yRotO;
            this.originalPitchO = entity.xRotO;
            this.originalHeadYawO = entity.yHeadRotO;
            this.originalBodyYawO = entity.yBodyRotO;
            this.originalDeltaMovement = entity.getDeltaMovement();
            this.originalLimbSwing = entity.walkAnimation.position();
            this.originalLimbSwingAmount = entity.walkAnimation.speed();
            this.originalAttackAnim = entity.attackAnim;
            this.originalOAttackAnim = entity.oAttackAnim;
        }

        public void restore(LivingEntity entity) {
            entity.setPos(originalPos.x, originalPos.y, originalPos.z);
            entity.setYRot(originalYaw);
            entity.setXRot(originalPitch);
            entity.yHeadRot = originalHeadYaw;
            entity.yBodyRot = originalBodyYaw;
            entity.yRotO = originalYawO;
            entity.xRotO = originalPitchO;
            entity.yHeadRotO = originalHeadYawO;
            entity.yBodyRotO = originalBodyYawO;
            entity.setDeltaMovement(originalDeltaMovement);
            entity.walkAnimation.position = originalLimbSwing;
            entity.walkAnimation.setSpeed(originalLimbSwingAmount);
            entity.attackAnim = originalAttackAnim;
            entity.oAttackAnim = originalOAttackAnim;
        }
    }
}