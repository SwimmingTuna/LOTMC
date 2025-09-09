package net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface LeftClickType {
    public abstract boolean handle(Supplier<NetworkEvent.Context> supplier);
}
