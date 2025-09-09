package net.swimmingtuna.lotm.util.ClientData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientShouldntRenderHandData {
    private static final Map<UUID, Boolean> shouldntRenderHandData = new HashMap<>();

    public static void setShouldntRender(boolean value, UUID uuid) {
        if (value) {
            shouldntRenderHandData.put(uuid, true);
        } else {
            shouldntRenderHandData.remove(uuid);
        }
    }

    public static boolean getShouldntRender(UUID uuid) {
        return shouldntRenderHandData.getOrDefault(uuid, false);
    }

    public static void clearAll() {
        shouldntRenderHandData.clear();
    }
}