package net.swimmingtuna.lotm.util.ClientData;

import net.swimmingtuna.lotm.LOTM;

public class ClientGrayscaleData {
    private static boolean isGrayscaleActive = false;
    private static int duration = 0;
    private static int maxDuration = 0;
    private static float intensity = 1.0f;

    public static void setGrayscaleEffect(int dur, float grayscaleIntensity) {
        isGrayscaleActive = true;
        duration = dur;
        maxDuration = dur;
        intensity = Math.max(0.0f, Math.min(1.0f, grayscaleIntensity));
    }

    public static void setGrayscaleEffect(int dur) {
        setGrayscaleEffect(dur, 1.0f);
    }

    public static void clearGrayscaleEffect() {
        isGrayscaleActive = false;
        duration = 0;
        maxDuration = 0;
        intensity = 1.0f;
    }

    public static boolean isActive() {
        return isGrayscaleActive && duration > 0;
    }

    public static int getDuration() {
        return duration;
    }

    public static int getMaxDuration() {
        return maxDuration;
    }

    public static float getIntensity() {
        return intensity;
    }

    public static float getCurrentIntensity() {
        if (!isActive()) return 0.0f;
        return intensity;
    }

    public static void decrementDuration() {
        if (duration > 0) {
            duration--;
        }
        if (duration <= 0) {
            isGrayscaleActive = false;
        }
    }
}