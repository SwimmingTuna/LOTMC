package net.swimmingtuna.lotm.capabilities.sealed_data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.INBTSerializable;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;

import java.util.*;

public class SealedDataCapability implements ISealedDataCapability, INBTSerializable<CompoundTag> {
    private HashMap<UUID, UUID> sealsCreators = new HashMap<>();
    private HashMap<UUID, Integer> sealsSequences = new HashMap<>();
    private HashMap<UUID, Boolean> sealsHasTimers = new HashMap<>();
    private HashMap<UUID, Integer> sealsTimers = new HashMap<>();
    private HashSet<UUID> sealsWithTimers = new HashSet<>();
    private HashMap<UUID, ABILITIES_SEAL_TYPES> sealedAbilitiesType = new HashMap<>();
    private HashMap<UUID, HashSet<SimpleAbilityItem>> sealedAbilitiesList = new HashMap<>();
    private HashMap<UUID, HashSet<Integer>> sealedAbilitiesSequences = new HashMap<>();

    @Override
    public HashMap<UUID, UUID> sealsCreators() {
        return this.sealsCreators;
    }

    @Override
    public HashMap<UUID, Integer> sealsSequences() {
        return this.sealsSequences;
    }

    @Override
    public HashMap<UUID, Boolean> sealsHasTimers() {
        return this.sealsHasTimers;
    }

    @Override
    public HashMap<UUID, Integer> sealsTimers() {
        return this.sealsTimers;
    }

    @Override
    public HashSet<UUID> sealsWithTimers(){
        return this.sealsWithTimers;
    }

    @Override
    public HashMap<UUID, ABILITIES_SEAL_TYPES> sealedAbilitiesType() {
        return this.sealedAbilitiesType;
    }

    @Override
    public HashMap<UUID, HashSet<SimpleAbilityItem>> sealedAbilitiesList() {
        return this.sealedAbilitiesList;
    }

    @Override
    public HashMap<UUID, HashSet<Integer>> sealedAbilitiesSequences() {
        return this.sealedAbilitiesSequences;
    }

    @Override
    public void setCreator(UUID sealUUID, UUID creator) {
        this.sealsCreators.put(sealUUID, creator);
    }

    @Override
    public void setSequence(UUID sealUUID, int sequence) {
        this.sealsSequences.put(sealUUID, sequence);
    }

    @Override
    public void toggleTimer(UUID sealUUID) {
        this.sealsHasTimers.put(sealUUID, true);
        this.sealsWithTimers.add(sealUUID);
    }

    @Override
    public void setTimer(UUID sealUUID, int counter) {
        this.sealsTimers.put(sealUUID, counter);
    }

    @Override
    public void setSealedAbilitiesType(UUID sealUUID, ABILITIES_SEAL_TYPES type) {
        this.sealedAbilitiesType.put(sealUUID, type);
    }

    @Override
    public void setSealedAbilityList(UUID sealUUID, HashSet<SimpleAbilityItem> abilities) {
        this.sealedAbilitiesList.put(sealUUID, abilities);
    }

    @Override
    public void setSealedAbilitiesSequence(UUID sealUUID, HashSet<Integer> sequence) {
        this.sealedAbilitiesSequences.put(sealUUID, sequence);
    }

