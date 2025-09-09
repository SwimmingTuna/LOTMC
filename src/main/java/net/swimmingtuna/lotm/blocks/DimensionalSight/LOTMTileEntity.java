package net.swimmingtuna.lotm.blocks.DimensionalSight;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.swimmingtuna.lotm.init.BlockEntityInit;

import java.util.ArrayList;
import java.util.UUID;

public class LOTMTileEntity extends UpdatingTileEntity {
    private UUID casterUUID;
    private boolean cloth;
    private boolean fay;
    public static final String ORDER_TAG = "catalyst_order";
    public static final String CASTER_ID = "caster_uuid";
    public static final String CLOTH_BOOLEAN = "cloth";
    public static final String FAY_TAG = "fay";
    private UnorderedList catalysts;

    public LOTMTileEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityInit.DIMENSIONAL_SIGHT_ENTITY.get(), pos, blockState);
    }

    public LOTMTileEntity(BlockEntityType type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    /**
     * Saves additional NBT data for this tile entity
     */
    @Override
    public void saveAdditional(CompoundTag compound) {
        CompoundTag order = new CompoundTag();
        if (this.catalysts == null) {
            this.catalysts = new UnorderedList();
        }

        ArrayList<String> orderList = this.catalysts.getOrder();

        // Save catalyst order
        for (int i = 0; i < orderList.size(); ++i) {
            order.putString("" + i, orderList.get(i));
        }

        compound.put("catalyst_order", order);
        compound.putBoolean("fay", this.fay);

        if (this.casterUUID != null) {
            compound.putUUID("caster_uuid", this.casterUUID);
        }

        compound.putBoolean("cloth", this.cloth);
        super.saveAdditional(compound);
    }

    /**
     * Loads NBT data for this tile entity
     */
    @Override
    public void load(CompoundTag compound) {
        ArrayList<String> order = new ArrayList<>();
        CompoundTag orderTag = compound.getCompound("catalyst_order");

        // Load catalyst order
        for (int i = 0; i < orderTag.getAllKeys().size(); ++i) {
            order.add(orderTag.getString("" + i));
        }

        this.catalysts = new UnorderedList(order);
        this.casterUUID = compound.getUUID("caster_uuid");
        this.cloth = compound.getBoolean("cloth");
        this.fay = compound.getBoolean("fay");
        super.load(compound);
    }

    public UnorderedList getCatalysts() {
        return this.catalysts;
    }

    public void setCatalysts(UnorderedList catalysts) {
        this.catalysts = catalysts;
        this.sendUpdates();
    }

    public int getNumCatalysts() {
        return this.catalysts == null ? 0 : this.catalysts.size();
    }


    public Player getCaster() {
        if (this.casterUUID != null && !this.casterUUID.equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))) {
            if (this.level instanceof ServerLevel && this.getCaster().getServer() != null) {
                return this.level.getServer().getPlayerList().getPlayer(this.casterUUID);
            } else {
                return this.level != null ? this.level.getPlayerByUUID(this.casterUUID) : null;
            }
        } else {
            return null;
        }
    }

    public UUID getCasterUUID() {
        return this.casterUUID;
    }

    public void setCasterUUID(UUID casterUUID) {
        this.casterUUID = casterUUID;
        this.sendUpdates();
    }

    public void setCaster(Player caster) {
        this.casterUUID = caster.getUUID();
        this.sendUpdates();
    }

    public boolean hasCloth() {
        return this.cloth;
    }

    public void setCloth(boolean cloth) {
        this.cloth = cloth;
    }

    public boolean isFay() {
        return this.fay;
    }

    public void setFay(boolean fay) {
        this.fay = fay;
    }
}