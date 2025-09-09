package net.swimmingtuna.lotm.blocks.DimensionalSight;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.swimmingtuna.lotm.init.BlockEntityInit;

import java.util.ArrayList;
import java.util.UUID;

public class DimensionalTileEntity extends UpdatingTileEntity {
    private UUID casterUUID;
    private boolean cloth;
    private boolean fay;

    public static final String ORDER_TAG = "catalyst_order";
    public static final String CASTER_ID = "caster_uuid";
    public static final String CLOTH_BOOLEAN = "cloth";
    public static final String FAY_TAG = "fay";

    private UnorderedList catalysts;

    public DimensionalTileEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityInit.MAHOUJIN.get(), pos, blockState);
    }

    public DimensionalTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        CompoundTag orderTag = new CompoundTag();

        if (this.catalysts == null) {
            this.catalysts = new UnorderedList();
        }

        ArrayList<String> orderList = this.catalysts.getOrder();

        for (int i = 0; i < orderList.size(); i++) {
            orderTag.putString(String.valueOf(i), orderList.get(i));
        }

        compound.put(ORDER_TAG, orderTag);
        compound.putBoolean(FAY_TAG, this.fay);

        if (this.casterUUID != null) {
            compound.putUUID(CASTER_ID, this.casterUUID);
        }

        compound.putBoolean(CLOTH_BOOLEAN, this.cloth);
        super.saveAdditional(compound);
    }

    @Override
    public void load(CompoundTag compound) {
        ArrayList<String> order = new ArrayList<>();
        CompoundTag orderTag = compound.getCompound(ORDER_TAG);

        for (int i = 0; i < orderTag.getAllKeys().size(); i++) {
            order.add(orderTag.getString(String.valueOf(i)));
        }

        this.catalysts = new UnorderedList(order);
        if (compound.hasUUID(CASTER_ID)) {
            this.casterUUID = compound.getUUID(CASTER_ID);
        } else {
            this.casterUUID = null;
        }
        this.cloth = compound.getBoolean(CLOTH_BOOLEAN);
        this.fay = compound.getBoolean(FAY_TAG);
        super.load(compound);
    }

    public UUID getCasterUUID() {
        return this.casterUUID;
    }

    public void setCaster(UUID casterUUID) {
        this.casterUUID = casterUUID;
        this.sendUpdates();
    }

    public void setCaster(LivingEntity caster) {
        this.casterUUID = caster.getUUID();
        this.sendUpdates();
    }

    public void removeThis() {
        if (this.level != null) {
            this.level.setBlock(this.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
        }
    }
}