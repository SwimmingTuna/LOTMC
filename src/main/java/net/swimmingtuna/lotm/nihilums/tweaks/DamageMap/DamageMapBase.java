package net.swimmingtuna.lotm.nihilums.tweaks.DamageMap;

import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public abstract class DamageMapBase {
    protected Map<Item, Float> damageMap = new HashMap<>(250);

    public Map<Item, Float> getDamageMap() {
        return damageMap;
    };

    public abstract void recalculateMap(int sequence);
}
