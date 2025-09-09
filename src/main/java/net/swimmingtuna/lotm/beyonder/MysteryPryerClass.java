package net.swimmingtuna.lotm.beyonder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.init.ItemInit;

import java.util.List;

public class MysteryPryerClass implements BeyonderClass {
    @Override
    public List<String> sequenceNames() {
        return List.of(
                "Hermit",
                "Knowledge Emperor",
                "Sage",
                "Clairvoyant",
                "Mysticologist",
                "Constellations Master",
                "Scrolls Professor",
                "Warlock",
                "Melee Scholar",
                "Mystery Pryer"
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
        return List.of(600, 400, 290, 230, 190, 145, 110, 95, 70, 45);
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
        return ChatFormatting.DARK_PURPLE;
    }


    @Override
    public void removeAllEvents(LivingEntity entity) {

    }

    @Override
    public void addAllEvents(LivingEntity entity, int sequence) {

    }
}
