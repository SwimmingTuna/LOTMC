package net.swimmingtuna.lotm.beyonder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.init.ItemInit;

import java.util.List;

public class LawyerClass implements BeyonderClass {
    @Override
    public List<String> sequenceNames() {
        return List.of(
                "Black Emperor",
                "Prince of Abolition",
                "Duke of Entropy",
                "Frenzied Mage",
                "Earl of the Fallen",
                "Mentor of Disorder",
                "Baron of Corruption",
                "Briber",
                "Barbarian",
                "Lawyer"
        );
    }



    @Override
    public List<Integer> antiDivination() {
        return List.of(20, 15, 13, 9, 5, 2, 1, 1, 1, 1);
    }

    @Override
    public List<Integer> divination() {
        return List.of(20, 15, 13, 9, 5, 2, 1, 1, 1, 1);
    }

    @Override
    public List<Integer> spiritualityLevels() {
        return List.of(10000, 5000, 3000, 1800, 1200, 700, 450, 300, 175, 125);
    }

    @Override
    public List<Integer> mentalStrength() {
        return List.of(540, 365, 275, 212, 171, 133, 95, 75, 60, 38);
    }

    @Override
    public List<Integer> spiritualityRegen() {
        return List.of(34, 22, 16, 12, 10, 8, 6, 5, 3, 2);
    }


    @Override
    public void applyAllModifiers(LivingEntity entity, int seq) {

    }


    @Override
    public Multimap<Integer, Item> getItems() {
        HashMultimap<Integer, Item> items = HashMultimap.create();
        items.put(9, ItemInit.ALLY_MAKER.get());
        return items;
    }

    @Override
    public ChatFormatting getColorFormatting() {
        return ChatFormatting.DARK_BLUE;
    }


    @Override
    public void removeAllEvents(LivingEntity entity) {

    }

    @Override
    public void addAllEvents(LivingEntity entity, int sequence) {

    }
}
