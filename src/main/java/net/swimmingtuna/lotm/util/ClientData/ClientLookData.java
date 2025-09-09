package net.swimmingtuna.lotm.util.ClientData;

public class ClientLookData {
    private static boolean smoothLooking = false;
    private static float targetYaw = 0f;
    private static float targetPitch = 0f;

    public static void setTargetRotation(float yaw, float pitch) {
        targetYaw = yaw;
        targetPitch = pitch;
    }

    public static void setSmoothLooking(boolean smooth) {
        smoothLooking = smooth;
    }

    public static boolean isSmoothLooking() {
        return smoothLooking;
    }

    public static float getTargetYaw() {
        return targetYaw;
    }

    public static float getTargetPitch() {
        return targetPitch;
    }
}
