package net.swimmingtuna.lotm.capabilities.sealed_data;

import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public interface ISealedDataCapability {

    HashMap<UUID, UUID> sealsCreators();                                            //<Seal UUID, Creator UUID> Seal Creator
    HashMap<UUID, Integer> sealsSequences();                                        //<Seal UUID, Sequence> Seal Sequence
    HashMap<UUID, Boolean> sealsHasTimers();                                        //<Seal UUID, Has Timer> Seal has Timer
    HashMap<UUID, Integer> sealsTimers();                                           //<Seal UUID, Timer> Time in ticks until the corresponding Seal diminish
    HashSet<UUID> sealsWithTimers();                                                //All seals that uses a timer, useful for not iterating over all seals on the tick method
    HashMap<UUID, ABILITIES_SEAL_TYPES> sealedAbilitiesType();                      //<Seal UUID, Sealed Abilities Type> Type of Sealed Abilities
    HashMap<UUID, HashSet<SimpleAbilityItem>> sealedAbilitiesList();                //<Seal UUID, Sealed Abilities> List of all Sealed Abilities
    HashMap<UUID, HashSet<Integer>> sealedAbilitiesSequences();                      //<Seal UUID, Sequences> Sequence which all Abilities are Sealed

    void setCreator(UUID sealUUID, UUID creator);                                   //Sets the Seal Creator
    void setSequence(UUID sealUUID, int sequence);                                  //Sets the Seal Sequence
    void toggleTimer(UUID sealUUID);                                                //Toggles the timer
    void setTimer(UUID sealUUID, int counter);                                      //Sets the timer counter
    void setSealedAbilitiesType(UUID sealUUID, ABILITIES_SEAL_TYPES type);          //Sets the type of Seal on the Abilities
    void setSealedAbilityList(UUID sealUUID, HashSet<SimpleAbilityItem> abilities); //Sets all the Sealed Abilities
    void setSealedAbilitiesSequence(UUID sealUUID, HashSet<Integer> sequence);      //Sets the Sequences which all Abilities are Sealed
    void removeSeal(UUID sealUUID);                                                 //Removes a Seal
}