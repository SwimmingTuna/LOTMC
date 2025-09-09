package net.swimmingtuna.lotm.util.ClientData;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.AbilityUsePacketC2S;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class ClientAbilityCombinationData {
    private static final byte[] keysClicked = new byte[5];
    private static boolean shouldSendResetMessage = false;

    public static byte[] getKeysClicked() {
        return keysClicked;
    }

    public static void setKeyClicked(int index, byte value) {
        if (index >= 0 && index < keysClicked.length) {
            keysClicked[index] = value;
        }
    }

    public static void resetKeysClicked() {
        Arrays.fill(keysClicked, (byte) 0);
    }

    public static boolean getShouldSendResetMessage() {
        return shouldSendResetMessage;
    }

    public static void setShouldSendResetMessage(boolean value) {
        shouldSendResetMessage = value;
    }


    public static void resetClicks() {
        if (getShouldSendResetMessage()) {
            resetKeysClicked();
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(Component.empty(), true);
            }
            setShouldSendResetMessage(false);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientSideLoginHandling() {
        ClientAbilityCombinationData.resetClicks();
    }

    public static void handleClick() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        byte[] keysClicked = ClientAbilityCombinationData.getKeysClicked();
        StringBuilder stringBuilder = new StringBuilder(5);
        for (byte b : keysClicked) {
            char charToAdd = switch (b) {
                case 1 -> 'X';
                case 2 -> 'O';
                default -> '_';
            };
            stringBuilder.append(charToAdd);
        }
        String actionBarString = StringUtils.join(stringBuilder.toString().toCharArray(), ' ');
        Component actionBarComponent = Component.literal(actionBarString).withStyle(ChatFormatting.BOLD);
        player.displayClientMessage(actionBarComponent, true);
        int filledPositions = 0;
        for (byte b : keysClicked) {
            if (b != 0) filledPositions++;
        }
        if (filledPositions >= 4) {
            if (keysClicked[4] != 0) {
                int abilityNumber = 0;
                for (int i = 0; i < keysClicked.length; i++) {
                    abilityNumber |= (keysClicked[i] - 1) << (4 - i);
                }
                ++abilityNumber;
                ClientAbilityCombinationData.resetKeysClicked();
                LOTMNetworkHandler.sendToServer(new AbilityUsePacketC2S(abilityNumber));
            }
        }
    }
}