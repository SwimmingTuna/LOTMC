package net.swimmingtuna.lotm.util.ClientData;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ClientShouldntRenderInvisibilityData {
    private static final Map<UUID, Integer> invisibilityTimers = new HashMap<>();

    public static void setShouldntRender(boolean value, UUID uuid, int timerTicks) {
        if (value) {
            int currentTimer = invisibilityTimers.getOrDefault(uuid, 0);
            if (timerTicks > currentTimer) {
                invisibilityTimers.put(uuid, timerTicks);
            }
        } else {
            invisibilityTimers.remove(uuid);
        }
    }

    public static boolean getShouldntRender(UUID uuid) {
        return invisibilityTimers.containsKey(uuid) && invisibilityTimers.get(uuid) > 0;
    }

    public static void tick() {
        Iterator<Map.Entry<UUID, Integer>> iterator = invisibilityTimers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            int newTimer = entry.getValue() - 1;
            if (newTimer <= 0) {
                iterator.remove();
            } else {
                entry.setValue(newTimer);
            }
        }
    }

    public static int getRemainingTicks(UUID uuid) {
        return invisibilityTimers.getOrDefault(uuid, 0);
    }

    public static void clearAll() {
        invisibilityTimers.clear();
    }
}