    @Override
    public void removeSeal(UUID sealUUID){
        sealsCreators.remove(sealUUID);
        sealsSequences.remove(sealUUID);
        sealsHasTimers.remove(sealUUID);
        sealsTimers.remove(sealUUID);
        sealsWithTimers.remove(sealUUID);
        sealedAbilitiesType.remove(sealUUID);
        sealedAbilitiesList.remove(sealUUID);
        sealedAbilitiesSequences.remove(sealUUID);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        ListTag creatorsTag = new ListTag();
        for (Map.Entry<UUID, UUID> entry : this.sealsCreators.entrySet()){
            CompoundTag entryTag = new CompoundTag();

            entryTag.putUUID("sealUUID", entry.getKey());
            entryTag.putUUID("creatorUUID", entry.getValue());

            creatorsTag.add(entryTag);
        }
        tag.put("sealsCreators", creatorsTag);

        ListTag sequencesTag = new ListTag();
        for (Map.Entry<UUID, Integer> entry : this.sealsSequences.entrySet()){
            CompoundTag entryTag = new CompoundTag();

            entryTag.putUUID("sealUUID", entry.getKey());
            entryTag.putInt("sequence", entry.getValue());

            sequencesTag.add(entryTag);
        }
        tag.put("sealsSequences", sequencesTag);

        ListTag hasTimerTag = new ListTag();
        for (Map.Entry<UUID, Boolean> entry : this.sealsHasTimers.entrySet()){
            CompoundTag entryTag = new CompoundTag();

            entryTag.putUUID("sealUUID", entry.getKey());
            entryTag.putBoolean("hasTimer", entry.getValue());

            hasTimerTag.add(entryTag);
        }
        tag.put("sealsHasTimers", hasTimerTag);

        ListTag timerTag = new ListTag();
        for (Map.Entry<UUID, Integer> entry : this.sealsTimers.entrySet()){
            CompoundTag entryTag = new CompoundTag();

            entryTag.putUUID("sealUUID", entry.getKey());
            entryTag.putInt("timer", entry.getValue());

            timerTag.add(entryTag);
        }
        tag.put("sealsTimers", timerTag);

        ListTag sealsWithTimersTag = new ListTag();
        for (UUID seal : this.sealsWithTimers){
            CompoundTag sealTag = new CompoundTag();
            sealTag.putUUID("sealUUID", seal);
            sealsWithTimersTag.add(sealTag);
        }
        tag.put("sealsWithTimers", sealsWithTimersTag);

        ListTag sealedTypeTag = new ListTag();
        for (Map.Entry<UUID, ABILITIES_SEAL_TYPES> entry : this.sealedAbilitiesType.entrySet()){
            CompoundTag entryTag = new CompoundTag();

            entryTag.putUUID("sealUUID", entry.getKey());
            entryTag.putString("sealType", entry.getValue().name());

            sealedTypeTag.add(entryTag);
        }
        tag.put("sealedAbilitiesType", sealedTypeTag);

        ListTag sealedAbilitiesTag = new ListTag();
        for (Map.Entry<UUID, HashSet<SimpleAbilityItem>> entry : this.sealedAbilitiesList.entrySet()){
            CompoundTag entryTag = new CompoundTag();

            entryTag.putUUID("sealUUID", entry.getKey());

            ListTag abilities = new ListTag();
            for (SimpleAbilityItem ability : entry.getValue()){
                ResourceLocation itemID = BuiltInRegistries.ITEM.getKey(ability);
                CompoundTag abilityId = new CompoundTag();
                abilityId.putString("abilityId", itemID.toString());
                abilities.add(abilityId);
            }
            entryTag.put("abilities", abilities);

            sealedAbilitiesTag.add(entryTag);
        }
        tag.put("sealedAbilities", sealedAbilitiesTag);

        ListTag sealedAbilitiesSequenceTag = new ListTag();
        for (Map.Entry<UUID, HashSet<Integer>> entry : this.sealedAbilitiesSequences.entrySet()){
            CompoundTag entryTag = new CompoundTag();

            entryTag.putUUID("sealUUID", entry.getKey());
            ListTag sequences = new ListTag();
            for (int sequence :entry.getValue()){
                CompoundTag sequenceTag = new CompoundTag();
                sequenceTag.putInt("sequence", sequence);
                sequences.add(sequenceTag);
            }
            entryTag.put("sequences", sequences);

            sealedAbilitiesSequenceTag.add(entryTag);
        }
        tag.put("sealedAbilitiesSequence", sealedAbilitiesSequenceTag);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        sealsCreators.clear();
        sealsSequences.clear();
        sealsHasTimers.clear();
        sealsTimers.clear();
        sealsWithTimers.clear();
        sealedAbilitiesType.clear();
        sealedAbilitiesList.clear();
        sealedAbilitiesSequences.clear();

        ListTag creatorsTag = tag.getList("sealsCreators", 10); // 10 = CompoundTag
        for (int i = 0; i < creatorsTag.size(); i++) {
            CompoundTag entry = creatorsTag.getCompound(i);
            UUID sealUUID = entry.getUUID("sealUUID");
            UUID creatorUUID = entry.getUUID("creatorUUID");
            sealsCreators.put(sealUUID, creatorUUID);
        }

        ListTag sequencesTag = tag.getList("sealsSequences", 10);
        for (int i = 0; i < sequencesTag.size(); i++) {
            CompoundTag entry = sequencesTag.getCompound(i);
            UUID sealUUID = entry.getUUID("sealUUID");
            int sequence = entry.getInt("sequence");
            sealsSequences.put(sealUUID, sequence);
        }

        ListTag hasTimerTag = tag.getList("sealsHasTimers", 10);
        for (int i = 0; i < hasTimerTag.size(); i++) {
            CompoundTag entry = hasTimerTag.getCompound(i);
            UUID sealUUID = entry.getUUID("sealUUID");
            boolean hasTimer = entry.getBoolean("hasTimer");
            sealsHasTimers.put(sealUUID, hasTimer);
        }

        ListTag timerTag = tag.getList("sealsTimers", 10);
        for (int i = 0; i < timerTag.size(); i++) {
            CompoundTag entry = timerTag.getCompound(i);
            UUID sealUUID = entry.getUUID("sealUUID");
            int timer = entry.getInt("timer");
            sealsTimers.put(sealUUID, timer);
        }

        ListTag sealsWithTimersTag = tag.getList("sealsWithTimers", 10);
        for (int i = 0; i < sealsWithTimersTag.size(); i++) {
            sealsWithTimers.add(sealsWithTimersTag.getCompound(i).getUUID("sealUUID"));
        }

        ListTag sealedTypeTag = tag.getList("sealedAbilitiesType", 10);
        for (int i = 0; i < sealedTypeTag.size(); i++) {
            CompoundTag entry = sealedTypeTag.getCompound(i);
            UUID sealUUID = entry.getUUID("sealUUID");
            ABILITIES_SEAL_TYPES type = ABILITIES_SEAL_TYPES.valueOf(entry.getString("sealType"));
            sealedAbilitiesType.put(sealUUID, type);
        }

        ListTag sealedAbilitiesTag = tag.getList("sealedAbilities", 10);
        for (int i = 0; i < sealedAbilitiesTag.size(); i++) {
            CompoundTag entry = sealedAbilitiesTag.getCompound(i);
            UUID sealUUID = entry.getUUID("sealUUID");

            ListTag abilitiesList = entry.getList("abilities", 10);
            HashSet<SimpleAbilityItem> abilitySet = new HashSet<>();
            for (int j = 0; j < abilitiesList.size(); j++) {
                CompoundTag abilityTag = abilitiesList.getCompound(j);
                String itemId = abilityTag.getString("abilityId");
                Item abilityItem = BuiltInRegistries.ITEM.get(new ResourceLocation(itemId));
                if (abilityItem != null && abilityItem instanceof SimpleAbilityItem ability) {
                    abilitySet.add(ability);
                }
            }

            sealedAbilitiesList.put(sealUUID, abilitySet);
        }

        ListTag sealedAbilitiesSequenceTag = tag.getList("sealedAbilitiesSequence", 10);
        for (int i = 0; i < sealedAbilitiesSequenceTag.size(); i++) {
            CompoundTag entry = sealedAbilitiesSequenceTag.getCompound(i);
            UUID sealUUID = entry.getUUID("sealUUID");

            ListTag sequencesList = entry.getList("sequences", 10);
            HashSet<Integer> sequencesSet = new HashSet<>();
            for (int j = 0; j < sequencesList.size(); j++){
                CompoundTag sealedSequencesTag = sequencesList.getCompound(j);
                sequencesSet.add(sealedSequencesTag.getInt("sequence"));
            }
            sealedAbilitiesSequences.put(sealUUID, sequencesSet);
        }
    }

    public void copyFrom(SealedDataCapability other) {
        this.sealsCreators = new HashMap<>(other.sealsCreators);
        this.sealsSequences = new HashMap<>(other.sealsSequences);
        this.sealsHasTimers = new HashMap<>(other.sealsHasTimers);
        this.sealsTimers = new HashMap<>(other.sealsTimers);
        this.sealsWithTimers = new HashSet<>(other.sealsWithTimers);
        this.sealedAbilitiesType = new HashMap<>(other.sealedAbilitiesType);

        this.sealedAbilitiesList = new HashMap<>();
        for (Map.Entry<UUID, HashSet<SimpleAbilityItem>> entry : other.sealedAbilitiesList.entrySet()) {
            this.sealedAbilitiesList.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }

        this.sealedAbilitiesSequences = new HashMap<>(other.sealedAbilitiesSequences);
    }
}