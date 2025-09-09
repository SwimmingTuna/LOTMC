package net.swimmingtuna.lotm.events;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.attributes.ModAttributes;
import net.swimmingtuna.lotm.nihilums.tweaks.Attributes.NightVisionLightHandler;
import net.swimmingtuna.lotm.client.AbilityOverlay;
import net.swimmingtuna.lotm.client.FlashOverlay;
import net.swimmingtuna.lotm.client.SpiritualityBarOverlay;
import net.swimmingtuna.lotm.client.WormOfStarOverlay;
import net.swimmingtuna.lotm.item.SealedArtifacts.DeathKnell;
import net.swimmingtuna.lotm.util.ClientData.ClientGrayscaleData;
import net.swimmingtuna.lotm.util.ClientData.ClientShouldntRenderInvisibilityData;
import net.swimmingtuna.lotm.util.SpiritWorld.SpiritWorldHandler;
import net.swimmingtuna.lotm.util.effect.ModEffects;

import static net.swimmingtuna.lotm.util.ClientUtil.renderGrayscaleUsingGUI;


@Mod.EventBusSubscriber(modid = LOTM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {
    public static ShaderInstance VOID_SHADER;

    /*
    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Pre event) {
        if (ClientShouldntRenderTransformData.getInstance().isTransformed()) {
            Mob mob = ClientShouldntRenderTransformData.getInstance().getCachedMob();
            event.setCanceled(true);
            Player player = event.getEntity();
            float partialTick = event.getPartialTick();
            double x = player.xo + (player.getX() - player.xo) * partialTick;
            double y = player.yo + (player.getY() - player.yo) * partialTick;
            double z = player.zo + (player.getZ() - player.zo) * partialTick;
            float yRot = lerpAngle(partialTick, player.yRotO, player.getYRot());
            float xRot = lerpAngle(partialTick, player.xRotO, player.getXRot());
            mob.setPos(x, y, z);
            mob.setYRot(yRot);
            mob.setXRot(xRot);
            mob.yHeadRot = player.yHeadRot;
            mob.yBodyRot = player.yBodyRot;

            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            dispatcher.render(mob, 0, 0, 0, 0, event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
        }
    }

     */


    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        // Decrement the timer and render overlay before GUI rendering
        if (ClientGrayscaleData.isActive()) {
            renderGrayscaleUsingGUI(event.getGuiGraphics());
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.player.getAttribute(ModAttributes.NIGHT_VISION.get()) != null) {
            float boost = (float) mc.player.getAttributeValue(ModAttributes.NIGHT_VISION.get());
            if (boost == 1.0) return;
            //NightVisionLightHandler.getLigthLevelInFov(mc.level, mc.player);
            if (mc.level.getRawBrightness(mc.player.blockPosition(), 0) > 6 && NightVisionLightHandler.checkDay(mc.level)) {
                return;
            }
            if (mc.level.getBrightness(LightLayer.BLOCK, mc.player.blockPosition()) != 0) {
                return;
            }
            //float lightFactor = 1.0F -  / 15.0F;
            //float boost = 1.0F + (rawBoost - 1.0F) * lightFactor;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(boost, boost, boost, 1.0F);
            RenderSystem.disableBlend();
        }
    }


    private static float lerpAngle(float partialTick, float prev, float current) {
        return prev + (current - prev) * partialTick;
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("flash_overlay", FlashOverlay.INSTANCE);
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "spirituality_overlay", SpiritualityBarOverlay.INSTANCE);
        event.registerAboveAll("worm_of_star_overlay", WormOfStarOverlay.INSTANCE);
        //event.registerAbove(VanillaGuiOverlay.PLAYER_HEALTH.id(), "lotm_health_overlay", HealthBarOverlay.INSTANCE);
        event.registerAboveAll("ability_overlay", AbilityOverlay.INSTANCE);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void zoomEvent(ViewportEvent.ComputeFov event) {
        Player player = event.getRenderer().getMinecraft().player;
        if (player != null && player.getMainHandItem().getItem() instanceof DeathKnell && player.isShiftKeyDown()) {
            event.setFOV(event.getFOV() * (1 - 0.5f));
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onPlaySound(PlaySoundEvent event) {
        Player player = Minecraft.getInstance().player;
        if (event.isCancelable()) {
            if (player != null && player.hasEffect(ModEffects.DEAFNESS.get())) {
                event.setCanceled(true);
            }
            if (event.getSound() instanceof EntityBoundSoundInstance entityBoundSoundInstance) {
                Entity entity = entityBoundSoundInstance.entity;
                if (entity != null) {
                    if (ClientShouldntRenderInvisibilityData.getShouldntRender(entity.getUUID())) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }


    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void livingRender(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        Player player = Minecraft.getInstance().player;
        if (player != null && !SpiritWorldHandler.bothInSameWorld(entity, player)) {
            event.setCanceled(true);
            if (event.getRenderer().shadowRadius == 1.0f) {
                event.getRenderer().shadowRadius = 0.0f;
            }
        }
        if (ClientShouldntRenderInvisibilityData.getShouldntRender(entity.getUUID())) {
            event.setCanceled(true);
            event.getRenderer().shadowRadius = 0.0f;
        }
    }


}