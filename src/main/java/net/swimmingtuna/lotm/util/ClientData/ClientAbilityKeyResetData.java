package net.swimmingtuna.lotm.util.ClientData;

public class ClientAbilityKeyResetData {
    private static int abilityResetTimer;

    public static void setAbilityResetTimer(int timer) {
        abilityResetTimer = timer;
    }

    public static int getAbilityResetTimer() {
        return abilityResetTimer;
    }

    public static void decrementAbilityResetTimer() {
        abilityResetTimer--;
    }
}
