package net.swimmingtuna.lotm.events;

import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.util.ClientData.*;
import net.swimmingtuna.lotm.util.KeyBinding;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import net.swimmingtuna.lotm.world.worldgen.dimension.DimensionInit;

import static net.swimmingtuna.lotm.networking.packet.ForceLookPacketS2C.setPlayerRotation;

public class KeyClientEvents {
    @Mod.EventBusSubscriber(modid = LOTM.MOD_ID, value = Dist.CLIENT)
    public static class ClientForgeEvents {

        @SubscribeEvent
        public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
            Player player = Minecraft.getInstance().player;
            if (player != null && (player.hasEffect(ModEffects.TUMBLE.get()) || ClientShouldntMoveData.getDontMoveTimer() >= 1)) {
                event.getInput().forwardImpulse = 0;
                event.getInput().leftImpulse = 0;
                event.getInput().jumping = false;
                event.getInput().shiftKeyDown = false;
            }
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && ClientLookData.isSmoothLooking()) {
                    float targetYaw = ClientLookData.getTargetYaw();
                    float targetPitch = ClientLookData.getTargetPitch();
                    float currentYaw = mc.player.getYRot();
                    float currentPitch = mc.player.getXRot();
                    float yawDiff = targetYaw - currentYaw;
                    float pitchDiff = targetPitch - currentPitch;
                    while (yawDiff > 180) yawDiff -= 360;
                    while (yawDiff < -180) yawDiff += 360;
                    float smoothFactor = 0.2f;
                    if (Math.abs(yawDiff) < 0.5f && Math.abs(pitchDiff) < 0.5f) {
                        setPlayerRotation(mc.player, targetYaw, targetPitch);
                        ClientLookData.setSmoothLooking(false);
                    } else {
                        float newYaw = currentYaw + yawDiff * smoothFactor;
                        float newPitch = currentPitch + pitchDiff * smoothFactor;
                        setPlayerRotation(mc.player, newYaw, newPitch);
                    }
                }
                ClientLeftclickCooldownData.decrementCooldown();
                ClientIgnoreShouldntRenderData.decrementAll();
                ClientShouldntRenderInvisibilityData.tick();
            }
        }


        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
           //if (KeyBinding.SPIRIT_VISION.consumeClick()) {
           //    LOTMNetworkHandler.sendToServer(new SpiritVisionC2S());
           //}
           //if (KeyBinding.SPIRIT_WORLD_TRAVERSAL.consumeClick()) {
           //    System.out.println("Worked");
           //    LOTMNetworkHandler.sendToServer(new SpiritWorldTraversalC2S());
           //}
            Player player = Minecraft.getInstance().player;
            if (player == null) return;


            // Check for left click
            if (KeyBinding.ABILITY_KEY_X.consumeClick()) {
                ClientAbilityKeyResetData.setAbilityResetTimer(100);
                byte[] keysClicked = ClientAbilityCombinationData.getKeysClicked();
                for (int i = 0; i < keysClicked.length; i++) {
                    if (keysClicked[i] == 0) {
                        ClientAbilityCombinationData.setKeyClicked(i, (byte) 1);
                        ClientAbilityCombinationData.handleClick();
                        int filledPositions = 0;
                        for (byte b : keysClicked) {
                            if (b != 0) filledPositions++;
                        }
                        if (filledPositions >= 5) {
                            ClientAbilityKeyResetData.setAbilityResetTimer(0);
                        }
                        break;
                    }
                }
            }

            // Handle right click
            if (KeyBinding.ABILITY_KEY_O.consumeClick()) {
                ClientAbilityKeyResetData.setAbilityResetTimer(100);
                byte[] keysClicked = ClientAbilityCombinationData.getKeysClicked();
                for (int i = 0; i < keysClicked.length; i++) {
                    if (keysClicked[i] == 0) {
                        ClientAbilityCombinationData.setKeyClicked(i, (byte) 2);
                        ClientAbilityCombinationData.handleClick();
                        int filledPositions = 0;
                        for (byte b : keysClicked) {
                            if (b != 0) filledPositions++;
                        }
                        if (filledPositions >= 5) {
                            ClientAbilityKeyResetData.setAbilityResetTimer(0);
                        }
                        break;
                    }
                }
            }
            if (KeyBinding.ABILITY_KEY_CLEAR.consumeClick()) {
                ClientAbilityCombinationData.resetKeysClicked();
                player.displayClientMessage(Component.literal("_ _ _ _ _").withStyle(ChatFormatting.BOLD), true);
            }
        }

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void onFogDensityEvent(ViewportEvent.RenderFog event) {
            Player player = Minecraft.getInstance().player;
            if (ClientFogData.getFogTimer() >= 40) {
                event.setFarPlaneDistance(Math.max(7,100 - ClientFogData.getFogTimer() * 2));
                event.setNearPlaneDistance(Math.max(1,90 - ClientFogData.getFogTimer() * 2));
                event.setCanceled(true);
            } else if (player.level().dimension().equals(DimensionInit.SPIRIT_WORLD_LEVEL_KEY)) {
                event.setFogShape(FogShape.SPHERE);
                int currentSequence = ClientSequenceData.getCurrentSequence();
                if (currentSequence == 0) {
                    event.setFarPlaneDistance(999);
                    event.setNearPlaneDistance(999);
                } else if (currentSequence != -1) {
                    int far = 100 - Math.max(10, currentSequence * 10);
                    int near = 97 - Math.max(7, currentSequence * 10);
                    event.setFarPlaneDistance(far);
                    event.setNearPlaneDistance(near);
                } else {
                    event.setFarPlaneDistance(6);
                    event.setNearPlaneDistance(4);
                }
                event.setCanceled(true);
            } else if (player.level().dimension().equals(DimensionInit.EXILED_DIMENSION_LEVEL_KEY)) {

            }

        }
    }

    @Mod.EventBusSubscriber(modid = LOTM.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            //event.register(KeyBinding.SPIRIT_VISION);
            //event.register(KeyBinding.SPIRIT_WORLD_TRAVERSAL);
            event.register(KeyBinding.ABILITY_KEY_O);
            event.register(KeyBinding.ABILITY_KEY_X);
            event.register(KeyBinding.ABILITY_KEY_CLEAR);
        }
    }


    @Mod.EventBusSubscriber(value = Dist.CLIENT)
    public static class NightRedFogProcedure {
        public static ViewportEvent.ComputeFogColor provider = null;
        public static float computedFogR = 0.0F;
        public static float computedFogG = 0.0F;
        public static float computedFogB = 0.0F;
        private static float fogAlpha = 0.0F;

        // Timing constants for smooth transitions
        private static final float TRANSITION_SPEED = 0.02F; // Smooth transition over ~16 minutes
        private static final long NIGHT_START = 13000L; // When night begins
        private static final long NIGHT_END = 23000L;   // When night ends
        private static final long DAY_CYCLE = 24000L;   // Full day cycle length

        private static float overlayAlpha = 0.0F;

        // Timing constants for smooth transitions
        private static final float NEW_TRANSITION_SPEED = 0.02F; // Faster transition than fog

        public NightRedFogProcedure() {
        }

        @SubscribeEvent
        public static void computeFogColor(ViewportEvent.ComputeFogColor event) {
            provider = event;
            ClientLevel level = Minecraft.getInstance().level;
            Entity entity = provider.getCamera().getEntity();
            if (level != null) {
                execute(entity);
            }
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.level != null && minecraft.player != null) {
                    if (!minecraft.level.dimension().equals(Level.OVERWORLD)) {
                        fogAlpha = 0.0F;
                        provider = null;
                    } else {
                        boolean isNight = isNightTime(minecraft.level);
                        boolean isInCave = isInCave(minecraft.player.blockPosition(), minecraft.level);
                        boolean shouldHaveFog = isNight && !isInCave;
                        float nightIntensity = getNightIntensity(minecraft.level);
                        float targetAlpha = shouldHaveFog ? nightIntensity : 0.0F;
                        if (fogAlpha < targetAlpha) {
                            fogAlpha = Math.min(targetAlpha, fogAlpha + TRANSITION_SPEED);
                        } else if (fogAlpha > targetAlpha) {
                            fogAlpha = Math.max(targetAlpha, fogAlpha - TRANSITION_SPEED);
                        }

                        provider = null;
                    }
                }
            }
        }

        public static void execute(Entity entity) {
            if (entity != null && provider != null) {
                int redFog = -6946816;
                setColor(fogAlpha, redFog);
            }
        }

        private static void setColor(float level, int color) {
            if (!(level <= 0.0F)) {
                float r = (float)(color >> 16 & 255) / 255.0F;
                float g = (float)(color >> 8 & 255) / 255.0F;
                float b = (float)(color & 255) / 255.0F;

                if (level >= 1.0F) {
                    provider.setRed(r);
                    provider.setGreen(g);
                    provider.setBlue(b);
                    computedFogR = r;
                    computedFogG = g;
                    computedFogB = b;
                } else {
                    float newR = Mth.lerp(level, provider.getRed(), r);
                    float newG = Mth.lerp(level, provider.getGreen(), g);
                    float newB = Mth.lerp(level, provider.getBlue(), b);
                    provider.setRed(newR);
                    provider.setGreen(newG);
                    provider.setBlue(newB);
                    computedFogR = newR;
                    computedFogG = newG;
                    computedFogB = newB;
                }
            }
        }

        private static boolean isInCave(BlockPos pos, ClientLevel level) {
            int y = pos.getY();
            int skyLight = level.getBrightness(LightLayer.SKY, pos);
            BlockState above = level.getBlockState(pos.above());
            return y < 50 && !level.canSeeSky(pos) && skyLight <= 2 && !above.isAir() && above.canOcclude();
        }

        private static boolean isNightTime(ClientLevel level) {
            long time = level.getDayTime() % DAY_CYCLE;
            return time >= NIGHT_START && time <= NIGHT_END;
        }

        private static float getNightIntensity(ClientLevel level) {
            long time = level.getDayTime() % DAY_CYCLE;

            if (time < NIGHT_START || time > NIGHT_END) {
                return 0.0F;
            }

            long nightDuration = NIGHT_END - NIGHT_START;
            long nightProgress = time - NIGHT_START;
            float normalizedTime = (float) nightProgress / (float) nightDuration;
            float intensity = (float) Math.sin(normalizedTime * Math.PI);
            return intensity * 0.8F;
        }
        @SubscribeEvent
        public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
            if (event.getOverlay().id().toString().equals("hotbar")) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null && mc.player != null) {
                    if (!mc.level.dimension().equals(Level.OVERWORLD)) {
                        overlayAlpha = 0.0F;
                        return;
                    }
                    boolean isNight = isNightTime(mc.level);
                    boolean canSeeSky = mc.level.canSeeSky(mc.player.blockPosition());
                    boolean isInCave = isInCave(mc.player.blockPosition(), mc.level);
                    boolean shouldDisplayOverlay = isNight && canSeeSky && !isInCave;
                    float nightIntensity = getNightIntensity(mc.level);
                    float targetAlpha = shouldDisplayOverlay ? nightIntensity * 0.5F : 0.0F; // 0.3F max intensity
                    if (overlayAlpha < targetAlpha) {
                        overlayAlpha = Math.min(targetAlpha, overlayAlpha + 0.05f);
                    } else if (overlayAlpha > targetAlpha) {
                        overlayAlpha = Math.max(targetAlpha, overlayAlpha - 0.05f);
                    }
                    if (overlayAlpha > 0.01F) {
                        GuiGraphics guiGraphics = event.getGuiGraphics();
                        int width = mc.getWindow().getGuiScaledWidth();
                        int height = mc.getWindow().getGuiScaledHeight();
                        float pitch = mc.player.getXRot();
                        float lookUpFactor = Math.max(0.0F, Math.min(1.0F, -pitch / 90.0F));
                        int baseAlpha = (int)(30.0F + (overlayAlpha * 0.7F + lookUpFactor * 0.3F) * 150.0F);
                        int finalAlpha = (int)((float)baseAlpha * overlayAlpha);
                        int color = finalAlpha << 24 | 0x550000; // Dark red
                        guiGraphics.fill(0, 0, width, height, color);
                    }
                }
            }
        }
    }
}
