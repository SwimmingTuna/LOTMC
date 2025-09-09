package net.swimmingtuna.lotm.mixin;

import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.LOTM;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ServerGamePacketListenerImpl.class})
public abstract class ServerPlayNetworkDesyncFixMixin implements ServerPlayerConnection, ServerGamePacketListener {
    @Shadow
    public ServerPlayer player;
    @Shadow
    private int dropSpamTickCount;

    public ServerPlayNetworkDesyncFixMixin() {
    }

    @Inject(
            method = {"handleSetCreativeModeSlot"},
            at = {@At("TAIL")}
    )
    public void onCreativeInventoryAction(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {
        ItemStack itemStack = packet.getItem();
        ResourceLocation wormOfStarId = new ResourceLocation("lotm", "wormofstar");
        boolean isWormOfStar = ForgeRegistries.ITEMS.getKey(itemStack.getItem()).equals(wormOfStarId);
        if (!isWormOfStar) {
            return;
        }

        boolean slotIsPositive = packet.getSlotNum() < 0;
        int maxStackSize = isWormOfStar ? LOTM.getMaxStackCount() : itemStack.getMaxStackSize();
        boolean isValid = itemStack.isEmpty() || itemStack.getDamageValue() >= 0 && itemStack.getCount() <= maxStackSize && !itemStack.isEmpty();
        boolean bl2 = packet.getSlotNum() >= 1 && packet.getSlotNum() <= 45;
        if (isValid && bl2) {
            this.player.inventoryMenu.getSlot(packet.getSlotNum()).setByPlayer(itemStack);
            this.player.inventoryMenu.broadcastChanges();
        } else if (slotIsPositive && isValid && this.dropSpamTickCount < 200) {
            this.dropSpamTickCount += 20;
            this.player.drop(itemStack, true);
        }
    }
}