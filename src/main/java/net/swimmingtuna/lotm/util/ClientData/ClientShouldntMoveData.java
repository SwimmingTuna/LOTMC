package net.swimmingtuna.lotm.util.ClientData;

public class ClientShouldntMoveData {
    private static int dontMoveTimer;

    public static void setDontMoveTimer(int timer) {
        dontMoveTimer = timer;
    }

    public static int getDontMoveTimer() {
        return dontMoveTimer;
    }

    public static void decrementDontMoveTimer() {
        dontMoveTimer--;
    }
}
