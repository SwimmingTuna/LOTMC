package net.swimmingtuna.lotm.capabilities.scribed_abilities;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

public class ScribedAbilitiesCapability implements IScribedAbilitiesCapability, INBTSerializable<CompoundTag> {
    private Map<Item, Integer> scribedAbilities = new HashMap<>();

    @Override
    public Map<Item, Integer> getScribedAbilities() {
        return scribedAbilities;
    }

    @Override
    public void copyScribeAbility(Item ability) {
        scribedAbilities.merge(ability, 1, Integer::sum);
    }

    @Override
    public boolean hasScribedAbility(Item ability) {
        return scribedAbilities.containsKey(ability);
    }

    @Override
    public void useScribeAbility(Item ability) {
        scribedAbilities.computeIfPresent(ability, (item, count) -> count > 1 ? count - 1 : null);
    }

    @Override
    public int getRemainUses(Item ability) {
        return scribedAbilities.getOrDefault(ability, 0);
    }

    @Override
    public int getScribedAbilitiesCount() {
        return scribedAbilities.values().stream().mapToInt(Integer::intValue).sum();
    }


    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();

        for (Map.Entry<Item, Integer> entry : scribedAbilities.entrySet()) {
            CompoundTag entryTag = new CompoundTag();

            // Serialize Item as ResourceLocation
            ResourceLocation itemID = BuiltInRegistries.ITEM.getKey(entry.getKey());
            entryTag.putString("Ability", itemID.toString());

            // Serialize Integer count
            entryTag.putInt("Count", entry.getValue());

            listTag.add(entryTag);
        }

        tag.put("scribedAbilities", listTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("scribedAbilities", Tag.TAG_LIST)) { // Should match serializeNBT
            ListTag listTag = tag.getList("scribedAbilities", Tag.TAG_COMPOUND);

            scribedAbilities.clear(); // Important to clear the old data before loading new!

            for (Tag listEntry : listTag) {
                CompoundTag entryTag = (CompoundTag) listEntry;

                // Get Item from ResourceLocation
                ResourceLocation itemID = new ResourceLocation(entryTag.getString("Ability"));
                Item item = BuiltInRegistries.ITEM.get(itemID);

                // Get Integer count
                int count = entryTag.getInt("Count");

                // Put in the map
                if (item != null) {
                    scribedAbilities.put(item, count);
                }
            }
        }
    }

    public void copyFrom(ScribedAbilitiesCapability other){
        this.scribedAbilities = new HashMap<>(other.scribedAbilities);
    }
}