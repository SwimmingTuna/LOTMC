package net.swimmingtuna.lotm.util.ClientData;

import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;

public class
ClientSequenceData {
    private static int currentSequence;
    private static BeyonderClass pathway;

    public static void setCurrentSequence(int sequence) {
        currentSequence = sequence;
    }

    public static int getCurrentSequence() {
        return currentSequence;
    }

    public static BeyonderClass getPathway() {
        return pathway;
    }
    public static void setCurrentPathway(BeyonderClass currentPathway) {
        pathway = currentPathway;
    }
}
