package net.swimmingtuna.lotm.beyonder.api;

import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IPathwayPassiveEvents;

import java.util.*;

public interface BeyonderClass extends IPathwayPassiveEvents {


    List<String> sequenceNames();

    List<Integer> spiritualityLevels();

    List<Integer> spiritualityRegen();


    //void tick(LivingEntity player, int sequence);

    Multimap<Integer, Item> getItems();

    List<Integer> mentalStrength();
    
    ChatFormatting getColorFormatting();

    default SimpleContainer getAbilityItemsContainer(int sequenceLevel) {
        SimpleContainer container = new SimpleContainer(45);
        Map<Integer, List<ItemStack>> orderedItems = new LinkedHashMap<>();
        for (int i = 9; i >= sequenceLevel; i--) {
            orderedItems.put(i, new ArrayList<>());
        }
        Multimap<Integer, Item> items = getItems();
        for (Map.Entry<Integer, Item> entry : items.entries()) {
            int level = entry.getKey();
            if (level >= sequenceLevel) {
                orderedItems.get(level).add(entry.getValue().getDefaultInstance());
            }
        }
        int slotIndex = 0;
        for (int i = 9; i >= sequenceLevel; i--) {
            List<ItemStack> levelItems = orderedItems.get(i);
            for (ItemStack stack : levelItems) {
                container.setItem(slotIndex++, stack);
            }
        }
        return container;
    }

    List<Integer> antiDivination();

    List<Integer> divination();

    void applyAllModifiers(LivingEntity entity, int seq);
}
