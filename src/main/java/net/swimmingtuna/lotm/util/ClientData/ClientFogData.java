package net.swimmingtuna.lotm.util.ClientData;

public class ClientFogData {
    private static int fogTimer;

    public static void setFogTimer(int timer) {
        fogTimer = timer;
    }

    public static int getFogTimer() {
        return fogTimer;
    }

    public static void decrementFog() {
        fogTimer--;
    }
}
