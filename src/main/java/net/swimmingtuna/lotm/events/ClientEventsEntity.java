package net.swimmingtuna.lotm.events;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntityRenderer;
import net.swimmingtuna.lotm.blocks.RealVoidBlockRenderer;
import net.swimmingtuna.lotm.entity.Model.*;
import net.swimmingtuna.lotm.entity.Renderers.*;
import net.swimmingtuna.lotm.entity.Renderers.PlayerMobRenderer.PlayerMobRenderer;
import net.swimmingtuna.lotm.init.BlockEntityInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.particle.*;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ClassModelLoader;

@Mod.EventBusSubscriber(modid = LOTM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventsEntity {
    @SubscribeEvent
    public static void registerLayerDefinition(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MeteorModel.METEOR_LOCATION, MeteorModel::createBodyLayer);
        event.registerLayerDefinition(BulletModel.BULLET_LOCATION, BulletModel::createBodyLayer);
        event.registerLayerDefinition(MeteorNoLevelModel.METEOR_LOCATION, MeteorNoLevelModel::createBodyLayer);
        event.registerLayerDefinition(FlashEntityModel.FLASH_LOCATION, FlashEntityModel::createBodyLayer);
        event.registerLayerDefinition(DragonBreathModel.LAYER, DragonBreathModel::createBodyLayer);
        event.registerLayerDefinition(WindBladeModel.WIND_BLADE_LOCATION, WindBladeModel::createBodyLayer);
        event.registerLayerDefinition(WindCushionModel.WIND_CUSHION_LOCATION, WindCushionModel::createBodyLayer);
        event.registerLayerDefinition(StoneEntityModel.STONE_MODEL_LOCATION, StoneEntityModel::createBodyLayer);
        event.registerLayerDefinition(EndstoneEntityModel.ENDSTONE_MODEL_LOCATION, EndstoneEntityModel::createBodyLayer);
        event.registerLayerDefinition(NetherrackEntityModel.NETHERRACK_MODEL_LOCATION, NetherrackEntityModel::createBodyLayer);
        event.registerLayerDefinition(LavaEntityModel.LAVA_ENTITY_LOCATION, LavaEntityModel::createBodyLayer);
        event.registerLayerDefinition(LightningBallModel.LIGHTNING_BALL_LOCATION, LightningBallModel::createBodyLayer);

    }
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityInit.DIMENSIONAL_SIGHT_SEAL_ENTITY.get(), DimensionalSightSealRenderer::new);
        event.registerEntityRenderer(EntityInit.FLASH_ENTITY.get(), FlashEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.SPACE_RIFT_ENTITY.get(), SpaceRiftRenderer::new);
        event.registerEntityRenderer(EntityInit.SPATIAL_CAGE_ENTITY.get(), SpatialCageRenderer::new);
        event.registerEntityRenderer(EntityInit.LOW_SEQUENCE_DOOR_ENTITY.get(), LowSequenceDoorRenderer::new);
        event.registerEntityRenderer(EntityInit.SWORD_OF_TWILIGHT_ENTITY.get(), SwordOfTwilightEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.MERCURY_CAGE_ENTITY.get(), MercuryCageRenderer::new);
        event.registerEntityRenderer(EntityInit.MERCURY_PORTAL_ENTITY.get(), MercuryPortalRenderer::new);
        event.registerEntityRenderer(EntityInit.DIVINE_HAND_RIGHT_ENTITY.get(), DivineHandRightRenderer::new);
        event.registerEntityRenderer(EntityInit.DIVINE_HAND_LEFT_ENTITY.get(), DivineHandLeftRenderer::new);
        event.registerEntityRenderer(EntityInit.SILVER_LIGHT_ENTITY.get(), SilverLightRenderer::new);
        event.registerEntityRenderer(EntityInit.GLOBE_OF_TWILIGHT_ENTITY.get(), GlobeOfTwilightRenderer::new);
        event.registerEntityRenderer(EntityInit.APPRENTICE_DOOR_ENTITY.get(), ApprenticeDoorRenderer::new);
        event.registerEntityRenderer(EntityInit.SPACE_FRAGMENTATION_ENTITY.get(), SpaceFragmentationRenderer::new);
        event.registerEntityRenderer(EntityInit.COLORED_BOX_ENTITY.get(), ColoredBoxEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.MERCURY_ENTITY.get(), MercuryEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.DAWN_RAY_ENTITY.get(), DawnRayRenderer::new);
        event.registerEntityRenderer(EntityInit.TWILIGHT_LIGHT.get(), TwilightLightRenderer::new);
        event.registerEntityRenderer(EntityInit.GUARDIAN_BOX_ENTITY.get(), GuardianBoxEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.METEOR_ENTITY.get(), MeteorEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.DEATH_KNELL_BULLET_ENTITY.get(), BulletEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.METEOR_TRAIL_ENTITY.get(), MeteorTrailRenderer::new);
        event.registerEntityRenderer(EntityInit.CIRCLE_ENTITY.get(), CircleEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.ENDSTONE_ENTITY.get(), EndstoneEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.PLAYER_MOB_ENTITY.get(), PlayerMobRenderer::new);
        event.registerEntityRenderer(EntityInit.NETHERRACK_ENTITY.get(), NetherrackEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.STONE_ENTITY.get(), StoneEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.LIGHTNING_BALL.get(), LightningBallRenderer::new);
        event.registerEntityRenderer(EntityInit.LAVA_ENTITY.get(), LavaEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.MC_LIGHTNING_BOLT.get(), MCLightningBoltEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.TORNADO_ENTITY.get(), TornadoEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.HURRICANE_OF_LIGHT_ENTITY.get(), HurricaneOfLightEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.METEOR_NO_LEVEL_ENTITY.get(), MeteorNoLevelEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.ROAR_ENTITY.get(), RoarEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.WHISPERS_OF_CORRUPTION_ENTITY.get(), WhisperOfCorruptionEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.STORM_SEAL_ENTITY.get(), StormSealEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.WATER_COLUMN_ENTITY.get(), WaterColumnEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.DRAGON_BREATH_ENTITY.get(), DragonBreathRenderer::new);
        event.registerEntityRenderer(EntityInit.WIND_BLADE_ENTITY.get(), WindBladeRenderer::new);
        event.registerEntityRenderer(EntityInit.WIND_CUSHION_ENTITY.get(), WindCushionRenderer::new);
        event.registerEntityRenderer(EntityInit.CUSTOM_FALLING_BLOCK_ENTITY.get(), CustomFallingBlockRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.REAL_VOID_BLOCK_ENTITY.get(), RealVoidBlockRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.DIMENSIONAL_SIGHT_ENTITY.get(), DimensionalSightTileEntityRenderer::new);

    }


    @SubscribeEvent
    public static void registerParticleProvidersEvent(final RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleInit.DOOR.get(), DoorParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.ACIDRAIN_PARTICLE.get(), AcidRainParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.PLAYER_TRAIL_PARTICLE.get(), PlayerTrailParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.MOB_TRAIL_PARTICLE.get(), MobTrailParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.NULL_PARTICLE.get(), NullParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.BLACK_CURTAIN.get(), BlackCurtainParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.VOID_BREAK_PARTICLE.get(), VoidBreakParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.METEOR_PARTICLE.get(), MeteorParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.TORNADO_PARTICLE.get(), NullParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.SONIC_BOOM_PARTICLE.get(), SonicBoomParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.SYMBOLIZATION_PARTICLE.get(), SonicBoomParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.HURRICANE_OF_LIGHT_PARTICLE_1.get(), HurricaneOfLightParticle1.Provider::new);
        event.registerSpriteSet(ParticleInit.HURRICANE_OF_LIGHT_PARTICLE_2.get(), HurricaneOfLightParticle2.Provider::new);
        event.registerSpriteSet(ParticleInit.HURRICANE_OF_LIGHT_PARTICLE_3.get(), HurricaneOfLightParticle3.Provider::new);
        event.registerSpriteSet(ParticleInit.ATTACKER_POISONED_PARTICLE.get(), AttackerPoisonedParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.BABY_ZOMBIE_PARTICLE.get(), BabyZombieParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.BREEZE_PARTICLE.get(), BreezeParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.CANT_USE_ABILITY_PARTICLE.get(), CantUseAbilityParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.DOUBLE_DAMAGE_PARTICLE.get(), DoubleDamageParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.EXPLOSION_PARTICLE.get(), ExplosionParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.FALLING_STONE_PARTICLE.get(), FallingStoneParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.GOO_GAZE_PARTICLE.get(), GOOGazeParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.GROUND_TREMOR_PARTICLE.get(), GroundTremorParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.HALF_DAMAGE_PARTICLE.get(), HalfDamageParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.HEAT_WAVE_PARTICLE.get(), HeatWaveParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.IGNORE_DAMAGE_PARTICLE.get(), IgnoreDamageParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.IGNORE_MOBS_PARTICLE.get(), IgnoreMobsParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.LIGHTNING_STORM_PARTICLE.get(), LightningStormParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.LOTM_LIGHTNING_PARTICLE.get(), LOTMLightningParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.DIAMOND_PARTICLE.get(), LuckDiamondParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.MC_LIGHTNING_PARTICLE.get(), MCLightningParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.METEOR_CALAMITY_PARTICLE.get(), MeteorCalamityParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.POISON_PARTICLE.get(), PoisonParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.REGENERATION_PARTICLE.get(), RegenerationParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.TORNADO_CALAMITY_PARTICLE.get(), TornadoCalamityParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.TRIP_PARTICLE.get(), TripParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.UNDEAD_ARMY_PARTICLE.get(), UndeadArmyParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.WARDEN_PARTICLE.get(), WardenParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.WIND_MOVE_PROJECTILES_PARTICLES.get(), WindProjectilesParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.FLASH_PARTICLE.get(), FlashParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.LONG_FLASH_PARTICLE.get(), LongFlashParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.WIND_UNEQUIP_ARMOR_PARTICLE.get(), WindArmorParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerItemColor(RegisterColorHandlersEvent.Item event){
        event.register((stack, tintIndex) -> {
            if(tintIndex == 1){
                if(stack.hasTag() && stack.getTag().contains("pathway")){
                    BeyonderClass pathway = BeyonderUtil.getPathwayByName(stack.getTag().getString("pathway"));
                    ChatFormatting format = pathway.getColorFormatting();
                    return format != null ? BeyonderUtil.chatFormatingToInt(format) : 0xFFFFFF;
                }
            }
            return 0xFFFFFF;
        }, ItemInit.BEYONDER_CHARACTERISTIC.get());
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ItemProperties.register(ItemInit.BEYONDER_CHARACTERISTIC.get(), new ResourceLocation("random_beyonder"), (stack, level, entity, seed) -> stack.hasTag() ? (float) stack.getTag().getInt("texture") : 0.0F);
    }

    @SubscribeEvent
    public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register("class", new ClassModelLoader());
    }
}