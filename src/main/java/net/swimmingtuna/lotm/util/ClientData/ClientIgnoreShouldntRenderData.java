package net.swimmingtuna.lotm.util.ClientData;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ClientIgnoreShouldntRenderData {
    private static final Map<UUID, Integer> ignoreData = new HashMap<>();

    public static void setIgnoreData(int value, UUID uuid) {
        if (value <= 0) {
            ignoreData.remove(uuid);
        } else {
            ignoreData.put(uuid, value);
        }
    }

    public static void decrementAll() {
        Iterator<Map.Entry<UUID, Integer>> iterator = ignoreData.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            int newValue = entry.getValue() - 1;
            if (newValue <= 0) {
                iterator.remove();
            } else {
                entry.setValue(newValue);
            }
        }
    }

    public static int getIgnoreData(UUID uuid) {
        return ignoreData.getOrDefault(uuid, 0);
    }
}