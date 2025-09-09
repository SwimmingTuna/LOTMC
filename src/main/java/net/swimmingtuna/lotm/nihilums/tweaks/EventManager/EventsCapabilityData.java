package net.swimmingtuna.lotm.nihilums.tweaks.EventManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public class EventsCapabilityData implements IEventsCapabilityData, INBTSerializable<CompoundTag> {
    private List<IFunction> listR = new ArrayList<>(INITIAL_CAPACITY);
    private List<IFunction> listRToDelete = new ArrayList<>(INITIAL_CAPACITY);

    private List<IFunction> listW = new ArrayList<>(INITIAL_CAPACITY);
    private List<IFunction> listWToDelete = new ArrayList<>(INITIAL_CAPACITY);

    private ListTag serialize(List<IFunction> list){
        ListTag tag = new ListTag();

        for(var obj : list){
            tag.add(StringTag.valueOf(obj.getID()));
        }

        return tag;
    }

    private void deserialize(CompoundTag compoundTag, List<IFunction> list ,String key){
        ListTag tag = compoundTag.getList(key, Tag.TAG_STRING);

        for(var obj : tag){
            IFunction func = FunctionsProvider.getByID(obj.getAsString());

            if(func != null)
                list.add(func);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag result = new CompoundTag();

        result.put(TAG_LIST_R_KEY, serialize(listR));
        result.put(TAG_LIST_R_TO_DELETE_KEY, serialize(listRToDelete));

        result.put(TAG_LIST_W_KEY, serialize(listW));
        result.put(TAG_LIST_W_TO_DELETE_KEY, serialize(listWToDelete));

        return result;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        deserialize(compoundTag, listR, TAG_LIST_R_KEY);
        deserialize(compoundTag, listRToDelete, TAG_LIST_R_TO_DELETE_KEY);

        deserialize(compoundTag, listW, TAG_LIST_W_KEY);
        deserialize(compoundTag, listWToDelete, TAG_LIST_W_TO_DELETE_KEY);
    }

    @Override
    public List<IFunction> getRegularEvents() {
        return listR;
    }

    @Override
    public List<IFunction> getWorldEvents() {
        return listW;
    }

    @Override
    public void markDeleteR(IFunction func) {
        if (listRToDelete.contains(func) || !listR.contains(func)) return;

        listRToDelete.add(func);
    }

    @Override
    public void addR(IFunction func) {
        if (listR.contains(func)) return;

        listR.add(func);
    }

    @Override
    public void markDeleteW(IFunction func) {
        if (listWToDelete.contains(func) || !listW.contains(func)) return;

        listWToDelete.add(func);
    }

    @Override
    public void addW(IFunction func) {
        if (listW.contains(func)) return;

        listW.add(func);
    }

    @Override
    public void deleteAllMarked() {
        for(var obj : listRToDelete){
            listR.remove(obj);
        }
        for(var obj : listWToDelete){
            listW.remove(obj);
        }
    }
}
